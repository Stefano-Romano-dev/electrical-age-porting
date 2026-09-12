@file:Suppress("PropertyName")

package mods.eln.platform.persistence

import mods.eln.sim.BatteryProcess
import mods.eln.sim.BatteryState
import mods.eln.sim.FurnaceProcess
import mods.eln.sim.RegulatorProcess
import mods.eln.sim.RegulatorState
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.CurrentSource
import mods.eln.sim.mna.component.Inductor
import mods.eln.sim.mna.component.PowerSource
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.component.ResistorSwitch
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.process.PowerSourceBipole
import mods.eln.sim.mna.state.VoltageState
import net.minecraft.nbt.CompoundTag

object BatteryProcessTagCodec {
    fun write(tag: CompoundTag, prefix: String, process: BatteryProcess) {
        val state = process.captureState()
        tag.putDouble(prefix + BatteryState.LEGACY_Q_SUFFIX, state.q)
        tag.putDouble(prefix + BatteryState.LEGACY_LIFE_SUFFIX, state.life)
    }

    fun read(tag: CompoundTag, prefix: String, process: BatteryProcess) {
        process.restoreState(
            BatteryState(
                q = tag.getDouble(prefix + BatteryState.LEGACY_Q_SUFFIX),
                life = tag.getDouble(prefix + BatteryState.LEGACY_LIFE_SUFFIX),
            ),
        )
    }
}

object RegulatorProcessTagCodec {
    fun write(tag: CompoundTag, prefix: String, process: RegulatorProcess) {
        val state = process.captureState()
        tag.putDouble(state.legacyErrorIntegratedKey(prefix, process.name), state.errorIntegrated)
        tag.putDouble(state.legacyTargetKey(prefix, process.name), state.target)
    }

    fun read(tag: CompoundTag, prefix: String, process: RegulatorProcess) {
        process.restoreState(
            RegulatorState(
                errorIntegrated = tag.getDouble(prefix + process.name + RegulatorState.ERROR_INTEGRATED_SUFFIX),
                target = tag.getDouble(prefix + process.name + RegulatorState.TARGET_SUFFIX),
            ),
        )
    }
}

object VoltageStateTagCodec {
    fun write(tag: CompoundTag, prefix: String, name: String, state: VoltageState) {
        tag.putFloat(prefix + name + "Uc", state.voltage.toFloat())
    }

    fun read(tag: CompoundTag, prefix: String, name: String, state: VoltageState) {
        val voltage = tag.getFloat(prefix + name + "Uc")
        state.voltage = if (voltage.isFinite()) voltage.toDouble() else 0.0
    }
}

object ThermalLoadTagCodec {
    fun write(tag: CompoundTag, prefix: String, name: String, load: ThermalLoad) {
        tag.putFloat(prefix + name + "Tc", load.temperatureCelsius.toFloat())
    }

    fun read(tag: CompoundTag, prefix: String, name: String, load: ThermalLoad) {
        val temperature = tag.getFloat(prefix + name + "Tc")
        load.temperatureCelsius = if (temperature.isFinite()) temperature.toDouble() else 0.0
    }
}

object FurnaceProcessTagCodec {
    fun write(tag: CompoundTag, prefix: String, name: String, process: FurnaceProcess) {
        tag.putFloat(prefix + name + "Q", process.combustibleEnergy.toFloat())
        tag.putDouble(prefix + name + "gain", process.gain)
    }

    fun read(tag: CompoundTag, prefix: String, name: String, process: FurnaceProcess) {
        process.combustibleEnergy = tag.getFloat(prefix + name + "Q").toDouble()
        process.setGain(tag.getDouble(prefix + name + "gain"))
    }
}

object VoltageSourceTagCodec {
    fun write(tag: CompoundTag, prefix: String, source: VoltageSource) {
        val key = prefix + source.name
        tag.putDouble(key + "U", source.voltage)
        tag.putDouble(key + "Istate", source.currentState.state)
    }

    fun read(tag: CompoundTag, prefix: String, source: VoltageSource) {
        val key = prefix + source.name
        source.setVoltage(tag.getDouble(key + "U"))
        source.currentState.state = tag.getDouble(key + "Istate")
    }
}

object CurrentSourceTagCodec {
    fun write(tag: CompoundTag, prefix: String, source: CurrentSource) {
        tag.putDouble(prefix + source.name + "I", source.current)
    }

    fun read(tag: CompoundTag, prefix: String, source: CurrentSource) {
        source.setCurrent(tag.getDouble(prefix + source.name + "I"))
    }
}

object InductorTagCodec {
    fun write(tag: CompoundTag, prefix: String, inductor: Inductor) {
        tag.putDouble(prefix + inductor.name + "Istate", inductor.currentState.state)
    }

    fun read(tag: CompoundTag, prefix: String, inductor: Inductor) {
        inductor.currentState.state = tag.getDouble(prefix + inductor.name + "Istate")
    }
}

object ResistorSwitchTagCodec {
    fun write(tag: CompoundTag, prefix: String, resistor: ResistorSwitch) {
        val key = prefix + resistor.name
        tag.putDouble(key + "R", resistor.baseResistance)
        tag.putBoolean(key + "State", resistor.state)
    }

    fun read(tag: CompoundTag, prefix: String, resistor: ResistorSwitch) {
        val key = prefix + resistor.name
        resistor.restoreState(tag.getDouble(key + "R"), tag.getBoolean(key + "State"))
    }
}

object PowerSourceTagCodec {
    fun write(tag: CompoundTag, prefix: String, source: PowerSource) {
        VoltageSourceTagCodec.write(tag, prefix, source)
        val key = prefix + source.name
        tag.putDouble(key + "P", source.power)
        tag.putDouble(key + "Umax", source.maximumVoltage)
        tag.putDouble(key + "Imax", source.maximumCurrent)
    }

    fun read(tag: CompoundTag, prefix: String, source: PowerSource) {
        VoltageSourceTagCodec.read(tag, prefix, source)
        val key = prefix + source.name
        source.setPower(tag.getDouble(key + "P"))
        source.setMaximumVoltage(tag.getDouble(key + "Umax"))
        source.setMaximumCurrent(tag.getDouble(key + "Imax"))
    }
}

object PowerSourceBipoleTagCodec {
    fun write(tag: CompoundTag, prefix: String, source: PowerSourceBipole) {
        tag.putDouble(prefix + "P", source.power)
        tag.putDouble(prefix + "Umax", source.maximumVoltage)
        tag.putDouble(prefix + "Imax", source.maximumCurrent)
    }

    fun read(tag: CompoundTag, prefix: String, source: PowerSourceBipole) {
        source.setPower(tag.getDouble(prefix + "P"))
        source.setMaximumVoltage(tag.getDouble(prefix + "Umax"))
        source.setMaximumCurrent(tag.getDouble(prefix + "Imax"))
    }
}

object ResistorTagCodec {
    /** The 1.24.8 NbtResistor ignores its name and uses only prefix + "R". */
    fun write(tag: CompoundTag, prefix: String, @Suppress("UNUSED_PARAMETER") name: String, resistor: Resistor) {
        tag.putDouble(prefix + "R", resistor.resistance)
    }

    fun read(tag: CompoundTag, prefix: String, @Suppress("UNUSED_PARAMETER") name: String, resistor: Resistor) {
        resistor.setResistance(tag.getDouble(prefix + "R"))
    }
}
