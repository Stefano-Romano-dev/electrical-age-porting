package mods.eln.node.six;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/** Exact modern boundary for the legacy non-air and opaque support rule. */
public final class SixNodeSupport {
    private SixNodeSupport() {}

    public static boolean isValid(LevelReader level, BlockPos hostPos, Direction mountedFace) {
        BlockPos supportPos = hostPos.relative(mountedFace);
        BlockState supportState = level.getBlockState(supportPos);
        return !supportState.isAir() && supportState.isSolidRender(level, supportPos);
    }
}
