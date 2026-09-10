package net.electricalage.eln.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.electricalage.eln.blockentity.SixNodeBlockEntity;
import net.electricalage.eln.component.LocalDirection;
import net.electricalage.eln.component.MountFace;
import net.electricalage.eln.registry.ElnItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class SixNodeBlockEntityRenderer implements BlockEntityRenderer<SixNodeBlockEntity> {
    private final ItemRenderer itemRenderer;

    public SixNodeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            SixNodeBlockEntity sixNode,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        sixNode.componentSnapshot().forEach((support, component) -> {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            applySupportRotation(poseStack, support);

            int quarterTurns = Math.floorMod(
                    component.front().ordinal() - LocalDirection.LEFT.ordinal(),
                    LocalDirection.values().length
            );
            poseStack.mulPose(Axis.YP.rotationDegrees(quarterTurns * 90.0F));

            ItemStack stack = new ItemStack(ElnItems.itemFor(component.type()));
            itemRenderer.renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    sixNode.getLevel(),
                    support.ordinal()
            );
            poseStack.popPose();
        });
    }

    private static void applySupportRotation(PoseStack poseStack, MountFace support) {
        switch (support) {
            case DOWN -> {
            }
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(270.0F));
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
            case EAST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
        }
    }
}
