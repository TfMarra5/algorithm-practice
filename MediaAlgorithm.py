"""
Design an algorithm for a Netflix movie suggestion system using machine learning techniques
(eg, matrix factorization, embeddings, or deep learning).
"""

print("Welcome to the Netflix Movie Suggestion System.")
# Simple content-based recommender with per-movie ratings (1–5)
# Builds a user profile as a weighted average of movie-genre one-hot vectors.

MOVIES = {
    # The id is: (title, vector [Action, Comedy, Drama, Horror, Mystery])
    0: ("Mad Max: Fury Road",                               [1,0,0,0,0]),
    1: ("John Wick",                                        [1,0,0,0,0]),
    2: ("The Dark Knight",                                  [1,0,0,0,0]),
    3: ("The Hangover",                                     [0,1,0,0,0]),
    4: ("Superbad",                                         [0,1,0,0,0]),
    5: ("Step Brothers",                                    [0,1,0,0,0]),
    6: ("The Shawshank Redemption",                         [0,0,1,0,0]),
    7: ("Forrest Gump",                                     [0,0,1,0,0]),
    8: ("The Godfather",                                    [0,0,1,0,0]),
    9: ("Get Out",                                          [0,0,0,1,0]),
    10:("A Quiet Place",                                    [0,0,0,1,0]),
    11:("The Conjuring",                                    [0,0,0,1,0]),
    12:("Knives Out",                                       [0,0,0,0,1]),
    13:("Gone Girl",                                        [0,0,0,0,1]),
    14:("Scooby-Doo",                                       [0,0,0,0,1])
}


GENRES = ["Action","Comedy","Drama","Horror","Mystery"]

def dot(a, b): return sum(x*y for x,y in zip(a,b))
def norm(a): return sum(x*x for x in a) ** 0.5
def cosine(a, b):
    na, nb = norm(a), norm(b)
    if na == 0 or nb == 0: return 0.0
    return dot(a,b)/(na*nb)

def print_catalog():
    print("\n=== Movie Catalog ===")
    for i,(t,vec) in MOVIES.items():
        g = ", ".join([GENRES[j] for j,v in enumerate(vec) if v==1])
        print(f"[{i:02d}] {t}  —  {g}")
    print()

def build_user_vector_from_ratings(ratings_dict):
    # ratings_dict: movie_id: rating 1 - 5
    if not ratings_dict:
        return [0]*5
    acc = [0.0]*5
    total_w = 0.0
    for mid, r in ratings_dict.items():
        if mid in MOVIES and 1 <= r <= 5:
            vec = MOVIES[mid][1]
            for k in range(5):
                acc[k] += vec[k] * r
            total_w += r
    if total_w == 0:
        return [0]*5
    return [x/total_w for x in acc]

def recommend(user_vec, exclude_ids=set(), topn=5):
    scored = []
    for i,(title,vec) in MOVIES.items():
        if i in exclude_ids:
            continue
        scored.append((cosine(user_vec, vec), i, title))
    scored.sort(reverse=True)
    return scored[:topn]

def menu():
    print("Welcome to the Simple Netflix-like Recommender with Ratings!")
    print_catalog()

    ratings = {}
    while True:
        cmd = input("\nType 'rate' to add/update a rating, 'done' to finish, or 'genre' to pick a single genre: ").strip().lower()
        if cmd == "done":
            break
        elif cmd == "rate":
            mid_str = input("Enter movie ID to rate (e.g., 0): ").strip()
            if not mid_str.isdigit():
                print("Invalid ID.")
                continue
            mid = int(mid_str)
            if mid not in MOVIES:
                print("Movie not found.")
                continue
            r_str = input("Enter your rating (1-5): ").strip()
            if not r_str.isdigit() or not (1 <= int(r_str) <= 5):
                print("Invalid rating. Must be 1..5.")
                continue
            ratings[mid] = int(r_str)
            title = MOVIES[mid][0]
            print(f"Saved: {title} → {ratings[mid]}/5")
        elif cmd == "genre":
            print("Genres: 1) Action  2) Comedy  3) Drama  4) Horror  5) Mystery")
            g = input("Choose a genre (1-5): ").strip()
            if g.isdigit() and 1 <= int(g) <= 5:
                user_vec = [0]*5
                user_vec[int(g)-1] = 1
                recs = recommend(user_vec, exclude_ids=set(ratings.keys()), topn=5)
                print("\n=== Recommended Movies ===")
                for score, i, title in recs:
                    print(f"- {title}  (similarity: {score:.2f})")
            else:
                print("Invalid genre option.")
        else:
            print("Unknown command. Use 'rate', 'done', or 'genre'.")

    # Build user vector from ratings and recommendation
    user_vec = build_user_vector_from_ratings(ratings)
    recs = recommend(user_vec, exclude_ids=set(ratings.keys()), topn=5)
    print("\n=== Recommended Movies ===")
    if not recs:
        print("- No recommendations. Try rating a few movies first or use 'genre' mode.")
    else:
        for score, i, title in recs:
            print(f"- {title}  (similarity: {score:.2f})")

    # To show what was rated
    if ratings:
        print("\nYour ratings:")
        for mid, r in sorted(ratings.items()):
            print(f"- [{mid:02d}] {MOVIES[mid][0]} → {r}/5")

    print("\nTip: More ratings = a better profile.\n")

def main():
    menu()

if __name__ == "__main__":
    main()
