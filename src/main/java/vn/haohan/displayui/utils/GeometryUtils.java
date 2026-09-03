/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.utils;

import org.joml.Vector3f;

/** Reusable 3D point operations used while assembling display geometry. */
public final class GeometryUtils {
    private GeometryUtils() { throw new AssertionError("utility class"); }

    public static void scaleAroundMidpoint(Vector3f first, Vector3f second, float scale) {
        if (scale == 1.0f) return;
        Vector3f center = new Vector3f(first).add(second).mul(0.5f);
        scaleAround(first, center, scale);
        scaleAround(second, center, scale);
    }

    public static void scaleAroundCentroid(Vector3f first, Vector3f second, Vector3f third, float scale) {
        if (scale == 1.0f) return;
        Vector3f center = new Vector3f(first).add(second).add(third).div(3.0f);
        scaleAround(first, center, scale);
        scaleAround(second, center, scale);
        scaleAround(third, center, scale);
    }

    public static void scaleAroundParallelogramCenter(Vector3f first, Vector3f second, Vector3f third, float scale) {
        if (scale == 1.0f) return;
        Vector3f center = new Vector3f(second).add(third).mul(0.5f);
        scaleAround(first, center, scale);
        scaleAround(second, center, scale);
        scaleAround(third, center, scale);
    }

    public static Vector3f[] normalizeTriangleWinding(Vector3f first, Vector3f second, Vector3f third) {
        if (signedArea(first, second, third) < 0.0f) return reverseTriangleWinding(first, second, third);
        return copy(first, second, third);
    }

    public static Vector3f[] normalizeParallelogramWinding(Vector3f first, Vector3f second, Vector3f third) {
        if (signedArea(first, second, third) < 0.0f) return reverseParallelogramWinding(first, second, third);
        return copy(first, second, third);
    }

    public static Vector3f[] reverseTriangleWinding(Vector3f first, Vector3f second, Vector3f third) {
        return new Vector3f[]{new Vector3f(first), new Vector3f(third), new Vector3f(second)};
    }

    public static Vector3f[] reverseParallelogramWinding(Vector3f first, Vector3f second, Vector3f third) {
        Vector3f widthEdge = new Vector3f(second).sub(first);
        return new Vector3f[]{new Vector3f(third), new Vector3f(third).add(widthEdge), new Vector3f(first)};
    }

    /** Returns the orientation vector used to determine a display line's facing. */
    public static Vector3f lineFacingNormal(Vector3f start, Vector3f end) {
        Vector3f direction = new Vector3f(end).sub(start);
        Vector3f up = Math.abs(direction.y / direction.length()) > 0.99f
                ? new Vector3f(1, 0, 0)
                : new Vector3f(0, 1, 0);
        return direction.cross(up).normalize();
    }

    private static float signedArea(Vector3f first, Vector3f second, Vector3f third) {
        return new Vector3f(second).sub(first).cross(new Vector3f(third).sub(first)).z;
    }

    private static void scaleAround(Vector3f point, Vector3f center, float scale) {
        point.set(new Vector3f(center).add(new Vector3f(point).sub(center).mul(scale)));
    }

    private static Vector3f[] copy(Vector3f first, Vector3f second, Vector3f third) {
        return new Vector3f[]{new Vector3f(first), new Vector3f(second), new Vector3f(third)};
    }
}
