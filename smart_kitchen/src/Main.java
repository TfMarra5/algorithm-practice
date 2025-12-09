public class Main {

    static Recipe default_cake() {
        Recipe r = new Recipe();
        r.name = "Vanilla Sponge Cake";
        r.servings = 8;
        r.pan_area_cm2 = 314.0;

        Ingredient i1 = new Ingredient();
        i1.name = "wheat_flour";
        i1.unit = "g";
        i1.quantity = 250;
        i1.kcal_per_unit = 3.6;

        Ingredient i2 = new Ingredient();
        i2.name = "sugar";
        i2.quantity = 180;
        i2.kcal_per_unit = 4.0;
        i2.unit = "g";

        Ingredient i3 = new Ingredient();
        i3.name = "butter";
        i3.quantity = 120;
        i3.unit = "g";
        i3.kcal_per_unit = 7.2;

        Ingredient i4 = new Ingredient();
        i4.name = "milk";
        i4.unit = "ml";
        i4.kcal_per_unit = 0.6;
        i4.quantity = 200;

        Ingredient i5 = new Ingredient();
        i5.name = "egg";
        i5.quantity = 3;
        i5.unit = "pcs";
        i5.kcal_per_unit = 70;

        Ingredient i6 = new Ingredient();
        i6.name = "baking_powder";
        i6.quantity = 10;
        i6.kcal_per_unit = 0;

        Ingredient i7 = new Ingredient();
        i7.name = "salt";
        i7.quantity = 2;
        i7.unit = "g";
        i7.kcal_per_unit = 0;


        r.ingredients.add(i1);
        r.ingredients.add(i2);
        r.ingredients.add(i3);
        r.ingredients.add(i4);
        r.ingredients.add(i5);
        r.ingredients.add(i6);
        r.ingredients.add(i7);

            Step s1 = new Step();
            s1.action = "preheat";
            s1.value = 180;
            s1.unit = "C";
            s1.note = "Preheat oven.";

            Step s2 = new Step();
            s2.action = "mix";
            s2.value = 0;
            s2.unit = "";
            s2.note = "Mix dry ingredients, add wet.";

            Step s3 = new Step();
            s3.action = "bake";
            s3.value = 35;
            s3.unit = "min";
            s3.note = "Bake until toothpick clean.";


        r.steps.add(s1);
        r.steps.add(s2);
        r.steps.add(s3);

        return r;
    }


    static void print_recipe(Recipe r) {
        System.out.println();
        System.out.println("Adjusted Recipe: " + r.name);
        System.out.println("Servings: " + r.servings);
        System.out.println("Pan area: " + r.pan_area_cm2 + " cm2");


        for (Ingredient ing : r.ingredients) {
            System.out.println(ing.name + ": " + ing.quantity + ing.unit +
                    " (" + ing.kcal() + "kcal)");
        }

        System.out.println("\nSteps:");
        for (Step st : r.steps) {
            String t = st.action;
            if (!st.unit.isEmpty()) {
                t += " " + st.value + st.unit;
            }
            System.out.println("* " + t + " - " + st.note);
        }

        System.out.println("Total kcal: " + Nutrition.totalKcal(r));
        System.out.println("per serving: " + Nutrition.kcalPerServ(r));
    }


    public static void main(String[] args) {

        Pantry p = new Pantry();
        p.stock.put("wheat_flour", 150.0);
        p.stock.put("gluten_free_mix", 0.0);

        p.stock.put("sugar", 200.0);
        p.stock.put("vegetable_oil", 150.0);
        p.stock.put("butter", 0.0);

        p.stock.put("oat_milk", 300.0);
        p.stock.put("milk", 0.0);
        p.stock.put("egg", 1.0);

        p.stock.put("flaxseed_meal+water", 100.0);
        p.stock.put("baking_powder", 20.0);
        p.stock.put("salt", 10.0);


        Constraints c = new Constraints();
        c.vegan = false;
        c.lactose_free = true;
        c.gluten_free = false;
        c.max_kcal_per_serv = 280.0;
        c.target_servings = 6;
        c.target_pan_area_cm2 = 380.0;
        c.avoid.add("peanut");

        Oven oven = new Oven();
        oven.sensor_offsetC = 8.0;

        Assistant asst = new Assistant();
        Recipe base = default_cake();

        double oven_offsetC = oven.sensor_offsetC;

        AssistantResult res = asst.plan(base, p, c, oven_offsetC);

        print_recipe(res.adjusted);

        if (!res.notes.isEmpty()) {
            System.out.println();
            System.out.println("Notes:");
            for (String n : res.notes) {
                System.out.println("- " + n);
            }
        }

        if (!res.warnings.isEmpty()) {
            System.out.println();
            System.out.println("Warnings:");
            for (String w : res.warnings) {
                System.out.println("- " + w);
            }
        }

        for (Step st : res.adjusted.steps) {
            if (st.action.equals("preheat")) {
                oven.preheat(st.value);
            } else if (st.action.equals("bake")) {
                System.out.println("[Oven] Baking for " + st.value + " minutes");
            }
        }


        System.out.println("\nDone.");
    }
}