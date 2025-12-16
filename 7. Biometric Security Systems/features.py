# features.py
import numpy as np, cv2

def crossing_number(window):
    # janela 3x3 binária 0/1 com pixel central como crista
    p = window.flatten()
    # ordem circular vizinhos
    idx = [1,2,5,8,7,6,3,0,1]
    return sum(abs(p[idx[i]]-p[idx[i+1]]) for i in range(8)) // 2

def minutiae_from_skeleton(skel):
    bin1 = (skel>0).astype(np.uint8)
    ys,xs = np.where(bin1==1)
    mins = []
    for y,x in zip(ys,xs):
        if y<1 or x<1 or y>=skel.shape[0]-1 or x>=skel.shape[1]-1: continue
        w = bin1[y-1:y+2, x-1:x+2]
        if w[1,1]==0: continue
        cn = crossing_number(w)
        if cn==1: t='T'
        elif cn==3: t='B'
        else: continue
        # direção por PCA local
        coords = np.column_stack(np.where(bin1[max(0,y-5):y+6, max(0,x-5):x+6]))
        if len(coords)<5: continue
        cov = np.cov(coords.T)
        vals, vecs = np.linalg.eig(cov)
        v = vecs[:, np.argmax(vals)]
        theta = np.arctan2(v[0], v[1])  # rad
        mins.append((x,y,theta,t))
    return np.array(mins, dtype=object)

def build_descriptors(mins, k=5):
    # descritor polar simples: k vizinhos mais próximos (dist, dtheta)
    if mins is None or len(mins)==0: return []
    pts = np.array([[m[0],m[1]] for m in mins], dtype=np.float32)
    descs = []
    for i,(x,y,theta,t) in enumerate(mins):
        d = np.linalg.norm(pts - pts[i], axis=1)
        order = np.argsort(d)[1:k+1]
        nb = []
        for j in order:
            dx,dy = pts[j]-pts[i]
            ang = np.arctan2(dy,dx) - theta
            nb.append((round(d[j],1), round(((ang+np.pi)%(2*np.pi))-np.pi,2)))
        descs.append(nb)
    return descs
