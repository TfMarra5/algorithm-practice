from typing import Dict
from content import print_catalog
from model import MFModel
from data import ALL_IDS, MOVIES
from hybrid import recommend
from storage import load_state, save_state

def train_if_ready(ratings: Dict[int,int], mf: MFModel):
    if len(ratings) >= 3:
        print(f"Training MF on your {len(ratings)} ratings …")
        mf.fit(ratings, epochs=250, lr=0.03, reg=0.05)
        print(f" → RMSE on your ratings: {mf.rmse(ratings):.3f}")
    else:
        print("Cold-start (few ratings). Using mostly content/popularity for now.")

def print_recs(recs):
    print("\n=== Recommended Movies (Hybrid + MMR) ===")
    if not recs:
        print("- No recommendations yet. Rate a few movies first."); return
    for mid, s in recs:
        print(f"- [{mid:02d}] {MOVIES[mid][0]}  (score: {s:.3f})")

def print_ratings(ratings):
    if not ratings: return
    print("\nYour ratings:")
    for mid in sorted(ratings.keys()):
        print(f"- [{mid:02d}] {MOVIES[mid][0]} → {ratings[mid]}/5")

def main():
    print("Welcome to the Netflix Movie Suggestion System (Hybrid)!")
    ratings, mf = load_state()
    print_catalog()
    while True:
        cmd = input("\nCommands: rate | remove | rec | show | reset | save | quit\n> ").strip().lower()
        if cmd == "quit": break
        elif cmd == "rate":
            mid = input("Movie ID: ").strip()
            if not mid.isdigit() or int(mid) not in MOVIES: print("Invalid ID."); continue
            r = input("Rating (1-5): ").strip()
            if not r.isdigit() or not (1 <= int(r) <= 5): print("Invalid rating."); continue
            ratings[int(mid)] = int(r)
            print(f"Saved: [{int(mid):02d}] {MOVIES[int(mid)][0]} → {int(r)}/5")
        elif cmd == "remove":
            mid = input("Movie ID to remove rating: ").strip()
            if mid.isdigit() and int(mid) in ratings: ratings.pop(int(mid)); print("Removed.")
            else: print("Not found.")
        elif cmd == "rec":
            train_if_ready(ratings, mf)
            recs = recommend(ratings, mf, topn=5)
            print_recs(recs); print_ratings(ratings)
        elif cmd == "show":
            print_catalog(); print_ratings(ratings)
        elif cmd == "reset":
            ratings.clear(); mf = MFModel(n_items=len(ALL_IDS))
            import os
            if os.path.exists("netflix_model.json"): os.remove("netflix_model.json")
            print("Cleared ratings and model.")
        elif cmd == "save":
            save_state(ratings, mf); print("State saved to netflix_model.json.")
        else:
            print("Unknown command.")

if __name__ == "__main__":
    main()
