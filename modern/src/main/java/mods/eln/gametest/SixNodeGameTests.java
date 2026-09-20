package mods.eln.gametest;

import mods.eln.ElectricalAge;
import mods.eln.PortingBaseline;
import mods.eln.node.six.MountedSixNodeComponent;
import mods.eln.node.six.SixNodeBlockEntity;
import mods.eln.node.six.SixNodeComponentCatalog;
import mods.eln.node.six.SixNodeComponentDefinition;
import mods.eln.node.six.SixNodeComponentStack;
import mods.eln.node.six.SixNodeElectricalGraph;
import mods.eln.node.six.SixNodePlacementOrientation;
import mods.eln.node.six.SixNodeRotation;
import mods.eln.registry.ElnContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.Map;
import java.util.List;

@GameTestHolder(PortingBaseline.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SixNodeGameTests {
    public SixNodeGameTests() {}

    @GameTest(template = "empty")
    public static void registeredBlockEntitySurvivesVanillaWorldReload(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(1, 1, 1);
        helper.setBlock(relativePos, ElnContent.SIX_NODE.get());

        SixNodeBlockEntity original = helper.getBlockEntity(relativePos);
        MountedSixNodeComponent cable = new MountedSixNodeComponent(
                SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId(), SixNodeRotation.RIGHT);
        helper.assertTrue(original.mount(Direction.WEST, cable), "The empty west face must accept the cable");

        CompoundTag saved = original.saveWithFullMetadata(helper.getLevel().registryAccess());
        helper.assertValueEqual(saved.getString("id"), "eln:six_node", "The serialized type id must remain stable");

        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.getLevel().removeBlockEntity(absolutePos);
        BlockEntity restored = BlockEntity.loadStatic(
                absolutePos,
                helper.getLevel().getBlockState(absolutePos),
                saved,
                helper.getLevel().registryAccess());
        helper.assertTrue(restored instanceof SixNodeBlockEntity, "Vanilla must resolve the registered block entity type");
        helper.getLevel().setBlockEntity(restored);

        SixNodeBlockEntity reloaded = helper.getBlockEntity(relativePos);
        helper.assertValueEqual(
                reloaded.contentsSnapshot().get(Direction.WEST), cable, "Mounted face state must survive world reload");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lowVoltageCableItemMountsOnInverseFaceAndConsumesOnlyOnce(GameTestHelper helper) {
        BlockPos supportPos = new BlockPos(1, 1, 1);
        BlockPos hostPos = supportPos.above();
        helper.setBlock(supportPos, Blocks.STONE);

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = SixNodeComponentStack.create(SixNodeComponentCatalog.LOW_VOLTAGE_CABLE);
        stack.setCount(2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        helper.placeAt(player, stack, supportPos.below(), Direction.UP);
        helper.assertBlockPresent(ElnContent.SIX_NODE.get(), hostPos);

        SixNodeBlockEntity host = helper.getBlockEntity(hostPos);
        MountedSixNodeComponent expected = new MountedSixNodeComponent(
                SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId(),
                SixNodePlacementOrientation.fromPlacement(Direction.DOWN, player.getDirection()));
        helper.assertValueEqual(
                host.contentsSnapshot().get(Direction.DOWN), expected, "The clicked top face must mount as DOWN");
        helper.assertValueEqual(stack.getCount(), 1, "A successful survival placement must consume one item");

        helper.placeAt(player, stack, supportPos.below(), Direction.UP);
        helper.assertValueEqual(host.contentsSnapshot().size(), 1, "An occupied face must remain unchanged");
        helper.assertValueEqual(stack.getCount(), 1, "An occupied face must not consume the item");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lowVoltageCableRejectsNonOpaqueSupportWithoutCreatingHost(GameTestHelper helper) {
        BlockPos supportPos = new BlockPos(1, 1, 1);
        BlockPos targetPos = supportPos.above();
        helper.setBlock(supportPos, Blocks.GLASS);

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = SixNodeComponentStack.create(SixNodeComponentCatalog.LOW_VOLTAGE_CABLE);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, supportPos.below(), Direction.UP);

        helper.assertBlockNotPresent(ElnContent.SIX_NODE.get(), targetPos);
        helper.assertValueEqual(stack.getCount(), 1, "A rejected placement must not consume the item");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void powerResistorItemMountsWithItsLegacyLeftPlacementRotation(GameTestHelper helper) {
        BlockPos supportPos = new BlockPos(1, 1, 1);
        BlockPos targetPos = supportPos.above();
        helper.setBlock(supportPos, Blocks.STONE);

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = SixNodeComponentStack.create(SixNodeComponentCatalog.POWER_RESISTOR);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, supportPos.below(), Direction.UP);

        helper.assertBlockPresent(ElnContent.SIX_NODE.get(), targetPos);
        SixNodeBlockEntity host = helper.getBlockEntity(targetPos);
        SixNodeRotation expectedRotation =
                SixNodePlacementOrientation.fromPlacement(Direction.DOWN, player.getDirection()).left();
        helper.assertValueEqual(
                host.contentsSnapshot().get(Direction.DOWN),
                new MountedSixNodeComponent(SixNodeComponentCatalog.POWER_RESISTOR.getId(), expectedRotation),
                "Power resistor placement must retain descriptor-specific legacy rotation");
        helper.assertValueEqual(stack.getCount(), 0, "A successful resistor placement must consume the item");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unsupportedFacesDropTypedItemsAndRemoveOnlyTheEmptyHost(GameTestHelper helper) {
        BlockPos hostPos = new BlockPos(1, 2, 1);
        BlockPos downSupport = hostPos.below();
        BlockPos westSupport = hostPos.west();
        helper.setBlock(downSupport, Blocks.STONE);
        helper.setBlock(westSupport, Blocks.STONE);
        helper.setBlock(hostPos, ElnContent.SIX_NODE.get());

        SixNodeBlockEntity host = helper.getBlockEntity(hostPos);
        MountedSixNodeComponent cable = new MountedSixNodeComponent(
                SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId(), SixNodeRotation.UP);
        helper.assertTrue(host.mount(Direction.DOWN, cable), "DOWN must accept the first cable");

        AABB selectionBounds = helper.getBlockState(hostPos)
                .getShape(helper.getLevel(), helper.absolutePos(hostPos))
                .bounds();
        helper.assertTrue(
                Math.abs(selectionBounds.minX) < 1.0E-9
                        && Math.abs(selectionBounds.minY - 0.02) < 1.0E-9
                        && Math.abs(selectionBounds.maxX - 1.0) < 1.0E-9
                        && Math.abs(selectionBounds.maxY - 0.20) < 1.0E-9,
                "DOWN selection shape must use the canonical 0.02..0.20 slab");
        helper.assertTrue(host.mount(Direction.WEST, cable), "WEST must accept the second cable");

        helper.setBlock(downSupport, Blocks.AIR);
        helper.assertBlockPresent(ElnContent.SIX_NODE.get(), hostPos);
        helper.assertTrue(!host.contentsSnapshot().containsKey(Direction.DOWN), "Unsupported DOWN must be removed");
        helper.assertValueEqual(host.contentsSnapshot().get(Direction.WEST), cable, "Supported WEST must remain");
        assertTypedCableDropCount(helper, hostPos, 1);

        helper.setBlock(westSupport, Blocks.AIR);
        helper.assertBlockNotPresent(ElnContent.SIX_NODE.get(), hostPos);
        assertTypedCableDropCount(helper, hostPos, 2);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void survivalPlayerBreakDropsLastFaceAndAllowsHostRemoval(GameTestHelper helper) {
        BlockPos hostPos = new BlockPos(1, 2, 1);
        helper.setBlock(hostPos.below(), Blocks.STONE);
        helper.setBlock(hostPos, ElnContent.SIX_NODE.get());
        SixNodeBlockEntity host = helper.getBlockEntity(hostPos);
        helper.assertTrue(
                host.mount(
                        Direction.DOWN,
                        new MountedSixNodeComponent(
                                SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId(), SixNodeRotation.RIGHT)),
                "Fresh host must accept the cable");

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        boolean removeHost = ElnContent.SIX_NODE.get().onDestroyedByPlayer(
                helper.getBlockState(hostPos),
                helper.getLevel(),
                helper.absolutePos(hostPos),
                player,
                false,
                helper.getBlockState(hostPos).getFluidState());

        helper.assertTrue(removeHost, "Breaking the last face must allow vanilla to remove the host");
        helper.assertTrue(host.contentsSnapshot().isEmpty(), "The broken face must be removed before host teardown");
        assertTypedCableDropCount(helper, hostPos, 1);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void creativePlayerBreaksLastFaceWithoutDrop(GameTestHelper helper) {
        BlockPos hostPos = new BlockPos(1, 2, 1);
        helper.setBlock(hostPos.below(), Blocks.STONE);
        helper.setBlock(hostPos, ElnContent.SIX_NODE.get());
        SixNodeBlockEntity host = helper.getBlockEntity(hostPos);
        helper.assertTrue(
                host.mount(
                        Direction.DOWN,
                        new MountedSixNodeComponent(
                                SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId(), SixNodeRotation.LEFT)),
                "Fresh host must accept the cable");

        var player = helper.makeMockPlayer(GameType.CREATIVE);
        boolean removeHost = ElnContent.SIX_NODE.get().onDestroyedByPlayer(
                helper.getBlockState(hostPos),
                helper.getLevel(),
                helper.absolutePos(hostPos),
                player,
                false,
                helper.getBlockState(hostPos).getFluidState());

        helper.assertTrue(removeHost, "Breaking the last face must allow vanilla to remove the host");
        helper.assertTrue(host.contentsSnapshot().isEmpty(), "Creative breaking must still remove the face");
        assertTypedCableDropCount(helper, hostPos, 0);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void externalHostRemovalDropsEveryMountedFace(GameTestHelper helper) {
        BlockPos hostPos = new BlockPos(1, 2, 1);
        helper.setBlock(hostPos.below(), Blocks.STONE);
        helper.setBlock(hostPos.west(), Blocks.STONE);
        helper.setBlock(hostPos, ElnContent.SIX_NODE.get());
        SixNodeBlockEntity host = helper.getBlockEntity(hostPos);
        MountedSixNodeComponent cable = new MountedSixNodeComponent(
                SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId(), SixNodeRotation.DOWN);
        helper.assertTrue(host.mount(Direction.DOWN, cable), "DOWN must accept the first cable");
        helper.assertTrue(host.mount(Direction.WEST, cable), "WEST must accept the second cable");

        helper.setBlock(hostPos, Blocks.AIR);

        helper.assertBlockNotPresent(ElnContent.SIX_NODE.get(), hostPos);
        assertTypedCableDropCount(helper, hostPos, 2);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void loadedCableFacesJoinAndLeaveTheLevelElectricalGraph(GameTestHelper helper) {
        BlockPos firstPos = new BlockPos(1, 2, 1);
        BlockPos secondPos = firstPos.east();
        helper.setBlock(firstPos.below(), Blocks.STONE);
        helper.setBlock(secondPos.below(), Blocks.STONE);
        helper.setBlock(firstPos, ElnContent.SIX_NODE.get());
        SixNodeBlockEntity firstHost = helper.getBlockEntity(firstPos);
        MountedSixNodeComponent cable = new MountedSixNodeComponent(
                SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId(), SixNodeRotation.LEFT);
        helper.assertTrue(firstHost.mount(Direction.DOWN, cable), "First host must accept its cable");

        helper.setBlock(secondPos, ElnContent.SIX_NODE.get());
        SixNodeBlockEntity secondHost = helper.getBlockEntity(secondPos);
        helper.assertTrue(secondHost.mount(Direction.DOWN, cable), "Second host must accept its cable");

        SixNodeElectricalGraph graph = ElectricalAge.simulationLifecycle().getGraph(helper.getLevel());
        BlockPos firstAbsolute = helper.absolutePos(firstPos);
        BlockPos secondAbsolute = helper.absolutePos(secondPos);
        var firstEndpoint = new SixNodeElectricalGraph.Endpoint(firstAbsolute, Direction.DOWN);
        var secondEndpoint = new SixNodeElectricalGraph.Endpoint(secondAbsolute, Direction.DOWN);
        helper.assertTrue(graph != null, "The server level must own an electrical graph");
        helper.assertTrue(graph.loadAt(firstAbsolute, Direction.DOWN) != null, "First cable load must be registered");
        helper.assertTrue(graph.loadAt(secondAbsolute, Direction.DOWN) != null, "Second cable load must be registered");
        helper.assertTrue(graph.hasConnection(firstEndpoint, secondEndpoint), "Adjacent cable faces must be joined");

        secondHost.unmount(Direction.DOWN);
        helper.assertTrue(graph.loadAt(secondAbsolute, Direction.DOWN) == null, "Unmount must remove the cable load");
        helper.assertTrue(!graph.hasConnection(firstEndpoint, secondEndpoint), "Unmount must remove the cable segment");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void sourceAndPowerResistorProduceLegacyDcCurrent(GameTestHelper helper) {
        BlockPos highPos = new BlockPos(0, 2, 1);
        BlockPos resistorPos = highPos.east();
        BlockPos groundPos = resistorPos.east();
        for (BlockPos pos : new BlockPos[] {highPos, resistorPos, groundPos}) {
            helper.setBlock(pos.below(), Blocks.STONE);
        }

        helper.setBlock(highPos, ElnContent.SIX_NODE.get());
        SixNodeBlockEntity high = helper.getBlockEntity(highPos);
        helper.assertTrue(
                high.mount(
                        Direction.DOWN,
                        new MountedSixNodeComponent(
                                SixNodeComponentCatalog.ELECTRICAL_SOURCE.getId(),
                                SixNodeRotation.LEFT,
                                Map.of(SixNodeElectricalGraph.VOLTAGE_PARAMETER, 50.0))),
                "High source must mount");

        helper.setBlock(resistorPos, ElnContent.SIX_NODE.get());
        SixNodeBlockEntity resistor = helper.getBlockEntity(resistorPos);
        helper.assertTrue(
                resistor.mount(
                        Direction.DOWN,
                        new MountedSixNodeComponent(
                                SixNodeComponentCatalog.POWER_RESISTOR.getId(), SixNodeRotation.LEFT)),
                "Power resistor must mount");

        helper.setBlock(groundPos, ElnContent.SIX_NODE.get());
        SixNodeBlockEntity ground = helper.getBlockEntity(groundPos);
        helper.assertTrue(
                ground.mount(
                        Direction.DOWN,
                        new MountedSixNodeComponent(
                                SixNodeComponentCatalog.ELECTRICAL_SOURCE.getId(),
                                SixNodeRotation.LEFT,
                                Map.of(SixNodeElectricalGraph.VOLTAGE_PARAMETER, 0.0))),
                "Ground source must mount");

        SixNodeElectricalGraph graph = ElectricalAge.simulationLifecycle().getGraph(helper.getLevel());
        BlockPos resistorAbsolute = helper.absolutePos(resistorPos);
        double expected = 50.0
                / (SixNodeElectricalGraph.EMPTY_RESISTOR_RESISTANCE
                        + 2.0 * SixNodeElectricalGraph.LOW_VOLTAGE_CABLE_RESISTANCE_PER_LOAD
                        + 2.0E-9);
        var meterPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack multimeter = new ItemStack(ElnContent.MULTIMETER.get());
        meterPlayer.setItemInHand(InteractionHand.MAIN_HAND, multimeter);
        helper.succeedWhen(() -> {
            Double current = graph.resistorCurrentAt(resistorAbsolute, Direction.DOWN);
            helper.assertTrue(current != null, "The resistor runtime must exist");
            helper.assertTrue(
                    Math.abs(Math.abs(current) - expected) < 1.0E-5,
                    "Expected legacy DC current " + expected + " A, got " + current);
            var hit = new BlockHitResult(
                    Vec3.atCenterOf(resistorAbsolute), Direction.DOWN, resistorAbsolute, false);
            InteractionResult result = ElnContent.MULTIMETER.get().useOn(new UseOnContext(
                    helper.getLevel(), meterPlayer, InteractionHand.MAIN_HAND, multimeter, hit));
            helper.assertValueEqual(
                    result, InteractionResult.SUCCESS, "The multimeter must consume the mounted-face interaction");
            helper.assertValueEqual(multimeter.getCount(), 1, "Measurement must not consume or damage the tool");
        });
    }

    @GameTest(template = "showcase")
    public static void allThreeComponentsMountOnEveryFaceForVisualParity(GameTestHelper helper) {
        List<SixNodeComponentDefinition> components = List.of(
                SixNodeComponentCatalog.LOW_VOLTAGE_CABLE,
                SixNodeComponentCatalog.ELECTRICAL_SOURCE,
                SixNodeComponentCatalog.POWER_RESISTOR);
        SixNodeRotation[] rotations = {
            SixNodeRotation.UP, SixNodeRotation.RIGHT, SixNodeRotation.DOWN
        };

        int directionColumn = 0;
        for (Direction face : List.of(
                Direction.WEST,
                Direction.EAST,
                Direction.DOWN,
                Direction.UP,
                Direction.NORTH,
                Direction.SOUTH)) {
            for (int componentRow = 0; componentRow < components.size(); componentRow++) {
                BlockPos hostPos = new BlockPos(3 + directionColumn * 4, 4, 3 + componentRow * 4);
                BlockPos supportPos = hostPos.relative(face);
                helper.setBlock(supportPos, Blocks.SMOOTH_STONE);
                helper.setBlock(hostPos, ElnContent.SIX_NODE.get());

                SixNodeBlockEntity host = helper.getBlockEntity(hostPos);
                SixNodeComponentDefinition definition = components.get(componentRow);
                Map<String, Double> parameters = definition == SixNodeComponentCatalog.ELECTRICAL_SOURCE
                        ? Map.of(SixNodeElectricalGraph.VOLTAGE_PARAMETER, 50.0)
                        : Map.of();
                MountedSixNodeComponent component = new MountedSixNodeComponent(
                        definition.getId(), rotations[componentRow], parameters);
                helper.assertTrue(
                        host.mount(face, component),
                        "Showcase host must accept " + definition.getId() + " on " + face);
                helper.assertValueEqual(
                        host.contentsSnapshot().get(face),
                        component,
                        "Showcase state must preserve type, face, rotation and parameters");
            }
            directionColumn++;
        }
        helper.succeed();
    }

    private static void assertTypedCableDropCount(GameTestHelper helper, BlockPos relativePos, int expectedCount) {
        AABB area = new AABB(helper.absolutePos(relativePos)).inflate(2.0);
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area);
        helper.assertValueEqual(drops.size(), expectedCount, "Unexpected component drop count");
        for (ItemEntity drop : drops) {
            helper.assertValueEqual(
                    SixNodeComponentStack.typeId(drop.getItem()),
                    SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId(),
                    "Drop must preserve the component type id");
        }
    }
}
