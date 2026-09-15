package mods.eln.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Map;
import mods.eln.PortingBaseline;
import mods.eln.node.six.MountedSixNodeComponent;
import mods.eln.node.six.SixNodeBlockEntity;
import mods.eln.node.six.SixNodeComponentCatalog;
import mods.eln.node.six.SixNodeElectricalGraph;
import mods.eln.node.six.SixNodeRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Renders the first canonical SixNode slice with the original face and LRDU transforms. */
public final class SixNodeBlockEntityRenderer implements BlockEntityRenderer<SixNodeBlockEntity> {
    public static final ModelResourceLocation ELECTRICAL_SOURCE_MODEL = standalone("six_node/electrical_source");
    public static final ModelResourceLocation POWER_RESISTOR_MODEL = standalone("six_node/power_resistor");

    private static final ResourceLocation CABLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(PortingBaseline.MOD_ID, "textures/six_node/cable.png");
    private static final float CABLE_HALF_WIDTH = (1.95F / 16.0F) / 2.0F;
    private static final float CABLE_HEIGHT = 0.95F / 16.0F;
    private static final float NODE_HALF_WIDTH = CABLE_HALF_WIDTH + 1.0F / 16.0F;
    private static final float NODE_HEIGHT = CABLE_HEIGHT + 1.0F / 16.0F;
    private static final float DISCONNECTED_ARM_LENGTH = CABLE_HALF_WIDTH + 3.0F / 16.0F;
    private static final int DEFAULT_CABLE_TINT = 51;

    private final BlockRenderDispatcher blockRenderer;

