import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class MFModel {

    private int k;
    private double[] p_u;
    private double[][] q_i;
    private double b_u;
    private double[] b_i;
    private double mu;
    private Random rng;

    public MFModel(int n_items) {
        this(n_items, 16, 42);
    }

    public MFModel(int n_items, int kk, long seed) {
        k = kk;
        rng = new Random(seed);

        p_u = new double[k];
        int i = 0;
        while (i < k) {
            p_u[i] = rng.nextGaussian() * 0.1;
            i++;
        }

        q_i = new double[n_items][k];
        int a = 0;
        while (a < n_items) {
            int b = 0;
            while (b < k) {
                q_i[a][b] = rng.nextGaussian() * 0.1;
                b++;
            }
            a++;
        }

        b_u = 0.0;
        b_i = new double[n_items];
        mu = 3.5;
    }

    public double predict(int itemId) {

        double dot = 0.0;

        int j = 0;
        while (j < k) {
            dot = dot + p_u[j] * q_i[itemId][j];
            j++;
        }

        double r = mu + b_u + b_i[itemId] + dot;

        if (r < 1.0) {
            r = 1.0;
        } else {
            if (r > 5.0) {
                r = 5.0;
            }
        }

        return r;
    }

    public void fit(Map<Integer, Integer> ratings, int epochs, double lr, double reg) {

        if (ratings == null) {
            return;
        }
        if (ratings.size() == 0) {
            return;
        }

        // recompute global mean
        double sum = 0.0;
        int cnt = 0;

        for (Map.Entry<Integer, Integer> e : ratings.entrySet()) {
            int r = e.getValue();
            sum = sum + r;
            cnt = cnt + 1;
        }

        if (cnt > 0) {
            mu = sum / cnt;
        } else {
            mu = 3.5;
        }

        // copy keys to list (so we can shuffle)
        List<Integer> items = new ArrayList<Integer>();
        for (Integer id : ratings.keySet()) {
            items.add(id);
        }

        int ep = 0;
        while (ep < epochs) {

            Collections.shuffle(items, rng);

            int idx = 0;
            while (idx < items.size()) {

                int itemId = items.get(idx);

                // rating value
                int r_ui = 0;
                if (ratings.containsKey(itemId)) {
                    r_ui = ratings.get(itemId);
                } else {
                    // shouldn't happen, but ok
                    r_ui = 0;
                }

                // compute dot product
                double dot = 0.0;
                int j = 0;
                while (j < k) {
                    dot = dot + p_u[j] * q_i[itemId][j];
                    j++;
                }

                // prediction without clamp (so training has gradient)
                double pred = mu + b_u + b_i[itemId] + dot;

                // error
                double err = r_ui - pred;

                // gradients (kinda expanded, not nice)
                double two = 2.0;
                double neg2e = (-two) * err;

                // update user bias
                double grad_bu = neg2e + (two * reg * b_u);
                b_u = b_u - lr * grad_bu;

                // update item bias
                double bi = b_i[itemId];
                double grad_bi = neg2e + (two * reg * bi);
                bi = bi - lr * grad_bi;
                b_i[itemId] = bi;

                // update latent vectors
                int t = 0;
                while (t < k) {

                    double pu = p_u[t];
                    double qi = q_i[itemId][t];

                    // grads
                    double g_pu = (neg2e * qi) + (two * reg * pu);
                    double g_qi = (neg2e * pu) + (two * reg * qi);

                    // update
                    pu = pu - lr * g_pu;
                    qi = qi - lr * g_qi;

                    p_u[t] = pu;
                    q_i[itemId][t] = qi;

                    t++;
                }

                idx++;
            }

            ep++;
        }

        // clamp biases a bit (just to keep them not insane)
        if (b_u > 2.0) {
            b_u = 2.0;
        } else {
            if (b_u < -2.0) {
                b_u = -2.0;
            }
        }

        int i = 0;
        while (i < b_i.length) {
            if (b_i[i] > 2.0) {
                b_i[i] = 2.0;
            } else {
                if (b_i[i] < -2.0) {
                    b_i[i] = -2.0;
                }
            }
            i++;
        }
    }

    public double rmse(Map<Integer, Integer> ratings) {

        if (ratings == null) {
            return Double.NaN;
        }
        if (ratings.size() == 0) {
            return Double.NaN;
        }

        double se = 0.0;
        int n = 0;

        for (Map.Entry<Integer, Integer> entry : ratings.entrySet()) {
            int itemId = entry.getKey();
            int r = entry.getValue();

            double pred = predict(itemId);
            double diff = (r - pred);

            se = se + diff * diff;
            n++;
        }

        if (n == 0) {
            return Double.NaN;
        }

        return Math.sqrt(se / n);
    }

    public Map<String, Object> toDict() {

        Map<String, Object> d = new HashMap<String, Object>();

        d.put("k", k);
        d.put("p_u", p_u);
        d.put("q_i", q_i);
        d.put("b_u", b_u);
        d.put("b_i", b_i);
        d.put("mu", mu);

        return d;
    }

    @SuppressWarnings("unchecked")
    public static MFModel fromDict(Map<String, Object> d) {

        int k = (Integer) d.get("k");
        double b_u = (Double) d.get("b_u");
        double mu = (Double) d.get("mu");

        List<Double> p_u_list = (List<Double>) d.get("p_u");
        List<Double> b_i_list = (List<Double>) d.get("b_i");
        List<List<Double>> q_i_list = (List<List<Double>>) d.get("q_i");

        int n_items = b_i_list.size();

        MFModel m = new MFModel(n_items, k, 42);

        m.p_u = new double[k];
        int i = 0;
        while (i < k) {
            m.p_u[i] = p_u_list.get(i);
            i++;
        }

        m.b_i = new double[n_items];
        int a = 0;
        while (a < n_items) {
            m.b_i[a] = b_i_list.get(a);
            a++;
        }

        m.q_i = new double[n_items][k];
        int r = 0;
        while (r < n_items) {
            List<Double> row = q_i_list.get(r);

            int c = 0;
            while (c < k) {
                m.q_i[r][c] = row.get(c);
                c++;
            }
            r++;
        }

        m.b_u = b_u;
        m.mu = mu;

        return m;
    }
}
