from typing import Dict, List, Tuple
import numpy as np
from data import ALL_IDS, POPULARITY, MAX_POP
from content import content_user_vector, movie_vec, cosine
from model import MFModel

def popularity_score(mid: int) -> float:
    return POPULARITY.get(mid, 0) / MAX_POP

def hybrid_scores(ratings: Dict[int,int], mf: MFModel, alpha: float=0.65, beta_pop: float=0.10) -> Dict[int,float]:
    # content
    u_vec = content_user_vector(ratings)
    cont_raw = {i: cosine(u_vec, movie_vec(i)) for i in ALL_IDS}
    if cont_raw:
        mx = max(cont_raw.values()); mn = min(cont_raw.values())
        cont = {i: 0.0 if mx==mn else (v-mn)/(mx-mn) for i,v in cont_raw.items()}
    else:
        cont = {i: 0.0 for i in ALL_IDS}
    # mf
    preds = {i: mf.predict(i) for i in ALL_IDS}
    pmin, pmax = min(preds.values()), max(preds.values())
    mf_norm = {i: 0.0 if pmax==pmin else (preds[i]-pmin)/(pmax-pmin) for i in ALL_IDS}
    # popularity
    pop = {i: popularity_score(i) for i in ALL_IDS}
    # blend
    return {i: alpha*mf_norm[i] + beta_pop*pop[i] + (1-alpha-beta_pop)*cont[i] for i in ALL_IDS}

def mmr_rerank(scores: Dict[int,float], k: int=5, lambda_rel: float=0.7) -> List[int]:
    selected: List[int] = []
    candidates = set(scores.keys())
    V = {i: movie_vec(i) for i in scores.keys()}
    def sim(i,j): return cosine(V[i], V[j])
    for _ in range(k):
        best=None; best_score=-1e9
        for i in list(candidates):
            if not selected:
                s = scores[i]
            else:
                max_sim = max(sim(i,j) for j in selected)
                s = lambda_rel*scores[i] - (1-lambda_rel)*max_sim
            if s>best_score:
                best_score=s; best=i
        if best is None: break
        selected.append(best); candidates.remove(best)
    return selected

def recommend(ratings: Dict[int,int], mf: MFModel, topn: int=5) -> List[Tuple[int,float]]:
    scores = hybrid_scores(ratings, mf, alpha=0.65, beta_pop=0.10)
    for mid in ratings.keys():
        scores.pop(mid, None)
    order = mmr_rerank(scores, k=topn, lambda_rel=0.7)
    return [(mid, scores[mid]) for mid in order]
