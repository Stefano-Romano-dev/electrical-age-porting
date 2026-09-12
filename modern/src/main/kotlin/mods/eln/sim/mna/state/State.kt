package mods.eln.sim.mna.state

import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.component.Component
import mods.eln.sim.mna.component.IAbstractor

/** A scalar unknown in the modified nodal analysis system. */
open class State {
    var state: Double = 0.0

    var id: Int = -1
        internal set

    var owner: String? = null
        private set

    private var localSubSystem: SubSystem? = null

    var abstractedBy: IAbstractor? = null

    val subSystem: SubSystem?
        get() = abstractedBy?.abstractorSubSystem ?: localSubSystem

    val isAbstracted: Boolean
        get() = abstractedBy != null

    val isNotSimulated: Boolean
        get() = localSubSystem == null && abstractedBy == null

    internal val directSubSystem: SubSystem?
        get() = localSubSystem

    private val connectedComponents = mutableListOf<Component>()

    var isPrivateSubSystem: Boolean = false
        private set

    private var farFromInterSystem: Boolean = false

    internal fun attachTo(subSystem: SubSystem) {
        localSubSystem = subSystem
    }

    internal fun detachFromSubSystem() {
        localSubSystem = null
        id = -1
    }

    internal fun addComponent(component: Component) {
        connectedComponents += component
    }

    internal fun removeComponent(component: Component) {
        connectedComponents -= component
    }

    fun connectedComponents(): List<Component> = connectedComponents.toList()

    fun connectedComponentsNotAbstracted(): List<Component> = connectedComponents.filterNot { it.isAbstracted }

    open fun canBeSimplifiedByLine(): Boolean = false

    fun setAsPrivate(): State = apply { isPrivateSubSystem = true }

    fun setAsMustBeFarFromInterSystem(): State = apply { farFromInterSystem = true }

    fun mustBeFarFromInterSystem(): Boolean = farFromInterSystem

    fun returnToRootSystem(rootSystem: mods.eln.sim.mna.RootSystem) {
        rootSystem.pendingStates += this
    }

    fun setOwner(owner: String?): State = apply { this.owner = owner }

    override fun toString(): String = "($id,${javaClass.simpleName})"
}
