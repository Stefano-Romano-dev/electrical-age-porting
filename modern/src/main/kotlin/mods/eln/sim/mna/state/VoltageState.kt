package mods.eln.sim.mna.state

class VoltageState : State() {
    var voltage: Double
        get() = state
        set(value) {
            state = value
        }
}
