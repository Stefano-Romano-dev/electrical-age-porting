package mods.eln.sim

import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.component.ResistorSwitch
import mods.eln.sim.mna.state.VoltageState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EnergyProcessParityTest {
    @Test
    fun `diode process follows voltage sign without using time`() {
        val positive = VoltageState().apply { voltage = 3.0 }
        val diode = ResistorSwitch("d", positive, null)
        val process = DiodeProcess(diode)

        process.process(Double.NaN)
        assertTrue(diode.state)

        positive.voltage = -1.0
        process.process(0.0)
        assertFalse(diode.state)
    }

    @Test
    fun `furnace converts current power to heat and combustible energy`() {
        val thermal = ThermalLoad()
        val furnace = FurnaceProcess(thermal).apply {
            combustibleEnergy = 100.0
            nominalCombustibleEnergy = 200.0
            nominalPower = 40.0
            setGain(0.5)
        }

        furnace.process(2.0)

        assertEquals(8.0, furnace.power)
        assertEquals(80.0, furnace.combustibleEnergy)
        assertEquals(10.0, thermal.PcTemp)
    }

    @Test
    fun `furnace gain applies minimum then legacy upper clamp`() {
        val furnace = FurnaceProcess(ThermalLoad())
        furnace.setGainMin(0.25)
        furnace.setGain(-2.0)
        assertEquals(0.25, furnace.gain)

        furnace.setGainMin(2.0)
        assertEquals(1.0, furnace.gain)
    }

    @Test
    fun `legacy resistor heat process transfers electrical power`() {
        val source = VoltageState().apply { voltage = 12.0 }
        val resistor = Resistor(source, null).apply { setResistance(6.0) }
        val thermal = ThermalLoad()

        ElectricalResistorHeatThermalLoad(resistor, thermal).process(123.0)

        assertEquals(24.0, thermal.PcTemp)
    }
}
