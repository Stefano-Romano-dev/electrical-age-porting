package mods.eln.sim.mna.component

import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.state.State

abstract class Component {
    var owner: String? = null
        private set

    private var localSubSystem: SubSystem? = null

    var abstractedBy: IAbstractor? = null

    val subSystem: SubSystem?
        get() = abstractedBy?.abstractorSubSystem ?: localSubSystem

    val isAbstracted: Boolean
        get() = abstractedBy != null

    internal val directSubSystem: SubSystem?
        get() = localSubSystem

    open fun addToSubsystem(subSystem: SubSystem) {
        localSubSystem = subSystem
    }

    open fun quitSubSystem() {
        localSubSystem = null
    }

    fun dirty() {
        abstractedBy?.dirty(this) ?: localSubSystem?.invalidate()
    }

    abstract fun applyToSubsystem(subSystem: SubSystem)

    abstract fun connectedStates(): Array<State?>

    open fun canBeReplacedByInterSystem(): Boolean = false

    open fun breakConnection() = Unit

    open fun onAddToRootSystem() = Unit

    open fun onRemoveFromRootSystem() = Unit

    open fun returnToRootSystem(rootSystem: mods.eln.sim.mna.RootSystem) {
        rootSystem.pendingComponents += this
    }

    fun setOwner(owner: String?): Component = apply { this.owner = owner }

    override fun toString(): String = "(${javaClass.simpleName})"
}
