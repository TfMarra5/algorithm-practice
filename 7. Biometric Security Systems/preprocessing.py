# preprocessing.py
import cv2
import numpy as np
from skimage.morphology import skeletonize

from utils import blockwise_zscore, otsu_binarize, morph_open_close


def segment(img, win=16, thr=15.0):
    img32 = img.astype(np.float32)

    lap = cv2.Laplacian(img32, cv2.CV_32F, ksize=3)
    var = lap * lap

    var = cv2.blur(var, (win, win))

    mask = (var > thr).astype(np.uint8)
    mask = mask * 255

    k = np.ones((9, 9), np.uint8)
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, k)

    return mask


def orientation_field(img, blk=16):
    gx = cv2.Sobel(img, cv2.CV_32F, 1, 0, ksize=3)
    gy = cv2.Sobel(img, cv2.CV_32F, 0, 1, ksize=3)

    a = gx * gy
    b = gx * gx - gy * gy

    a = cv2.blur(a, (blk, blk))
    b = cv2.blur(b, (blk, blk))

    ang = 0.5 * np.arctan2(2.0 * a, b)

    return ang


def gabor_enhance(img, ang, blk=16):
    h = img.shape[0]
    w = img.shape[1]

    out = np.zeros((h, w), dtype=np.float32)

    y = 0
    while y < h:
        x = 0
        while x < w:
            yy = y
            xx = x

            if yy >= ang.shape[0]:
                yy = ang.shape[0] - 1
            if xx >= ang.shape[1]:
                xx = ang.shape[1] - 1

            theta = float(ang[yy, xx])

            kern = cv2.getGaborKernel((9, 9), 4.0, theta, 8.0, 0.5, 0, ktype=cv2.CV_32F)

            patch = img[y:y + blk, x:x + blk]
            if patch.size == 0:
                x = x + blk
                continue

            f = cv2.filter2D(patch, cv2.CV_32F, kern)
            out[y:y + blk, x:x + blk] = f

            x = x + blk
        y = y + blk

    out2 = cv2.normalize(out, None, 0, 255, cv2.NORM_MINMAX)
    out2 = out2.astype(np.uint8)

    return out2


def preprocess(gray):
    norm = blockwise_zscore(gray)

    mask = segment(norm)

    ang = orientation_field(norm)
    enh = gabor_enhance(norm, ang)

    bw = otsu_binarize(enh)

    _tmp, bw = morph_open_close(bw, 3)

    # fingerprint ridges should be 1, sometimes the binary is inverted
    ridges = (bw == 0).astype(np.uint8)

    sk = skeletonize(ridges).astype(np.uint8)
    skel = sk * 255

    skel = cv2.bitwise_and(skel, skel, mask=mask)

    return skel, mask
