package mods.eln.sim.mna

class SubSystemDebugSnapshot(
    val conductanceMatrix: Array<DoubleArray>,
    val rhsVector: DoubleArray,
    val stateLabels: Array<String>,
    val stateOwners: Array<String?>,
    val componentLabels: Array<String>,
    val componentOwners: Array<String?>,
    val componentConnections: Array<IntArray>,
    val isSingular: Boolean,
)
