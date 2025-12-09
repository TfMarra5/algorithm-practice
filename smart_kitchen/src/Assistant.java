import java.util.*;
import static java.lang.Math.*;

class SubRule {
    String from;
    String to;
    double ratio;
    String reason;

    SubRule(String f, String t, double r, String re) {
        from = f;
        to = t;
        ratio = r;
        reason = re;
    }
}

class AssistantResult {
    Recipe adjusted;
    List<String> notes = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
}

class Nutrition {
    static double totalKcal(Recipe r) {
        double sum = 0;
        for (Ingredient ing : r.ingredients) sum += ing.kcal();
        return sum;
    }

    static double kcalPerServ(Recipe r) {
        if (r.servings > 0) return totalKcal(r) / r.servings;
        return totalKcal(r);
    }
}

public class Assistant {
    List<SubRule> rules = new ArrayList<>();

    public Assistant() {
        rules.add(new SubRule("egg", "flaxseed_meal+water", 1.0, "Vegan egg: 1 egg ≈ 10g flax + 30ml water"));
        rules.add(new SubRule("milk", "oat_milk", 1.0, "Lactose-free swap"));
        rules.add(new SubRule("butter", "vegetable_oil", 0.8, "80% oil for butter by weight"));
        rules.add(new SubRule("wheat_flour", "gluten_free_mix", 1.1, "GF mix needs ~10% more"));
        rules.add(new SubRule("yogurt", "soy_yogurt", 1.0, "Dairy-free yogurt"));
        rules.add(new SubRule("cream", "coconut_cream", 1.0, "Dairy-free cream"));
        rules.add(new SubRule("sugar", "brown_sugar", 1.0, "Interchangeable in most cakes"));
    }

    Optional<SubRule> findSub(String name) {
        for (SubRule r : rules) {
            if (r.from.equals(name)) return Optional.of(r);
        }
        return Optional.empty();
    }

    Optional<SubRule> findSubOriginal(String newname) {
        for (SubRule r : rules) {
            if (r.to.equals(newname)) return Optional.of(r);
        }
        return Optional.empty();
    }

    static void scaleForServings(Recipe r, int target) {
        if (target <= 0 || r.servings <= 0) return;
        double s = (double) target / r.servings;

        for (Ingredient ing : r.ingredients) {
            ing.quantity = round1(ing.quantity * s);
        }

        for (Step st : r.steps) {
            if (st.action.equals("bake") && st.unit.equals("min")) {
                st.value = round1(st.value * pow(s, 0.15)); 
            }
        }

        r.servings = target;
    }

    static void scaleForPanArea(Recipe r, double targetArea) {
        if (r.pan_area_cm2 <= 0 || targetArea <= 0) return; 
        double s = targetArea / r.pan_area_cm2;

        for (Ingredient ing : r.ingredients) {
            ing.quantity = round1(ing.quantity * s);
        }

        r.pan_area_cm2 = targetArea;

        for (Step st : r.steps) {
            if (st.action.equals("bake") && st.unit.equals("min")) {
                st.value = round1(st.value * pow(s, 0.25)); 
            }
        }
    }

    void applyDiet(Recipe r, Constraints c, AssistantResult ar) {
        for (Ingredient ing : r.ingredients) {
            String n = ing.name;
            boolean swapped = false;

            if (c.vegan && (n.equals("egg") || n.equals("butter") || n.equals("milk") || n.equals("cream") || n.equals("yogurt"))) {
                var sub = findSub(n);
                if (sub.isPresent()) {
                    ar.notes.add("Vegan: swapped " + n + " -> " + sub.get().to + " (" + sub.get().reason + ")");
                    ing.name = sub.get().to;
                    swapped = true;
                } else ar.warnings.add("No vegan substitute for " + n);
            }

            if (!swapped && c.lactose_free && (n.equals("milk") || n.equals("butter") || n.equals("cream") || n.equals("yogurt"))) {
                var sub = findSub(n);
                if (sub.isPresent()) {
                    ar.notes.add("Lactose-free: swapped " + n + " -> " + sub.get().to);
                    ing.name = sub.get().to;
                    swapped = true;
                } else ar.warnings.add("No lactose-free substitute for " + n);
            }

            if (c.gluten_free && n.equals("wheat_flour")) {
                var sub = findSub(n);
                if (sub.isPresent()) {
                    ar.notes.add("Gluten-free: swapped wheat_flour -> " + sub.get().to);
                    ing.name = sub.get().to;
                }
            }

            for (String bad : c.avoid) {
                if (ing.name.contains(bad))
                    ar.warnings.add("Contains avoided ingredient: " + ing.name);
            }
        }

        for (Ingredient ing : r.ingredients) {
            var sub = findSubOriginal(ing.name);
            if (sub.isPresent()) {
                if (sub.get().from.equals("butter") && ing.name.equals("vegetable_oil")) {
                    ing.quantity = round1(ing.quantity * sub.get().ratio);
                    ing.unit = "ml";
                } else if (sub.get().from.equals("wheat_flour") && ing.name.equals("gluten_free_mix")) {
                    ing.quantity = round1(ing.quantity * sub.get().ratio);
                }
            }
        }
    }

