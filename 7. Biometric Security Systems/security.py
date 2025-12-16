# security.py
import os
import json
import numpy as np
from cryptography.hazmat.primitives.ciphers.aead import AESGCM


def cancelable_transform(mins, token):
    # deterministic transform based on token
    seed = abs(hash(token)) % (2 ** 32)
    rng = np.random.default_rng(seed)

    ang = rng.uniform(-0.35, 0.35)
    shift = rng.integers(-12, 12, size=2)
    tx = int(shift[0])
    ty = int(shift[1])

    out = []

    i = 0
    while i < len(mins):
        x = float(mins[i][0])
        y = float(mins[i][1])
        theta = float(mins[i][2])
        t = mins[i][3]

        xr = x * np.cos(ang) - y * np.sin(ang)
        yr = x * np.sin(ang) + y * np.cos(ang)

        xr = xr + tx
        yr = yr + ty

        th = theta + ang
        th = (th + np.pi) % (2 * np.pi) - np.pi

        xr = round(xr, 1)
        yr = round(yr, 1)
        th = round(th, 2)

        out.append((xr, yr, th, t))
        i += 1

    return out


def encrypt_template(tmpl, key):
    aes = AESGCM(key)

    nonce = os.urandom(12)

    txt = json.dumps(tmpl)
    data = txt.encode("utf-8")

    enc = aes.encrypt(nonce, data, None)

    return nonce + enc


def decrypt_template(enc, key):
    aes = AESGCM(key)

    nonce = enc[:12]
    ct = enc[12:]

    data = aes.decrypt(nonce, ct, None)

    txt = data.decode("utf-8")
    tmpl = json.loads(txt)

    return tmpl
