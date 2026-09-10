package net.electricalage.eln.network;

import net.electricalage.eln.component.MountFace;
import net.electricalage.eln.component.MountedComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.WeakHashMap;

/** Owns the live electrical network of each loaded server dimension. */
public final class ServerElectricalNetworks {
    private static final Map<ServerLevel, WorldElectricalNetwork> BY_LEVEL = new WeakHashMap<>();

    private ServerElectricalNetworks() {
    }

    public static void update(
            Level level,
            BlockPos position,
            Map<MountFace, MountedComponent> components
    ) {
        if (level instanceof ServerLevel serverLevel) {
            network(serverLevel).update(asGridPosition(position), components);
        }
    }

    public static void remove(Level level, BlockPos position) {
        if (level instanceof ServerLevel serverLevel) {
            WorldElectricalNetwork network = BY_LEVEL.get(serverLevel);
            if (network != null) {
                network.remove(asGridPosition(position));
            }
        }
    }

    public static WorldElectricalNetwork network(ServerLevel level) {
        return BY_LEVEL.computeIfAbsent(level, ignored -> new WorldElectricalNetwork());
    }

    private static GridPosition asGridPosition(BlockPos position) {
        return new GridPosition(position.getX(), position.getY(), position.getZ());
    }
}