    void applyPantry(Recipe r, Pantry p, AssistantResult ar) {
        double limit = 1.0;

        for (Ingredient ing : r.ingredients) {
            double need = ing.quantity;
            double have = p.qty(ing.name);

            if (have <= 0 || have < need) {
                var back = findSub(ing.name);

                if (back.isPresent() && p.has(back.get().to, need * back.get().ratio)) {
                    ar.notes.add("Pantry: using " + back.get().to + " instead of " + ing.name);
                } else if (have <= 0) {
                    ar.warnings.add("Missing ingredient: " + ing.name + " (" + need + ing.unit + ")");
                    limit = min(limit, 0.0);
                } else {
                    limit = min(limit, have / need);
                }
            }
        }

        if (limit < 0.999) {
            ar.notes.add("Scaling recipe by pantry ratio = " + round1(limit));

            for (Ingredient ing : r.ingredients) {
                ing.quantity = round1(ing.quantity * limit);
            }

            for (Step st : r.steps) {
                if (st.action.equals("bake")) {
                    st.value = round1(st.value * pow(limit, 0.15));
                }
            }
        }
    }

    void applyKcalLimit(Recipe r, Constraints c, AssistantResult ar) {
        if (c.max_kcal_per_serv <= 0) return;

        double kps = Nutrition.kcalPerServ(r);
        if (kps <= c.max_kcal_per_serv) return;

        double need = c.max_kcal_per_serv / kps;
        double ratio = max(0.7, need);

        for (Ingredient ing : r.ingredients) {
            if (ing.name.equals("sugar") || ing.name.equals("brown_sugar") ||
                ing.name.equals("butter") || ing.name.equals("vegetable_oil")) {
                ing.quantity = round1(ing.quantity * ratio);
            }
        }

        ar.notes.add("Reduced sugar/fat for kcal limit " + c.max_kcal_per_serv);
    }

    void adjustStepsForOvenOffset(Recipe r, double offset, AssistantResult ar) {
        for (Step st : r.steps) {
            if (st.action.equals("preheat") && st.unit.equals("C")) {
                st.value = round1(st.value + offset);
                if (abs(offset) > 0.1)
                    ar.notes.add("Adjusted preheat by " + offset + "C");
            }
            if (st.action.equals("bake") && st.unit.equals("min") && offset < -2)
                st.value = round1(st.value * 1.05);
        }
    }

    public AssistantResult plan(Recipe base, Pantry pantry, Constraints c, double offset) {
        AssistantResult ar = new AssistantResult();
        ar.adjusted = base.copy();

        if (c.target_servings != null)
            scaleForServings(ar.adjusted, c.target_servings);

        if (c.target_pan_area_cm2 != null)
            scaleForPanArea(ar.adjusted, c.target_pan_area_cm2);

        applyDiet(ar.adjusted, c, ar);
        applyPantry(ar.adjusted, pantry, ar);

        applyKcalLimit(ar.adjusted, c, ar);
        adjustStepsForOvenOffset(ar.adjusted, offset, ar);

        return ar;
    }

    static double round1(double x) {
        return Math.round(x * 10.0) / 10.0;
    }
}