package mods.eln.node.six

import net.minecraft.resources.ResourceLocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SixNodeComponentCatalogTest {
    @Test
    fun `foundation ids match the 1 24 8 descriptor damage values`() {
        assertEquals(192, SixNodeComponentCatalog.ELECTRICAL_SOURCE.legacyId)
        assertEquals(2052, SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.legacyId)
        assertEquals(6180, SixNodeComponentCatalog.POWER_RESISTOR.legacyId)

        assertEquals("eln:electrical_source", SixNodeComponentCatalog.ELECTRICAL_SOURCE.id.toString())
        assertEquals("eln:low_voltage_cable", SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.id.toString())
        assertEquals("eln:power_resistor", SixNodeComponentCatalog.POWER_RESISTOR.id.toString())
    }

    @Test
    fun `catalog resolves both identities without conflating them`() {
        val catalog = SixNodeComponentCatalog.FOUNDATION

        catalog.definitions.forEach { definition ->
            assertEquals(definition, catalog.get(definition.id))
            assertEquals(definition, catalog.getByLegacyId(definition.legacyId))
        }
        assertNull(catalog.get(ResourceLocation.fromNamespaceAndPath("eln", "missing")))
        assertNull(catalog.getByLegacyId(-1))
    }

    @Test
    fun `catalog rejects duplicate modern and legacy identities`() {
        val source = SixNodeComponentCatalog.ELECTRICAL_SOURCE

        assertThrows(IllegalArgumentException::class.java) {
            SixNodeComponentCatalog(listOf(source, source.copy(legacyId = 999)))
        }
        assertThrows(IllegalArgumentException::class.java) {
            SixNodeComponentCatalog(
                listOf(
                    source,
                    source.copy(id = ResourceLocation.fromNamespaceAndPath("eln", "another_source")),
                ),
            )
        }
    }

    @Test
    fun `legacy sub id remains a six bit field`() {
        assertEquals(2052, SixNodeComponentCatalog.legacyId(32, 4))
        assertThrows(IllegalArgumentException::class.java) { SixNodeComponentCatalog.legacyId(32, 64) }
        assertThrows(IllegalArgumentException::class.java) { SixNodeComponentCatalog.legacyId(-1, 0) }
    }
}
