package mods.eln.node.six;

import java.util.Map;
import java.util.HashMap;
import mods.eln.ElectricalAge;
import mods.eln.platform.persistence.SixNodeContentsTagCodec;
import mods.eln.registry.ElnContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent world owner of the pure six-face contents model. */
public final class SixNodeBlockEntity extends BlockEntity {
    private final SixNodeContents contents = new SixNodeContents();

    public SixNodeBlockEntity(BlockPos pos, BlockState state) {
        super(ElnContent.SIX_NODE_BLOCK_ENTITY.get(), pos, state);
    }

    public Map<Direction, MountedSixNodeComponent> contentsSnapshot() {
        return contents.snapshot();
    }

    public boolean mount(Direction face, MountedSixNodeComponent component) {
        if (!contents.mount(face, component)) return false;
        contentsChanged();
        return true;
    }

    public MountedSixNodeComponent unmount(Direction face) {
        MountedSixNodeComponent removed = contents.unmount(face);
        if (removed != null) contentsChanged();
        return removed;
    }

    public boolean rotate(Direction face, SixNodeRotation rotation) {
        if (!contents.rotate(face, rotation)) return false;
        contentsChanged();
        return true;
    }

    public boolean setElectricalSourceVoltage(Direction face, double voltage) {
        if (!Double.isFinite(voltage)) return false;
        MountedSixNodeComponent current = contents.get(face);
        if (current == null || !current.getTypeId().equals(SixNodeComponentCatalog.ELECTRICAL_SOURCE.getId())) {
            return false;
        }
        Map<String, Double> parameters = new HashMap<>(current.getParameters());
        parameters.put(SixNodeElectricalGraph.VOLTAGE_PARAMETER, voltage);
        if (!contents.replace(face, new MountedSixNodeComponent(
                current.getTypeId(), current.getRotation(), Map.copyOf(parameters)))) return false;
        contentsChanged();
        return true;
    }

    private void contentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            var lifecycle = ElectricalAge.simulationLifecycle();
            if (lifecycle != null) lifecycle.update(this);
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            var lifecycle = ElectricalAge.simulationLifecycle();
            if (lifecycle != null) lifecycle.attach(this);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            var lifecycle = ElectricalAge.simulationLifecycle();
            if (lifecycle != null) lifecycle.detach(this);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        SixNodeContentsTagCodec.INSTANCE.write(tag, contents);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        SixNodeContentsTagCodec.INSTANCE.read(tag, contents);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
