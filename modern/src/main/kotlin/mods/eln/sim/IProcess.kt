package mods.eln.sim

/** A time-stepped, platform-independent simulation process. */
fun interface IProcess {
    fun process(time: Double)
}
