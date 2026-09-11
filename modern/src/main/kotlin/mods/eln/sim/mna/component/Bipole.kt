package mods.eln.sim.mna.component

import mods.eln.sim.mna.state.State

abstract class Bipole(
    aPin: State? = null,
    bPin: State? = null,
) : Component() {
    var aPin: State? = null
        private set

    var bPin: State? = null
        private set

    init {
        connectTo(aPin, bPin)
    }

    fun connectTo(aPin: State?, bPin: State?): Bipole = apply {
        breakConnection()
        this.aPin = aPin
        this.bPin = bPin
        aPin?.addComponent(this)
        bPin?.addComponent(this)
        dirty()
    }

    override fun breakConnection() {
        aPin?.removeComponent(this)
        bPin?.removeComponent(this)
        aPin = null
        bPin = null
        dirty()
    }

    override fun connectedStates(): Array<State?> = arrayOf(aPin, bPin)

    abstract val current: Double

    open val voltage: Double
        get() = (aPin?.state ?: 0.0) - (bPin?.state ?: 0.0)

    override fun toString(): String = "[$aPin ${javaClass.simpleName} $bPin]"
}
