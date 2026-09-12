package mods.eln.sim.mna.state

open class VoltageStateLineReady : VoltageState() {
    private var lineSimplificationEnabled = false

    fun setCanBeSimplifiedByLine(value: Boolean) {
        lineSimplificationEnabled = value
    }

    override fun canBeSimplifiedByLine(): Boolean = lineSimplificationEnabled
}
