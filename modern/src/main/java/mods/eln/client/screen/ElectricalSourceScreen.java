package mods.eln.client.screen;

import java.util.Locale;
import mods.eln.client.gui.ElnNumericEditBox;
import mods.eln.gui.LegacyNumberFormat;
import mods.eln.menu.ElectricalSourceMenu;
import mods.eln.network.SetElectricalSourceVoltagePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** Minimal modern equivalent of the legacy one-field electrical source GUI. */
public final class ElectricalSourceScreen extends AbstractContainerScreen<ElectricalSourceMenu> {
    private ElnNumericEditBox voltage;

    public ElectricalSourceScreen(ElectricalSourceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 140;
        imageHeight = 62;
        titleLabelX = 8;
        titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        voltage = new ElnNumericEditBox(
                font,
                leftPos + 8,
                topPos + 33,
                96,
                18,
                Component.translatable("gui.eln.output_voltage"),
                menu.initialVoltage(),
                value -> PacketDistributor.sendToServer(new SetElectricalSourceVoltagePayload(
                        menu.hostPos(), menu.face(), (float) value)));
        addRenderableWidget(voltage);
        setInitialFocus(voltage);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF20252B);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFFB8B8B8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x303030, false);
        graphics.drawString(
                font, Component.translatable("gui.eln.output_voltage"), 8, 21, 0x303030, false);
        graphics.drawString(font, "V", 110, 38, 0x303030, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /** Exercises the same edit-and-focus-loss path as a player in the dedicated development probe. */
    public void commitVoltageForDevelopmentProbe(float value) {
        voltage.setFocused(true);
        voltage.setValue(LegacyNumberFormat.formatFloat(value, Locale.getDefault()).trim());
        voltage.setFocused(false);
    }
}
