import java.util.*;

public class DemoData {

    public static HashMap<String, Product> products() {
        HashMap<String, Product> p = new HashMap<>();

        p.put("P1", new Product("P1", "Laptop Pro", "Electronics", "BrandX",
                new String[]{"high-end", "work", "pc"}));
        p.put("P2", new Product("P2", "Gaming Mouse", "Electronics", "BrandY",
                new String[]{"gaming", "pc", "peripheral"}));
        p.put("P3", new Product("P3", "Coffee Maker", "Home", "BrandZ",
                new String[]{"kitchen", "appliance", "brew"}));
        p.put("P4", new Product("P4", "Novel SciFi", "Books", "BrandA",
                new String[]{"scifi", "fiction", "bestseller"}));
        p.put("P5", new Product("P5", "E-Reader", "Electronics", "BrandX",
                new String[]{"reading", "portable"}));
        p.put("P6", new Product("P6", "Fantasy Book", "Books", "BrandB",
                new String[]{"fantasy", "fiction"}));
        p.put("P7", new Product("P7", "Blender", "Home", "BrandZ",
                new String[]{"kitchen", "appliance", "smoothie"}));
        p.put("P8", new Product("P8", "External HDD", "Electronics", "BrandY",
                new String[]{"storage", "pc", "work"}));

        return p;
    }

    public static HashMap<String, HashMap<String, Double>> interactions() {
        HashMap<String, HashMap<String, Double>> inter = new HashMap<>();

        HashMap<String, Double> u1 = new HashMap<>();
        u1.put("P1", 3.0);
        u1.put("P8", 2.0);
        u1.put("P3", 1.0);
        inter.put("U1", u1);

        HashMap<String, Double> u2 = new HashMap<>();
        u2.put("P2", 3.0);
        u2.put("P4", 3.0);
        inter.put("U2", u2);

        HashMap<String, Double> u3 = new HashMap<>();
        u3.put("P3", 3.0);
        u3.put("P7", 2.0);
        u3.put("P4", 1.0);
        inter.put("U3", u3);

        // U4: cold start (no entry on purpose)
        return inter;
    }
}
