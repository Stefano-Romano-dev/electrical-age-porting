package mods.eln.menu;

import mods.eln.node.six.MountedSixNodeComponent;
import mods.eln.node.six.SixNodeBlockEntity;
import mods.eln.node.six.SixNodeComponentCatalog;
import mods.eln.registry.ElnContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import mods.eln.node.six.SixNodeElectricalGraph;

/** Slotless menu identifying one configurable electrical-source face. */
public final class ElectricalSourceMenu extends AbstractContainerMenu {
    private final BlockPos hostPos;
    private final Direction face;
    private final float initialVoltage;

    public ElectricalSourceMenu(int containerId, Inventory inventory, BlockPos hostPos, Direction face, float voltage) {
        super(ElnContent.ELECTRICAL_SOURCE_MENU.get(), containerId);
        this.hostPos = hostPos.immutable();
        this.face = face;
        this.initialVoltage = voltage;
    }

    public static ElectricalSourceMenu fromNetwork(
            int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        return new ElectricalSourceMenu(
                containerId,
                inventory,
                buffer.readBlockPos(),
                buffer.readEnum(Direction.class),
                buffer.readFloat());
    }

    public static boolean open(
            Player player, BlockPos hostPos, Direction face, MountedSixNodeComponent component) {
        if (!component.getTypeId().equals(SixNodeComponentCatalog.ELECTRICAL_SOURCE.getId())) return false;
        float voltage = component.getParameters()
                .getOrDefault(SixNodeElectricalGraph.VOLTAGE_PARAMETER, 0.0)
                .floatValue();
        return player.openMenu(
                        new SimpleMenuProvider(
                                (containerId, inventory, ignored) -> new ElectricalSourceMenu(
                                        containerId, inventory, hostPos, face, voltage),
                                Component.translatable("menu.eln.electrical_source")),
                        buffer -> {
                            buffer.writeBlockPos(hostPos);
                            buffer.writeEnum(face);
                            buffer.writeFloat(voltage);
                        })
                .isPresent();
    }

    public BlockPos hostPos() {
        return hostPos;
    }

    public Direction face() {
        return face;
    }

    public float initialVoltage() {
        return initialVoltage;
    }

    public boolean applyVoltage(Player player, float voltage) {
        if (!Float.isFinite(voltage) || !stillValid(player)) return false;
        if (!(player.level().getBlockEntity(hostPos) instanceof SixNodeBlockEntity host)) return false;
        return host.setElectricalSourceVoltage(face, voltage);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.distanceToSqr(
                        hostPos.getX() + 0.5, hostPos.getY() + 0.5, hostPos.getZ() + 0.5)
                > 64.0) return false;
        if (!(player.level().getBlockEntity(hostPos) instanceof SixNodeBlockEntity host)) return false;
        MountedSixNodeComponent component = host.contentsSnapshot().get(face);
        return component != null
                && component.getTypeId().equals(SixNodeComponentCatalog.ELECTRICAL_SOURCE.getId());
    }
}
