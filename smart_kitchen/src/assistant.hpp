#pragma once
#include "domain.hpp"
#include <set>
#include <iostream>

// Simple substitution rules (expand as needed)
struct SubRule {
    std::string from;
    std::string to;
    double ratio; // how much 'to' per 1 unit of 'from' quantity (g->g)
    std::string reason;
};

struct AssistantResult {
    Recipe adjusted;
    std::vector<std::string> notes;
    std::vector<std::string> warnings;
};

struct Nutrition {
    static double total_kcal(const Recipe& r){
        double sum=0.0; for(const auto& ing: r.ingredients) sum+=ing.kcal(); return sum;
    }
    static double kcal_per_serv(const Recipe& r){
        return (r.servings>0) ? total_kcal(r)/r.servings : total_kcal(r);
    }
};

struct Assistant {
    std::vector<SubRule> rules;

    Assistant(){
        // common substitutions
        rules.push_back({"egg", "flaxseed_meal+water", 1.0, "Vegan egg: 1 egg ≈ 10g flax + 30ml water"});
        rules.push_back({"milk", "oat_milk", 1.0, "Lactose-free swap"});
        rules.push_back({"butter", "vegetable_oil", 0.8, "80% oil for butter by weight"});
        rules.push_back({"wheat_flour", "gluten_free_mix", 1.1, "GF mix needs ~10% more"});
        rules.push_back({"yogurt", "soy_yogurt", 1.0, "Dairy-free yogurt"});
        rules.push_back({"cream", "coconut_cream", 1.0, "Dairy-free cream"});
        rules.push_back({"sugar", "brown_sugar", 1.0, "Interchangeable in most cakes"});
    }

    static void scale_for_servings(Recipe& r, int target){
        if (target<=0 || r.servings<=0) return;
        double s = double(target)/double(r.servings);
        for(auto& ing: r.ingredients) ing.quantity = round1(ing.quantity * s);
        for(auto& st: r.steps){
            if (st.action=="bake" && st.unit=="min"){
                st.value = round1(st.value * std::pow(s, 0.15)); // mild time scaling
            }
        }
        r.servings = target;
    }

    static void scale_for_pan_area(Recipe& r, double target_area){
        if (target_area<=0 || r.pan_area_cm2<=0) return;
        double s = target_area / r.pan_area_cm2;
        for(auto& ing: r.ingredients) ing.quantity = round1(ing.quantity * s);
        r.pan_area_cm2 = target_area;
        for(auto& st: r.steps){
            if (st.action=="bake" && st.unit=="min"){
                st.value = round1(st.value * std::pow(s, 0.25)); // thickness vs time
            }
        }
    }

    void apply_diet(Recipe& r, const Constraints& c, AssistantResult& ar){
        for(auto& ing: r.ingredients){
            std::string n = ing.name;
            // basic diet checks
            if (c.vegan && (n=="egg" || n=="butter" || n=="milk" || n=="cream" || n=="yogurt")){
                auto sub = find_sub(n);
                if (sub){
                    ar.notes.push_back("Vegan: swapped "+n+" -> "+sub->to+" ("+sub->reason+")");
                    ing.name = sub->to; // simplistic: keep quantity similar (ratio later)
                }else{
                    ar.warnings.push_back("No vegan substitute found for "+n);
                }
            }
            if (c.lactose_free && (n=="milk" || n=="butter" || n=="cream" || n=="yogurt")){
                auto sub = find_sub(n);
                if (sub){
                    ar.notes.push_back("Lactose-free: swapped "+n+" -> "+sub->to+" ("+sub->reason+")");
                    ing.name = sub->to;
                }else{
                    ar.warnings.push_back("No lactose-free substitute for "+n);
                }
            }
            if (c.gluten_free && (n=="wheat_flour")){
                auto sub = find_sub(n);
                if (sub){
                    ar.notes.push_back("Gluten-free: swapped wheat_flour -> "+sub->to+" ("+sub->reason+")");
                    ing.name = sub->to;
                }else{
                    ar.warnings.push_back("No gluten-free substitute for wheat_flour");
                }
            }
            for(const auto& bad: c.avoid){
                if (n.find(bad)!=std::string::npos){
                    ar.warnings.push_back("Contains avoided ingredient: "+n);
                }
            }
        }
        // apply ratios after renaming
        for(auto& ing: r.ingredients){
            auto sub = find_sub_original(ing.name);
            if (sub){
                // already renamed, set quantities appropriately (simple policies)
                if (sub->from=="butter" && ing.name=="vegetable_oil"){
                    ing.quantity = round1(ing.quantity * sub->ratio);
                    ing.unit = "ml";
                }else if (sub->from=="wheat_flour" && ing.name=="gluten_free_mix"){
                    ing.quantity = round1(ing.quantity * sub->ratio);
                }
            }
        }
    }

