package mods.eln.platform.persistence

import mods.eln.node.six.MountedSixNodeComponent
import mods.eln.node.six.SixNodeComponentCatalog
import mods.eln.node.six.SixNodeContents
import mods.eln.node.six.SixNodeRotation
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SixNodeContentsTagCodecTest {
    @Test
    fun `modern six node shell round trips face type and legacy rotation`() {
        val contents = SixNodeContents().apply {
            mount(
                Direction.WEST,
                MountedSixNodeComponent(SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.id, SixNodeRotation.LEFT),
            )
            mount(
                Direction.UP,
                MountedSixNodeComponent(SixNodeComponentCatalog.POWER_RESISTOR.id, SixNodeRotation.DOWN),
            )
            mount(
                Direction.SOUTH,
                MountedSixNodeComponent(SixNodeComponentCatalog.ELECTRICAL_SOURCE.id, SixNodeRotation.UP),
            )
        }
        val tag = CompoundTag()

        SixNodeContentsTagCodec.write(tag, contents)

        assertEquals(1, tag.getInt("formatVersion"))
        val faces = tag.getCompound("faces")
        assertEquals("eln:low_voltage_cable", faces.getCompound("west").getString("type"))
        assertEquals(0, faces.getCompound("west").getByte("rotation").toInt())
        assertEquals(2, faces.getCompound("up").getByte("rotation").toInt())
        assertEquals(3, faces.getCompound("south").getByte("rotation").toInt())

        val restored = SixNodeContents()
        assertTrue(SixNodeContentsTagCodec.read(tag, restored))
        assertEquals(contents.snapshot(), restored.snapshot())
    }

    @Test
    fun `unknown valid type survives persistence for forward compatibility`() {
        val unknown = ResourceLocation.fromNamespaceAndPath("eln", "future_component")
        val source = SixNodeContents().apply {
            mount(Direction.DOWN, MountedSixNodeComponent(unknown, SixNodeRotation.RIGHT))
        }
        val tag = CompoundTag()
        SixNodeContentsTagCodec.write(tag, source)

        val restored = SixNodeContents()
        assertTrue(SixNodeContentsTagCodec.read(tag, restored))
        assertEquals(unknown, restored.get(Direction.DOWN)?.typeId)
    }

    @Test
    fun `unsupported schema cannot erase live contents`() {
        val contents = SixNodeContents().apply {
            mount(
                Direction.NORTH,
                MountedSixNodeComponent(SixNodeComponentCatalog.ELECTRICAL_SOURCE.id, SixNodeRotation.UP),
            )
        }
        val tag = CompoundTag().apply { putInt("formatVersion", 99) }
        assertFalse(SixNodeContentsTagCodec.read(tag, contents))
        assertEquals(SixNodeComponentCatalog.ELECTRICAL_SOURCE.id, contents.get(Direction.NORTH)?.typeId)
    }

    @Test
    fun `invalid rotation follows legacy LRDU fallback to left`() {
        val face = CompoundTag().apply {
            putString("type", SixNodeComponentCatalog.POWER_RESISTOR.id.toString())
            putByte("rotation", 127)
        }
        val tag = CompoundTag().apply {
            putInt("formatVersion", SixNodeContentsTagCodec.FORMAT_VERSION)
            put("faces", CompoundTag().apply { put("east", face) })
        }
        val restored = SixNodeContents()

        assertTrue(SixNodeContentsTagCodec.read(tag, restored))
        assertEquals(SixNodeRotation.LEFT, restored.get(Direction.EAST)?.rotation)
    }
}
