package mods.eln.node.six

import mods.eln.PortingBaseline
import net.minecraft.resources.ResourceLocation

data class SixNodeComponentDefinition(
    val id: ResourceLocation,
    val legacyId: Int,
    val legacyName: String,
    val legacyAssets: List<String>,
)

class SixNodeComponentCatalog(definitions: Iterable<SixNodeComponentDefinition>) {
    private val definitionsById: Map<ResourceLocation, SixNodeComponentDefinition>
    private val definitionsByLegacyId: Map<Int, SixNodeComponentDefinition>

    val definitions: List<SixNodeComponentDefinition>

    init {
        this.definitions = definitions.toList()
        definitionsById = this.definitions.associateBy { it.id }
        definitionsByLegacyId = this.definitions.associateBy { it.legacyId }
        require(definitionsById.size == this.definitions.size) { "Duplicate SixNode component resource id" }
        require(definitionsByLegacyId.size == this.definitions.size) { "Duplicate SixNode component legacy id" }
    }

    fun get(id: ResourceLocation): SixNodeComponentDefinition? = definitionsById[id]

    fun getByLegacyId(legacyId: Int): SixNodeComponentDefinition? = definitionsByLegacyId[legacyId]

    companion object {
        private fun eln(path: String): ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(PortingBaseline.MOD_ID, path)

        @JvmField
        val ELECTRICAL_SOURCE = SixNodeComponentDefinition(
            id = eln("electrical_source"),
            legacyId = legacyId(group = 3, subId = 0),
            legacyName = "Electrical Source",
            legacyAssets = listOf("model/voltagesource/voltagesource.obj", "model/voltagesource/voltagesource.png"),
        )

        @JvmField
        val LOW_VOLTAGE_CABLE = SixNodeComponentDefinition(
            id = eln("low_voltage_cable"),
            legacyId = legacyId(group = 32, subId = 4),
            legacyName = "Low Voltage Cable",
            legacyAssets = listOf("sprites/cable.png"),
        )

        @JvmField
        val POWER_RESISTOR = SixNodeComponentDefinition(
            id = eln("power_resistor"),
            legacyId = legacyId(group = 96, subId = 36),
            legacyName = "Power Resistor",
            legacyAssets = listOf(
                "model/PowerElectricPrimitives/PowerElectricPrimitives.obj",
                "model/PowerElectricPrimitives/PowerElectricPrimitives.png",
                "textures/blocks/powerresistor.png",
            ),
        )

        @JvmField
        val FOUNDATION = SixNodeComponentCatalog(
            listOf(ELECTRICAL_SOURCE, LOW_VOLTAGE_CABLE, POWER_RESISTOR),
        )

        @JvmStatic
        fun legacyId(group: Int, subId: Int): Int {
            require(group in 0..0x1FF) { "Legacy SixNode group must fit the historical shifted id" }
            require(subId in 0..0x3F) { "Legacy SixNode sub-id must fit six bits" }
            return (group shl 6) + subId
        }
    }
}
