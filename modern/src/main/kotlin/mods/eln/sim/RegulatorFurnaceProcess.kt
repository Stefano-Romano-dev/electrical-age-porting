package mods.eln.sim

class RegulatorFurnaceProcess(
    name: String,
    val furnace: FurnaceProcess,
) : RegulatorProcess(name) {
    override fun getHit(): Double = furnace.load.temperatureCelsius

    override fun setCmd(cmd: Double) {
        furnace.setGain(cmd)
    }
}
