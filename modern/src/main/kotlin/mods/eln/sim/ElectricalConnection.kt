package mods.eln.sim

import mods.eln.sim.mna.component.InterSystem

class ElectricalConnection(
    val L1: ElectricalLoad,
    val L2: ElectricalLoad,
) : InterSystem() {
    fun notifyRsChange() {
        setResistance(L1.serialResistance + L2.serialResistance)
    }

    override fun onAddToRootSystem() {
        connectTo(L1, L2)
        notifyRsChange()
    }

    override fun onRemoveFromRootSystem() {
        breakConnection()
    }
}
