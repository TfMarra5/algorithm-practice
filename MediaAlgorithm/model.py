import math, random
import numpy as np
from typing import Dict

class MFModel:
    """Matrix Factorization with biases for a single active user."""
    def __init__(self, n_items: int, k: int = 16, seed: int = 42):
        rng = np.random.default_rng(seed)
        self.k = k
        self.p_u = rng.normal(0, 0.1, size=k)        # user factors
        self.q_i = rng.normal(0, 0.1, size=(n_items, k)) # item factors
        self.b_u = 0.0                               # user bias
        self.b_i = np.zeros(n_items)                 # item biases
        self.mu  = 3.5                               # global mean

    def predict(self, item_id: int) -> float:
        r = self.mu + self.b_u + self.b_i[item_id] + self.p_u.dot(self.q_i[item_id])
        return float(np.clip(r, 1.0, 5.0))

    def fit(self, ratings: Dict[int,int], epochs: int=250, lr: float=0.03, reg: float=0.05):
        if not ratings: return
        items = list(ratings.keys())
        self.mu = float(np.mean(list(ratings.values())))
        for _ in range(epochs):
            random.shuffle(items)
            for i in items:
                r_ui = ratings[i]
                pred = self.mu + self.b_u + self.b_i[i] + self.p_u.dot(self.q_i[i])
                e = r_ui - pred
                self.b_u  -= lr * (-2*e + 2*reg*self.b_u)
                self.b_i[i]-= lr * (-2*e + 2*reg*self.b_i[i])
                pu_grad = -2*e*self.q_i[i] + 2*reg*self.p_u
                qi_grad = -2*e*self.p_u   + 2*reg*self.q_i[i]
                self.p_u  -= lr * pu_grad
                self.q_i[i]-= lr * qi_grad
        self.b_u = float(np.clip(self.b_u, -2, 2))
        self.b_i = np.clip(self.b_i, -2, 2)

    def rmse(self, ratings: Dict[int,int]) -> float:
        if not ratings: return float("nan")
        se = 0.0
        for i,r in ratings.items():
            se += (r - self.predict(i))**2
        return math.sqrt(se/len(ratings))

    def to_dict(self):
        return {
            "k": self.k, "p_u": self.p_u.tolist(), "q_i": self.q_i.tolist(),
            "b_u": self.b_u, "b_i": self.b_i.tolist(), "mu": self.mu
        }
    @staticmethod
    def from_dict(d):
        m = MFModel(n_items=len(d["b_i"]), k=d["k"])
        import numpy as np
        m.p_u = np.array(d["p_u"], dtype=float)
        m.q_i = np.array(d["q_i"], dtype=float)
        m.b_u = float(d["b_u"])
        m.b_i = np.array(d["b_i"], dtype=float)
        m.mu  = float(d["mu"])
        return m
