import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static class RecState {
        Map<Integer, Integer> ratings;
        MFModel mf;

        public RecState(Map<Integer, Integer> r, MFModel m) {
            ratings = r;
            mf = m;
        }
    }

    public static void main(String[] args) {

        System.out.println("Welcome to the Netflix Movie Suggestion System (Hybrid)");

        Storage.SavedState loaded = Storage.loadState();
        RecState state = new RecState(loaded.ratings, loaded.mf);

        Content.printCatalog();

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.println("\nCommands: rate | remove | rec | show | reset | save | quit");
            System.out.print("> ");

            String cmd = sc.nextLine();
            cmd = cmd.trim().toLowerCase();

            if (cmd.equals("quit")) {
                break;
            }

            if (cmd.equals("rate")) {

                System.out.print("Movie ID: ");
                String sId = sc.nextLine();

                int id = -1;
                try {
                    id = Integer.parseInt(sId);
                } catch (Exception e) {
                    System.out.println("Invalid ID.");
                }

                if (!Data.MOVIES.containsKey(id)) {
                    System.out.println("Invalid ID.");
                    continue;
                }

                System.out.print("Rating (1-5): ");
                String sR = sc.nextLine();

                int r = 0;
                try {
                    r = Integer.parseInt(sR);
                } catch (Exception e) {
                    System.out.println("Invalid rating.");
                    continue;
                }

                if (r < 1 || r > 5) {
                    System.out.println("Invalid rating.");
                    continue;
                }

                state.ratings.put(id, r);

                System.out.println("Saved: [" + twoDigits(id) + "] "
                        + Data.MOVIES.get(id).title + " -> " + r + "/5");
            }

            else if (cmd.equals("remove")) {

                System.out.print("Movie ID to remove: ");
                String s = sc.nextLine();

                int id = -1;
                try {
                    id = Integer.parseInt(s);
                } catch (Exception e) {
                    System.out.println("Not found.");
                }

                if (state.ratings.containsKey(id)) {
                    state.ratings.remove(id);
                    System.out.println("Removed.");
                } else {
                    System.out.println("Not found.");
                }
            }

            else if (cmd.equals("rec")) {

                if (state.ratings.size() >= 3) {
                    System.out.println("Training MF...");
                    state.mf.fit(state.ratings, 250, 0.03, 0.05);
                    double err = state.mf.rmse(state.ratings);
                    System.out.println("RMSE: " + round3(err));
                } else {
                    System.out.println("Few ratings, using cold-start logic.");
                }

                List<Hybrid.RecItem> recs = Hybrid.recommend(state.ratings, state.mf, 5);

                System.out.println("\n=== Recommendations ===");

                int i = 0;
                while (i < recs.size()) {
                    Hybrid.RecItem it = recs.get(i);
                    int mid = it.movieId;

                    System.out.println("- [" + twoDigits(mid) + "] "
                            + Data.MOVIES.get(mid).title
                            + " (score: " + round3(it.score) + ")");

                    i = i + 1;
                }

                printRatings(state.ratings);
            }

            else if (cmd.equals("show")) {
                Content.printCatalog();
                printRatings(state.ratings);
            }

            else if (cmd.equals("reset")) {

                state.ratings.clear();
                state.mf = new MFModel(Data.ALL_IDS.size());

                File f = new File("netflix_model.txt");
                if (f.exists()) {
                    f.delete();
                }

                System.out.println("Everything cleared.");
            }

            else if (cmd.equals("save")) {
                Storage.saveState(state.ratings, state.mf);
                System.out.println("Saved.");
            }

            else {
                System.out.println("Unknown command.");
            }
        }

        sc.close();
        System.out.println("Bye.");
    }

    private static void printRatings(Map<Integer, Integer> ratings) {

        if (ratings.size() == 0) {
            return;
        }

        System.out.println("\nYour ratings:");

        List<Integer> ids = new ArrayList<Integer>(ratings.keySet());
        Collections.sort(ids);

        int x = 0;
        while (x < ids.size()) {
            int id = ids.get(x);
            int r = ratings.get(id);

            System.out.println("- [" + twoDigits(id) + "] "
                    + Data.MOVIES.get(id).title
                    + " -> " + r + "/5");

            x++;
        }
    }

    private static String twoDigits(int x) {
        String s = "" + x;

        if (x >= 0) {
            if (x < 10) {
                s = "0" + s;
            }
        }

        return s;
    }

    private static String round3(double v) {
        long t = (long) (v * 1000.0);
        double out = t / 1000.0;
        return "" + out;
    }
}
