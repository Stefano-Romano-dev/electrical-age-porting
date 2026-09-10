package net.electricalage.eln.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DcCircuitTest {
    private static final double TOLERANCE = 1.0e-9;

    @Test
    void solvesVoltageDividerAndReportsCurrentAndPower() {
        DcCircuit circuit = new DcCircuit();
        CircuitNode supply = circuit.node("supply");
        CircuitNode output = circuit.node("output");

        circuit.addVoltageSource("supply", supply, CircuitNode.GROUND, 12.0);
        Resistor upper = circuit.addResistor("upper", supply, output, 1_000.0);
        Resistor lower = circuit.addResistor("lower", output, CircuitNode.GROUND, 2_000.0);

        DcSolution solution = circuit.solve();

        assertEquals(12.0, solution.voltage(supply), TOLERANCE);
        assertEquals(8.0, solution.voltage(output), TOLERANCE);
        assertEquals(0.004, solution.resistorCurrent(upper), TOLERANCE);
        assertEquals(0.004, solution.resistorCurrent(lower), TOLERANCE);
        assertEquals(0.032, solution.resistorPower(lower), TOLERANCE);
        assertEquals(-0.004, solution.sourceCurrent("supply"), TOLERANCE);
    }

    @Test
    void rejectsFloatingCircuit() {
        DcCircuit circuit = new DcCircuit();
        CircuitNode first = circuit.node("first");
        CircuitNode second = circuit.node("second");
        circuit.addResistor("floating", first, second, 100.0);

        assertThrows(IllegalStateException.class, circuit::solve);
    }
}
