import numpy as np
from typing import Dict
from data import GENRES, MOVIES, ALL_IDS

def cosine(a: np.ndarray, b: np.ndarray) -> float:
    na = np.linalg.norm(a); nb = np.linalg.norm(b)
    if na==0 or nb==0: return 0.0
    return float(a.dot(b) / (na*nb))

def movie_vec(mid: int) -> np.ndarray:
    return np.array(MOVIES[mid][1], dtype=float)

def content_user_vector(ratings: Dict[int,int]) -> np.ndarray:
    if not ratings: return np.zeros(len(GENRES))
    acc = np.zeros(len(GENRES)); wtot = 0.0
    for mid, r in ratings.items():
        if mid in MOVIES and 1<=r<=5:
            acc += movie_vec(mid) * r
            wtot += r
    return acc / wtot if wtot>0 else np.zeros(len(GENRES))

def print_catalog():
    print("\n=== Movie Catalog ===")
    for i,(t,vec) in MOVIES.items():
        g = ", ".join([GENRES[j] for j,v in enumerate(vec) if v==1])
        print(f"[{i:02d}] {t}  —  {g}")
    print()
