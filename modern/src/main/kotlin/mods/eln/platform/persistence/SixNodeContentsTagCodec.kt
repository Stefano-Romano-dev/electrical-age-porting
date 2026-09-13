package mods.eln.platform.persistence

import java.util.EnumMap
import mods.eln.node.six.MountedSixNodeComponent
import mods.eln.node.six.SixNodeContents
import mods.eln.node.six.SixNodeRotation
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation

object SixNodeContentsTagCodec {
    const val FORMAT_VERSION = 1

    private const val VERSION_KEY = "formatVersion"
    private const val FACES_KEY = "faces"
    private const val TYPE_KEY = "type"
    private const val ROTATION_KEY = "rotation"

    fun write(tag: CompoundTag, contents: SixNodeContents) {
        tag.putInt(VERSION_KEY, FORMAT_VERSION)
        val faces = CompoundTag()
        contents.snapshot().forEach { (face, component) ->
            val faceTag = CompoundTag()
            faceTag.putString(TYPE_KEY, component.typeId.toString())
            faceTag.putByte(ROTATION_KEY, component.rotation.legacyCode.toByte())
            faces.put(face.serializedName, faceTag)
        }
        tag.put(FACES_KEY, faces)
    }

    /** Returns false and leaves current contents untouched for an unsupported schema. */
    fun read(tag: CompoundTag, contents: SixNodeContents): Boolean {
        if (tag.getInt(VERSION_KEY) != FORMAT_VERSION) return false

        val faces = tag.getCompound(FACES_KEY)
        val restored = EnumMap<Direction, MountedSixNodeComponent>(Direction::class.java)
        Direction.entries.forEach { face ->
            if (!faces.contains(face.serializedName)) return@forEach
            val faceTag = faces.getCompound(face.serializedName)
            val typeId = ResourceLocation.tryParse(faceTag.getString(TYPE_KEY)) ?: return@forEach
            val rotation = SixNodeRotation.fromLegacyCode(faceTag.getByte(ROTATION_KEY).toInt())
            restored[face] = MountedSixNodeComponent(typeId, rotation)
        }
        contents.replaceFromPersistence(restored)
        return true
    }
}
