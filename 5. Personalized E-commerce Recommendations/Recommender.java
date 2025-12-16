import java.util.*;

public class Recommender {

    HashMap<String, HashMap<String, Double>> inter;

    HashMap<String, HashMap<String, Double>> itemVec;
    HashMap<String, HashMap<String, Double>> sim;
    HashMap<String, Double> pop;

    public Recommender(HashMap<String, HashMap<String, Double>> inter) {
        this.inter = inter;
        itemVec = new HashMap<>();
        sim = new HashMap<>();
        pop = new HashMap<>();
    }

    double cosine(HashMap<String, Double> a, HashMap<String, Double> b) {
        if (a == null) return 0.0;
        if (b == null) return 0.0;
        if (a.size() == 0) return 0.0;
        if (b.size() == 0) return 0.0;

        HashMap<String, Double> small = a;
        HashMap<String, Double> big = b;

        if (a.size() > b.size()) {
            small = b;
            big = a;
        }

        double dot = 0.0;
        for (String k : small.keySet()) {
            double v1 = small.get(k);
            Double v2 = big.get(k);
            if (v2 == null) v2 = 0.0;
            dot += v1 * v2;
        }

        double na = 0.0;
        for (double v : a.values()) na += v * v;

        double nb = 0.0;
        for (double v : b.values()) nb += v * v;

        if (na == 0.0) return 0.0;
        if (nb == 0.0) return 0.0;

        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    void addToItemVec(String item, String user, double score) {
        HashMap<String, Double> v = itemVec.get(item);
        if (v == null) {
            v = new HashMap<>();
            itemVec.put(item, v);
        }
        v.put(user, score);
    }

    void addToPop(String item, double score) {
        Double old = pop.get(item);
        if (old == null) old = 0.0;
        pop.put(item, old + score);
    }

    public void build(HashMap<String, Product> products) {
        itemVec = new HashMap<>();
        sim = new HashMap<>();
        pop = new HashMap<>();

        for (String user : inter.keySet()) {
            HashMap<String, Double> u = inter.get(user);
            for (String item : u.keySet()) {
                double sc = u.get(item);
                addToItemVec(item, user, sc);
                addToPop(item, sc);
            }
        }

        ArrayList<String> ids = new ArrayList<>();
        for (String id : products.keySet()) ids.add(id);

        int i = 0;
        while (i < ids.size()) {
            String a = ids.get(i);

            HashMap<String, Double> rowA = sim.get(a);
            if (rowA == null) {
                rowA = new HashMap<>();
                sim.put(a, rowA);
            }

            HashMap<String, Double> vecA = itemVec.get(a);
            if (vecA == null) vecA = new HashMap<>();

            int j = i;
            while (j < ids.size()) {
                String b = ids.get(j);

                HashMap<String, Double> rowB = sim.get(b);
                if (rowB == null) {
                    rowB = new HashMap<>();
                    sim.put(b, rowB);
                }

                HashMap<String, Double> vecB = itemVec.get(b);
                if (vecB == null) vecB = new HashMap<>();

                double s = cosine(vecA, vecB);
                if (s > 0.0) {
                    rowA.put(b, s);
                    rowB.put(a, s);
                }

                j++;
            }

            i++;
        }
    }

    double cfScore(String user, String targetItem) {
        HashMap<String, Double> u = inter.get(user);
        if (u == null) return 0.0;
        if (u.size() == 0) return 0.0;

        HashMap<String, Double> row = sim.get(targetItem);
        if (row == null) return 0.0;
        if (row.size() == 0) return 0.0;

        double sum = 0.0;
        double den = 0.0;

        for (String seen : u.keySet()) {
            Double s = row.get(seen);
            if (s == null) s = 0.0;

            if (s > 0.0) {
                double r = u.get(seen);
                sum += s * r;
                den += s;
            }
        }

        double ans = 0.0;
        if (den != 0.0) ans = sum / den;
        return ans;
    }

    double cbfScore(String user, String itemId, HashMap<String, Product> products) {
        HashMap<String, Double> u = inter.get(user);
        if (u == null) return 0.0;
        if (u.size() == 0) return 0.0;

        HashMap<String, Double> profile = new HashMap<>();
        double total = 0.0;

        for (String seen : u.keySet()) {
            double w = u.get(seen);
            total += w;

            Product p = products.get(seen);
            if (p == null) continue;

            for (String f : p.feats.keySet()) {
                Double old = profile.get(f);
                if (old == null) old = 0.0;
                profile.put(f, old + p.feats.get(f) * w);
            }
        }

        if (total == 0.0) return 0.0;

        ArrayList<String> keys = new ArrayList<>(profile.keySet());
        int i = 0;
        while (i < keys.size()) {
            String k = keys.get(i);
            profile.put(k, profile.get(k) / total);
            i++;
        }

        Product t = products.get(itemId);
        if (t == null) return 0.0;

        return cosine(profile, t.feats);
    }

    double popBoost(String itemId) {
        Double v = pop.get(itemId);
        if (v == null) v = 0.0;
        return v / 10.0;
    }

    public ArrayList<Rec> recommend(String user, int topN, double alpha, HashMap<String, Product> products) {
        HashMap<String, Double> u = inter.get(user);
        if (u == null) u = new HashMap<>();

        boolean cold = (u.size() == 0);
        double a = alpha;
        if (cold) a = 0.0;

        HashSet<String> seen = new HashSet<>();
        for (String it : u.keySet()) seen.add(it);

        ArrayList<Rec> list = new ArrayList<>();

        for (Product p : products.values()) {
            if (seen.contains(p.id)) continue;

            Rec r = new Rec(p);

            r.cbf = cbfScore(user, p.id, products);
            r.cf = cfScore(user, p.id);

            if (r.cf == 0.0 && a > 0.0) r.score = r.cbf;
            else r.score = a * r.cf + (1.0 - a) * r.cbf;

            if (r.score < 0.001) {
                double b = popBoost(p.id);
                if (cold) r.score = b;
                else r.score = r.score + b;
            }

            list.add(r);
        }

        Collections.sort(list, new Comparator<Rec>() {
            public int compare(Rec x, Rec y) {
                if (x.score == y.score) return 0;
                if (x.score < y.score) return 1;
                return -1;
            }
        });

        ArrayList<Rec> out = new ArrayList<>();
        int idx = 0;
        while (idx < list.size()) {
            if (out.size() >= topN) break;
            out.add(list.get(idx));
            idx++;
        }

        return out;
    }
}
