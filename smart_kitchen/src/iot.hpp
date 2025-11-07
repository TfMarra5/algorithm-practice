#pragma once
#include <string>
#include <iostream>
#include <random>

// Minimal IoT mock interfaces
struct Oven {
    double targetC = 0.0;
    double sensor_offsetC = 0.0; // +5 means reads 5C hotter than real
    void preheat(double C){
        targetC = C;
        std::cout << "[Oven] Preheating to " << C << " C (sensor offset " << sensor_offsetC << " C)\n";
    }
    bool is_ready() const {
        return true; // mock
    }
};

struct Scale {
    double measure(const std::string& what){
        std::mt19937 rng(123);
        std::uniform_real_distribution<> d(95.0, 105.0);
        double pct = d(rng) / 100.0;
        std::cout << "[Scale] Measuring " << what << ": " << pct*100 << "% of target\n";
        return pct;
    }
};

struct Thermometer {
    double actual_oven_temp(double setpoint, double offset){
        // simulate an oven that is off by -offset (so if offset +10C, actual is -10)
        return setpoint - offset;
    }
};
