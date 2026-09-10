package net.electricalage.eln.block;

import com.mojang.serialization.MapCodec;
import net.electricalage.eln.blockentity.SixNodeBlockEntity;
import net.electricalage.eln.registry.ElnItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public final class SixNodeBlock extends Block implements EntityBlock {
    public static final MapCodec<SixNodeBlock> CODEC = simpleCodec(SixNodeBlock::new);

    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");

    private static final Map<Direction, BooleanProperty> FACE_PROPERTIES = createFaceProperties();
    private static final Map<Direction, VoxelShape> FACE_SHAPES = createFaceShapes();

    public SixNodeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(DOWN, false)
                .setValue(UP, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new SixNodeBlockEntity(position, state);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        VoxelShape shape = Shapes.empty();
        for (Direction direction : Direction.values()) {
            if (state.getValue(propertyFor(direction))) {
                shape = Shapes.or(shape, FACE_SHAPES.get(direction));
            }
        }
        return shape.isEmpty() ? Shapes.block() : shape;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Direction supportDirection = hitResult.getDirection().getOpposite();
        if (!(level.getBlockEntity(position) instanceof SixNodeBlockEntity sixNode)) {
            return InteractionResult.PASS;
        }

        var component = sixNode.componentAt(supportDirection);
        if (component.isEmpty()) return InteractionResult.PASS;

        if (!level.isClientSide) {
            sixNode.removeComponent(supportDirection);
            popResource(level, position, new ItemStack(ElnItems.itemFor(component.get().type())));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos position, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()
                && level.getBlockEntity(position) instanceof SixNodeBlockEntity sixNode) {
            sixNode.componentSnapshot().values().forEach(component ->
                    popResource(level, position, new ItemStack(ElnItems.itemFor(component.type()))));
        }
        return super.playerWillDestroy(level, position, state, player);
    }

    public static BooleanProperty propertyFor(Direction direction) {
        return FACE_PROPERTIES.get(direction);
    }

    private static Map<Direction, BooleanProperty> createFaceProperties() {
        Map<Direction, BooleanProperty> properties = new EnumMap<>(Direction.class);
        properties.put(Direction.DOWN, DOWN);
        properties.put(Direction.UP, UP);
        properties.put(Direction.NORTH, NORTH);
        properties.put(Direction.SOUTH, SOUTH);
        properties.put(Direction.WEST, WEST);
        properties.put(Direction.EAST, EAST);
        return Map.copyOf(properties);
    }

    private static Map<Direction, VoxelShape> createFaceShapes() {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.DOWN, Block.box(1, 0, 3, 15, 9, 13));
        shapes.put(Direction.UP, Block.box(1, 7, 3, 15, 16, 13));
        shapes.put(Direction.NORTH, Block.box(1, 3, 0, 15, 13, 9));
        shapes.put(Direction.SOUTH, Block.box(1, 3, 7, 15, 13, 16));
        shapes.put(Direction.WEST, Block.box(0, 3, 1, 9, 13, 15));
        shapes.put(Direction.EAST, Block.box(7, 3, 1, 16, 13, 15));
        return Map.copyOf(shapes);
    }
}
