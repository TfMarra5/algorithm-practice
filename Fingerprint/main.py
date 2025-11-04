# main.py
import os, json, sys
from utils import read_gray
from preprocessing import preprocess
from features import minutiae_from_skeleton, build_descriptors
from match import candidate_pairs, align_rigid, score
from security import cancelable_transform, encrypt_template, decrypt_template

KEY = os.environ.get("FP_AES_KEY", None)
if KEY is None:
    # 256-bit key para demo; em produção use KMS/HSM
    import secrets; KEY = secrets.token_bytes(32)

STORE = "fingerprints"

def process_to_template(path_img):
    gray = read_gray(path_img)
    skel, _ = preprocess(gray)
    mins = minutiae_from_skeleton(skel)
    desc = build_descriptors(mins, k=5)
    return {"mins": [tuple(m) for m in mins], "desc": desc, "dpi": 500, "v": 1}

def enroll(user_id, img_path, token):
    tmpl = process_to_template(img_path)
    tmpl_c = {"mins": cancelable_transform(tmpl["mins"], token),
              "desc": tmpl["desc"], "dpi": tmpl["dpi"], "v": tmpl["v"]}
    enc = encrypt_template(tmpl_c, KEY)
    os.makedirs(STORE, exist_ok=True)
    with open(os.path.join(STORE, f"{user_id}.bin"), "wb") as f: f.write(enc)

def verify(user_id, img_path, token, thr=0.4):
    probe = process_to_template(img_path)
    probe_c = {"mins": cancelable_transform(probe["mins"], token), "desc": probe["desc"]}
    with open(os.path.join(STORE, f"{user_id}.bin"), "rb") as f: enc=f.read()
    ref = decrypt_template(enc, KEY)
    pairs = candidate_pairs(np.array(probe_c["mins"], dtype=object),
                            np.array(ref["mins"], dtype=object))
    T, inliers = align_rigid(np.array(probe_c["mins"], dtype=object),
                             np.array(ref["mins"], dtype=object), pairs)
    sc = score(np.array(probe_c["mins"], dtype=object),
               np.array(ref["mins"], dtype=object), T, pairs, inliers if inliers is not None else np.zeros(0,dtype=bool))
    return sc, (sc >= thr)

if __name__ == "__main__":
    cmd = sys.argv[1]
    if cmd=="enroll":
        _,_,uid,img,token = sys.argv
        enroll(uid, img, token)
        print("enrolled")
    elif cmd=="verify":
        _,_,uid,img,token = sys.argv
        sc, ok = verify(uid, img, token)
        print(f"score={sc:.3f} -> {'ACCEPT' if ok else 'REJECT'}")
