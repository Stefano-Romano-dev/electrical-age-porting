package net.electricalage.eln.component;

/** A direction along the four edges of a mounted component's support face. */
public enum LocalDirection {
    UP,
    RIGHT,
    DOWN,
    LEFT;

    public LocalDirection clockwise() {
        return values()[(ordinal() + 1) & 3];
    }

    public LocalDirection counterClockwise() {
        return values()[(ordinal() + 3) & 3];
    }

    public LocalDirection opposite() {
        return values()[(ordinal() + 2) & 3];
    }

    public static LocalDirection fromOrdinal(int value) {
        return values()[Math.floorMod(value, values().length)];
    }
}
