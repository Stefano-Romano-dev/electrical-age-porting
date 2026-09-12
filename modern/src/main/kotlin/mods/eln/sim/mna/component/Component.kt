package mods.eln.sim.mna.component

import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.state.State

abstract class Component {
    var owner: String? = null
        private set

    var subSystem: SubSystem? = null
        private set

    open fun addToSubsystem(subSystem: SubSystem) {
        this.subSystem = subSystem
    }

    open fun quitSubSystem() {
        subSystem = null
    }

    fun dirty() {
        subSystem?.invalidate()
    }

    abstract fun applyToSubsystem(subSystem: SubSystem)

    abstract fun connectedStates(): Array<State?>

    open fun canBeReplacedByInterSystem(): Boolean = false

    open fun breakConnection() = Unit

    open fun onAddToRootSystem() = Unit

    open fun onRemoveFromRootSystem() = Unit

    fun returnToRootSystem(rootSystem: mods.eln.sim.mna.RootSystem) {
        rootSystem.pendingComponents += this
    }

    fun setOwner(owner: String?): Component = apply { this.owner = owner }

    override fun toString(): String = "(${javaClass.simpleName})"
}
