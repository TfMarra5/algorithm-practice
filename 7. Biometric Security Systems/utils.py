# utils.py
import cv2, numpy as np

def read_gray(path):
    img = cv2.imread(path, cv2.IMREAD_GRAYSCALE)
    if img is None: raise ValueError(f"Cannot read {path}")
    return img

def blockwise_zscore(img, blk=16):
    h,w = img.shape; out = img.astype(np.float32).copy()
    for y in range(0,h,blk):
        for x in range(0,w,blk):
            patch = out[y:y+blk, x:x+blk]
            mu, sigma = patch.mean(), patch.std() + 1e-6
            out[y:y+blk, x:x+blk] = (patch - mu) / sigma
    out = cv2.normalize(out, None, 0, 255, cv2.NORM_MINMAX).astype(np.uint8)
    return out

def otsu_binarize(img):
    _, bw = cv2.threshold(img, 0, 255, cv2.THRESH_BINARY+cv2.THRESH_OTSU)
    return bw

def morph_open_close(bw, k=3):
    kern = np.ones((k,k), np.uint8)
    return cv2.morphologyEx(bw, cv2.MORPH_OPEN, kern), cv2.morphologyEx(bw, cv2.MORPH_CLOSE, kern)
