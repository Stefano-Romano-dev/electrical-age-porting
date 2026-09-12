package mods.eln.sim.mna.component

import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.misc.ISubSystemProcessI

class Delay : Bipole(), ISubSystemProcessI {
    var impedance: Double = 0.0
        private set

    private var conductance = 0.0
    private var oldIa = 0.0
    private var oldIb = 0.0

    override val current: Double
        get() = oldIa - oldIb

    fun setImpedance(impedance: Double): Delay = apply {
        this.impedance = impedance
        conductance = 1.0 / impedance
    }

    override fun addToSubsystem(subSystem: SubSystem) {
        super.addToSubsystem(subSystem)
        subSystem.addProcess(this)
    }

    override fun applyToSubsystem(subSystem: SubSystem) {
        subSystem.addToA(aPin, aPin, conductance)
        subSystem.addToA(bPin, bPin, conductance)
    }

    override fun simProcessI(subSystem: SubSystem) {
        val a = requireNotNull(aPin)
        val b = requireNotNull(bPin)
        val iA = a.state * conductance + oldIa
        val iB = b.state * conductance + oldIb
        val target = (iA - iB) / 2.0
        val aPinCurrent = target - (a.state + b.state) * 0.5 * conductance
        val bPinCurrent = -target - (a.state + b.state) * 0.5 * conductance

        subSystem.addToI(a, -aPinCurrent)
        subSystem.addToI(b, -bPinCurrent)
        oldIa = aPinCurrent
        oldIb = bPinCurrent
    }
}
