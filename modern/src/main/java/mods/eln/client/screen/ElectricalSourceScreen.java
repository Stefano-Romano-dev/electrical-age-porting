package mods.eln.client.screen;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import mods.eln.menu.ElectricalSourceMenu;
import mods.eln.network.SetElectricalSourceVoltagePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** Minimal modern equivalent of the legacy one-field electrical source GUI. */
public final class ElectricalSourceScreen extends AbstractContainerScreen<ElectricalSourceMenu> {
    private EditBox voltage;

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
        voltage = new EditBox(
                font,
                leftPos + 8,
                topPos + 33,
                96,
                18,
                Component.translatable("gui.eln.output_voltage"));
        voltage.setMaxLength(32);
        voltage.setValue(NumberFormat.getNumberInstance(Locale.getDefault()).format(menu.initialVoltage()));
        voltage.setResponder(this::sendIfValid);
        addRenderableWidget(voltage);
        setInitialFocus(voltage);
    }

    private void sendIfValid(String text) {
        try {
            Number number = NumberFormat.getNumberInstance(Locale.getDefault()).parse(text);
            float parsed = number.floatValue();
            if (!Float.isFinite(parsed)) return;
            PacketDistributor.sendToServer(new SetElectricalSourceVoltagePayload(
                    menu.hostPos(), menu.face(), parsed));
        } catch (ParseException ignored) {
            // Legacy GUI also leaves the previous value untouched while the text is not parseable.
        }
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
}
