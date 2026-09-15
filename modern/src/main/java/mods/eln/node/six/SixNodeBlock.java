package mods.eln.node.six;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Host block for up to six face-mounted Electrical Age components. */
public final class SixNodeBlock extends BaseEntityBlock {
    public static final MapCodec<SixNodeBlock> CODEC = simpleCodec(SixNodeBlock::new);
    private static final Map<Direction, VoxelShape> FACE_SHAPES = Map.of(
            Direction.WEST, Block.box(0.32, 0.0, 0.0, 3.2, 16.0, 16.0),
            Direction.EAST, Block.box(12.8, 0.0, 0.0, 15.68, 16.0, 16.0),
            Direction.DOWN, Block.box(0.0, 0.32, 0.0, 16.0, 3.2, 16.0),
            Direction.UP, Block.box(0.0, 12.8, 0.0, 16.0, 15.68, 16.0),
            Direction.NORTH, Block.box(0.0, 0.0, 0.32, 16.0, 16.0, 3.2),
            Direction.SOUTH, Block.box(0.0, 0.0, 12.8, 16.0, 16.0, 15.68));

    public SixNodeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SixNodeBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!(level.getBlockEntity(pos) instanceof SixNodeBlockEntity host)) return Shapes.empty();
        VoxelShape shape = Shapes.empty();
        for (Direction face : host.contentsSnapshot().keySet()) {
            shape = Shapes.or(shape, FACE_SHAPES.get(face));
        }
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean onDestroyedByPlayer(
            BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof SixNodeBlockEntity host)) return false;

        Direction face = resolveBreakFace(pos, player, host.contentsSnapshot());
        MountedSixNodeComponent removed = face == null ? null : host.unmount(face);
        if (removed == null) return false;
        if (!player.isCreative()) dropComponent(level, pos, removed);
        return host.contentsSnapshot().isEmpty();
    }

    @Override
    protected void neighborChanged(
            BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean moved) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof SixNodeBlockEntity host)) return;

        for (Map.Entry<Direction, MountedSixNodeComponent> entry : host.contentsSnapshot().entrySet()) {
            if (SixNodeSupport.isValid(level, pos, entry.getKey())) continue;
            MountedSixNodeComponent removed = host.unmount(entry.getKey());
            if (removed != null) dropComponent(level, pos, removed);
        }
        if (host.contentsSnapshot().isEmpty()) level.removeBlock(pos, false);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())
                && !level.isClientSide
                && level.getBlockEntity(pos) instanceof SixNodeBlockEntity host) {
            for (MountedSixNodeComponent component : host.contentsSnapshot().values()) {
                dropComponent(level, pos, component);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    private static Direction resolveBreakFace(
            BlockPos pos, Player player, Map<Direction, MountedSixNodeComponent> contents) {
        Vec3 start = new Vec3(player.getX(), player.getY() + 1.62, player.getZ());
        return SixNodeBreakSelection.resolve(pos, start, player.getViewVector(0.5F), contents);
    }

    private static void dropComponent(Level level, BlockPos pos, MountedSixNodeComponent component) {
        ItemStack stack = SixNodeComponentStack.create(component.getTypeId());
        Block.popResource(level, pos, stack);
    }
}
