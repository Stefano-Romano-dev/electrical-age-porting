package mods.eln.gui;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.OptionalDouble;

/** Locale-aware numeric conversion shared by modern ELN configuration fields. */
public final class LegacyNumberFormat {
    private LegacyNumberFormat() {}

    public static OptionalDouble parseFinite(String text, Locale locale) {
        try {
            double value = NumberFormat.getNumberInstance(locale).parse(text).doubleValue();
            return Double.isFinite(value) ? OptionalDouble.of(value) : OptionalDouble.empty();
        } catch (ParseException ignored) {
            return OptionalDouble.empty();
        }
    }

    /** Mirrors GuiTextFieldEln.setText(float): two decimals below 1000, otherwise none. */
    public static String formatFloat(float value, Locale locale) {
        String pattern = Math.abs(value) < 1000.0F ? "%3.2f" : "%3.0f";
        return String.format(locale, pattern, value);
    }
}
