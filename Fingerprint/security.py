# security.py
import os, json, numpy as np
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

def cancelable_transform(mins, token):
    # token -> ângulo + translação determinísticos
    rng = np.random.default_rng(abs(hash(token)) % (2**32))
    ang = rng.uniform(-0.35, 0.35)  # ~±20°
    tx, ty = rng.integers(-12, 12, size=2)
    out=[]
    for x,y,theta,t in mins:
        xr = x*np.cos(ang)-y*np.sin(ang)+tx
        yr = x*np.sin(ang)+y*np.cos(ang)+ty
        th = ((theta+ang+np.pi)%(2*np.pi))-np.pi
        # quantização leve (não-invertível na prática)
        out.append((round(xr,1), round(yr,1), round(th,2), t))
    return out

def encrypt_template(tmpl:dict, key:bytes):
    aes = AESGCM(key)
    nonce = os.urandom(12)
    blob = json.dumps(tmpl).encode()
    ct = aes.encrypt(nonce, blob, None)
    return nonce+ct

def decrypt_template(enc:bytes, key:bytes):
    aes = AESGCM(key)
    nonce, ct = enc[:12], enc[12:]
    blob = aes.decrypt(nonce, ct, None)
    return json.loads(blob.decode())
