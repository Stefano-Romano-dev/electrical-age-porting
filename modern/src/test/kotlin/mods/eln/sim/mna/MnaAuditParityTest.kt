package mods.eln.sim.mna

import mods.eln.sim.mna.component.Capacitor
import mods.eln.sim.mna.component.Inductor
import mods.eln.sim.mna.component.Monopole
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.component.Transformer
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.state.VoltageState
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val AUDIT_EPSILON = 1e-9

class MnaAuditParityTest {
    @Test
    fun `debug snapshot captures matrix labels owners and connections`() {
        val a = VoltageState().apply { setOwner("node-a") }
        val b = VoltageState().apply { setOwner("node-b") }
        val resistor = Resistor(a, b).setResistance(2.0).apply { setOwner("R1") }
        val subSystem = SubSystem(dt = 0.1)
        subSystem.addState(a).addState(b).addComponent(resistor)

        val snapshot = subSystem.captureDebugSnapshot()

        assertTrue(snapshot.isSingular)
        assertArrayEquals(doubleArrayOf(0.5, -0.5), snapshot.conductanceMatrix[a.id], AUDIT_EPSILON)
        assertArrayEquals(doubleArrayOf(-0.5, 0.5), snapshot.conductanceMatrix[b.id], AUDIT_EPSILON)
        assertEquals(listOf("node-a", "node-b"), snapshot.stateOwners.toList())
        assertTrue(snapshot.stateLabels[0].contains("VoltageState"))
        assertTrue(snapshot.stateLabels[0].contains("node-a"))
        assertTrue(snapshot.componentLabels.single().contains("Resistor"))
        assertTrue(snapshot.componentLabels.single().contains("R1"))
        assertArrayEquals(intArrayOf(a.id, b.id), snapshot.componentConnections.single())
    }

    @Test
    fun `subsystem accessors preserve state and safe ground behavior`() {
        val state = VoltageState()
        val resistor = Resistor(state, null)
        val subSystem = SubSystem(dt = 0.05)
        subSystem.addState(state).addComponent(resistor)

        subSystem.setX(state, 3.5)

        assertEquals(3.5, subSystem.getX(state), AUDIT_EPSILON)
        assertEquals(0.0, subSystem.getXSafe(null), AUDIT_EPSILON)
        assertEquals(1, subSystem.componentSize())
        assertTrue(subSystem.toString().contains("Resistor"))
    }

    @Test
    fun `monopole retains the legacy connection-list behavior`() {
        val first = VoltageState()
        val second = VoltageState()
        val monopole = object : Monopole() {
            override fun applyToSubsystem(subSystem: SubSystem) = Unit
        }

        monopole.connectTo(first)
        monopole.connectTo(second)

        assertEquals(second, monopole.connectedStates().single())
        assertTrue(monopole in first.connectedComponents())
        assertTrue(monopole in second.connectedComponents())
    }

    @Test
    fun `bipole break removes registrations but retains legacy pin references`() {
        val a = VoltageState()
        val b = VoltageState()
        val source = VoltageSource("source", a, b)

        source.breakConnection()

        assertEquals(a, source.aPin)
        assertEquals(b, source.bPin)
        assertFalse(source in a.connectedComponents())
        assertFalse(source in b.connectedComponents())
    }

    @Test
    fun `state connection list retains duplicate legacy entries`() {
        val state = VoltageState()
        val resistor = Resistor(state, state)

        assertEquals(2, state.connectedComponents().count { it === resistor })
        resistor.breakConnection()
        assertTrue(state.connectedComponents().isEmpty())
    }

    @Test
    fun `capacitor matrix and rhs match legacy coefficients`() {
        val a = VoltageState()
        val b = VoltageState()
        val capacitor = Capacitor(a, b).setCoulombs(2.0)
        val subSystem = SubSystem(dt = 0.5)
        subSystem.addState(a).addState(b).addComponent(capacitor)
        subSystem.captureDebugSnapshot()
        a.state = 5.0
        b.state = 1.0
        capacitor.simProcessI(subSystem)

        val snapshot = subSystem.captureDebugSnapshot()
        assertArrayEquals(doubleArrayOf(4.0, -4.0), snapshot.conductanceMatrix[a.id], AUDIT_EPSILON)
        assertArrayEquals(doubleArrayOf(-4.0, 4.0), snapshot.conductanceMatrix[b.id], AUDIT_EPSILON)
        assertEquals(16.0, snapshot.rhsVector[a.id], AUDIT_EPSILON)
        assertEquals(-16.0, snapshot.rhsVector[b.id], AUDIT_EPSILON)
    }

    @Test
    fun `inductor matrix and rhs match legacy coefficients`() {
        val a = VoltageState()
        val b = VoltageState()
        val inductor = Inductor("L", a, b).setInductance(2.0)
        val subSystem = SubSystem(dt = 0.5)
        subSystem.addState(a).addState(b).addComponent(inductor)
        subSystem.captureDebugSnapshot()
        inductor.currentState.state = 3.0
        inductor.simProcessI(subSystem)

        val snapshot = subSystem.captureDebugSnapshot()
        assertEquals(-4.0, snapshot.conductanceMatrix[inductor.currentState.id][inductor.currentState.id], AUDIT_EPSILON)
        assertEquals(1.0, snapshot.conductanceMatrix[a.id][inductor.currentState.id], AUDIT_EPSILON)
        assertEquals(-1.0, snapshot.conductanceMatrix[b.id][inductor.currentState.id], AUDIT_EPSILON)
        assertEquals(-12.0, snapshot.rhsVector[inductor.currentState.id], AUDIT_EPSILON)
    }

    @Test
    fun `transformer matrix retains all ratio coefficients`() {
        val a = VoltageState()
        val b = VoltageState()
        val transformer = Transformer(a, b).setRatio(2.0)
        val subSystem = SubSystem(dt = 0.1)
        subSystem.addState(a).addState(b).addComponent(transformer)

        val snapshot = subSystem.captureDebugSnapshot()
        val ai = transformer.aCurrentState.id
        val bi = transformer.bCurrentState.id
        assertEquals(-2.0, snapshot.conductanceMatrix[bi][a.id], AUDIT_EPSILON)
        assertEquals(-0.5, snapshot.conductanceMatrix[ai][b.id], AUDIT_EPSILON)
        assertEquals(2.0, snapshot.conductanceMatrix[ai][bi], AUDIT_EPSILON)
        assertEquals(2.0, snapshot.conductanceMatrix[bi][bi], AUDIT_EPSILON)
    }

    @Test
    fun `snapshot arrays remain detached from later matrix rebuilds`() {
        val state = VoltageState()
        val resistor = Resistor(state, null).setResistance(2.0)
        val subSystem = SubSystem(dt = 0.1)
        subSystem.addState(state).addComponent(resistor)
        val before = subSystem.captureDebugSnapshot()

        resistor.setResistance(4.0)
        val after = subSystem.captureDebugSnapshot()

        assertEquals(0.5, before.conductanceMatrix[0][0], AUDIT_EPSILON)
        assertEquals(0.25, after.conductanceMatrix[0][0], AUDIT_EPSILON)
        assertFalse(before.conductanceMatrix === after.conductanceMatrix)
    }
}
