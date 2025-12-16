import java.util.Map;

public class Content {

    public static double cosine(double[] a, double[] b) {

        double dot = 0.0;
        double na = 0.0;
        double nb = 0.0;

        int i = 0;
        while (i < a.length) {
            dot = dot + a[i] * b[i];
            na = na + a[i] * a[i];
            nb = nb + b[i] * b[i];
            i++;
        }

        na = Math.sqrt(na);
        nb = Math.sqrt(nb);

        if (na == 0.0 || nb == 0.0) {
            return 0.0;
        }

        return dot / (na * nb);
    }

    public static double[] movieVec(int id) {

        double[] v = new double[Data.GENRES.length];

        if (Data.MOVIES.containsKey(id)) {
            int[] iv = Data.MOVIES.get(id).genreVector;

            int i = 0;
            while (i < iv.length) {
                v[i] = iv[i];
                i++;
            }
        }

        return v;
    }

    public static double[] contentUserVector(Map<Integer, Integer> ratings) {

        double[] acc = new double[Data.GENRES.length];
        double tot = 0.0;

        for (Map.Entry<Integer, Integer> e : ratings.entrySet()) {
            int id = e.getKey();
            int r = e.getValue();

            if (r >= 1 && r <= 5) {
                double[] mv = movieVec(id);

                int j = 0;
                while (j < mv.length) {
                    acc[j] = acc[j] + mv[j] * r;
                    j++;
                }

                tot = tot + r;
            }
        }

        if (tot > 0.0) {
            int i = 0;
            while (i < acc.length) {
                acc[i] = acc[i] / tot;
                i++;
            }
        }

        return acc;
    }

    public static void printCatalog() {

        System.out.println("\n=== Movie Catalog ===");

        int i = 0;
        while (i < Data.ALL_IDS.size()) {

            int id = Data.ALL_IDS.get(i);
            Data.MovieInfo info = Data.MOVIES.get(id);

            String g = "";
            boolean first = true;

            int j = 0;
            while (j < info.genreVector.length) {
                if (info.genreVector[j] == 1) {
                    if (!first) {
                        g = g + ", ";
                    }
                    g = g + Data.GENRES[j];
                    first = false;
                }
                j++;
            }

            System.out.println("[" + (id < 10 ? "0" + id : id) + "] "
                    + info.title + " - " + g);

            i++;
        }
    }
}
