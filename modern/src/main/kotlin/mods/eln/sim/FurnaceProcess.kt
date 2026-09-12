package mods.eln.sim

class FurnaceProcess(
    val load: ThermalLoad,
) : IProcess {
    var combustibleEnergy: Double = 0.0
    var nominalCombustibleEnergy: Double = 1.0
    var nominalPower: Double = 1.0
    var gain: Double = 1.0
        private set
    private var gainMin: Double = 0.0

    override fun process(time: Double) {
        val energyConsumed = power * time
        combustibleEnergy -= energyConsumed
        load.PcTemp += energyConsumed / time
    }

    fun setGain(gain: Double) {
        var bounded = gain
        if (bounded < gainMin) bounded = gainMin
        if (bounded > 1.0) bounded = 1.0
        this.gain = bounded
    }

    fun setGainMin(gainMin: Double) {
        this.gainMin = gainMin
        setGain(gain)
    }

    val power: Double
        get() = combustibleEnergy / nominalCombustibleEnergy * nominalPower * gain
}
