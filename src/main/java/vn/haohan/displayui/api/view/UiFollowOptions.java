/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 */
package vn.haohan.displayui.api.view;

/**
 * Tuning parameters for the single player-follow implementation.
 *
 * <p>Interpolation is a client-side blending window in ticks. Increasing it
 * makes movement smoother, at the cost of additional visual latency.</p>
 */
public record UiFollowOptions(double distance, float positionDamping,
                              float rotationDamping, int interpolationTicks) {
    public UiFollowOptions {
        if (!Double.isFinite(distance) || distance <= 0.0) {
            throw new IllegalArgumentException("distance must be positive and finite");
        }
        validateDamping(positionDamping, "positionDamping");
        validateDamping(rotationDamping, "rotationDamping");
        if (interpolationTicks < 0) {
            throw new IllegalArgumentException("interpolationTicks cannot be negative");
        }
    }

    public static UiFollowOptions defaults() {
        return new UiFollowOptions(3.0, 0.22f, 0.22f, 4);
    }

    public static UiFollowOptions of(double distance, float damping, int interpolationTicks) {
        return new UiFollowOptions(distance, damping, damping, interpolationTicks);
    }

    public UiFollowOptions distance(double value) {
        return new UiFollowOptions(value, positionDamping, rotationDamping, interpolationTicks);
    }

    public UiFollowOptions damping(float value) {
        return new UiFollowOptions(distance, value, value, interpolationTicks);
    }

    public UiFollowOptions positionDamping(float value) {
        return new UiFollowOptions(distance, value, rotationDamping, interpolationTicks);
    }

    public UiFollowOptions rotationDamping(float value) {
        return new UiFollowOptions(distance, positionDamping, value, interpolationTicks);
    }

    public UiFollowOptions interpolationTicks(int value) {
        return new UiFollowOptions(distance, positionDamping, rotationDamping, value);
    }

    private static void validateDamping(float value, String name) {
        if (!Float.isFinite(value) || value < 0.0f || value > 1.0f) {
            throw new IllegalArgumentException(name + " must be between 0.0 and 1.0");
        }
    }
}
