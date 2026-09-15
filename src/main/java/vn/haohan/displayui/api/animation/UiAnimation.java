/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package vn.haohan.displayui.api.animation;

import java.util.Objects;

/**
 * Immutable animation configuration applied to nodes in a UI scene.
 * <p>
 * Spatial offsets offsetX, offsetY are measured in logical UI canvas pixels; offsetZ is measured in world-space block units;
 * opacity values are normalized from {@code 0.0f} (completely transparent) to {@code 1.0f} (fully opaque);
 * duration and delay parameters are measured in Minecraft server ticks (20 ticks = 1 second).
 *
 * @param durationTicks animation duration in server ticks (>= 1)
 * @param delayTicks    delay before animation begins executing (ticks, >= 0)
 * @param easing        motion curve easing function ({@link Easings})
 * @param fromOpacity   starting opacity [0.0f - 1.0f]
 * @param toOpacity     ending opacity [0.0f - 1.0f]
 * @param fromScale     starting scale multiplier (>= 0.0f)
 * @param toScale       ending scale multiplier (>= 0.0f)
 * @param offsetX       initial horizontal position offset (pixels)
 * @param offsetY       initial vertical position offset (pixels)
 * @param offsetZ       initial depth position offset
 */
public record UiAnimation(
        int durationTicks,
        int delayTicks,
        Easings easing,
        float fromOpacity,
        float toOpacity,
        float fromScale,
        float toScale,
        float offsetX,
        float offsetY,
        float offsetZ
) {
    public UiAnimation {
        if (durationTicks < 1) throw new IllegalArgumentException("durationTicks must be positive");
        if (delayTicks < 0) throw new IllegalArgumentException("delayTicks cannot be negative");
        Objects.requireNonNull(easing, "easing");
        validateUnit(fromOpacity, "fromOpacity");
        validateUnit(toOpacity, "toOpacity");
        if (!Float.isFinite(fromScale) || fromScale < 0.0f) {
            throw new IllegalArgumentException("fromScale must be finite and non-negative");
        }
        if (!Float.isFinite(toScale) || toScale < 0.0f) {
            throw new IllegalArgumentException("toScale must be finite and non-negative");
        }
        validateFinite(offsetX, "offsetX");
        validateFinite(offsetY, "offsetY");
        validateFinite(offsetZ, "offsetZ");
    }

    /**
     * Creates a builder to configure custom {@link UiAnimation} instances.
     *
     * @return a new {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a fade-in animation using {@link Easings#OutCubic}.
     *
     * @param durationTicks duration in server ticks
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation fadeIn(int durationTicks) {
        return fadeIn(durationTicks, Easings.OutCubic);
    }

    /**
     * Creates a fade-in animation with custom easing.
     *
     * @param durationTicks duration in server ticks
     * @param easing        easing curve
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation fadeIn(int durationTicks, Easings easing) {
        return builder().durationTicks(durationTicks).easing(easing)
                .opacity(0.0f, 1.0f).build();
    }

    /**
     * Creates a fade-out animation using {@link Easings#InCubic}.
     *
     * @param durationTicks duration in server ticks
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation fadeOut(int durationTicks) {
        return fadeOut(durationTicks, Easings.InCubic);
    }

    /**
     * Creates a fade-out animation with custom easing.
     *
     * @param durationTicks duration in server ticks
     * @param easing        easing curve
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation fadeOut(int durationTicks, Easings easing) {
        return builder().durationTicks(durationTicks).easing(easing)
                .opacity(1.0f, 0.0f).build();
    }

    /**
     * Creates a slide-in animation from a given direction over a specified distance.
     *
     * @param durationTicks duration in server ticks
     * @param direction     slide direction ({@link Direction})
     * @param distance      initial shift distance (pixels)
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation slideIn(int durationTicks, Direction direction, float distance) {
        return slideIn(durationTicks, direction, distance, Easings.OutCubic);
    }

    /**
     * Creates a slide-in animation from a given direction with custom easing.
     *
     * @param durationTicks duration in server ticks
     * @param direction     slide direction ({@link Direction})
     * @param distance      initial shift distance (pixels)
     * @param easing        easing curve
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation slideIn(int durationTicks, Direction direction,
                                      float distance, Easings easing) {
        Objects.requireNonNull(direction, "direction");
        if (!Float.isFinite(distance) || distance < 0.0f) {
            throw new IllegalArgumentException("distance must be finite and non-negative");
        }
        return builder().durationTicks(durationTicks).easing(easing)
                .offset(direction.x() * distance, direction.y() * distance,
                        direction.z() * distance).build();
    }

    /**
     * Creates a scale-in animation with subtle bounce using {@link Easings#BackOut}.
     *
     * @param durationTicks duration in server ticks
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation scaleIn(int durationTicks) {
        return scaleIn(durationTicks, Easings.BackOut);
    }

    /**
     * Creates a scale-in animation with custom easing.
     *
     * @param durationTicks duration in server ticks
     * @param easing        easing curve
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation scaleIn(int durationTicks, Easings easing) {
        return builder().durationTicks(durationTicks).easing(easing)
                .scale(0.0f, 1.0f).build();
    }

    /**
     * Returns a copy with an execution delay before playback begins.
     *
     * @param ticks delay in server ticks
     * @return a new {@link UiAnimation} instance
     */
    public UiAnimation delay(int ticks) {
        return new UiAnimation(durationTicks, ticks, easing, fromOpacity, toOpacity,
                fromScale, toScale, offsetX, offsetY, offsetZ);
    }

    /**
     * Checks whether this animation is static (introduces no change in opacity, scale, or position).
     *
     * @return {@code true} if no transformation occurs
     */
    public boolean isStatic() {
        return offsetX == 0.0f && offsetY == 0.0f && offsetZ == 0.0f
                && fromScale == 1.0f && toScale == 1.0f
                && fromOpacity == 1.0f && toOpacity == 1.0f;
    }

    /**
     * Cardinal direction in logical UI canvas coordinate space.
     */
    public enum Direction {
        LEFT(-1, 0, 0),
        RIGHT(1, 0, 0),
        TOP(0, -1, 0),
        BOTTOM(0, 1, 0),
        FRONT(0, 0, 1),
        BACK(0, 0, -1);

        private final float x;
        private final float y;
        private final float z;

        Direction(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float x() { return x; }
        public float y() { return y; }
        public float z() { return z; }
    }

    /**
     * Builder utility for configuring customizable {@link UiAnimation} instances.
     */
    public static final class Builder {
        private int durationTicks = 10;
        private int delayTicks;
        private Easings easing = Easings.OutCubic;
        private float fromOpacity = 1.0f;
        private float toOpacity = 1.0f;
        private float fromScale = 1.0f;
        private float toScale = 1.0f;
        private float offsetX;
        private float offsetY;
        private float offsetZ;

        /**
         * Sets animation duration in Minecraft game ticks.
         *
         * @param ticks duration in server ticks (>= 1)
         * @return this builder
         */
        public Builder durationTicks(int ticks) {
            durationTicks = ticks;
            return this;
        }

        /**
         * Sets delay before starting playback.
         *
         * @param ticks delay in server ticks (>= 0)
         * @return this builder
         */
        public Builder delayTicks(int ticks) {
            delayTicks = ticks;
            return this;
        }

        /**
         * Sets the motion easing curve.
         *
         * @param value easing curve ({@link Easings})
         * @return this builder
         */
        public Builder easing(Easings value) {
            easing = Objects.requireNonNull(value, "easing");
            return this;
        }

        /**
         * Sets the opacity range from start to finish.
         *
         * @param from starting opacity [0.0f - 1.0f]
         * @param to   ending opacity [0.0f - 1.0f]
         * @return this builder
         */
        public Builder opacity(float from, float to) {
            fromOpacity = from;
            toOpacity = to;
            return this;
        }

        /**
         * Sets the scale multiplier range from start to finish.
         *
         * @param from starting scale (>= 0.0f)
         * @param to   ending scale (>= 0.0f)
         * @return this builder
         */
        public Builder scale(float from, float to) {
            fromScale = from;
            toScale = to;
            return this;
        }

        /**
         * Sets translation offsets across X, Y, and Z axes.
         *
         * @param x horizontal offset (pixels)
         * @param y vertical offset (pixels)
         * @param z depth offset (world units)
         * @return this builder
         */
        public Builder offset(float x, float y, float z) {
            offsetX = x;
            offsetY = y;
            offsetZ = z;
            return this;
        }

        /**
         * Sets translation offset given a direction and magnitude.
         *
         * @param direction translation direction
         * @param distance  distance (pixels)
         * @return this builder
         */
        public Builder offset(Direction direction, float distance) {
            Objects.requireNonNull(direction, "direction");
            return offset(direction.x() * distance, direction.y() * distance,
                    direction.z() * distance);
        }

        /**
         * Builds the configured {@link UiAnimation}.
         *
         * @return a new {@link UiAnimation} instance
         */
        public UiAnimation build() {
            return new UiAnimation(durationTicks, delayTicks, easing,
                    fromOpacity, toOpacity, fromScale, toScale,
                    offsetX, offsetY, offsetZ);
        }
    }

    private static void validateUnit(float value, String name) {
        if (!Float.isFinite(value) || value < 0.0f || value > 1.0f) {
            throw new IllegalArgumentException(name + " must be between 0 and 1");
        }
    }

    private static void validateFinite(float value, String name) {
        if (!Float.isFinite(value)) throw new IllegalArgumentException(name + " must be finite");
    }
}
