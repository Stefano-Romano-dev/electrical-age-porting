package mods.eln.sim.mna

import mods.eln.sim.mna.component.Capacitor
import mods.eln.sim.mna.component.Delay
import mods.eln.sim.mna.component.Inductor
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.state.VoltageState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val DYNAMIC_EPSILON = 1e-9

class DynamicComponentParityTest {
    @Test
    fun `capacitor preserves legacy energy and zero reported current`() {
        val a = VoltageState().apply { state = 3.0 }
        val b = VoltageState()
        val capacitor = Capacitor(a, b).setCoulombs(2.0)

        assertEquals(9.0, capacitor.energy, DYNAMIC_EPSILON)
        assertEquals(0.0, capacitor.current, DYNAMIC_EPSILON)
    }

    @Test
    fun `rc charge follows legacy backward Euler samples`() {
        val root = RootSystem(dt = 0.1, interSystemOverSampling = 1)
        val supply = VoltageState()
        val output = VoltageState()
        val source = VoltageSource("source", supply, null).setVoltage(1.0)
        val resistor = Resistor(supply, output).setResistance(10.0)
        val capacitor = Capacitor(output, null).setCoulombs(0.01)

        listOf(supply, output).forEach(root::addState)
        listOf(source, resistor, capacitor).forEach(root::addComponent)

        val expected = listOf(0.5, 0.75, 0.875, 0.9375)
        expected.forEach { voltage ->
            root.step()
            assertEquals(voltage, output.state, DYNAMIC_EPSILON)
        }
    }

    @Test
    fun `changing capacitance invalidates the matrix and changes the next sample`() {
        val root = RootSystem(dt = 0.1, interSystemOverSampling = 1)
        val supply = VoltageState()
        val output = VoltageState()
        val capacitor = Capacitor(output, null).setCoulombs(0.01)
        listOf(supply, output).forEach(root::addState)
        listOf(
            VoltageSource("source", supply, null).setVoltage(1.0),
            Resistor(supply, output).setResistance(10.0),
            capacitor,
        ).forEach(root::addComponent)

        root.step()
        assertEquals(0.5, output.state, DYNAMIC_EPSILON)
        capacitor.setCoulombs(0.02)
        root.step()
        assertEquals(2.0 / 3.0, output.state, DYNAMIC_EPSILON)
    }

    @Test
    fun `inductor preserves energy and reset semantics`() {
        val inductor = Inductor("L1").setInductance(2.0)
        inductor.currentState.state = 3.0
        assertEquals(9.0, inductor.energy, DYNAMIC_EPSILON)

        inductor.resetStates()
        assertEquals(0.0, inductor.current, DYNAMIC_EPSILON)
    }

    @Test
    fun `rl current follows legacy backward Euler samples`() {
        val root = RootSystem(dt = 0.1, interSystemOverSampling = 1)
        val supply = VoltageState()
        val output = VoltageState()
        val inductor = Inductor("L1", output, null).setInductance(1.0)
        listOf(supply, output).forEach(root::addState)
        listOf(
            VoltageSource("source", supply, null).setVoltage(1.0),
            Resistor(supply, output).setResistance(10.0),
            inductor,
        ).forEach(root::addComponent)

        listOf(0.05, 0.075, 0.0875, 0.09375).forEach { current ->
            root.step()
            assertEquals(current, inductor.current, DYNAMIC_EPSILON)
        }
    }

    @Test
    fun `inductor removes its current state and process on subsystem break`() {
        val root = RootSystem(dt = 0.1, interSystemOverSampling = 1)
        val node = VoltageState()
        val inductor = Inductor("L1", node, null).setInductance(1.0)
        root.addState(node)
        root.addComponent(inductor)
        root.generate()
        val subSystem = root.systems.single()

        subSystem.breakSystem()

        assertFalse(inductor.currentState in subSystem.states)
        assertTrue(inductor in root.pendingComponents)
    }

    @Test
    fun `delay preserves the original first and second process samples`() {
        val a = VoltageState().apply { state = 2.0 }
        val b = VoltageState()
        val delay = Delay().setImpedance(2.0)
        delay.connectTo(a, b)
        val subSystem = SubSystem(dt = 0.1)
        subSystem.addState(a)
        subSystem.addState(b)
        subSystem.addComponent(delay)
        assertFalse(subSystem.isSingular)

        delay.simProcessI(subSystem)
        assertEquals(1.0, delay.current, DYNAMIC_EPSILON)
        delay.simProcessI(subSystem)
        assertEquals(2.0, delay.current, DYNAMIC_EPSILON)
    }
}
