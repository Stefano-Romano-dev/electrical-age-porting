package net.electricalage.eln.simulation;

/**
 * A two-terminal resistor. Its current and power equations are retained from
 * the original Electrical Age MNA component.
 */
public record Resistor(String id, CircuitNode positive, CircuitNode negative, double resistanceOhms) {
    public Resistor {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("A resistor id cannot be blank");
        }
        if (positive == null || negative == null) {
            throw new IllegalArgumentException("A resistor must have two nodes");
        }
        if (!Double.isFinite(resistanceOhms) || resistanceOhms <= 0.0) {
            throw new IllegalArgumentException("Resistance must be finite and greater than zero");
        }
    }

    public double current(double positiveVoltage, double negativeVoltage) {
        return (positiveVoltage - negativeVoltage) / resistanceOhms;
    }

    public double power(double positiveVoltage, double negativeVoltage) {
        double voltage = positiveVoltage - negativeVoltage;
        return voltage * current(positiveVoltage, negativeVoltage);
    }
}
