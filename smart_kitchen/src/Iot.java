import java.util.*;

// ----------------------------------------
// Oven
// ----------------------------------------
class Oven {
    public double targetC = 0.0;
    public double sensor_offsetC = 0.0;

    public void preheat(double C) {
        targetC = C;
        System.out.println("[Oven] Preheating to " + C + " C (sensor offset " + sensor_offsetC + " C)");
    }

    public boolean is_ready() {
        return true;
    }
}

// ----------------------------------------
// Scale
// ----------------------------------------
class Scale {

    public double measure(String what) {
        Random rng = new Random(123);
        double pct = 95.0 + (rng.nextDouble() * 10.0);
        pct /= 100.0;

        System.out.println("[Scale] Measuring " + what + ": " + (pct * 100) + "% of target");
        return pct;
    }
}

// ----------------------------------------
// Thermometer
// ----------------------------------------
class Thermometer {

    public double actual_oven_temp(double setpoint, double offset) {
        return setpoint - offset;
    }
}