    public SixNodeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(
            SixNodeBlockEntity host,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay) {
        for (Map.Entry<Direction, MountedSixNodeComponent> entry : host.contentsSnapshot().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList()) {
            poseStack.pushPose();
            applyLegacyFaceTransform(poseStack, entry.getKey());
            renderComponent(host, entry.getKey(), entry.getValue(), poseStack, buffers, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private void renderComponent(
            SixNodeBlockEntity host,
            Direction face,
            MountedSixNodeComponent component,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay) {
        if (component.getTypeId().equals(SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId())) {
            renderCable(host, face, component, poseStack, buffers, packedLight, packedOverlay);
            return;
        }

        if (component.getTypeId().equals(SixNodeComponentCatalog.ELECTRICAL_SOURCE.getId())) {
            renderConnectedCableArms(host, face, component, poseStack, buffers, packedLight, packedOverlay);
            rotateLrdu(poseStack, component.getRotation());
            renderBakedModel(ELECTRICAL_SOURCE_MODEL, host, poseStack, buffers, packedLight, packedOverlay);
            return;
        }

        if (component.getTypeId().equals(SixNodeComponentCatalog.POWER_RESISTOR.getId())) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            rotateLrdu(poseStack, component.getRotation());
            renderBakedModel(POWER_RESISTOR_MODEL, host, poseStack, buffers, packedLight, packedOverlay);
        }
    }

    private void renderBakedModel(
            ModelResourceLocation location,
            SixNodeBlockEntity host,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(location);
        RandomSource random = RandomSource.create(42L);
        for (RenderType renderType : model.getRenderTypes(host.getBlockState(), random, ModelData.EMPTY)) {
            blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(),
                    buffers.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false)),
                    host.getBlockState(),
                    model,
                    1.0F,
                    1.0F,
                    1.0F,
                    packedLight,
                    packedOverlay,
                    ModelData.EMPTY,
                    renderType);
        }
    }

    private static void renderCable(
            SixNodeBlockEntity host,
            Direction face,
            MountedSixNodeComponent component,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(CABLE_TEXTURE));
        List<SixNodeRotation> connections = connectedRotations(host, face, component);

        if (drawNode(connections)) {
            renderBox(
                    poseStack,
                    consumer,
                    0.0F,
                    NODE_HEIGHT,
                    -NODE_HALF_WIDTH,
                    NODE_HALF_WIDTH,
                    -NODE_HALF_WIDTH,
                    NODE_HALF_WIDTH,
                    0.5F,
                    1.0F,
                    255,
                    packedLight,
                    packedOverlay);
        }

        if (connections.isEmpty()) {
            for (SixNodeRotation rotation : SixNodeRotation.values()) {
                renderCableArm(rotation, DISCONNECTED_ARM_LENGTH, poseStack, consumer, packedLight, packedOverlay);
            }
        } else {
            for (SixNodeRotation rotation : connections) {
                Direction edge = SixNodeElectricalGraph.worldDirection(face, rotation);
                renderCableArm(
                        rotation,
                        connectedArmLength(host, face, component, edge),
                        poseStack,
                        consumer,
                        packedLight,
                        packedOverlay);
            }
        }
    }

    private static void renderConnectedCableArms(
            SixNodeBlockEntity host,
            Direction face,
            MountedSixNodeComponent component,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(CABLE_TEXTURE));
        for (SixNodeRotation rotation : connectedRotations(host, face, component)) {
            Direction edge = SixNodeElectricalGraph.worldDirection(face, rotation);
            renderCableArm(
                    rotation,
                    connectedArmLength(host, face, component, edge),
                    poseStack,
                    consumer,
                    packedLight,
                    packedOverlay);
        }
    }

    private static List<SixNodeRotation> connectedRotations(
            SixNodeBlockEntity host,
            Direction face,
            MountedSixNodeComponent component) {
        return List.of(
                        SixNodeRotation.LEFT,
                        SixNodeRotation.RIGHT,
                        SixNodeRotation.DOWN,
                        SixNodeRotation.UP)
                .stream()
                .filter(rotation -> isConnected(host, face, component, SixNodeElectricalGraph.worldDirection(face, rotation)))
                .toList();
    }

    private static float connectedArmLength(
            SixNodeBlockEntity host,
            Direction face,
            MountedSixNodeComponent component,
            Direction edge) {
        Level level = host.getLevel();
        if (level == null || !SixNodeElectricalGraph.supportsTerminal(face, component, edge)) return 0.5F;
        BlockPos pos = host.getBlockPos();
        if (hasTerminal(level, pos.relative(edge), face, edge.getOpposite())) return 0.5F;
        if (hasTerminal(level, pos, edge, face)) {
            return legacyInternalArmLength(face, edge);
        }
        if (hasTerminal(level, pos.relative(face).relative(edge), edge.getOpposite(), face.getOpposite())) {
            return legacyOuterCornerArmLength(face, edge);
        }
        return 0.5F;
    }

    static float legacyInternalArmLength(Direction face, Direction edge) {
        return legacyFaceIndex(face) > legacyFaceIndex(edge) ? 0.5F - CABLE_HEIGHT : 0.5F;
    }

    static float legacyOuterCornerArmLength(Direction face, Direction edge) {
        return legacyFaceIndex(edge) > legacyFaceIndex(face.getOpposite()) ? 0.5F + CABLE_HEIGHT : 0.5F;
    }

    private static int legacyFaceIndex(Direction direction) {
        return switch (direction) {
            case WEST -> 0;
            case EAST -> 1;
            case DOWN -> 2;
            case UP -> 3;
            case NORTH -> 4;
            case SOUTH -> 5;
        };
    }

    /** Mirrors CableRender.drawNode: no cap on a straight two-way cable. */
    static boolean drawNode(List<SixNodeRotation> connections) {
        if (connections.size() <= 1) return true;
        boolean horizontal = connections.contains(SixNodeRotation.LEFT)
                || connections.contains(SixNodeRotation.RIGHT);
        boolean vertical = connections.contains(SixNodeRotation.DOWN)
                || connections.contains(SixNodeRotation.UP);
        return horizontal && vertical;
    }

    private static boolean isConnected(
            SixNodeBlockEntity host,
            Direction face,
            MountedSixNodeComponent component,
            Direction edge) {
        if (!SixNodeElectricalGraph.supportsTerminal(face, component, edge)) return false;
        Level level = host.getLevel();
        if (level == null) return false;
        BlockPos pos = host.getBlockPos();
        return hasTerminal(level, pos.relative(edge), face, edge.getOpposite())
                || hasTerminal(level, pos, edge, face)
                || hasTerminal(level, pos.relative(face).relative(edge), edge.getOpposite(), face.getOpposite());
    }

    private static boolean hasTerminal(Level level, BlockPos pos, Direction face, Direction edge) {
        if (!(level.getBlockEntity(pos) instanceof SixNodeBlockEntity other)) return false;
        MountedSixNodeComponent component = other.contentsSnapshot().get(face);
        return component != null && SixNodeElectricalGraph.supportsTerminal(face, component, edge);
    }

    private static void renderCableArm(
            SixNodeRotation direction,
            float length,
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay) {
        float y0 = -CABLE_HALF_WIDTH;
        float y1 = CABLE_HALF_WIDTH;
        float z0 = -CABLE_HALF_WIDTH;
        float z1 = CABLE_HALF_WIDTH;
        switch (direction) {
            case LEFT -> z0 = -length;
            case RIGHT -> z1 = length;
            case DOWN -> y0 = -length;
            case UP -> y1 = length;
        }
        renderBox(
                poseStack,
                consumer,
                0.0F,
                CABLE_HEIGHT,
                y0,
                y1,
                z0,
                z1,
                0.0F,
                0.5F,
                DEFAULT_CABLE_TINT,
                packedLight,
                packedOverlay);
    }

    private static void renderBox(
            PoseStack poseStack,
            VertexConsumer consumer,
            float x0,
            float x1,
            float y0,
            float y1,
            float z0,
            float z1,
            float u0,
            float u1,
            int tint,
            int light,
            int overlay) {
        quad(poseStack, consumer, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, 1, 0, 0, u0, u1, tint, light, overlay);
        quad(poseStack, consumer, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0, -1, 0, 0, u0, u1, tint, light, overlay);
        quad(poseStack, consumer, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, 0, 1, 0, u0, u1, tint, light, overlay);
        quad(poseStack, consumer, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, 0, -1, 0, u0, u1, tint, light, overlay);
        quad(poseStack, consumer, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, 0, 0, 1, u0, u1, tint, light, overlay);
        quad(poseStack, consumer, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, 0, 0, -1, u0, u1, tint, light, overlay);
    }

    private static void quad(
            PoseStack poseStack,
            VertexConsumer consumer,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3,
            float nx,
            float ny,
            float nz,
            float u0,
            float u1,
            int tint,
            int light,
            int overlay) {
        vertex(consumer, poseStack.last(), x0, y0, z0, u0, 0.0F, nx, ny, nz, tint, light, overlay);
        vertex(consumer, poseStack.last(), x1, y1, z1, u0, 1.0F, nx, ny, nz, tint, light, overlay);
        vertex(consumer, poseStack.last(), x2, y2, z2, u1, 1.0F, nx, ny, nz, tint, light, overlay);
        vertex(consumer, poseStack.last(), x3, y3, z3, u1, 0.0F, nx, ny, nz, tint, light, overlay);
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            float nx,
            float ny,
            float nz,
            int tint,
            int light,
            int overlay) {
        consumer.addVertex(pose, x, y, z)
                .setColor(tint, tint, tint, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    static void applyLegacyFaceTransform(PoseStack poseStack, Direction face) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        switch (face) {
            case WEST -> {}
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case DOWN -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                poseStack.scale(1.0F, -1.0F, -1.0F);
            }
            case UP -> poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
        poseStack.translate(-0.5F, 0.0F, 0.0F);
    }

    private static void rotateLrdu(PoseStack poseStack, SixNodeRotation rotation) {
        float degrees = switch (rotation) {
            case LEFT -> 0.0F;
            case UP -> 90.0F;
            case RIGHT -> 180.0F;
            case DOWN -> 270.0F;
        };
        if (degrees != 0.0F) poseStack.mulPose(Axis.XP.rotationDegrees(degrees));
    }

    private static ModelResourceLocation standalone(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(PortingBaseline.MOD_ID, path));
    }
}
