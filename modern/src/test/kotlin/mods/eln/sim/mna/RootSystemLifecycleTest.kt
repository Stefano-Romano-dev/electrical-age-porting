package mods.eln.sim.mna

import mods.eln.sim.mna.component.CurrentSource
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.misc.IRootSystemPreStepProcess
import mods.eln.sim.mna.misc.IDestructor
import mods.eln.sim.mna.misc.ISubSystemProcessFlush
import mods.eln.sim.mna.state.VoltageState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val ROOT_EPSILON = 1e-9

class RootSystemLifecycleTest {
    @Test
    fun `generate builds one subsystem and break restores pending topology`() {
        val root = RootSystem(dt = 0.1, interSystemOverSampling = 1)
        val a = VoltageState()
        val b = VoltageState()
        val resistor = Resistor(a, b).setResistance(5.0)

        root.addState(a)
        root.addState(b)
        root.addComponent(resistor)
        root.generate()

        assertEquals(1, root.subSystemCount)
        val subSystem = root.systems.single()
        assertTrue(a in subSystem.states)
        assertTrue(b in subSystem.states)
        assertTrue(resistor in subSystem.components)
        assertTrue(root.pendingStates.isEmpty())
        assertTrue(root.pendingComponents.isEmpty())
        assertSame(subSystem, root.findSubSystemWith(a))

        assertTrue(subSystem.breakSystem())
        assertEquals(0, root.subSystemCount)
        assertTrue(a in root.pendingStates)
        assertTrue(b in root.pendingStates)
        assertTrue(resistor in root.pendingComponents)
        assertNull(a.subSystem)
        assertNull(b.subSystem)
        assertNull(resistor.subSystem)
        assertFalse(subSystem.breakSystem())
    }

    @Test
    fun `root step solves a connected voltage divider`() {
        val root = RootSystem(dt = 0.05, interSystemOverSampling = 1)
        val input = VoltageState()
        val output = VoltageState()
        val source = VoltageSource("source", input, null).setVoltage(12.0)
        val upper = Resistor(input, output).setResistance(100.0)
        val lower = Resistor(output, null).setResistance(200.0)

        listOf(input, output).forEach(root::addState)
        listOf(source, upper, lower).forEach(root::addComponent)
        root.step()

        assertEquals(1, root.subSystemCount)
        assertEquals(12.0, input.voltage, ROOT_EPSILON)
        assertEquals(8.0, output.voltage, ROOT_EPSILON)
        assertEquals(0.04, source.current, ROOT_EPSILON)
    }

    @Test
    fun `adding a component breaks and regenerates an existing subsystem`() {
        val root = RootSystem(dt = 0.05, interSystemOverSampling = 1)
        val node = VoltageState()
        val source = VoltageSource("source", node, null).setVoltage(10.0)
        val firstLoad = Resistor(node, null).setResistance(10.0)

        root.addState(node)
        root.addComponent(source)
        root.addComponent(firstLoad)
        root.step()
        assertEquals(1.0, source.current, ROOT_EPSILON)

        val secondLoad = Resistor(node, null).setResistance(10.0)
        root.addComponent(secondLoad)
        assertEquals(0, root.subSystemCount)
        assertTrue(node in root.pendingStates)

        root.step()
        assertEquals(1, root.subSystemCount)
        assertEquals(2.0, source.current, ROOT_EPSILON)
    }

    @Test
    fun `private state boundaries produce separate subsystems`() {
        val root = RootSystem(dt = 0.1, interSystemOverSampling = 1)
        val privateState = VoltageState()
        privateState.setAsPrivate()
        val publicState = VoltageState()
        val bridge = Resistor(privateState, publicState)

        root.addState(privateState)
        root.addState(publicState)
        root.addComponent(bridge)
        root.generate()

        assertEquals(2, root.subSystemCount)
        assertTrue(bridge.isAbstracted)
        assertTrue(root.pendingComponents.isEmpty())
    }

    @Test
    fun `pre-step oversampling and flush process follow the legacy lifecycle`() {
        val root = RootSystem(dt = 0.1, interSystemOverSampling = 3)
        var preCount = 0
        var flushCount = 0
        val pre = IRootSystemPreStepProcess { preCount++ }
        val flush = ISubSystemProcessFlush { flushCount++ }

        root.addProcess(pre)
        root.addProcess(flush)
        root.step()

        assertEquals(3, preCount)
        assertEquals(1, flushCount)

        root.removeProcess(pre)
        root.removeProcess(flush)
        root.step()
        assertEquals(3, preCount)
        assertEquals(1, flushCount)
    }

    @Test
    fun `current source circuit can be owned and stepped by root`() {
        val root = RootSystem(dt = 0.1, interSystemOverSampling = 1)
        val node = VoltageState()
        val source = CurrentSource("source", node, null).setCurrent(0.02)
        val resistor = Resistor(node, null).setResistance(50.0)

        root.addState(node)
        root.addComponent(source)
        root.addComponent(resistor)
        assertTrue(root.isRegistered(node))

        root.step()

        assertTrue(root.isRegistered(node))
        assertEquals(1.0, node.voltage, ROOT_EPSILON)
    }

    @Test
    fun `pending state and component can be removed before generation`() {
        val root = RootSystem(dt = 0.1, interSystemOverSampling = 1)
        val node = VoltageState()
        val resistor = Resistor(node, null)

        root.addState(node)
        root.addComponent(resistor)
        root.removeComponent(resistor)
        root.removeState(node)

        assertFalse(node in root.pendingStates)
        assertFalse(resistor in root.pendingComponents)
        assertFalse(root.isRegistered(node))
    }

    @Test
    fun `removed component is rediscovered through stale state connectivity like legacy`() {
        val root = RootSystem(dt = 0.1, interSystemOverSampling = 1)
        val node = VoltageState()
        val resistor = Resistor(node, null)

        root.addState(node)
        root.addComponent(resistor)
        root.generate()

        root.removeComponent(resistor)
        root.generate()

        assertFalse(resistor in root.pendingComponents)
        assertSame(root.findSubSystemWith(node), resistor.subSystem)
        assertTrue(resistor in root.findSubSystemWith(node)?.components.orEmpty())
    }

    @Test
    fun `breaking a subsystem runs destructors exactly once`() {
        val root = RootSystem(dt = 0.1, interSystemOverSampling = 1)
        val node = VoltageState()
        val resistor = Resistor(node, null)
        root.addState(node)
        root.addComponent(resistor)
        root.generate()

        var calls = 0
        val subSystem = root.systems.single()
        subSystem.breakDestructors += IDestructor { calls++ }

        assertTrue(subSystem.breakSystem())
        assertFalse(subSystem.breakSystem())
        assertEquals(1, calls)
    }
}
