/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.utils;

/** Shared dependency-free helpers for frequently repeated UI math. */
public final class MathUtils {
    private MathUtils() { throw new AssertionError("utility class"); }

    public static int clamp(int value, int minimum, int maximum) {
        return Math.clamp(value, minimum, maximum);
    }

    public static float clamp(float value, float minimum, float maximum) {
        return Math.clamp(value, minimum, maximum);
    }

    public static double clamp(double value, double minimum, double maximum) {
        return Math.clamp(value, minimum, maximum);
    }

    public static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    public static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    /** Returns an angle in the conventional range [-180, 180]. */
    public static float normalizeDegrees(float degrees) {
        float normalized = degrees % 360.0f;
        if (normalized > 180.0f) normalized -= 360.0f;
        if (normalized < -180.0f) normalized += 360.0f;
        return normalized;
    }

    /** Returns the smallest absolute difference between two angles. */
    public static float angleDifference(float first, float second) {
        return Math.abs(signedAngleDifference(first, second));
    }

    /** Returns the shortest signed rotation from {@code second} to {@code first}. */
    public static float signedAngleDifference(float first, float second) {
        return normalizeDegrees(first - second);
    }

    /** Rounds a value to the nearest multiple of {@code step}. */
    public static double roundToStep(double value, double step) {
        if (step <= 0.0) return value;
        return Math.round(value / step) * step;
    }
}
