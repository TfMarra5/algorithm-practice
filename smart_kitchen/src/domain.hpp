#pragma once
#include <string>
#include <vector>
#include <unordered_map>
#include <optional>
#include <algorithm>
#include <cmath>

struct Ingredient {
    std::string name;
    double quantity;      // in grams or ml (assume grams default)
    std::string unit;     // "g", "ml", "pcs"
    double kcal_per_unit; // kcal per unit (g/ml/pcs as defined in unit)

    double kcal() const {
        return quantity * kcal_per_unit;
    }
};

struct Step {
    std::string action;  // e.g., "mix", "preheat", "bake"
    double value;        // temperature/time etc. depending on action
    std::string unit;    // "C" for temp, "min" for time, etc.
    std::string note;
};

struct Recipe {
    std::string name;
    std::vector<Ingredient> ingredients;
    std::vector<Step> steps;
    int servings = 8;
    // area in cm^2 for pan scaling (e.g., 20cm round => pi r^2)
    double pan_area_cm2 = 314.0;
};

struct Pantry {
    // name -> available quantity (unitless same unit as recipe uses)
    std::unordered_map<std::string, double> stock;
    bool has(const std::string& n, double need) const {
        auto it = stock.find(n);
        return it != stock.end() && it->second + 1e-9 >= need;
    }
    double qty(const std::string& n) const {
        auto it = stock.find(n);
        return (it==stock.end()) ? 0.0 : it->second;
    }
};

struct Constraints {
    bool vegan = false;
    bool lactose_free = false;
    bool gluten_free = false;
    double max_kcal_per_serv = 0.0; // 0 means ignore
    std::vector<std::string> avoid; // keywords to avoid
    std::optional<int> target_servings;
    std::optional<double> target_pan_area_cm2;
};

inline double round1(double x){ return std::round(x*10.0)/10.0; }
