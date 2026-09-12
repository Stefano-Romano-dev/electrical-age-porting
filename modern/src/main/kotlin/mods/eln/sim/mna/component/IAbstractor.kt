package mods.eln.sim.mna.component

import mods.eln.sim.mna.SubSystem

interface IAbstractor {
    fun dirty(component: Component)

    val abstractorSubSystem: SubSystem?
}
