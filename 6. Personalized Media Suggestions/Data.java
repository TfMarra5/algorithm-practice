import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Data {

    public static final String[] GENRES = {"Action", "Comedy", "Drama", "Horror", "Mystery"};

    public static class MovieInfo {
        public String title;
        public int[] genreVector;

        public MovieInfo(String t, int[] gv) {
            title = t;
            genreVector = gv;
        }
    }

    public static final Map<Integer, MovieInfo> MOVIES = new HashMap<Integer, MovieInfo>();

    static {
        MOVIES.put(0,  new MovieInfo("Mad Max: Fury Road",       new int[]{1,0,0,0,0}));
        MOVIES.put(1,  new MovieInfo("John Wick",                new int[]{1,0,0,0,0}));
        MOVIES.put(2,  new MovieInfo("The Dark Knight",          new int[]{1,0,0,0,0}));

        MOVIES.put(3,  new MovieInfo("The Hangover",             new int[]{0,1,0,0,0}));
        MOVIES.put(4,  new MovieInfo("Superbad",                 new int[]{0,1,0,0,0}));
        MOVIES.put(5,  new MovieInfo("Step Brothers",            new int[]{0,1,0,0,0}));

        MOVIES.put(6,  new MovieInfo("The Shawshank Redemption", new int[]{0,0,1,0,0}));
        MOVIES.put(7,  new MovieInfo("Forrest Gump",             new int[]{0,0,1,0,0}));
        MOVIES.put(8,  new MovieInfo("The Godfather",            new int[]{0,0,1,0,0}));

        MOVIES.put(9,  new MovieInfo("Get Out",                  new int[]{0,0,0,1,0}));
        MOVIES.put(10, new MovieInfo("A Quiet Place",            new int[]{0,0,0,1,0}));
        MOVIES.put(11, new MovieInfo("The Conjuring",            new int[]{0,0,0,1,0}));

        MOVIES.put(12, new MovieInfo("Knives Out",               new int[]{0,0,0,0,1}));
        MOVIES.put(13, new MovieInfo("Gone Girl",                new int[]{0,0,0,0,1}));
        MOVIES.put(14, new MovieInfo("Scooby-Doo",               new int[]{0,0,0,0,1}));
    }

    public static final List<Integer> ALL_IDS = new ArrayList<Integer>();

    static {
        int i = 0;
        while (i <= 14) {
            ALL_IDS.add(i);
            i++;
        }
    }

    public static final Map<Integer, Integer> POPULARITY = new HashMap<Integer, Integer>();

    static {
        POPULARITY.put(2, 900);
        POPULARITY.put(8, 850);
        POPULARITY.put(6, 800);
        POPULARITY.put(7, 760);

        POPULARITY.put(0, 740);
        POPULARITY.put(1, 700);
        POPULARITY.put(12, 680);
        POPULARITY.put(3, 640);

        POPULARITY.put(4, 620);
        POPULARITY.put(9, 600);
        POPULARITY.put(10, 580);
        POPULARITY.put(5, 560);

        POPULARITY.put(11, 540);
        POPULARITY.put(13, 520);
        POPULARITY.put(14, 400);
    }

    public static final double MAX_POP = findMaxPop();

    private static double findMaxPop() {
        int max = 0;

        for (int id : ALL_IDS) {
            Integer v = POPULARITY.get(id);
            if (v != null) {
                if (v > max) {
                    max = v;
                }
            }
        }

        return (double) max;
    }
}
