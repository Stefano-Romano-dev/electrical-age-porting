package mods.eln.node.six;

import mods.eln.registry.ElnContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Typed SixNode component item, beginning with the legacy low-voltage cable placement path. */
public final class SixNodeComponentItem extends Item {
    public SixNodeComponentItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        SixNodeComponentDefinition definition =
                SixNodeComponentStack.definition(stack, SixNodeComponentCatalog.FOUNDATION);
        if (definition == null) return super.getName(stack);
        return Component.translatable("item.eln." + definition.getId().getPath());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        SixNodeComponentDefinition definition =
                SixNodeComponentStack.definition(stack, SixNodeComponentCatalog.FOUNDATION);

        if (player == null || definition == null) {
            return InteractionResult.FAIL;
        }

        Direction clickedFace = context.getClickedFace();
        BlockPos clickedPos = context.getClickedPos();
        BlockPos targetPos = level.getBlockState(clickedPos).canBeReplaced()
                ? clickedPos
                : clickedPos.relative(clickedFace);
        Direction mountedFace = clickedFace.getOpposite();

        if (level.isOutsideBuildHeight(targetPos) || !player.mayUseItemAt(targetPos, clickedFace, stack)) {
            return InteractionResult.FAIL;
        }

        BlockState targetState = level.getBlockState(targetPos);
        BlockEntity targetBlockEntity = level.getBlockEntity(targetPos);
        SixNodeBlockEntity existingHost = targetBlockEntity instanceof SixNodeBlockEntity sixNode ? sixNode : null;
        boolean creatingHost = existingHost == null;

        if (creatingHost && !targetState.canBeReplaced()) return InteractionResult.FAIL;
        if (!creatingHost && existingHost.contentsSnapshot().containsKey(mountedFace)) {
            return InteractionResult.FAIL;
        }

        if (!SixNodeSupport.isValid(level, targetPos, mountedFace)) return InteractionResult.FAIL;

        if (level.isClientSide) return InteractionResult.SUCCESS;

        SixNodeRotation rotation = SixNodePlacementOrientation.fromPlacement(mountedFace, player.getDirection());
        if (SixNodeComponentCatalog.POWER_RESISTOR.equals(definition)) rotation = rotation.left();
        MountedSixNodeComponent component = new MountedSixNodeComponent(definition.getId(), rotation);

        SixNodeBlockEntity host = existingHost;
        if (creatingHost) {
            if (!level.setBlock(targetPos, ElnContent.SIX_NODE.get().defaultBlockState(), Block.UPDATE_ALL)) {
                return InteractionResult.FAIL;
            }
            BlockEntity created = level.getBlockEntity(targetPos);
            if (!(created instanceof SixNodeBlockEntity sixNode)) {
                level.setBlock(targetPos, targetState, Block.UPDATE_ALL);
                return InteractionResult.FAIL;
            }
            host = sixNode;
        }

        if (!host.mount(mountedFace, component)) {
            if (creatingHost) level.setBlock(targetPos, targetState, Block.UPDATE_ALL);
            return InteractionResult.FAIL;
        }

        if (!player.isCreative()) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
