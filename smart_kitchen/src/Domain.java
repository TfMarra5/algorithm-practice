import java.util.*;

// ----------------------------------------
// Ingredient
// ----------------------------------------
class Ingredient {
    public String name;
    public double quantity;
    public String unit;
    public double kcal_per_unit;

    public double kcal() {
        return quantity * kcal_per_unit;
    }
}

// ----------------------------------------
// Step
// ----------------------------------------
class Step {
    public String action;
    public double value;
    public String unit;
    public String note;
}

// ----------------------------------------
// Recipe
// ----------------------------------------
class Recipe {
    public String name;
    public List<Ingredient> ingredients = new ArrayList<>();
    public List<Step> steps = new ArrayList<>();
    public int servings = 8;
    public double pan_area_cm2 = 314.0;

    public Recipe copy() {
        Recipe r = new Recipe();
        r.name = this.name;
        r.servings = this.servings;
        r.pan_area_cm2 = this.pan_area_cm2;

        for (Ingredient ing : this.ingredients) {
            Ingredient i = new Ingredient();
            i.name = ing.name;
            i.quantity = ing.quantity;
            i.unit = ing.unit;
            i.kcal_per_unit = ing.kcal_per_unit;
            r.ingredients.add(i);
        }

        for (Step st : this.steps) {
            Step s = new Step();
            s.action = st.action;
            s.value = st.value;
            s.unit = st.unit;
            s.note = st.note;
            r.steps.add(s);
        }

        return r;
    }
}

// ----------------------------------------
// Pantry
// ----------------------------------------
class Pantry {
    public Map<String, Double> stock = new HashMap<>();

    public boolean has(String n, double need) {
        Double q = stock.get(n);
        return q != null && q + 1e-9 >= need;
    }

    public double qty(String n) {
        Double q = stock.get(n);
        return q == null ? 0.0 : q;
    }
}

// ----------------------------------------
// Constraints
// ----------------------------------------
class Constraints {
    public boolean vegan = false;
    public boolean lactose_free = false;
    public boolean gluten_free = false;
    public double max_kcal_per_serv = 0.0;

    public List<String> avoid = new ArrayList<>();

    public Integer target_servings = null;
    public Double target_pan_area_cm2 = null;
}

class Util {
    public static double round1(double x) {
        return Math.round(x * 10.0) / 10.0;
    }
}