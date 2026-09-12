package mods.eln.sim.process.heater

import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.state.VoltageState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HeaterProcessParityTest {
    @Test
    fun `resistor process always transfers electrical power`() {
        val a = VoltageState().apply { voltage = 6.0 }
        val resistor = Resistor(a, null).apply { setResistance(3.0) }
        val thermal = ThermalLoad()

        ResistorHeatThermalLoad(resistor, thermal).process(1.0)

        assertEquals(12.0, thermal.PcTemp)
    }

    @Test
    fun `diode process skips the sample where resistance changes`() {
        val a = VoltageState().apply { voltage = 10.0 }
        val resistor = Resistor(a, null).apply { setResistance(5.0) }
        val thermal = ThermalLoad()
        val process = DiodeHeatThermalLoad(resistor, thermal)

        process.process(1.0)
        assertEquals(20.0, thermal.PcTemp)

        resistor.setResistance(10.0)
        process.process(1.0)
        assertEquals(20.0, thermal.PcTemp)

        process.process(1.0)
        assertEquals(30.0, thermal.PcTemp)
    }

    @Test
    fun `electrical load heater uses legacy half current convention`() {
        val load = ElectricalLoad().apply {
            voltage = 10.0
            setSerialResistance(2.0)
        }
        SubSystem(null, 0.1).addState(load)
        Resistor(load, null).apply { setResistance(5.0) }
        val thermal = ThermalLoad().apply { heatCapacity = 10.0 }

        ElectricalLoadHeatThermalLoad(load, thermal).process(1.0)

        assertEquals(4.0, thermal.PcTemp)
    }

    @Test
    fun `electrical load heater clamps temperature rate`() {
        val load = ElectricalLoad().apply {
            voltage = 10.0
            setSerialResistance(2.0)
        }
        SubSystem(null, 0.1).addState(load)
        Resistor(load, null).apply { setResistance(5.0) }
        val thermal = ThermalLoad().apply { heatCapacity = 10.0 }

        ElectricalLoadHeatThermalLoad(load, thermal)
            .limitTemperatureRate(0.1)
            .process(1.0)

        assertEquals(1.0, thermal.PcTemp)
    }

    @Test
    fun `electrical load heater skips unsimulated load`() {
        val electrical = ElectricalLoad()
        val thermal = ThermalLoad()

        ElectricalLoadHeatThermalLoad(electrical, thermal).process(1.0)

        assertEquals(0.0, thermal.PcTemp)
    }
}
