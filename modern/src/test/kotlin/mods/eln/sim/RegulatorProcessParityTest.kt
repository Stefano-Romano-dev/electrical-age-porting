package mods.eln.sim

import mods.eln.sim.mna.component.Resistor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private class TestRegulator(name: String = "r") : RegulatorProcess(name) {
    var measuredValue = 0.0
    val commands = mutableListOf<Double>()

    override fun getHit(): Double = measuredValue

    override fun setCmd(cmd: Double) {
        commands += cmd
    }
}

class RegulatorProcessParityTest {
    @Test
    fun `none commands full output while manual leaves command untouched`() {
        val regulator = TestRegulator()

        regulator.process(1.0)
        regulator.setManual()
        regulator.process(1.0)

        assertEquals(listOf(1.0), regulator.commands)
    }

    @Test
    fun `on off controller retains dead band and strict thresholds`() {
        val regulator = TestRegulator().apply {
            target = 50.0
            setOnOff(0.2, 100.0)
        }
        assertEquals(listOf(0.0), regulator.commands)

        regulator.measuredValue = 60.0
        regulator.process(1.0)
        regulator.measuredValue = 61.0
        regulator.process(1.0)
        regulator.measuredValue = 40.0
        regulator.process(1.0)
        regulator.measuredValue = 39.0
        regulator.process(1.0)

        assertEquals(listOf(0.0, 0.0, 1.0), regulator.commands)
    }

    @Test
    fun `analog controller retains legacy derivative multiplication by time`() {
        val regulator = TestRegulator().apply {
            target = 10.0
            measuredValue = 8.0
            setAnalog(2.0, 0.5, 0.25, 10.0)
        }

        regulator.process(2.0)

        assertEquals(0.2, regulator.commands.single(), 1.0e-12)
        assertEquals(0.2, regulator.errorIntegrated, 1.0e-12)
        assertEquals(8.0, regulator.hitLast)
    }

    @Test
    fun `changing analog gains after boot resets integral and samples hit`() {
        val regulator = TestRegulator().apply {
            target = 10.0
            measuredValue = 5.0
            setAnalog(1.0, 1.0, 0.0, 10.0)
            process(1.0)
        }
        assertEquals(0.5, regulator.errorIntegrated)

        regulator.measuredValue = 7.0
        regulator.setAnalog(2.0, 1.0, 0.0, 10.0)

        assertEquals(0.0, regulator.errorIntegrated)
        assertEquals(7.0, regulator.hitLast)
    }

    @Test
    fun `regulator snapshot preserves keys and only repairs NaN integral`() {
        val regulator = TestRegulator("controller")
        regulator.restoreState(RegulatorState(Double.NaN, Double.POSITIVE_INFINITY))
        val state = regulator.captureState()

        assertEquals(0.0, state.errorIntegrated)
        assertEquals(Double.POSITIVE_INFINITY, state.target)
        assertEquals("pcontrollererrorIntegrated", state.legacyErrorIntegratedKey("p", regulator.name))
        assertEquals("pcontrollertarget", state.legacyTargetKey("p", regulator.name))
    }

    @Test
    fun `furnace regulator reads temperature and applies gain clamp`() {
        val furnace = FurnaceProcess(ThermalLoad().apply { temperatureCelsius = 120.0 })
        val regulator = RegulatorFurnaceProcess("f", furnace).apply {
            target = 100.0
            setOnOff(0.1, 100.0)
        }

        regulator.process(1.0)

        assertEquals(0.0, furnace.gain)
    }

    @Test
    fun `thermal resistor regulator retains command thresholds`() {
        val thermal = ThermalLoad().apply { temperatureCelsius = 0.0 }
        val resistor = Resistor()
        val regulator = RegulatorThermalLoadToElectricalResistor("heater", thermal, resistor).apply {
            minimumResistance = 10.0
            target = 50.0
            setAnalog(0.02, 0.0, 0.0, 1.0)
        }

        regulator.process(1.0)
        assertEquals(10.0, resistor.resistance)

        thermal.temperatureCelsius = 49.975
        regulator.process(1.0)
        assertEquals(ThermalLoad.HIGH_IMPEDANCE, resistor.resistance)
    }
}
