package mods.eln.sim.mna.state

import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.component.Component

/** A scalar unknown in the modified nodal analysis system. */
open class State {
    var state: Double = 0.0

    var id: Int = -1
        internal set

    var owner: String? = null
        private set

    internal var subSystem: SubSystem? = null
        private set

    private val connectedComponents = mutableListOf<Component>()

    internal fun attachTo(subSystem: SubSystem) {
        this.subSystem = subSystem
    }

    internal fun detachFromSubSystem() {
        subSystem = null
        id = -1
    }

    internal fun addComponent(component: Component) {
        if (component !in connectedComponents) {
            connectedComponents += component
        }
    }

    internal fun removeComponent(component: Component) {
        connectedComponents -= component
    }

    fun connectedComponents(): List<Component> = connectedComponents.toList()

    fun setOwner(owner: String?): State = apply { this.owner = owner }

    override fun toString(): String = "($id,${javaClass.simpleName})"
}
