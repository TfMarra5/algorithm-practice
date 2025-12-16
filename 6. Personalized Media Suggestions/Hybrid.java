import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hybrid {

    public static class RecItem {
        int movieId;
        double score;

        RecItem(int m, double s) {
            movieId = m;
            score = s;
        }
    }

    private static double popScore(int id) {
        Integer p = Data.POPULARITY.get(id);
        if (p == null) {
            return 0.0;
        }
        return p / Data.MAX_POP;
    }

    public static List<RecItem> recommend(Map<Integer, Integer> ratings, MFModel mf, int k) {

        Map<Integer, Double> scores = new HashMap<Integer, Double>();

        double[] uVec = Content.contentUserVector(ratings);

        int i = 0;
        while (i < Data.ALL_IDS.size()) {
            int id = Data.ALL_IDS.get(i);

            double c = Content.cosine(uVec, Content.movieVec(id));
            double m = mf.predict(id) / 5.0;
            double p = popScore(id);

            double s = 0.65 * m + 0.10 * p + 0.25 * c;
            scores.put(id, s);

            i++;
        }

        for (int id : ratings.keySet()) {
            scores.remove(id);
        }

        List<RecItem> out = new ArrayList<RecItem>();

        int count = 0;
        while (count < k && scores.size() > 0) {

            int best = -1;
            double bestVal = -999;

            for (Map.Entry<Integer, Double> e : scores.entrySet()) {
                if (e.getValue() > bestVal) {
                    bestVal = e.getValue();
                    best = e.getKey();
                }
            }

            if (best == -1) {
                break;
            }

            out.add(new RecItem(best, bestVal));
            scores.remove(best);
            count++;
        }

        return out;
    }
}
