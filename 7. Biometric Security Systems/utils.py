# utils.py
import cv2
import numpy as np


def read_gray(path):
    img = cv2.imread(path, cv2.IMREAD_GRAYSCALE)

    if img is None:
        raise ValueError("Cannot read image: " + str(path))

    return img


def blockwise_zscore(img, blk=16):
    h = img.shape[0]
    w = img.shape[1]

    out = img.astype(np.float32).copy()

    y = 0
    while y < h:
        x = 0
        while x < w:
            patch = out[y:y + blk, x:x + blk]

            if patch.size == 0:
                x = x + blk
                continue

            mu = patch.mean()
            sigma = patch.std()

            if sigma == 0:
                sigma = 1e-6

            patch2 = (patch - mu) / sigma
            out[y:y + blk, x:x + blk] = patch2

            x = x + blk
        y = y + blk

    out = cv2.normalize(out, None, 0, 255, cv2.NORM_MINMAX)
    out = out.astype(np.uint8)

    return out


def otsu_binarize(img):
    t, bw = cv2.threshold(
        img,
        0,
        255,
        cv2.THRESH_BINARY + cv2.THRESH_OTSU
    )

    return bw


def morph_open_close(bw, k=3):
    kern = np.ones((k, k), np.uint8)

    opened = cv2.morphologyEx(bw, cv2.MORPH_OPEN, kern)
    closed = cv2.morphologyEx(bw, cv2.MORPH_CLOSE, kern)

    return opened, closed
