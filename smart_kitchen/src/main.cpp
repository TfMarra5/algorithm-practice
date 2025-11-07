#include "domain.hpp"
#include "assistant.hpp"
#include "iot.hpp"
#include <iostream>

Recipe default_cake(){
    Recipe r;
    r.name = "Vanilla Sponge Cake";
    r.servings = 8;
    r.pan_area_cm2 = 314.0; // ~ 20cm round
    r.ingredients = {
        {"wheat_flour", 250, "g", 3.6},
        {"sugar", 180, "g", 4.0},
        {"butter", 120, "g", 7.2},
        {"milk", 200, "ml", 0.6},
        {"egg", 3, "pcs", 70.0},  // kcal per egg approx
        {"baking_powder", 10, "g", 0.0},
        {"salt", 2, "g", 0.0}
    };
    r.steps = {
        {"preheat", 180, "C",  "Preheat oven."},
        {"mix", 0, "",        "Mix dry ingredients, add wet."},
        {"bake", 35, "min",   "Bake until toothpick clean."}
    };
    return r;
}

void print_recipe(const Recipe& r){
    std::cout << "\n== Adjusted Recipe: " << r.name << " ==\n";
    std::cout << "Servings: " << r.servings << " | Pan area: " << r.pan_area_cm2 << " cm^2\n";
    for(const auto& ing: r.ingredients){
        std::cout << "- " << ing.name << ": " << ing.quantity << " " << ing.unit
                  << " (" << ing.kcal() << " kcal)\n";
    }
    std::cout << "Steps:\n";
    for(const auto& st: r.steps){
        std::cout << "  * " << st.action;
        if (!st.unit.empty()) std::cout << " " << st.value << " " << st.unit;
        std::cout << " — " << st.note << "\n";
    }
    std::cout << "Total kcal: " << Nutrition::total_kcal(r)
              << " | per serving: " << Nutrition::kcal_per_serv(r) << "\n";
}

int main(int argc, char** argv){
    // Inputs (in real app: parse CLI/JSON). Here we fix some demo constraints.
    Pantry p{{ // available stock (same units as recipe)
        {"wheat_flour", 150}, {"gluten_free_mix", 0},
        {"sugar", 200}, {"vegetable_oil", 150}, {"butter", 0},
        {"oat_milk", 300}, {"milk", 0},
        {"egg", 1}, {"flaxseed_meal+water", 100},
        {"baking_powder", 20}, {"salt", 10}
    }};
    Constraints c;
    c.vegan = false;
    c.lactose_free = true;
    c.gluten_free = false;
    c.max_kcal_per_serv = 280.0;
    c.target_servings = 6;                // user wants 6 servings
    c.target_pan_area_cm2 = 380.0;        // different pan
    c.avoid = {"peanut"};                 // example avoid list

    Oven oven; oven.sensor_offsetC = +8.0; // sensor reads +8C hotter than real
    Assistant asst;
    Recipe base = default_cake();

    // use thermometer to compute offset (actual = setpoint - offset)
    Thermometer th;
    double oven_offsetC = oven.sensor_offsetC; // simplified

    AssistantResult res = asst.plan(base, p, c, oven_offsetC);

    print_recipe(res.adjusted);
    if (!res.notes.empty()){
        std::cout << "\nNotes:\n";
        for(const auto& s: res.notes) std::cout << "- " << s << "\n";
    }
    if (!res.warnings.empty()){
        std::cout << "\nWarnings:\n";
        for(const auto& s: res.warnings) std::cout << "- " << s << "\n";
    }

    // drive mock oven
    for(const auto& st: res.adjusted.steps){
        if (st.action=="preheat"){
            oven.preheat(st.value);
        }
        if (st.action=="bake"){
            std::cout << "[Oven] Baking for " << st.value << " minutes.\n";
        }
    }
    std::cout << "\nDone.\n";
    return 0;
}
