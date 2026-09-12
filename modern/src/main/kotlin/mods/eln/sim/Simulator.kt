package mods.eln.sim

import mods.eln.sim.mna.RootSystem
import mods.eln.sim.mna.component.Component
import mods.eln.sim.mna.state.State

/**
 * Platform-independent owner of the legacy multi-rate simulation schedule.
 * A NeoForge server-tick adapter will call [tick]; no event bus belongs here.
 */
class Simulator(
    val callPeriod: Double,
    val electricalPeriod: Double,
    electricalInterSystemOverSampling: Int,
    val thermalPeriod: Double,
    ambientExchange: ThermalAmbientExchange? = null,
) : ThermalLoadValidator {
    val mna = RootSystem(electricalPeriod, electricalInterSystemOverSampling)

    private val thermalSimulator = ThermalSimulator(ambientExchange)
    private val slowProcesses = mutableListOf<IProcess>()
    private val slowPreProcesses = mutableListOf<IProcess>()
    private val slowPostProcesses = mutableListOf<IProcess>()
    private val electricalProcesses = mutableListOf<IProcess>()
    private val thermalFastProcesses = mutableListOf<IProcess>()
    private val thermalSlowProcesses = mutableListOf<IProcess>()
    private val thermalFastConnections = mutableListOf<ThermalConnection>()
    private val thermalSlowConnections = mutableListOf<ThermalConnection>()
    private val thermalFastLoads = mutableListOf<ThermalLoad>()
    private val thermalSlowLoads = mutableListOf<ThermalLoad>()
    private val pendingDestructions = mutableSetOf<() -> Unit>()

    private var timeout = 0.0
    private var electricalTimeout = 0.0
    private var thermalTimeout = 0.0

    override fun getMinimalThermalC(Rs: Double, Rp: Double): Double =
        thermalPeriod * 3.0 / (1.0 / (1.0 / Rp + 1.0 / Rs))

    override fun checkThermalLoad(Rs: Double, Rp: Double, C: Double): Boolean {
        if (C < getMinimalThermalC(Rs, Rp)) {
            throw IllegalStateException("Thermal load outside safe limits.")
        }
        return true
    }

    fun addElectricalComponent(component: Component) = mna.addComponent(component)
    fun removeElectricalComponent(component: Component) = mna.removeComponent(component)
    fun addElectricalLoad(load: State) = mna.addState(load)
    fun removeElectricalLoad(load: State) = mna.removeState(load)

    /** Returns false for the same fast/slow mismatch rejected by the legacy simulator. */
    fun addThermalConnection(connection: ThermalConnection): Boolean {
        if (connection.L1.isSlow != connection.L2.isSlow) return false
        if (connection.L1.isSlow) thermalSlowConnections += connection else thermalFastConnections += connection
        return true
    }

    fun removeThermalConnection(connection: ThermalConnection) {
        thermalSlowConnections -= connection
        thermalFastConnections -= connection
    }

    fun addThermalLoad(load: ThermalLoad) {
        if (load.isSlow) thermalSlowLoads += load else thermalFastLoads += load
    }

    fun removeThermalLoad(load: ThermalLoad) {
        thermalSlowLoads -= load
        thermalFastLoads -= load
    }

    fun addSlowProcess(process: IProcess) { slowProcesses += process }
    fun removeSlowProcess(process: IProcess) { slowProcesses -= process }
    fun addSlowPreProcess(process: IProcess) { slowPreProcesses += process }
    fun removeSlowPreProcess(process: IProcess) { slowPreProcesses -= process }
    fun addSlowPostProcess(process: IProcess) { slowPostProcesses += process }
    fun removeSlowPostProcess(process: IProcess) { slowPostProcesses -= process }
    fun addElectricalProcess(process: IProcess) { electricalProcesses += process }
    fun removeElectricalProcess(process: IProcess) { electricalProcesses -= process }
    fun addThermalFastProcess(process: IProcess) { thermalFastProcesses += process }
    fun removeThermalFastProcess(process: IProcess) { thermalFastProcesses -= process }
    fun addThermalSlowProcess(process: IProcess) { thermalSlowProcesses += process }
    fun removeThermalSlowProcess(process: IProcess) { thermalSlowProcesses -= process }

    fun queueDestruction(action: () -> Unit) {
        pendingDestructions += action
    }

    fun tick() {
        slowPreProcesses.toTypedArray().forEach { it.process(SLOW_PERIOD) }
        timeout += callPeriod

        while (timeout > 0.0) {
            if (timeout < electricalTimeout && timeout < thermalTimeout) {
                thermalTimeout -= timeout
                electricalTimeout -= timeout
                timeout = 0.0
                break
            }

            val elapsed: Double
            if (electricalTimeout <= thermalTimeout) {
                elapsed = electricalTimeout
                electricalTimeout += electricalPeriod
                mna.step()
                electricalProcesses.forEach { it.process(electricalPeriod) }
            } else {
                elapsed = thermalTimeout
                thermalTimeout += thermalPeriod
                thermalSimulator.step(
                    thermalPeriod,
                    thermalFastConnections,
                    thermalFastProcesses,
                    thermalFastLoads,
                )
            }
            thermalTimeout -= elapsed
            electricalTimeout -= elapsed
            timeout -= elapsed
        }

        thermalSimulator.step(
            SLOW_PERIOD,
            thermalSlowConnections,
            thermalSlowProcesses,
            thermalSlowLoads,
        )
        slowProcesses.toTypedArray().forEach { it.process(SLOW_PERIOD) }
        pendingDestructions.forEach { it() }
        pendingDestructions.clear()
        slowPostProcesses.forEach { it.process(SLOW_PERIOD) }
    }

    companion object {
        const val SLOW_PERIOD: Double = 1.0 / 20.0
    }
}

interface ThermalLoadValidator {
    fun getMinimalThermalC(Rs: Double, Rp: Double): Double
    fun checkThermalLoad(Rs: Double, Rp: Double, C: Double): Boolean
}
