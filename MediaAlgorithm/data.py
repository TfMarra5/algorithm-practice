from typing import Dict, Tuple, List

GENRES = ["Action","Comedy","Drama","Horror","Mystery"]

# id -> (title, one-hot genre vector)
MOVIES: Dict[int, Tuple[str, List[int]]] = {
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

ALL_IDS = list(MOVIES.keys())

# fake popularity counts just for example
POPULARITY = {
    2: 900, 8: 850, 6: 800, 7: 760, 0: 740, 1: 700,
    12: 680, 3: 640, 4: 620, 9: 600, 10: 580, 5: 560, 11: 540, 13: 520, 14: 400
}
MAX_POP = max(POPULARITY.values())
