package net.electricalage.eln.simulation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small DC modified-nodal-analysis solver used to establish a loader-independent
 * simulation boundary. Dynamic components from the original solver will be
 * introduced after this vertical slice is integrated with the world.
 */
public final class DcCircuit {
    private static final double PIVOT_EPSILON = 1.0e-12;

    private final Map<String, CircuitNode> nodes = new LinkedHashMap<>();
    private final List<Resistor> resistors = new ArrayList<>();
    private final List<VoltageSource> voltageSources = new ArrayList<>();

    public CircuitNode node(String id) {
        if (CircuitNode.GROUND.id().equals(id)) {
            return CircuitNode.GROUND;
        }
        return nodes.computeIfAbsent(id, CircuitNode::new);
    }

    public Resistor addResistor(String id, CircuitNode positive, CircuitNode negative, double resistanceOhms) {
        ensureUniqueComponentId(id);
        Resistor resistor = new Resistor(id, positive, negative, resistanceOhms);
        resistors.add(resistor);
        return resistor;
    }

    public VoltageSource addVoltageSource(
            String id, CircuitNode positive, CircuitNode negative, double voltage
    ) {
        ensureUniqueComponentId(id);
        VoltageSource source = new VoltageSource(id, positive, negative, voltage);
        voltageSources.add(source);
        return source;
    }

    public DcSolution solve() {
        int nodeCount = nodes.size();
        int size = nodeCount + voltageSources.size();
        if (size == 0) {
            return new DcSolution(Map.of(), Map.of());
        }

        Map<CircuitNode, Integer> nodeIndexes = new LinkedHashMap<>();
        int index = 0;
        for (CircuitNode node : nodes.values()) {
            nodeIndexes.put(node, index++);
        }

        double[][] matrix = new double[size][size];
        double[] rightHandSide = new double[size];

        for (Resistor resistor : resistors) {
            double conductance = 1.0 / resistor.resistanceOhms();
            stampConductance(matrix, nodeIndexes, resistor.positive(), resistor.negative(), conductance);
        }

        for (int sourceIndex = 0; sourceIndex < voltageSources.size(); sourceIndex++) {
            VoltageSource source = voltageSources.get(sourceIndex);
            int equation = nodeCount + sourceIndex;
            stampVoltageSource(matrix, rightHandSide, nodeIndexes, source, equation);
        }

        double[] result = solveLinearSystem(matrix, rightHandSide);
        Map<CircuitNode, Double> voltages = new LinkedHashMap<>();
        nodeIndexes.forEach((node, nodeIndex) -> voltages.put(node, result[nodeIndex]));

        Map<String, Double> currents = new LinkedHashMap<>();
        for (int sourceIndex = 0; sourceIndex < voltageSources.size(); sourceIndex++) {
            currents.put(voltageSources.get(sourceIndex).id(), result[nodeCount + sourceIndex]);
        }
        return new DcSolution(voltages, currents);
    }

    private void ensureUniqueComponentId(String id) {
        boolean resistorExists = resistors.stream().anyMatch(component -> component.id().equals(id));
        boolean sourceExists = voltageSources.stream().anyMatch(component -> component.id().equals(id));
        if (resistorExists || sourceExists) {
            throw new IllegalArgumentException("Duplicate circuit component id: " + id);
        }
    }

    private static void stampConductance(
            double[][] matrix,
            Map<CircuitNode, Integer> indexes,
            CircuitNode positive,
            CircuitNode negative,
            double conductance
    ) {
        Integer positiveIndex = indexes.get(positive);
        Integer negativeIndex = indexes.get(negative);
        if (positiveIndex != null) {
            matrix[positiveIndex][positiveIndex] += conductance;
        }
        if (negativeIndex != null) {
            matrix[negativeIndex][negativeIndex] += conductance;
        }
        if (positiveIndex != null && negativeIndex != null) {
            matrix[positiveIndex][negativeIndex] -= conductance;
            matrix[negativeIndex][positiveIndex] -= conductance;
        }
    }

    private static void stampVoltageSource(
            double[][] matrix,
            double[] rightHandSide,
            Map<CircuitNode, Integer> indexes,
            VoltageSource source,
            int equation
    ) {
        Integer positiveIndex = indexes.get(source.positive());
        Integer negativeIndex = indexes.get(source.negative());
        if (positiveIndex != null) {
            matrix[positiveIndex][equation] += 1.0;
            matrix[equation][positiveIndex] += 1.0;
        }
        if (negativeIndex != null) {
            matrix[negativeIndex][equation] -= 1.0;
            matrix[equation][negativeIndex] -= 1.0;
        }
        rightHandSide[equation] = source.voltage();
    }

    private static double[] solveLinearSystem(double[][] coefficients, double[] values) {
        int size = values.length;
        double[][] augmented = new double[size][size + 1];
        for (int row = 0; row < size; row++) {
            System.arraycopy(coefficients[row], 0, augmented[row], 0, size);
            augmented[row][size] = values[row];
        }

        for (int pivot = 0; pivot < size; pivot++) {
            int bestRow = pivot;
            for (int row = pivot + 1; row < size; row++) {
                if (Math.abs(augmented[row][pivot]) > Math.abs(augmented[bestRow][pivot])) {
                    bestRow = row;
                }
            }
            if (Math.abs(augmented[bestRow][pivot]) < PIVOT_EPSILON) {
                throw new IllegalStateException("Circuit has no unique DC solution");
            }

            double[] temporary = augmented[pivot];
            augmented[pivot] = augmented[bestRow];
            augmented[bestRow] = temporary;

            for (int row = pivot + 1; row < size; row++) {
                double factor = augmented[row][pivot] / augmented[pivot][pivot];
                for (int column = pivot; column <= size; column++) {
                    augmented[row][column] -= factor * augmented[pivot][column];
                }
            }
        }

        double[] result = new double[size];
        for (int row = size - 1; row >= 0; row--) {
            double value = augmented[row][size];
            for (int column = row + 1; column < size; column++) {
                value -= augmented[row][column] * result[column];
            }
            result[row] = value / augmented[row][row];
        }
        return result;
    }
}
