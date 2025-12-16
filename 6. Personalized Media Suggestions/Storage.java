import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Storage {

    private static final String SAVE_FILE = "netflix_model.txt";

    public static void saveState(Map<Integer, Integer> ratings, MFModel mf) {

        File file = new File(SAVE_FILE);

        try (FileWriter w = new FileWriter(file)) {

            w.write("RATINGS_START\n");

            for (Map.Entry<Integer, Integer> e : ratings.entrySet()) {
                w.write(e.getKey() + ":" + e.getValue() + "\n");
            }

            w.write("RATINGS_END\n");

            Map<String, Object> d = mf.toDict();

            w.write("MF_MODEL_START\n");

            for (Map.Entry<String, Object> e : d.entrySet()) {
                String key = e.getKey();
                Object val = e.getValue();
                w.write(key + "=" + arrayToString(val) + "\n");
            }

            w.write("MF_MODEL_END\n");

        } catch (IOException ex) {
            System.out.println("Error saving: " + ex.getMessage());
        }
    }

    private static String arrayToString(Object obj) {

        if (obj == null) {
            return "null";
        }

        if (obj instanceof double[]) {
            double[] arr = (double[]) obj;

            String s = "[";
            int i = 0;
            while (i < arr.length) {
                s = s + arr[i];
                if (i < arr.length - 1) {
                    s = s + ",";
                }
                i++;
            }
            s = s + "]";
            return s;
        }

        if (obj instanceof double[][]) {
            double[][] arr = (double[][]) obj;

            String s = "[[";
            int i = 0;
            while (i < arr.length) {
                int j = 0;
                while (j < arr[i].length) {
                    s = s + arr[i][j];
                    if (j < arr[i].length - 1) {
                        s = s + ",";
                    }
                    j++;
                }
                if (i < arr.length - 1) {
                    s = s + "][";
                }
                i++;
            }
            s = s + "]]";
            return s;
        }

        return "" + obj;
    }

    public static class SavedState {
        public Map<Integer, Integer> ratings;
        public MFModel mf;
    }

    public static SavedState loadState() {

        File file = new File(SAVE_FILE);

        SavedState st = new SavedState();
        st.ratings = new HashMap<Integer, Integer>();
        st.mf = new MFModel(Data.ALL_IDS.size());

        if (!file.exists()) {
            return st;
        }

        try (Scanner sc = new Scanner(file)) {

            String section = "";

            Map<String, Object> mfData = new HashMap<String, Object>();

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line == null) {
                    continue;
                }
                line = line.trim();

                if (line.equals("RATINGS_START")) {
                    section = "r";
                    continue;
                }
                if (line.equals("RATINGS_END")) {
                    section = "";
                    continue;
                }
                if (line.equals("MF_MODEL_START")) {
                    section = "m";
                    continue;
                }
                if (line.equals("MF_MODEL_END")) {
                    section = "";
                    continue;
                }

                if (section.equals("r")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        try {
                            int id = Integer.parseInt(parts[0].trim());
                            int rr = Integer.parseInt(parts[1].trim());
                            st.ratings.put(id, rr);
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                } else if (section.equals("m")) {

                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {

                        String key = parts[0].trim();
                        String val = parts[1].trim();

                        try {
                            if (key.equals("k")) {
                                mfData.put(key, Integer.parseInt(val));
                            } else if (key.equals("b_u") || key.equals("mu")) {
                                mfData.put(key, Double.parseDouble(val));
                            } else {

                                if (val.startsWith("[") && val.endsWith("]")) {

                                    if (key.equals("p_u") || key.equals("b_i")) {
                                        List<Double> one = parse1D(val);
                                        mfData.put(key, one);
                                    } else if (key.equals("q_i")) {
                                        List<List<Double>> two = parse2D(val);
                                        mfData.put(key, two);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // ignore broken line
                        }
                    }
                }
            }

            if (mfData.containsKey("k")) {
                st.mf = MFModel.fromDict(mfData);
            }

        } catch (IOException ex) {
            System.out.println("Error loading: " + ex.getMessage());
        }

        return st;
    }

    private static List<Double> parse1D(String val) {
        // expects: [1,2,3]
        List<Double> out = new ArrayList<Double>();

        if (val == null) {
            return out;
        }

        String s = val.trim();
        if (!s.startsWith("[") || !s.endsWith("]")) {
            return out;
        }

        s = s.substring(1, s.length() - 1).trim();
        if (s.length() == 0) {
            return out;
        }

        String[] parts = s.split(",");
        int i = 0;
        while (i < parts.length) {
            String p = parts[i].trim();
            if (p.length() > 0) {
                try {
                    out.add(Double.parseDouble(p));
                } catch (Exception e) {
                    // ignore
                }
            }
            i++;
        }

        return out;
    }

    private static List<List<Double>> parse2D(String val) {
        // expects something like: [[...][...][...]]
        List<List<Double>> out = new ArrayList<List<Double>>();

        if (val == null) {
            return out;
        }

        String s = val.trim();
        if (!s.startsWith("[[") || !s.endsWith("]]")) {
            return out;
        }

        // remove outer [[ and ]]
        s = s.substring(2, s.length() - 2);

        if (s.trim().length() == 0) {
            return out;
        }

        String[] rows = s.split("\\]\\[");

        int i = 0;
        while (i < rows.length) {
            String row = rows[i];
            List<Double> r = new ArrayList<Double>();

            String[] nums = row.split(",");
            int j = 0;
            while (j < nums.length) {
                String p = nums[j].trim();
                if (p.length() > 0) {
                    try {
                        r.add(Double.parseDouble(p));
                    } catch (Exception e) {
                        // ignore
                    }
                }
                j++;
            }

            out.add(r);
            i++;
        }

        return out;
    }
}
