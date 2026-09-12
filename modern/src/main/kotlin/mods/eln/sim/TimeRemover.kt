package mods.eln.sim

class TimeRemover(
    val observer: ITimeRemoverObserver,
    private val simulator: Simulator,
) : IProcess {
    var timeout: Double = 0.0
        private set

    fun setTimeout(timeout: Double) {
        if (this.timeout <= 0.0) {
            observer.timeRemoverAdd()
            simulator.addSlowProcess(this)
        }
        this.timeout = timeout
    }

    override fun process(time: Double) {
        if (isArmed) {
            timeout -= time
            if (timeout <= 0.0) shot()
        }
    }

    val isArmed: Boolean
        get() = timeout > 0.0

    fun shot() {
        timeout = 0.0
        observer.timeRemoverRemove()
        simulator.removeSlowProcess(this)
    }
}
