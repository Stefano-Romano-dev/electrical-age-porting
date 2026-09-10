package net.electricalage.eln.simulation;

public record VoltageSource(String id, CircuitNode positive, CircuitNode negative, double voltage) {
    public VoltageSource {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("A voltage source id cannot be blank");
        }
        if (positive == null || negative == null) {
            throw new IllegalArgumentException("A voltage source must have two nodes");
        }
        if (!Double.isFinite(voltage)) {
            throw new IllegalArgumentException("Voltage must be finite");
        }
    }
}
