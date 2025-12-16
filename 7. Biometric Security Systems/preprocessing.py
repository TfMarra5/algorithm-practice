# preprocessing.py
import cv2, numpy as np
from skimage.morphology import skeletonize
from utils import blockwise_zscore, otsu_binarize, morph_open_close

def segment(img, win=16, thr=15.0):
    # máscara por variância local
    img32 = img.astype(np.float32)
    var = cv2.Laplacian(img32, cv2.CV_32F, ksize=3)**2
    var = cv2.blur(var, (win,win))
    mask = (var > thr).astype(np.uint8)*255
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, np.ones((9,9), np.uint8))
    return mask

def orientation_field(img, blk=16):
    gx = cv2.Sobel(img, cv2.CV_32F, 1,0,ksize=3)
    gy = cv2.Sobel(img, cv2.CV_32F, 0,1,ksize=3)
    ang = 0.5*np.arctan2(2*cv2.blur(gx*gy,(blk,blk)),
                         cv2.blur(gx*gx - gy*gy,(blk,blk)))
    return ang  # radianos

def gabor_enhance(img, ang, blk=16):
    h,w = img.shape
    out = np.zeros_like(img, dtype=np.float32)
    for y in range(0,h,blk):
        for x in range(0,w,blk):
            theta = ang[min(y,ang.shape[0]-1), min(x,ang.shape[1]-1)]
            k = cv2.getGaborKernel((9,9), 4.0, theta, 8.0, 0.5, 0, ktype=cv2.CV_32F)
            patch = img[y:y+blk, x:x+blk]
            out[y:y+blk, x:x+blk] = cv2.filter2D(patch, cv2.CV_32F, k)
    out = cv2.normalize(out, None, 0, 255, cv2.NORM_MINMAX).astype(np.uint8)
    return out

def preprocess(gray):
    norm = blockwise_zscore(gray)
    mask = segment(norm)
    enh = gabor_enhance(norm, orientation_field(norm))
    bw = otsu_binarize(enh)
    _, bw = morph_open_close(bw, 3)
    # dedo = crista=1 → inverter se necessário
    skel = skeletonize((bw==0).astype(np.uint8)).astype(np.uint8)*255
    skel = cv2.bitwise_and(skel, skel, mask=mask)
    return skel, mask
