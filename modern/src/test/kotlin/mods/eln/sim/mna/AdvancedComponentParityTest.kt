package mods.eln.sim.mna

import kotlin.math.sqrt
import mods.eln.sim.mna.component.PowerSource
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.component.ResistorSwitch
import mods.eln.sim.mna.component.Transformer
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.process.PowerSourceBipole
import mods.eln.sim.mna.process.TransformerInterSystemProcess
import mods.eln.sim.mna.state.VoltageState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val ADVANCED_EPSILON = 1e-8

class AdvancedComponentParityTest {
    @Test
    fun `resistor switch preserves base off and high impedance semantics`() {
        val switch = ResistorSwitch("switch")
        switch.setOffResistance(150.0)
        switch.setResistance(25.0)
        assertEquals(150.0, switch.resistance, ADVANCED_EPSILON)

        switch.setState(true)
        assertEquals(25.0, switch.resistance, ADVANCED_EPSILON)
        switch.highImpedance()
        assertTrue(switch.state)
        assertEquals(150.0, switch.resistance, ADVANCED_EPSILON)

        switch.restoreState(baseResistance = 0.0, state = true)
        assertEquals(150.0, switch.baseResistance, ADVANCED_EPSILON)
        assertEquals(150.0, switch.resistance, ADVANCED_EPSILON)
    }

    @Test
    fun `transformer enforces the configured voltage ratio`() {
        val root = RootSystem(0.1, 1)
        val primary = VoltageState()
        val secondary = VoltageState()
        val transformer = Transformer(primary, secondary).setRatio(2.0)
        listOf(primary, secondary).forEach(root::addState)
        listOf(
            VoltageSource("source", primary, null).setVoltage(5.0),
            transformer,
            Resistor(secondary, null).setResistance(10.0),
        ).forEach(root::addComponent)

        root.step()

        assertEquals(5.0, primary.state, ADVANCED_EPSILON)
        assertEquals(10.0, secondary.state, ADVANCED_EPSILON)
        assertEquals(0.0, transformer.current, ADVANCED_EPSILON)
        assertEquals(5, root.systems.single().stateCount)
    }

    @Test
    fun `transformer ratio change keeps the legacy matrix invalidation behavior`() {
        val root = RootSystem(0.1, 1)
        val primary = VoltageState()
        val secondary = VoltageState()
        val transformer = Transformer(primary, secondary)
        listOf(primary, secondary).forEach(root::addState)
        listOf(
            VoltageSource("source", primary, null).setVoltage(5.0),
            transformer,
            Resistor(secondary, null).setResistance(10.0),
        ).forEach(root::addComponent)

        root.step()
        assertEquals(5.0, secondary.state, ADVANCED_EPSILON)
        transformer.setRatio(2.0)
        root.step()
        assertEquals(5.0, secondary.state, ADVANCED_EPSILON)

        root.systems.single().invalidate()
        root.step()
        assertEquals(10.0, secondary.state, ADVANCED_EPSILON)
    }

    @Test
    fun `power source delivers requested power into a resistive load`() {
        val root = RootSystem(0.1, 1)
        val node = VoltageState()
        val source = PowerSource("power", node)
        source.setPower(5.0)
        source.setMaximums(100.0, 100.0)
        root.addState(node)
        root.addComponent(Resistor(node, null).setResistance(10.0))
        root.addComponent(source)

        root.step()

        assertEquals(sqrt(50.0), node.state, ADVANCED_EPSILON)
        assertEquals(5.0, source.effectivePower, ADVANCED_EPSILON)
    }

    @Test
    fun `power source applies voltage and current limits in legacy order`() {
        val root = RootSystem(0.1, 1)
        val node = VoltageState()
        val source = PowerSource("power", node)
        source.setPower(1_000.0)
        source.setMaximums(100.0, 0.2)
        root.addState(node)
        root.addComponent(Resistor(node, null).setResistance(10.0))
        root.addComponent(source)

        root.step()
        assertEquals(2.0, node.state, ADVANCED_EPSILON)

        source.setMaximums(1.0, 100.0)
        root.step()
        assertEquals(1.0, node.state, ADVANCED_EPSILON)
    }

