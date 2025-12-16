# match.py
import numpy as np
from sklearn.linear_model import RANSACRegressor

def candidate_pairs(minsA, minsB, r=15.0, tau=np.deg2rad(20)):
    pairs=[]
    for i,(xA,yA,thA,tA) in enumerate(minsA):
        for j,(xB,yB,thB,tB) in enumerate(minsB):
            if tA!=tB: continue
            if abs(((thA-thB+np.pi)%(2*np.pi))-np.pi) > tau: continue
            if (abs(xA-xB)+abs(yA-yB))> 5*r: continue
            pairs.append((i,j))
    return pairs

def align_rigid(minsA, minsB, pairs, iters=3):
    if len(pairs)<3: return None
    A = np.array([[minsA[i][0], minsA[i][1]] for i,_ in pairs])
    B = np.array([[minsB[j][0], minsB[j][1]] for _,j in pairs])
    # modelo afim 2D (aproxima rotação+translação)
    X = np.hstack([A, np.ones((A.shape[0],1))])
    ransac = RANSACRegressor().fit(X, B)
    coef = ransac.estimator_.coef_; intercept = ransac.estimator_.intercept_
    def T(P):
        return P @ coef[:,:2].T + intercept
    inliers = ransac.inlier_mask_
    return T, inliers

def score(minsA, minsB, T, pairs, inliers, r=10.0, tau=np.deg2rad(15)):
    if T is None: return 0.0
    ptsA = np.array([[minsA[i][0], minsA[i][1]] for i,_ in pairs])
    ptsB = np.array([[minsB[j][0], minsB[j][1]] for _,j in pairs])
    PA = T(ptsA)
    pos_ok = np.linalg.norm(PA - ptsB, axis=1) <= r
    ang_ok = np.array([abs(((minsA[i][2]-minsB[j][2]+np.pi)%(2*np.pi))-np.pi)<=tau for i,j in pairs])
    ok = pos_ok & ang_ok & inliers
    matched = ok.sum()
    denom = max(1, min(len(minsA), len(minsB)))
    return matched/denom

