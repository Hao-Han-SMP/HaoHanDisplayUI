/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 */
package vn.haohan.displayui.api.view;

/**
 * Tuning parameters for player-follow implementations.
 *
 * <p>Supports both gaze-deadzone tracking (stationary when crosshair is inside panel bounds)
 * and smooth client-side interpolation via Display entity duration parameters.</p>
 */
public record UiFollowOptions(
        double distance,
        float positionDamping,
        float rotationDamping,
        int interpolationTicks,
        boolean gazeDeadzone,
        float deadzoneMargin,
        Float minX,
        Float maxX,
        Float minY,
        Float maxY,
        double minDistanceRatio,
        double maxDistanceRatio
) {
    public UiFollowOptions {
        if (!Double.isFinite(distance) || distance <= 0.0) {
            throw new IllegalArgumentException("distance must be positive and finite");
        }
        validateDamping(positionDamping, "positionDamping");
        validateDamping(rotationDamping, "rotationDamping");
        if (interpolationTicks < 0) {
            throw new IllegalArgumentException("interpolationTicks cannot be negative");
        }
        if (minDistanceRatio < 0.0 || maxDistanceRatio <= minDistanceRatio) {
            throw new IllegalArgumentException("invalid distance ratios");
        }
    }

    public UiFollowOptions(double distance, float positionDamping,
                          float rotationDamping, int interpolationTicks) {
        this(distance, positionDamping, rotationDamping, interpolationTicks,
                true, 8.0f, null, null, null, null, 0.55, 1.55);
    }

    public static UiFollowOptions defaults() {
        return new UiFollowOptions(3.0, 0.42f, 0.45f, 4);
    }

    public static UiFollowOptions of(double distance, float damping, int interpolationTicks) {
        return new UiFollowOptions(distance, damping, damping, interpolationTicks);
    }

    public boolean hasCustomBounds() {
        return minX != null && maxX != null && minY != null && maxY != null;
    }

    public UiFollowOptions distance(double value) {
        return new UiFollowOptions(value, positionDamping, rotationDamping, interpolationTicks,
                gazeDeadzone, deadzoneMargin, minX, maxX, minY, maxY, minDistanceRatio, maxDistanceRatio);
    }

    public UiFollowOptions damping(float value) {
        return new UiFollowOptions(distance, value, value, interpolationTicks,
                gazeDeadzone, deadzoneMargin, minX, maxX, minY, maxY, minDistanceRatio, maxDistanceRatio);
    }

    public UiFollowOptions positionDamping(float value) {
        return new UiFollowOptions(distance, value, rotationDamping, interpolationTicks,
                gazeDeadzone, deadzoneMargin, minX, maxX, minY, maxY, minDistanceRatio, maxDistanceRatio);
    }

    public UiFollowOptions rotationDamping(float value) {
        return new UiFollowOptions(distance, positionDamping, value, interpolationTicks,
                gazeDeadzone, deadzoneMargin, minX, maxX, minY, maxY, minDistanceRatio, maxDistanceRatio);
    }

    public UiFollowOptions interpolationTicks(int value) {
        return new UiFollowOptions(distance, positionDamping, rotationDamping, value,
                gazeDeadzone, deadzoneMargin, minX, maxX, minY, maxY, minDistanceRatio, maxDistanceRatio);
    }

    public UiFollowOptions gazeDeadzone(boolean value) {
        return new UiFollowOptions(distance, positionDamping, rotationDamping, interpolationTicks,
                value, deadzoneMargin, minX, maxX, minY, maxY, minDistanceRatio, maxDistanceRatio);
    }

    public UiFollowOptions deadzoneMargin(float value) {
        return new UiFollowOptions(distance, positionDamping, rotationDamping, interpolationTicks,
                gazeDeadzone, value, minX, maxX, minY, maxY, minDistanceRatio, maxDistanceRatio);
    }

    public UiFollowOptions bounds(float minX, float maxX, float minY, float maxY) {
        return new UiFollowOptions(distance, positionDamping, rotationDamping, interpolationTicks,
                gazeDeadzone, deadzoneMargin, minX, maxX, minY, maxY, minDistanceRatio, maxDistanceRatio);
    }

    public UiFollowOptions distanceRatios(double minRatio, double maxRatio) {
        return new UiFollowOptions(distance, positionDamping, rotationDamping, interpolationTicks,
                gazeDeadzone, deadzoneMargin, minX, maxX, minY, maxY, minRatio, maxRatio);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private double distance = 3.0;
        private float positionDamping = 0.42f;
        private float rotationDamping = 0.45f;
        private int interpolationTicks = 4;
        private boolean gazeDeadzone = true;
        private float deadzoneMargin = 8.0f;
        private Float minX = null;
        private Float maxX = null;
        private Float minY = null;
        private Float maxY = null;
        private double minDistanceRatio = 0.55;
        private double maxDistanceRatio = 1.55;

        public Builder distance(double distance) {
            this.distance = distance;
            return this;
        }

        public Builder damping(float damping) {
            this.positionDamping = damping;
            this.rotationDamping = damping;
            return this;
        }

        public Builder positionDamping(float positionDamping) {
            this.positionDamping = positionDamping;
            return this;
        }

        public Builder rotationDamping(float rotationDamping) {
            this.rotationDamping = rotationDamping;
            return this;
        }

        public Builder interpolationTicks(int interpolationTicks) {
            this.interpolationTicks = interpolationTicks;
            return this;
        }

        public Builder gazeDeadzone(boolean gazeDeadzone) {
            this.gazeDeadzone = gazeDeadzone;
            return this;
        }

        public Builder deadzoneMargin(float deadzoneMargin) {
            this.deadzoneMargin = deadzoneMargin;
            return this;
        }

        public Builder bounds(float minX, float maxX, float minY, float maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            return this;
        }

        public Builder distanceRatios(double minRatio, double maxRatio) {
            this.minDistanceRatio = minRatio;
            this.maxDistanceRatio = maxRatio;
            return this;
        }

        public UiFollowOptions build() {
            return new UiFollowOptions(distance, positionDamping, rotationDamping, interpolationTicks,
                    gazeDeadzone, deadzoneMargin, minX, maxX, minY, maxY, minDistanceRatio, maxDistanceRatio);
        }
    }

    private static void validateDamping(float value, String name) {
        if (!Float.isFinite(value) || value < 0.0f || value > 1.0f) {
            throw new IllegalArgumentException(name + " must be between 0.0 and 1.0");
        }
    }
}
