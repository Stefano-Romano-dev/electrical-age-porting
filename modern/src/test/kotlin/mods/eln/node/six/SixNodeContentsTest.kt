package mods.eln.node.six

import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SixNodeContentsTest {
    @Test
    fun `legacy side indexes map explicitly instead of using vanilla ordinal`() {
        val expected = listOf(
            Direction.WEST,
            Direction.EAST,
            Direction.DOWN,
            Direction.UP,
            Direction.NORTH,
            Direction.SOUTH,
        )

        expected.forEachIndexed { index, face ->
            assertEquals(face, SixNodeLegacyFacing.fromLegacyIndex(index))
            assertEquals(index, SixNodeLegacyFacing.toLegacyIndex(face))
        }
        assertNull(SixNodeLegacyFacing.fromLegacyIndex(-1))
        assertNull(SixNodeLegacyFacing.fromLegacyIndex(6))
    }

    @Test
    fun `six independent faces can hold different component identities`() {
        val contents = SixNodeContents()

        Direction.entries.forEachIndexed { index, face ->
            assertTrue(
                contents.mount(
                    face,
                    MountedSixNodeComponent(
                        ResourceLocation.fromNamespaceAndPath("eln", "component_$index"),
                        SixNodeRotation.entries[index % SixNodeRotation.entries.size],
                    ),
                ),
            )
        }

        assertEquals(6, contents.occupiedFaceCount)
        assertEquals(Direction.entries.toSet(), contents.snapshot().keys)
    }

    @Test
    fun `occupied face rejects replacement until explicitly unmounted`() {
        val contents = SixNodeContents()
        val source = MountedSixNodeComponent(SixNodeComponentCatalog.ELECTRICAL_SOURCE.id, SixNodeRotation.UP)
        val cable = MountedSixNodeComponent(SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.id, SixNodeRotation.LEFT)

        assertTrue(contents.mount(Direction.NORTH, source))
        assertFalse(contents.mount(Direction.NORTH, cable))
        assertEquals(source, contents.get(Direction.NORTH))
        assertEquals(source, contents.unmount(Direction.NORTH))
        assertNull(contents.get(Direction.NORTH))
        assertTrue(contents.mount(Direction.NORTH, cable))
    }

    @Test
    fun `rotation only changes an occupied face`() {
        val contents = SixNodeContents()
        assertFalse(contents.rotate(Direction.UP, SixNodeRotation.RIGHT))

        contents.mount(
            Direction.UP,
            MountedSixNodeComponent(SixNodeComponentCatalog.POWER_RESISTOR.id, SixNodeRotation.DOWN),
        )
        assertTrue(contents.rotate(Direction.UP, SixNodeRotation.RIGHT))
        assertEquals(SixNodeRotation.RIGHT, contents.get(Direction.UP)?.rotation)
    }
}
