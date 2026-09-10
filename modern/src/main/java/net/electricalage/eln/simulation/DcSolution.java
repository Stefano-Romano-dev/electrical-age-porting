package net.electricalage.eln.simulation;

import java.util.Map;

public final class DcSolution {
    private final Map<CircuitNode, Double> nodeVoltages;
    private final Map<String, Double> sourceCurrents;

    DcSolution(Map<CircuitNode, Double> nodeVoltages, Map<String, Double> sourceCurrents) {
        this.nodeVoltages = Map.copyOf(nodeVoltages);
        this.sourceCurrents = Map.copyOf(sourceCurrents);
    }

    public double voltage(CircuitNode node) {
        if (CircuitNode.GROUND.equals(node)) {
            return 0.0;
        }
        Double voltage = nodeVoltages.get(node);
        if (voltage == null) {
            throw new IllegalArgumentException("Node is not part of this solution: " + node.id());
        }
        return voltage;
    }

    public double sourceCurrent(String sourceId) {
        Double current = sourceCurrents.get(sourceId);
        if (current == null) {
            throw new IllegalArgumentException("Unknown voltage source: " + sourceId);
        }
        return current;
    }

    public double resistorCurrent(Resistor resistor) {
        return resistor.current(voltage(resistor.positive()), voltage(resistor.negative()));
    }

    public double resistorPower(Resistor resistor) {
        return resistor.power(voltage(resistor.positive()), voltage(resistor.negative()));
    }
}
