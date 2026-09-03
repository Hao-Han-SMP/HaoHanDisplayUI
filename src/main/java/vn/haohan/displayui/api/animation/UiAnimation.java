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
 * A small, immutable animation description applied to every node in a scene.
 * X/Y offsets are logical UI pixels, Z offsets are world units, opacity is
 * normalized from {@code 0} to {@code 1}, and duration/delay are measured in
 * Minecraft ticks.
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

    public static Builder builder() {
        return new Builder();
    }

    public static UiAnimation fadeIn(int durationTicks) {
        return fadeIn(durationTicks, Easings.OutCubic);
    }

    public static UiAnimation fadeIn(int durationTicks, Easings easing) {
        return builder().durationTicks(durationTicks).easing(easing)
                .opacity(0.0f, 1.0f).build();
    }

    public static UiAnimation fadeOut(int durationTicks) {
        return fadeOut(durationTicks, Easings.InCubic);
    }

    public static UiAnimation fadeOut(int durationTicks, Easings easing) {
        return builder().durationTicks(durationTicks).easing(easing)
                .opacity(1.0f, 0.0f).build();
    }

    public static UiAnimation slideIn(int durationTicks, Direction direction, float distance) {
        return slideIn(durationTicks, direction, distance, Easings.OutCubic);
    }

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

    public static UiAnimation scaleIn(int durationTicks) {
        return scaleIn(durationTicks, Easings.BackOut);
    }

    public static UiAnimation scaleIn(int durationTicks, Easings easing) {
        return builder().durationTicks(durationTicks).easing(easing)
                .scale(0.0f, 1.0f).build();
    }

    /** Returns a copy with a delay before the animation begins. */
    public UiAnimation delay(int ticks) {
        return new UiAnimation(durationTicks, ticks, easing, fromOpacity, toOpacity,
                fromScale, toScale, offsetX, offsetY, offsetZ);
    }

    /** Direction in the scene's logical screen coordinate system. */
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

        float x() { return x; }
        float y() { return y; }
        float z() { return z; }
    }

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

        public Builder durationTicks(int ticks) {
            durationTicks = ticks;
            return this;
        }

        public Builder delayTicks(int ticks) {
            delayTicks = ticks;
            return this;
        }

        public Builder easing(Easings value) {
            easing = Objects.requireNonNull(value, "easing");
            return this;
        }

        public Builder opacity(float from, float to) {
            fromOpacity = from;
            toOpacity = to;
            return this;
        }

        public Builder scale(float from, float to) {
            fromScale = from;
            toScale = to;
            return this;
        }

        public Builder offset(float x, float y, float z) {
            offsetX = x;
            offsetY = y;
            offsetZ = z;
            return this;
        }

        public Builder offset(Direction direction, float distance) {
            Objects.requireNonNull(direction, "direction");
            return offset(direction.x() * distance, direction.y() * distance,
                    direction.z() * distance);
        }

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
