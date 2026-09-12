package mods.eln.sim.mna.state

open class VoltageState : State() {
    var voltage: Double
        get() = state
        set(value) {
            state = value
        }
}
