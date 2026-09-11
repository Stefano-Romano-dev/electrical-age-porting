package mods.eln.sim.mna.misc

import mods.eln.sim.mna.SubSystem

/** Contributes the time-dependent right-hand side of an MNA system. */
fun interface ISubSystemProcessI {
    fun simProcessI(subSystem: SubSystem)
}
