import java.util.*;

public class Product {
    String id;
    String name;
    String category;
    String brand;
    ArrayList<String> tags;
    HashMap<String, Double> feats;

    public Product(String id, String name, String category, String brand, String[] tagsArr) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.brand = brand;

        tags = new ArrayList<>();
        int i = 0;
        while (i < tagsArr.length) {
            tags.add(tagsArr[i]);
            i++;
        }

        feats = new HashMap<>();
        feats.put("cat:" + category, 1.0);
        feats.put("brand:" + brand, 1.0);

        int j = 0;
        while (j < tags.size()) {
            String t = tags.get(j);
            String k = "tag:" + t;

            Double old = feats.get(k);
            if (old == null) old = 0.0;

            feats.put(k, old + 1.0);
            j++;
        }
    }
}