    @Test
    fun `power source keeps legacy NaN power fallback`() {
        val root = RootSystem(0.1, 1)
        val node = VoltageState()
        val source = PowerSource("power", node)
        source.setPower(Double.NaN)
        source.setMaximums(100.0, 10.0)
        root.addState(node)
        root.addComponent(Resistor(node, null).setResistance(10.0))
        root.addComponent(source)

        root.step()

        assertEquals(0.0, source.voltage, ADVANCED_EPSILON)
        assertEquals(0.0, node.state, ADVANCED_EPSILON)
    }

    @Test
    fun `transformer inter-system process matches the legacy Thevenin formula`() {
        val aState = VoltageState()
        val bState = VoltageState()
        val aSource = VoltageSource("a", aState, null)
        val bSource = VoltageSource("b", bState, null)
        val aSystem = SubSystem(dt = 0.1)
        val bSystem = SubSystem(dt = 0.1)
        aSystem.addState(aState).addComponent(Resistor(aState, null).setResistance(10.0)).addComponent(aSource)
        bSystem.addState(bState).addComponent(Resistor(bState, null).setResistance(20.0)).addComponent(bSource)

        val process = TransformerInterSystemProcess(aState, bState, aSource, bSource)
        process.setRatio(2.0)
        val a = aSystem.getTh(aState, aSource)
        val b = bSystem.getTh(bState, bSource)
        val expected = (a.voltage * b.resistance + process.ratio * b.voltage * a.resistance) /
            (b.resistance + process.ratio * process.ratio * a.resistance)

        process.rootSystemPreStepProcess()

        assertEquals(expected, aSource.voltage, ADVANCED_EPSILON)
        assertEquals(expected * 2.0, bSource.voltage, ADVANCED_EPSILON)
    }

    @Test
    fun `bipole power source preserves maximum voltage branch`() {
        val aState = VoltageState()
        val bState = VoltageState()
        val aSource = VoltageSource("a", aState, null)
        val bSource = VoltageSource("b", bState, null)
        val aSystem = SubSystem(dt = 0.1)
        val bSystem = SubSystem(dt = 0.1)
        aSystem.addState(aState).addComponent(Resistor(aState, null).setResistance(10.0)).addComponent(aSource)
        bSystem.addState(bState).addComponent(Resistor(bState, null).setResistance(20.0)).addComponent(bSource)
        val process = PowerSourceBipole(aState, bState, aSource, bSource)
        process.setPower(5.0)
        process.setMaximums(0.0, 100.0)
        val a = aSystem.getTh(aState, aSource)
        val b = bSystem.getTh(bState, bSource)

        process.rootSystemPreStepProcess()

        assertEquals(a.voltage, aSource.voltage, ADVANCED_EPSILON)
        assertEquals(b.voltage, bSource.voltage, ADVANCED_EPSILON)
    }

    @Test
    fun `bipole power source keeps legacy NaN fallback`() {
        val aState = VoltageState().apply { state = Double.NaN }
        val bState = VoltageState().apply { state = Double.NaN }
        val aSource = VoltageSource("a", aState, null)
        val bSource = VoltageSource("b", bState, null)
        val aSystem = SubSystem(dt = 0.1)
        val bSystem = SubSystem(dt = 0.1)
        aSystem.addState(aState).addComponent(aSource)
        bSystem.addState(bState).addComponent(bSource)
        val process = PowerSourceBipole(aState, bState, aSource, bSource)
        process.setPower(1.0)
        process.setMaximums(10.0, 10.0)

        process.rootSystemPreStepProcess()

        assertEquals(5.0, aSource.voltage, ADVANCED_EPSILON)
        assertEquals(-5.0, bSource.voltage, ADVANCED_EPSILON)
    }

    @Test
    fun `removing power source unregisters its root process`() {
        val root = RootSystem(0.1, 1)
        val node = VoltageState()
        val source = PowerSource("power", node)
        source.setPower(5.0)
        source.setMaximums(100.0, 100.0)
        root.addState(node)
        root.addComponent(Resistor(node, null).setResistance(10.0))
        root.addComponent(source)
        root.step()

        root.removeComponent(source)
        root.removeState(node)
        root.step()

        assertFalse(source in root.pendingComponents)
        assertEquals(0, root.subSystemCount)
    }
}
