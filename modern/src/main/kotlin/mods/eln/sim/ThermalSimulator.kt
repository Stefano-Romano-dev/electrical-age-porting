package mods.eln.sim

/** Platform boundary for the optional room-aware ambient exchange. */
fun interface ThermalAmbientExchange {
    /** Returns positive power leaving [load], or null for the legacy zero-Celsius ambient fallback. */
    fun exchange(load: ThermalLoad, time: Double): Double?
}

/** The thermal half of the legacy Simulator, with the original operation order. */
class ThermalSimulator(
    private val ambientExchange: ThermalAmbientExchange? = null,
) {
    fun step(
        time: Double,
        connections: Iterable<ThermalConnection>,
        processes: Iterable<IProcess>,
        loads: Iterable<ThermalLoad>,
    ) {
        connections.forEach { connection ->
            val power = (connection.L2.temperatureCelsius - connection.L1.temperatureCelsius) /
                (connection.L2.Rs + connection.L1.Rs)
            connection.L1.PcTemp += power
            connection.L2.PcTemp -= power
            connection.L1.PrsTemp += kotlin.math.abs(power)
            connection.L2.PrsTemp += kotlin.math.abs(power)
        }

        processes.forEach { it.process(time) }

        loads.forEach { load ->
            val roomPower = if (load.hasSimCoordinate()) ambientExchange?.exchange(load, time) else null
            load.PcTemp -= roomPower ?: (load.temperatureCelsius / load.Rp)
            load.temperatureCelsius += load.PcTemp * time / load.heatCapacity
            load.Pc = load.PcTemp
            load.Prs = load.PrsTemp
            load.Psp = load.PspTemp
            load.PcTemp = 0.0
            load.PrsTemp = 0.0
            load.PspTemp = 0.0
        }
    }
}
