# main.py
import os
import sys
import json

import numpy as np

from utils import read_gray
from preprocessing import preprocess
from features import minutiae_from_skeleton, build_descriptors
from match import candidate_pairs, align_rigid, score
from security import cancelable_transform, encrypt_template, decrypt_template


KEY = os.environ.get("FP_AES_KEY", None)

if KEY is None:
    import secrets
    KEY = secrets.token_bytes(32)
else:
    # allow hex key from env (common in demos)
    if isinstance(KEY, str):
        kk = KEY.strip()
        try:
            if len(kk) == 64:
                KEY = bytes.fromhex(kk)
            else:
                KEY = kk.encode("utf-8")
        except Exception:
            KEY = kk.encode("utf-8")


STORE = "fingerprints"


def process_to_template(img_path):
    gray = read_gray(img_path)

    skel, _mask = preprocess(gray)

    mins = minutiae_from_skeleton(skel)
    desc = build_descriptors(mins, k=5)

    # keep it simple: a dict with what we need
    tmpl = {}
    tmpl["mins"] = []
    i = 0
    while i < len(mins):
        tmpl["mins"].append(tuple(mins[i]))
        i += 1

    tmpl["desc"] = desc
    tmpl["dpi"] = 500
    tmpl["v"] = 1

    return tmpl


def enroll(user_id, img_path, token):
    tmpl = process_to_template(img_path)

    mins_c = cancelable_transform(tmpl["mins"], token)

    tmpl_c = {}
    tmpl_c["mins"] = mins_c
    tmpl_c["desc"] = tmpl["desc"]
    tmpl_c["dpi"] = tmpl["dpi"]
    tmpl_c["v"] = tmpl["v"]

    enc = encrypt_template(tmpl_c, KEY)

    if not os.path.exists(STORE):
        os.makedirs(STORE)

    out_path = os.path.join(STORE, str(user_id) + ".bin")

    with open(out_path, "wb") as f:
        f.write(enc)


def verify(user_id, img_path, token, thr=0.4):
    probe = process_to_template(img_path)

    probe_c = {}
    probe_c["mins"] = cancelable_transform(probe["mins"], token)
    probe_c["desc"] = probe["desc"]

    ref_path = os.path.join(STORE, str(user_id) + ".bin")

    with open(ref_path, "rb") as f:
        enc = f.read()

    ref = decrypt_template(enc, KEY)

    A = np.array(probe_c["mins"], dtype=object)
    B = np.array(ref["mins"], dtype=object)

    pairs = candidate_pairs(A, B)

    T, inliers = align_rigid(A, B, pairs)

    if inliers is None:
        inliers = np.zeros(0, dtype=bool)

    sc = score(A, B, T, pairs, inliers)

    ok = False
    if sc >= thr:
        ok = True

    return sc, ok


def print_usage():
    print("Usage:")
    print("  python main.py enroll <user_id> <image_path> <token>")
    print("  python main.py verify <user_id> <image_path> <token>")


if __name__ == "__main__":

    if len(sys.argv) < 2:
        print_usage()
        raise SystemExit

    cmd = sys.argv[1].strip().lower()

    if cmd == "enroll":

        if len(sys.argv) < 5:
            print_usage()
            raise SystemExit

        uid = sys.argv[2]
        img = sys.argv[3]
        token = sys.argv[4]

        enroll(uid, img, token)
        print("enrolled")

    elif cmd == "verify":

        if len(sys.argv) < 5:
            print_usage()
            raise SystemExit

        uid = sys.argv[2]
        img = sys.argv[3]
        token = sys.argv[4]

        sc, ok = verify(uid, img, token)

        if ok:
            res = "ACCEPT"
        else:
            res = "REJECT"

        print("score=" + str(round(float(sc), 3)) + " -> " + res)

    else:
        print("Unknown command:", cmd)
        print_usage()
