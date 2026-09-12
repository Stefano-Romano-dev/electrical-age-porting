package mods.eln.sim.mna.component

import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.misc.ISubSystemProcessI
import mods.eln.sim.mna.state.State

class Capacitor(
    aPin: State? = null,
    bPin: State? = null,
) : Bipole(aPin, bPin), ISubSystemProcessI {
    var coulombs: Double = 0.0
        private set

    private var coulombsPerStep = 0.0

    override val current: Double
        get() = 0.0

    val energy: Double
        get() = voltage * voltage * coulombs / 2.0

    fun setCoulombs(coulombs: Double): Capacitor = apply {
        this.coulombs = coulombs
        dirty()
    }

    override fun applyToSubsystem(subSystem: SubSystem) {
        coulombsPerStep = coulombs / subSystem.dt
        subSystem.addToA(aPin, aPin, coulombsPerStep)
        subSystem.addToA(aPin, bPin, -coulombsPerStep)
        subSystem.addToA(bPin, bPin, coulombsPerStep)
        subSystem.addToA(bPin, aPin, -coulombsPerStep)
    }

    override fun simProcessI(subSystem: SubSystem) {
        val previousVoltage = (aPin?.state ?: 0.0) - (bPin?.state ?: 0.0)
        val contribution = previousVoltage * coulombsPerStep
        subSystem.addToI(aPin, contribution)
        subSystem.addToI(bPin, -contribution)
    }

    override fun addToSubsystem(subSystem: SubSystem) {
        super.addToSubsystem(subSystem)
        subSystem.addProcess(this)
    }

    override fun quitSubSystem() {
        directSubSystem?.removeProcess(this)
        super.quitSubSystem()
    }
}