    void apply_pantry(Recipe& r, const Pantry& p, AssistantResult& ar){
        // If missing, see if substitution can rescue; else scale recipe down by limiting ingredient
        // First pass: track limiting ratio
        double limit_ratio = 1.0;
        for(const auto& ing: r.ingredients){
            double need = ing.quantity;
            double have = p.qty(ing.name);
            if (have<=0.0){
                // try substitution with direct mapped stock e.g., milk->oat_milk if pantry has oat_milk
                auto back = find_sub(ing.name);
                if (back && p.has(back->to, need*back->ratio)){
                    ar.notes.push_back("Pantry: using "+back->to+" instead of "+ing.name);
                }else{
                    ar.warnings.push_back("Missing ingredient: "+ing.name+" ("+std::to_string(need)+ing.unit+")");
                    // limit_ratio potentially zero (can't make)
                    limit_ratio = std::min(limit_ratio, 0.0);
                }
            }else{
                limit_ratio = std::min(limit_ratio, have / need);
            }
        }
        if (limit_ratio < 0.999){
            ar.notes.push_back("Scaling recipe by pantry limit ratio = "+std::to_string(round1(limit_ratio)));
            for(auto& ing: r.ingredients) ing.quantity = round1(ing.quantity * limit_ratio);
            // baking time slight adjust
            for(auto& st: r.steps){
                if (st.action=="bake" && st.unit=="min"){
                    st.value = round1(st.value * std::pow(limit_ratio, 0.15));
                }
            }
        }
    }

    void apply_kcal_limit(Recipe& r, const Constraints& c, AssistantResult& ar){
        if (c.max_kcal_per_serv <= 0.0) return;
        double kps = Nutrition::kcal_per_serv(r);
        if (kps <= c.max_kcal_per_serv) return;
        // Reduce sugar & fat proportionally up to 30% to meet target
        double ratio = std::max(0.7, c.max_kcal_per_serv / kps);
        for(auto& ing: r.ingredients){
            if (ing.name=="sugar" || ing.name=="brown_sugar" || ing.name=="butter" || ing.name=="vegetable_oil"){
                ing.quantity = round1(ing.quantity * ratio);
            }
        }
        ar.notes.push_back("Adjusted sugar/fat to meet kcal/serv target ("+std::to_string(c.max_kcal_per_serv)+")");
    }

    void adjust_steps_for_oven_offset(Recipe& r, double oven_offsetC, AssistantResult& ar){
        // If oven runs cold, increase temp; or extend bake time
        for(auto& st: r.steps){
            if (st.action=="preheat" && st.unit=="C"){
                st.value = round1(st.value + oven_offsetC);
                if (std::abs(oven_offsetC)>0.1){
                    ar.notes.push_back("Adjusted preheat by "+std::to_string(oven_offsetC)+"C due to oven sensor.");
                }
            }
            if (st.action=="bake" && st.unit=="min" && oven_offsetC < -2.0){
                st.value = round1(st.value * 1.05); // small extension if cold
            }
        }
    }

    AssistantResult plan(const Recipe& base, const Pantry& pantry, const Constraints& c, double oven_offsetC){
        AssistantResult ar; ar.adjusted = base;
        // scale by servings/pan first
        if (c.target_servings) scale_for_servings(ar.adjusted, *c.target_servings);
        if (c.target_pan_area_cm2) scale_for_pan_area(ar.adjusted, *c.target_pan_area_cm2);
        // diet-driven swaps
        apply_diet(ar.adjusted, c, ar);
        // pantry impact
        apply_pantry(ar.adjusted, pantry, ar);
        // kcal limit
        apply_kcal_limit(ar.adjusted, c, ar);
        // oven offset
        adjust_steps_for_oven_offset(ar.adjusted, oven_offsetC, ar);
        return ar;
    }

private:
    std::optional<SubRule> find_sub(const std::string& name) const{
        for(const auto& r: rules) if (r.from==name) return r;
        return std::nullopt;
    }
    std::optional<SubRule> find_sub_original(const std::string& newname) const{
        for(const auto& r: rules) if (r.to==newname) return r;
        return std::nullopt;
    }
};
