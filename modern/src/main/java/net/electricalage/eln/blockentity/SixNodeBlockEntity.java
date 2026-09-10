package net.electricalage.eln.blockentity;

import net.electricalage.eln.block.SixNodeBlock;
import net.electricalage.eln.component.LocalDirection;
import net.electricalage.eln.component.MountedComponent;
import net.electricalage.eln.component.MountedComponentType;
import net.electricalage.eln.component.MountFace;
import net.electricalage.eln.registry.ElnBlockEntities;
import net.electricalage.eln.network.ServerElectricalNetworks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class SixNodeBlockEntity extends BlockEntity {
    private static final int DATA_VERSION = 1;
    private static final String COMPONENT_PREFIX = "component_";

    private final Map<Direction, MountedComponent> components = new EnumMap<>(Direction.class);

    public SixNodeBlockEntity(BlockPos position, BlockState state) {
        super(ElnBlockEntities.SIX_NODE.get(), position, state);
    }

    public boolean addResistor(Direction supportDirection, LocalDirection front) {
        return addComponent(supportDirection, MountedComponent.resistor(front));
    }

    public boolean addComponent(Direction supportDirection, MountedComponent component) {
        if (components.containsKey(supportDirection)) {
            return false;
        }
        components.put(supportDirection, component);
        updateStateAndNotify();
        return true;
    }

    public boolean removeResistor(Direction supportDirection) {
        if (!hasResistor(supportDirection)) {
            return false;
        }
        return removeComponent(supportDirection);
    }

    public boolean removeComponent(Direction supportDirection) {
        if (components.remove(supportDirection) == null) {
            return false;
        }

        if (level != null && components.isEmpty()) {
            level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), 3);
        } else {
            updateStateAndNotify();
        }
        return true;
    }

    public boolean hasResistor(Direction supportDirection) {
        return componentAt(supportDirection)
                .map(component -> component.type() == MountedComponentType.RESISTOR)
                .orElse(false);
    }

    public int resistorCount() {
        return (int) components.values().stream()
                .filter(component -> component.type() == MountedComponentType.RESISTOR)
                .count();
    }

    public Optional<MountedComponent> componentAt(Direction supportDirection) {
        return Optional.ofNullable(components.get(supportDirection));
    }

    public Map<MountFace, MountedComponent> componentSnapshot() {
        Map<MountFace, MountedComponent> snapshot = new EnumMap<>(MountFace.class);
        components.forEach((direction, component) ->
                snapshot.put(MountFace.valueOf(direction.name()), component));
        return Map.copyOf(snapshot);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateWorldNetwork();
    }

    @Override
    public void setRemoved() {
        if (level != null) {
            ServerElectricalNetworks.remove(level, worldPosition);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("component_data_version", DATA_VERSION);
        for (Map.Entry<Direction, MountedComponent> entry : components.entrySet()) {
            MountedComponent component = entry.getValue();
            CompoundTag componentTag = new CompoundTag();
            componentTag.putString("type", component.type().id());
            componentTag.putInt("front", component.front().ordinal());
            componentTag.putDouble("resistance_ohms", component.resistanceOhms());
            componentTag.putDouble("voltage_volts", component.voltageVolts());
            tag.put(COMPONENT_PREFIX + entry.getKey().getName(), componentTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        components.clear();
        for (Direction direction : Direction.values()) {
            CompoundTag componentTag = tag.getCompound(COMPONENT_PREFIX + direction.getName());
            MountedComponentType.fromId(componentTag.getString("type")).ifPresent(type -> {
                LocalDirection front = LocalDirection.fromOrdinal(componentTag.getInt("front"));
                double resistance = componentTag.getDouble("resistance_ohms");
                double voltage = componentTag.getDouble("voltage_volts");
                try {
                    components.put(direction, new MountedComponent(type, front, resistance, voltage));
                } catch (IllegalArgumentException ignored) {
                    // Ignore malformed component data instead of invalidating the whole SixNode.
                }
            });
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }
    }

    private void updateStateAndNotify() {
        setChanged();
        if (level == null) {
            return;
        }

        BlockState oldState = getBlockState();
        BlockState newState = oldState;
        for (Direction direction : Direction.values()) {
            newState = newState.setValue(SixNodeBlock.propertyFor(direction), components.containsKey(direction));
        }
        level.setBlock(worldPosition, newState, 3);
        level.sendBlockUpdated(worldPosition, oldState, newState, 3);
        updateWorldNetwork();
    }

    private void updateWorldNetwork() {
        if (level != null) {
            ServerElectricalNetworks.update(level, worldPosition, componentSnapshot());
        }
    }
}
