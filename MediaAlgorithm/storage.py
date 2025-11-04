import os, json
from typing import Dict, Tuple
from data import ALL_IDS
from model import MFModel

SAVE_FILE = "netflix_model.json"

def save_state(ratings: Dict[int,int], mf: MFModel):
    data = {"ratings": ratings, "mf": mf.to_dict()}
    with open(SAVE_FILE, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

def load_state() -> Tuple[Dict[int,int], MFModel]:
    if not os.path.exists(SAVE_FILE):
        return {}, MFModel(n_items=len(ALL_IDS))
    with open(SAVE_FILE, "r", encoding="utf-8") as f:
        d = json.load(f)
    ratings = {int(k): int(v) for k,v in d.get("ratings", {}).items()}
    mf = MFModel.from_dict(d["mf"])
    return ratings, mf
