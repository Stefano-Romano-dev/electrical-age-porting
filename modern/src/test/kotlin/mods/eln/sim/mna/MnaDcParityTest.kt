package mods.eln.sim.mna

import mods.eln.sim.mna.component.CurrentSource
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.state.VoltageState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val EPSILON = 1e-9

class MnaDcParityTest {
    @Test
    fun `current source and resistor match the 1_24_8 example`() {
        val node = VoltageState()
        val source = CurrentSource("source", node, null).setCurrent(0.01)
        val resistor = Resistor(node, null).setResistance(10.0)
        val system = SubSystem(dt = 0.1)
            .addState(node)
            .addComponent(source)
            .addComponent(resistor)

        system.step()

        assertFalse(system.isSingular)
        assertEquals(0.1, node.voltage, EPSILON)
        assertEquals(0.1, resistor.voltage, EPSILON)
        assertEquals(0.01, resistor.current, EPSILON)
        assertEquals(0.1, source.voltage, EPSILON)
    }

    @Test
    fun `voltage source drives the original resistor example`() {
        val node = VoltageState()
        val source = VoltageSource("source", node, null).setVoltage(1.0)
        val resistor = Resistor(node, null).setResistance(10.0)
        val system = SubSystem(dt = 0.05)
            .addState(node)
            .addComponent(source)
            .addComponent(resistor)

        system.step()

        assertEquals(1.0, node.voltage, EPSILON)
        assertEquals(0.1, resistor.current, EPSILON)
        assertEquals(0.1, source.current, EPSILON)
        assertEquals(0.1, source.power, EPSILON)
    }

    @Test
    fun `voltage divider solves a two-node circuit`() {
        val input = VoltageState()
        val output = VoltageState()
        val source = VoltageSource("source", input, null).setVoltage(12.0)
        val upper = Resistor(input, output).setResistance(100.0)
        val lower = Resistor(output, null).setResistance(200.0)
        val system = SubSystem(dt = 0.05)
            .addState(input)
            .addState(output)
            .addComponent(source)
            .addComponent(upper)
            .addComponent(lower)

        system.step()

        assertEquals(12.0, input.voltage, EPSILON)
        assertEquals(8.0, output.voltage, EPSILON)
        assertEquals(0.04, upper.current, EPSILON)
        assertEquals(0.04, lower.current, EPSILON)
        assertEquals(3, system.componentCount)
        assertEquals(3, system.stateCount)
    }

    @Test
    fun `singular systems are detected and flushed to zero`() {
        val floatingNode = VoltageState().apply { voltage = 42.0 }
        val system = SubSystem(dt = 0.05).addState(floatingNode)

        system.step()

        assertTrue(system.isSingular)
        assertEquals(0.0, floatingNode.voltage, EPSILON)
    }

    @Test
    fun `resistance changes rebuild the matrix while source changes update the rhs`() {
        val node = VoltageState()
        val source = CurrentSource("source", node, null).setCurrent(0.01)
        val resistor = Resistor(node, null).setResistance(10.0)
        val system = SubSystem(dt = 0.05)
            .addState(node)
            .addComponent(source)
            .addComponent(resistor)

        system.step()
        assertEquals(0.1, node.voltage, EPSILON)

        resistor.setResistance(20.0)
        system.step()
        assertEquals(0.2, node.voltage, EPSILON)

        source.setCurrent(0.02)
        system.step()
        assertEquals(0.4, node.voltage, EPSILON)
    }
}
