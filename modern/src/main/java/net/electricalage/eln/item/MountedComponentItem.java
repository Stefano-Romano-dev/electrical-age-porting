package net.electricalage.eln.item;

import net.electricalage.eln.blockentity.SixNodeBlockEntity;
import net.electricalage.eln.component.FaceOrientation;
import net.electricalage.eln.component.LocalDirection;
import net.electricalage.eln.component.MountFace;
import net.electricalage.eln.component.MountedComponent;
import net.electricalage.eln.registry.ElnBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public class MountedComponentItem extends Item {
    private final Function<LocalDirection, MountedComponent> componentFactory;

    public MountedComponentItem(
            Properties properties,
            Function<LocalDirection, MountedComponent> componentFactory
    ) {
        super(properties);
        this.componentFactory = componentFactory;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPosition = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockState clickedState = level.getBlockState(clickedPosition);

        BlockPos hostPosition;
        Direction supportDirection;
        if (clickedState.is(ElnBlocks.SIX_NODE.get())) {
            hostPosition = clickedPosition;
            supportDirection = clickedFace.getOpposite();
        } else {
            hostPosition = clickedPosition.relative(clickedFace);
            supportDirection = clickedFace.getOpposite();
            BlockState hostState = level.getBlockState(hostPosition);
            if (!hostState.is(ElnBlocks.SIX_NODE.get()) && !hostState.canBeReplaced()) {
                return InteractionResult.FAIL;
            }
        }

        BlockPos supportPosition = hostPosition.relative(supportDirection);
        if (!level.getBlockState(supportPosition)
                .isFaceSturdy(level, supportPosition, supportDirection.getOpposite())) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!level.getBlockState(hostPosition).is(ElnBlocks.SIX_NODE.get())) {
            level.setBlock(hostPosition, ElnBlocks.SIX_NODE.get().defaultBlockState(), 3);
        }

        LocalDirection front = placementFront(supportDirection, context.getHorizontalDirection());
        if (!(level.getBlockEntity(hostPosition) instanceof SixNodeBlockEntity sixNode)
                || !sixNode.addComponent(supportDirection, componentFactory.apply(front))) {
            return InteractionResult.FAIL;
        }

        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static LocalDirection placementFront(Direction supportDirection, Direction playerDirection) {
        if (supportDirection.getAxis().isHorizontal()) {
            return LocalDirection.LEFT;
        }

        LocalDirection facing = FaceOrientation.fromWorld(asMountFace(supportDirection), asMountFace(playerDirection));
        if (supportDirection == Direction.UP) {
            facing = facing.opposite();
        }
        return facing.counterClockwise();
    }

    private static MountFace asMountFace(Direction direction) {
        return MountFace.valueOf(direction.name());
    }
}
