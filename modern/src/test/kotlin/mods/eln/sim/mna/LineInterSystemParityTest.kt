package mods.eln.sim.mna

import mods.eln.sim.mna.component.InterSystem
import mods.eln.sim.mna.component.InterSystemAbstraction
import mods.eln.sim.mna.component.Line
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.misc.MnaConst
import mods.eln.sim.mna.state.VoltageState
import mods.eln.sim.mna.state.VoltageStateLineReady
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val INTER_SYSTEM_EPSILON = 1e-6

class LineInterSystemParityTest {
    @Test
    fun `line aggregation preserves resistance and intermediate voltage`() {
        val root = RootSystem(0.1, 1)
        val a = VoltageState()
        val middle = VoltageStateLineReady().apply { setCanBeSimplifiedByLine(true) }
        val b = VoltageState()
        val first = Resistor(a, middle).setResistance(2.0)
        val second = Resistor(middle, b).setResistance(2.0)

        listOf(a, middle, b).forEach(root::addState)
        listOf(first, second).forEach(root::addComponent)
        root.generate()

        val line = root.systems.single().components.filterIsInstance<Line>().single()
        assertEquals(4.0, line.resistance, INTER_SYSTEM_EPSILON)
        assertTrue(first.isAbstracted)
        assertTrue(middle.isAbstracted)

        first.setResistance(4.0)
        assertEquals(6.0, line.resistance, INTER_SYSTEM_EPSILON)
        first.setResistance(2.0)

        a.state = 4.0
        b.state = 0.0
        line.simProcessFlush()
        assertEquals(2.0, middle.state, INTER_SYSTEM_EPSILON)
    }

    @Test
    fun `breaking a line subsystem restores its original topology`() {
        val root = RootSystem(0.1, 1)
        val a = VoltageState()
        val middle = VoltageStateLineReady().apply { setCanBeSimplifiedByLine(true) }
        val b = VoltageState()
        val first = Resistor(a, middle).setResistance(2.0)
        val second = Resistor(middle, b).setResistance(3.0)

        listOf(a, middle, b).forEach(root::addState)
        listOf(first, second).forEach(root::addComponent)
        root.generate()
        root.systems.single().breakSystem()

        assertFalse(first.isAbstracted)
        assertFalse(second.isAbstracted)
        assertFalse(middle.isAbstracted)
        assertTrue(first in root.pendingComponents)
        assertTrue(second in root.pendingComponents)
        assertTrue(middle in root.pendingStates)
    }

    @Test
    fun `inter-system marker survives line aggregation`() {
        val line = Line()
        line.addResistor(Resistor().setResistance(2.0))
        line.addResistor(InterSystem().setResistance(3.0))
        line.recalculateResistance()

        assertTrue(line.canBeReplacedByInterSystem())
        assertEquals(5.0, line.resistance, INTER_SYSTEM_EPSILON)
    }

    @Test
    fun `large replaceable network is partitioned and bridged`() {
        val root = RootSystem(0.1, 1)
        val states = Array(105) { VoltageState() }
        val bridges = (0 until states.lastIndex).map { index ->
            InterSystem(states[index], states[index + 1]).setResistance(1.0)
        }
        states.forEach(root::addState)
        bridges.forEach(root::addComponent)

        root.generate()

        assertTrue(root.subSystemCount > 1)
        assertTrue(bridges.any { it.isAbstracted })
        assertTrue(root.pendingComponents.isEmpty())
    }

    @Test
    fun `legacy two-network example converges to matching voltages`() {
        val root = RootSystem(0.1, 1)
        val n1 = VoltageState()
        val n2 = VoltageState()
        val n11 = VoltageState()
        val n12 = VoltageState()

        val components = listOf(
            VoltageSource("u1", n1, null).setVoltage(1.0),
            Resistor(n1, n2).setResistance(10.0),
            Resistor(n2, null).setResistance(20.0),
            VoltageSource("u11", n11, null).setVoltage(1.0),
            Resistor(n11, n12).setResistance(10.0),
            Resistor(n12, null).setResistance(30.0),
            InterSystem(n2, n12).setResistance(10.0),
        )
        listOf(n1, n2, n11, n12).forEach(root::addState)
        components.forEach(root::addComponent)

        repeat(50) { root.step() }
        assertEquals(0.6896551724, n2.state, INTER_SYSTEM_EPSILON)
        assertEquals(0.7241379310, n12.state, INTER_SYSTEM_EPSILON)

        root.addComponent(Resistor(n12, null).setResistance(30.0))
        repeat(50) { root.step() }
        assertEquals(0.6470588235, n2.state, INTER_SYSTEM_EPSILON)
        assertEquals(0.6176470588, n12.state, INTER_SYSTEM_EPSILON)
    }

    @Test
    fun `inter-system abstraction recalibrates and is destroyed with its systems`() {
        val root = RootSystem(0.1, 1)
        val a = VoltageState().apply { state = 6.0 }
        val b = VoltageState().apply { state = 2.0 }
        a.setAsPrivate()
        val bridge = Resistor(a, b).setResistance(4.0)
        listOf(a, b).forEach(root::addState)
        root.addComponent(bridge)
        root.generate()

        val abstraction = bridge.abstractedBy as InterSystemAbstraction
        assertEquals(2.0, abstraction.aNewResistor.resistance, INTER_SYSTEM_EPSILON)
        bridge.setResistance(8.0)
        assertEquals(4.0, abstraction.aNewResistor.resistance, INTER_SYSTEM_EPSILON)

        root.breakSystems(root.findSubSystemWith(a)!!)
        assertFalse(bridge.isAbstracted)
        assertNull(bridge.subSystem)
        assertEquals(0, root.subSystemCount)
    }

    @Test
    fun `thevenin fallback preserves the legacy high impedance result`() {
        val subSystem = SubSystem(dt = 0.1)
        val node = VoltageState()
        val source = VoltageSource("test", node, null)
        subSystem.addState(node)
        subSystem.addComponent(source)

        val equivalent = subSystem.getTh(node, source)
        assertEquals(1e19, equivalent.resistance)
        assertEquals(0.0, equivalent.voltage)
        assertTrue(SubSystem.Thevenin(MnaConst.HIGH_IMPEDANCE, 0.0).isHighImpedance)
    }
}
