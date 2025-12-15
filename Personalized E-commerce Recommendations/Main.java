import java.util.*;

public class Main {

    static double round3(double x) {
        return Math.round(x * 1000.0) / 1000.0;
    }

    static void printRecs(String user, ArrayList<Rec> recs, HashMap<String, HashMap<String, Double>> inter) {
        HashMap<String, Double> u = inter.get(user);
        if (u == null) u = new HashMap<>();

        System.out.println("User " + user + " history: " + u.keySet());

        int i = 0;
        while (i < recs.size()) {
            Rec r = recs.get(i);
            System.out.println((i + 1) + ") " + r.p.name +
                    "  score=" + round3(r.score) +
                    "  cf=" + round3(r.cf) +
                    "  cbf=" + round3(r.cbf));
            i++;
        }
    }

    public static void main(String[] args) {
        HashMap<String, Product> products = DemoData.products();
        HashMap<String, HashMap<String, Double>> inter = DemoData.interactions();

        Recommender rec = new Recommender(inter);
        rec.build(products);

        int topN = 5;
        double alpha = 0.7;

        System.out.println("Recommender demo");

        String u1 = "U1";
        ArrayList<Rec> r1 = rec.recommend(u1, topN, alpha, products);
        System.out.println();
        System.out.println("Top " + topN + " for " + u1);
        printRecs(u1, r1, inter);

        String u4 = "U4";
        ArrayList<Rec> r4 = rec.recommend(u4, topN, alpha, products);
        System.out.println();
        System.out.println("Top " + topN + " for " + u4 + " (cold start)");
        printRecs(u4, r4, inter);
    }
}
