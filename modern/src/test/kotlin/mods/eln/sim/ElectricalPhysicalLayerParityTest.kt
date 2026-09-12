package mods.eln.sim

import mods.eln.sim.mna.RootSystem
import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.component.Line
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.misc.MnaConst
import mods.eln.sim.mna.state.VoltageState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class ElectricalPhysicalLayerParityTest {
    @Test
    fun `electrical load starts at legacy high impedance`() {
        val load = ElectricalLoad()

        assertEquals(MnaConst.HIGH_IMPEDANCE, load.serialResistance)
        assertNull(ElectricalLoad.groundLoad)
    }

    @Test
    fun `connection resistance follows both serial resistances`() {
        val first = ElectricalLoad().apply { setSerialResistance(2.5) }
        val second = ElectricalLoad().apply { setSerialResistance(7.5) }
        val connection = ElectricalConnection(first, second)
        val root = RootSystem(0.1, 1)

        root.addComponent(connection)
        assertEquals(10.0, connection.resistance)

        first.setSerialResistance(5.0)
        assertEquals(12.5, connection.resistance)

        root.removeComponent(connection)
        assertSame(first, connection.aPin)
        assertSame(second, connection.bPin)
        assertFalse(connection in first.connectedComponents())
        assertFalse(connection in second.connectedComponents())
    }

    @Test
    fun `load current keeps legacy half sum and excludes line abstraction`() {
        val load = ElectricalLoad().apply { voltage = 12.0 }
        val groundBranch = Resistor(load, null).apply { setResistance(3.0) }
        val other = VoltageState().apply { voltage = 4.0 }
        val secondBranch = Resistor(load, other).apply { setResistance(2.0) }
        val line = Line().apply {
            connectTo(load, null)
            setResistance(1.0)
        }

        assertEquals(4.0, groundBranch.current)
        assertEquals(4.0, secondBranch.current)
        assertEquals(4.0, load.current)
        assertEquals(12.0, line.current)
    }

    @Test
    fun `electrical load counts as simulated after subsystem attachment`() {
        val load = ElectricalLoad()
        assertEquals(true, load.isNotSimulated)

        SubSystem(null, 0.1).addState(load)

        assertEquals(false, load.isNotSimulated)
    }
}
