# features.py
import numpy as np
import cv2

def crossing_number(win):
    # 3x3 binary window
    p = win.reshape(-1)

    # neighbors in a circle (go around and come back to the first)
    order = [1, 2, 5, 8, 7, 6, 3, 0, 1]


    changes = 0
    i = 0
    while i < 8:
        a = p[order[i]]
        b = p[order[i + 1]]
        if a != b:
            changes = changes + 1
        i = i + 1

    return changes // 2


def minutiae_from_skeleton(skel):
    # turn into 0/1
    bin1 = (skel > 0).astype(np.uint8)

    h = skel.shape[0]
    w = skel.shape[1]

    ys, xs = np.where(bin1 == 1)

    mins = []

    i = 0
    while i < len(xs):
        y = int(ys[i])
        x = int(xs[i])

        # boundary checks (so 3x3 window exists)
        if y <= 0:
            i = i + 1
            continue
        if x <= 0:
            i = i + 1
            continue
        if y >= h - 1:
            i = i + 1
            continue
        if x >= w - 1:
            i = i + 1
            continue

        win = bin1[y - 1:y + 2, x - 1:x + 2]

        if win[1, 1] == 0:
            i = i + 1
            continue

        cn = crossing_number(win)

        if cn == 1:
            t = "T"
        elif cn == 3:
            t = "B"
        else:
            i = i + 1
            continue

        # local region for direction
        y0 = y - 5
        x0 = x - 5
        y1 = y + 6
        x1 = x + 6

        if y0 < 0:
            y0 = 0
        if x0 < 0:
            x0 = 0
        if y1 > h:
            y1 = h
        if x1 > w:
            x1 = w

        region = bin1[y0:y1, x0:x1]
        coords = np.column_stack(np.where(region == 1))

        if coords is None:
            i = i + 1
            continue
        if len(coords) < 5:
            i = i + 1
            continue

        ok = True
        try:
            cov = np.cov(coords.T)
            vals, vecs = np.linalg.eig(cov)
        except Exception:
            ok = False

        if not ok:
            i = i + 1
            continue

        idx = int(np.argmax(vals))
        v = vecs[:, idx]

        theta = np.arctan2(v[0], v[1])

        mins.append((x, y, theta, t))
        i = i + 1

    return np.array(mins, dtype=object)


def build_descriptors(mins, k=5):
    if mins is None:
        return []
    if len(mins) == 0:
        return []

    # make point array
    pts = []
    i = 0
    while i < len(mins):
        px = float(mins[i][0])
        py = float(mins[i][1])
        pts.append([px, py])
        i = i + 1
    pts = np.array(pts, dtype=np.float32)

    descs = []

    i = 0
    while i < len(mins):
        theta = float(mins[i][2])

        # compute distances (manual loop, not super pretty but fine)
        d = np.zeros((len(mins),), dtype=np.float32)
        j = 0
        while j < len(mins):
            dx = pts[j][0] - pts[i][0]
            dy = pts[j][1] - pts[i][1]
            d[j] = float(np.sqrt(dx * dx + dy * dy))
            j = j + 1

        order = np.argsort(d)

        nb = []
        cnt = 0

        j = 1  # skip itself (distance 0)
        while j < len(order) and cnt < k:
            idx = int(order[j])

            dx = pts[idx][0] - pts[i][0]
            dy = pts[idx][1] - pts[i][1]

            ang = np.arctan2(dy, dx) - theta

            # normalize to [-pi, pi]
            ang = ((ang + np.pi) % (2 * np.pi)) - np.pi

            dist_val = round(float(d[idx]), 1)
            ang_val = round(float(ang), 2)

            nb.append((dist_val, ang_val))

            cnt = cnt + 1
            j = j + 1

        descs.append(nb)
        i = i + 1

    return descs
