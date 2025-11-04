Recommender (Hybrid) — minimal project

Run:
1) cd recommender
2) python -m pip install -r requirements.txt
3) python main.py

Commands inside the app:
- rate    : add/update a rating (1..5)
- remove  : delete a rating
- rec     : train (if >=3 ratings) and show top-5 hybrid recommendations
- show    : print catalog and your ratings
- reset   : clear everything (including saved model)
- save    : save ratings & model to netflix_model.json

Notes:
- Hybrid scores = 0.65*MF + 0.25*content + 0.10*popularity (tuned in code).
- MMR re-ranking increases diversity in the top-5.
- All pure Python + NumPy.
