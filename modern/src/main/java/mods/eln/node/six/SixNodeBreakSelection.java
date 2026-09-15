package mods.eln.node.six;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Legacy face-plane ray selection, including its fixed face priority and look-vector fallback. */
public final class SixNodeBreakSelection {
    private static final Direction[] LEGACY_FACE_ORDER = {
        Direction.WEST, Direction.EAST, Direction.DOWN,
        Direction.UP, Direction.NORTH, Direction.SOUTH
    };

    private SixNodeBreakSelection() {}

    public static Direction resolve(
            BlockPos pos, Vec3 start, Vec3 look, Map<Direction, MountedSixNodeComponent> contents) {
        Vec3 end = start.add(look.scale(8.0));
        for (Direction face : LEGACY_FACE_ORDER) {
            if (!contents.containsKey(face)) continue;
            if (intersectsLegacyFace(pos, start, end, face)) return face;
        }

        Direction best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Direction face : LEGACY_FACE_ORDER) {
            if (!contents.containsKey(face)) continue;
            double score = -(look.x * face.getStepX() + look.y * face.getStepY() + look.z * face.getStepZ());
            if (score > bestScore) {
                bestScore = score;
                best = face;
            }
        }
        return best;
    }

    private static boolean intersectsLegacyFace(BlockPos pos, Vec3 start, Vec3 end, Direction face) {
        double plane = switch (face.getAxis()) {
            case X -> face == Direction.WEST ? pos.getX() : pos.getX() + 1.0;
            case Y -> face == Direction.DOWN ? pos.getY() : pos.getY() + 1.0;
            case Z -> face == Direction.NORTH ? pos.getZ() : pos.getZ() + 1.0;
        };
        double startAxis = axis(start, face.getAxis());
        double endAxis = axis(end, face.getAxis());
        boolean planeInDirectedSegment = switch (face) {
            case WEST, DOWN, NORTH -> inDirectedRange(plane, endAxis, startAxis);
            case EAST, UP, SOUTH -> inDirectedRange(plane, startAxis, endAxis);
        };
        if (!planeInDirectedSegment) return false;

        double denominator = endAxis - startAxis;
        if (denominator == 0.0) return false;
        double ratio = (plane - startAxis) / denominator;
        if (ratio > 1.1) return false;

        Vec3 hit = start.add(end.subtract(start).scale(ratio));
        return switch (face.getAxis()) {
            case X -> inUnit(hit.y, pos.getY()) && inUnit(hit.z, pos.getZ());
            case Y -> inUnit(hit.x, pos.getX()) && inUnit(hit.z, pos.getZ());
            case Z -> inUnit(hit.x, pos.getX()) && inUnit(hit.y, pos.getY());
        };
    }

    private static double axis(Vec3 vector, Direction.Axis axis) {
        return switch (axis) {
            case X -> vector.x;
            case Y -> vector.y;
            case Z -> vector.z;
        };
    }

    private static boolean inUnit(double value, int minimum) {
        return value >= minimum && value <= minimum + 1.0;
    }

    private static boolean inDirectedRange(double value, double minimum, double maximum) {
        return value >= minimum && value <= maximum;
    }
}
