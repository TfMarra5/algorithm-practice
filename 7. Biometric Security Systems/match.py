# match.py
import numpy as np
from sklearn.linear_model import RANSACRegressor


def ang_diff(a, b):
    d = a - b
    d = (d + np.pi) % (2 * np.pi) - np.pi
    if d < 0:
        d = -d
    return d


def candidate_pairs(minsA, minsB, r=15.0, tau=np.deg2rad(20)):
    pairs = []

    if minsA is None or minsB is None:
        return pairs
    if len(minsA) == 0 or len(minsB) == 0:
        return pairs

    i = 0
    while i < len(minsA):
        xA = float(minsA[i][0])
        yA = float(minsA[i][1])
        thA = float(minsA[i][2])
        tA = minsA[i][3]

        j = 0
        while j < len(minsB):
            xB = float(minsB[j][0])
            yB = float(minsB[j][1])
            thB = float(minsB[j][2])
            tB = minsB[j][3]

            if tA != tB:
                j += 1
                continue

            dth = ang_diff(thA, thB)
            if dth > tau:
                j += 1
                continue

            # cheap check before doing anything else
            if (abs(xA - xB) + abs(yA - yB)) > 5.0 * r:
                j += 1
                continue

            pairs.append((i, j))
            j += 1

        i += 1

    return pairs


def align_rigid(minsA, minsB, pairs):
    if pairs is None:
        return None, None
    if len(pairs) < 3:
        return None, None

    A = []
    B = []

    i = 0
    while i < len(pairs):
        ia = pairs[i][0]
        ib = pairs[i][1]

        A.append([float(minsA[ia][0]), float(minsA[ia][1])])
        B.append([float(minsB[ib][0]), float(minsB[ib][1])])

        i += 1

    A = np.array(A, dtype=np.float32)
    B = np.array(B, dtype=np.float32)

    ones = np.ones((A.shape[0], 1), dtype=np.float32)
    X = np.hstack([A, ones])

    try:
        ransac = RANSACRegressor()
        ransac.fit(X, B)
    except Exception:
        return None, None

    est = ransac.estimator_
    coef = est.coef_
    intercept = est.intercept_
    inliers = ransac.inlier_mask_

    def T(P):
        P = np.asarray(P, dtype=np.float32)
        return P @ coef[:, :2].T + intercept

    return T, inliers


def score(minsA, minsB, T, pairs, inliers, r=10.0, tau=np.deg2rad(15)):
    if T is None:
        return 0.0
    if pairs is None:
        return 0.0
    if len(pairs) == 0:
        return 0.0

    if inliers is None:
        inliers = np.ones((len(pairs),), dtype=bool)

    ptsA = []
    ptsB = []

    i = 0
    while i < len(pairs):
        ia = pairs[i][0]
        ib = pairs[i][1]
        ptsA.append([float(minsA[ia][0]), float(minsA[ia][1])])
        ptsB.append([float(minsB[ib][0]), float(minsB[ib][1])])
        i += 1

    ptsA = np.array(ptsA, dtype=np.float32)
    ptsB = np.array(ptsB, dtype=np.float32)

    PA = T(ptsA)

    pos_ok = np.linalg.norm(PA - ptsB, axis=1) <= r

    ang_ok = np.zeros((len(pairs),), dtype=bool)
    i = 0
    while i < len(pairs):
        ia = pairs[i][0]
        ib = pairs[i][1]
        a = float(minsA[ia][2])
        b = float(minsB[ib][2])
        if ang_diff(a, b) <= tau:
            ang_ok[i] = True
        i += 1

    ok = pos_ok & ang_ok & inliers

    matched = 0
    i = 0
    while i < len(ok):
        if ok[i]:
            matched += 1
        i += 1

    denom = min(len(minsA), len(minsB))
    if denom <= 0:
        denom = 1

    return matched / float(denom)
