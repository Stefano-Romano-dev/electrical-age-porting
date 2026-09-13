package mods.eln.node.six

import java.util.EnumMap
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation

enum class SixNodeRotation(val legacyCode: Int) {
    LEFT(0),
    RIGHT(1),
    DOWN(2),
    UP(3),
    ;

    companion object {
        @JvmStatic
        fun fromLegacyCode(code: Int): SixNodeRotation = entries.firstOrNull { it.legacyCode == code } ?: LEFT
    }
}

data class MountedSixNodeComponent(
    val typeId: ResourceLocation,
    val rotation: SixNodeRotation,
)

object SixNodeLegacyFacing {
    private val facesByIndex = arrayOf(
        Direction.WEST,
        Direction.EAST,
        Direction.DOWN,
        Direction.UP,
        Direction.NORTH,
        Direction.SOUTH,
    )

    @JvmStatic
    fun fromLegacyIndex(index: Int): Direction? = facesByIndex.getOrNull(index)

    @JvmStatic
    fun toLegacyIndex(face: Direction): Int = facesByIndex.indexOf(face)
}

class SixNodeContents {
    private val components = EnumMap<Direction, MountedSixNodeComponent>(Direction::class.java)

    val occupiedFaceCount: Int
        get() = components.size

    fun get(face: Direction): MountedSixNodeComponent? = components[face]

    /** Matches the legacy createSubBlock contract: an occupied face rejects the placement. */
    fun mount(face: Direction, component: MountedSixNodeComponent): Boolean {
        if (components.containsKey(face)) return false
        components[face] = component
        return true
    }

    fun unmount(face: Direction): MountedSixNodeComponent? = components.remove(face)

    fun rotate(face: Direction, rotation: SixNodeRotation): Boolean {
        val current = components[face] ?: return false
        components[face] = current.copy(rotation = rotation)
        return true
    }

    fun snapshot(): Map<Direction, MountedSixNodeComponent> = components.toMap()

    internal fun replaceFromPersistence(restored: Map<Direction, MountedSixNodeComponent>) {
        components.clear()
        components.putAll(restored)
    }
}
