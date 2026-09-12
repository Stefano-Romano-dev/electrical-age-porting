package mods.eln.sim.process.destruct

import mods.eln.sim.IProcess
import mods.eln.sim.Simulator

class DelayedDestruction(
    val dest: IDestructible,
    var timeout: Double,
    private val simulator: Simulator,
) : IProcess {
    init {
        simulator.addSlowProcess(this)
    }

    override fun process(time: Double) {
        timeout -= time
        if (timeout <= 0.0) {
            dest.destructImpl()
            simulator.removeSlowProcess(this)
        }
    }
}
