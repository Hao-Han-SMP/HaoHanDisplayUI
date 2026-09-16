/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 */
package vn.haohan.displayui.api.view;

/**
 * Physical motion and damping parameters for player-following UI behavior.
 * <p>
 * {@code interpolationTicks} controls the client-side display entity interpolation window.
 * Higher tick values produce smoother visual motion at the cost of a slight tracking latency.
 *
 * @param distance           target follow distance maintained from player eye location in blocks
 * @param positionDamping    translational spring damping factor (0.0f - 1.0f)
 * @param rotationDamping    rotational orientation damping factor (0.0f - 1.0f)
 * @param interpolationTicks client-side entity interpolation duration in server ticks (>= 0)
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

    /**
     * Returns standard follow configuration (distance 3.0 blocks, damping 0.22f, interpolation 4 ticks).
     *
     * @return default {@link UiFollowOptions}
     */
    public static UiFollowOptions defaults() {
        return new UiFollowOptions(3.0, 0.22f, 0.22f, 4);
    }

    /**
     * Creates follow options with identical position and rotation damping.
     *
     * @param distance           target follow distance in blocks
     * @param damping            unified damping factor (0.0f - 1.0f)
     * @param interpolationTicks client interpolation ticks
     * @return a new {@link UiFollowOptions} instance
     */
    public static UiFollowOptions of(double distance, float damping, int interpolationTicks) {
        return new UiFollowOptions(distance, damping, damping, interpolationTicks);
    }

    /**
     * Creates a copy of these options with modified follow distance.
     *
     * @param value new distance in blocks
     * @return a new {@link UiFollowOptions} instance
     */
    public UiFollowOptions distance(double value) {
        return new UiFollowOptions(value, positionDamping, rotationDamping, interpolationTicks);
    }

    /**
     * Creates a copy of these options with unified damping applied to position and rotation.
     *
     * @param value new damping factor (0.0f - 1.0f)
     * @return a new {@link UiFollowOptions} instance
     */
    public UiFollowOptions damping(float value) {
        return new UiFollowOptions(distance, value, value, interpolationTicks);
    }

    /**
     * Creates a copy of these options with customized translational damping.
     *
     * @param value positional damping factor (0.0f - 1.0f)
     * @return a new {@link UiFollowOptions} instance
     */
    public UiFollowOptions positionDamping(float value) {
        return new UiFollowOptions(distance, value, rotationDamping, interpolationTicks);
    }

    /**
     * Creates a copy of these options with customized rotational damping.
     *
     * @param value rotational damping factor (0.0f - 1.0f)
     * @return a new {@link UiFollowOptions} instance
     */
    public UiFollowOptions rotationDamping(float value) {
        return new UiFollowOptions(distance, positionDamping, value, interpolationTicks);
    }

    /**
     * Creates a copy of these options with modified client interpolation ticks.
     *
     * @param value client interpolation ticks
     * @return a new {@link UiFollowOptions} instance
     */
    public UiFollowOptions interpolationTicks(int value) {
        return new UiFollowOptions(distance, positionDamping, rotationDamping, value);
    }

    private static void validateDamping(float value, String name) {
        if (!Float.isFinite(value) || value < 0.0f || value > 1.0f) {
            throw new IllegalArgumentException(name + " must be between 0.0 and 1.0");
        }
    }
}

