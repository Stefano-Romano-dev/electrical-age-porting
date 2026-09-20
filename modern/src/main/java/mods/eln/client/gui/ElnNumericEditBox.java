package mods.eln.client.gui;

import java.util.Locale;
import java.util.function.DoubleConsumer;
import mods.eln.gui.LegacyNumberFormat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** Reusable numeric field preserving the commit behavior of legacy GuiTextFieldEln. */
public final class ElnNumericEditBox extends EditBox {
    private final DoubleConsumer commitListener;
    private double committedValue;

    public ElnNumericEditBox(
            Font font,
            int x,
            int y,
            int width,
            int height,
            Component narration,
            double initialValue,
            DoubleConsumer commitListener) {
        super(font, x, y, width, height, narration);
        this.commitListener = commitListener;
        this.committedValue = initialValue;
        setMaxLength(150);
        setValue(LegacyNumberFormat.formatFloat((float) initialValue, Locale.getDefault()));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isFocused() && (keyCode == 257 || keyCode == 335)) {
            commit();
            setFocused(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void setFocused(boolean focused) {
        if (isFocused() && !focused) commit();
        super.setFocused(focused);
    }

    public void commit() {
        var parsed = LegacyNumberFormat.parseFinite(getValue(), Locale.getDefault());
        if (parsed.isEmpty()) {
            setValue(LegacyNumberFormat.formatFloat((float) committedValue, Locale.getDefault()));
            return;
        }
        double value = parsed.getAsDouble();
        if (Double.compare(value, committedValue) == 0) return;
        committedValue = value;
        commitListener.accept(value);
    }
}
