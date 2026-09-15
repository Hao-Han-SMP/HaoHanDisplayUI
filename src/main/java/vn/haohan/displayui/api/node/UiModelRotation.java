/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HaoHanDisplayUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HaoHanDisplayUI. If not, see <https://www.gnu.org/licenses/>.
 */
package vn.haohan.displayui.api.node;

import vn.haohan.displayui.utils.MathUtils;
import java.util.Objects;

/**
 * 3D spatial rotation configuration and constraints for entity models in the UI canvas.
 * <p>
 * Supports independent axis locks (yaw, pitch, roll), angular min/max limits,
 * angle quantization steps (snapping), and multiple interactive rotation modes.
 *
 * @param mode          interactive rotation mode (cursor tracking, hover spin, or continuous auto-spin)
 * @param lockYaw       {@code true} to lock the Yaw axis (prevent horizontal turning)
 * @param lockPitch     {@code true} to lock the Pitch axis (prevent vertical tilting)
 * @param lockRoll      {@code true} to lock the Roll axis (prevent banking/leaning)
 * @param minYaw        minimum allowed Yaw angle (degrees)
 * @param maxYaw        maximum allowed Yaw angle (degrees)
 * @param minPitch      minimum allowed Pitch angle (degrees)
 * @param maxPitch      maximum allowed Pitch angle (degrees)
 * @param minRoll       minimum allowed Roll angle (degrees)
 * @param maxRoll       maximum allowed Roll angle (degrees)
 * @param step          angular quantization snap increment in degrees (0.0f = continuous smooth rotation)
 * @param sensitivity   cursor-tracking sensitivity multiplier (default 1.0f)
 * @param autoSpinSpeed automatic vertical spin velocity (degrees per tick, 20 ticks = 1 second)
 */
public record UiModelRotation(
        Mode mode,
        boolean lockYaw,
        boolean lockPitch,
        boolean lockRoll,
        float minYaw,
        float maxYaw,
        float minPitch,
        float maxPitch,
        float minRoll,
        float maxRoll,
        float step,
        float sensitivity,
        float autoSpinSpeed
) {
    /**
     * Interactive 3D rotation mode for model viewers.
     */
    public enum Mode {
        /** Smoothly tilts and turns towards the viewer's mouse cursor relative to the model center. */
        CURSOR_TRACKING,
        /** Spins continuously around its vertical axis only when the viewer hovers over the model hitbox. */
        HOVER_SPIN,
        /** Spins continuously at all times regardless of hover state. */
        AUTO_SPIN
    }

    public UiModelRotation {
        Objects.requireNonNull(mode, "mode");
        if (!Float.isFinite(minYaw) || !Float.isFinite(maxYaw)
                || !Float.isFinite(minPitch) || !Float.isFinite(maxPitch)
                || !Float.isFinite(minRoll) || !Float.isFinite(maxRoll)
                || !Float.isFinite(step) || !Float.isFinite(sensitivity)
                || !Float.isFinite(autoSpinSpeed)) {
            throw new IllegalArgumentException("rotation parameters must be finite");
        }
        if (minYaw > maxYaw) throw new IllegalArgumentException("minYaw cannot exceed maxYaw");
        if (minPitch > maxPitch) throw new IllegalArgumentException("minPitch cannot exceed maxPitch");
        if (minRoll > maxRoll) throw new IllegalArgumentException("minRoll cannot exceed maxRoll");
        if (step < 0.0f) throw new IllegalArgumentException("step must be non-negative");
        if (sensitivity <= 0.0f) throw new IllegalArgumentException("sensitivity must be positive");
    }

    /**
     * Creates default rotation settings for models.
     * <p>
     * Cursor tracking enabled, roll locked, yaw permitted in [-180, 180], pitch in [-60, 60].
     *
     * @return default {@link UiModelRotation} instance
     */
    public static UiModelRotation defaults() {
        return new UiModelRotation(
                Mode.CURSOR_TRACKING,
                false, false, true,
                -180.0f, 180.0f,
                -60.0f, 60.0f,
                0.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        );
    }

    /**
     * Creates an unconstrained 3D rotation setting allowing unrestricted free tracking across all 3 axes.
     *
     * @return free {@link UiModelRotation} instance
     */
    public static UiModelRotation free() {
        return new UiModelRotation(
                Mode.CURSOR_TRACKING,
                false, false, false,
                -360.0f, 360.0f,
                -180.0f, 180.0f,
                -180.0f, 180.0f,
                0.0f, 1.0f, 0.0f
        );
    }

    /**
     * Creates a completely locked static rotation setting (model remains rigidly stationary).
     *
     * @return locked {@link UiModelRotation} instance
     */
    public static UiModelRotation locked() {
        return new UiModelRotation(
                Mode.CURSOR_TRACKING,
                true, true, true,
                0.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        );
    }

    /**
     * Creates a rotation setting constrained strictly to the horizontal Yaw axis.
     *
     * @return yaw-only {@link UiModelRotation} instance
     */
    public static UiModelRotation yawOnly() {
        return defaults().withLockPitch(true).withLockRoll(true);
    }

    /**
     * Creates a rotation setting constrained strictly to the vertical Pitch axis.
     *
     * @return pitch-only {@link UiModelRotation} instance
     */
    public static UiModelRotation pitchOnly() {
        return defaults().withLockYaw(true).withLockPitch(false).withLockRoll(true);
    }

    /**
     * Creates an automatic continuous spin setting at a specified speed.
     *
     * @param degreesPerTick rotation increment per tick (degrees/tick, e.g. 2.0f = 40 deg/sec)
     * @return {@link UiModelRotation} configured with {@link Mode#AUTO_SPIN}
     */
    public static UiModelRotation autoSpin(float degreesPerTick) {
        return new UiModelRotation(
                Mode.AUTO_SPIN,
                false, true, true,
                -360.0f, 360.0f,
                0.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 1.0f, degreesPerTick
        );
    }

    /**
     * Creates an automatic spin setting triggered only when the viewer hovers over the model.
     *
     * @param degreesPerTick rotation increment per tick while hovered (degrees/tick)
     * @return {@link UiModelRotation} configured with {@link Mode#HOVER_SPIN}
     */
    public static UiModelRotation hoverSpin(float degreesPerTick) {
        return new UiModelRotation(
                Mode.HOVER_SPIN,
                false, true, true,
                -360.0f, 360.0f,
                0.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 1.0f, degreesPerTick
        );
    }

    /**
     * Returns a copy with updated interaction mode.
     *
     * @param nextMode new mode
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withMode(Mode nextMode) {
        return new UiModelRotation(nextMode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, minPitch, maxPitch, minRoll, maxRoll,
                step, sensitivity, autoSpinSpeed);
    }

    /**
     * Returns a copy with updated axis locks for yaw, pitch, and roll.
     *
     * @param yaw   whether to lock yaw
     * @param pitch whether to lock pitch
     * @param roll  whether to lock roll
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withLocks(boolean yaw, boolean pitch, boolean roll) {
        return new UiModelRotation(mode, yaw, pitch, roll,
                minYaw, maxYaw, minPitch, maxPitch, minRoll, maxRoll,
                step, sensitivity, autoSpinSpeed);
    }

    /**
     * Returns a copy with updated Yaw lock state.
     *
     * @param locked {@code true} to lock yaw
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withLockYaw(boolean locked) {
        return withLocks(locked, lockPitch, lockRoll);
    }

    /**
     * Returns a copy with updated Pitch lock state.
     *
     * @param locked {@code true} to lock pitch
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withLockPitch(boolean locked) {
        return withLocks(lockYaw, locked, lockRoll);
    }

    /**
     * Returns a copy with updated Roll lock state.
     *
     * @param locked {@code true} to lock roll
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withLockRoll(boolean locked) {
        return withLocks(lockYaw, lockPitch, locked);
    }

    /**
     * Returns a copy with updated Yaw angle limits.
     *
     * @param min minimum yaw (degrees)
     * @param max maximum yaw (degrees)
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withYawRange(float min, float max) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                min, max, minPitch, maxPitch, minRoll, maxRoll,
                step, sensitivity, autoSpinSpeed);
    }

    /**
     * Returns a copy with updated Pitch angle limits.
     *
     * @param min minimum pitch (degrees)
     * @param max maximum pitch (degrees)
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withPitchRange(float min, float max) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, min, max, minRoll, maxRoll,
                step, sensitivity, autoSpinSpeed);
    }

    /**
     * Returns a copy with updated Roll angle limits.
     *
     * @param min minimum roll (degrees)
     * @param max maximum roll (degrees)
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withRollRange(float min, float max) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, minPitch, maxPitch, min, max,
                step, sensitivity, autoSpinSpeed);
    }

    /**
     * Returns a copy with an updated angular quantization snap step.
     *
     * @param stepAngle snap step angle (degrees, e.g. 15.0f for 15-degree steps, or 0 for smooth)
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withStep(float stepAngle) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, minPitch, maxPitch, minRoll, maxRoll,
                stepAngle, sensitivity, autoSpinSpeed);
    }

    /**
     * Returns a copy with updated cursor-tracking sensitivity.
     *
     * @param factor sensitivity factor (> 0.0f)
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withSensitivity(float factor) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, minPitch, maxPitch, minRoll, maxRoll,
                step, factor, autoSpinSpeed);
    }

    /**
     * Returns a copy with updated auto spin speed.
     *
     * @param speed spin velocity (degrees per tick)
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withAutoSpinSpeed(float speed) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, minPitch, maxPitch, minRoll, maxRoll,
                step, sensitivity, speed);
    }

    /**
     * Switches to {@link Mode#AUTO_SPIN} and unlocks the Yaw axis.
     *
     * @param degreesPerTick spin velocity per tick
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withAutoSpin(float degreesPerTick) {
        return withMode(Mode.AUTO_SPIN).withLockYaw(false).withAutoSpinSpeed(degreesPerTick);
    }

    /**
     * Switches to {@link Mode#HOVER_SPIN} and unlocks the Yaw axis.
     *
     * @param degreesPerTick spin velocity per tick while hovered
     * @return a new {@link UiModelRotation} instance
     */
    public UiModelRotation withHoverSpin(float degreesPerTick) {
        return withMode(Mode.HOVER_SPIN).withLockYaw(false).withAutoSpinSpeed(degreesPerTick);
    }

    /**
     * Clamps and quantizes a raw Yaw angle against lock status, range bounds, and snap steps.
     *
     * @param rawYaw raw input yaw angle
     * @return constrained yaw angle (degrees)
     */
    public float clampYaw(float rawYaw) {
        if (lockYaw) return 0.0f;
        if (mode == Mode.AUTO_SPIN || mode == Mode.HOVER_SPIN || (minYaw <= -360.0f && maxYaw >= 360.0f)) {
            return applyStep(MathUtils.normalizeDegrees(rawYaw));
        }
        float clamped = MathUtils.clamp(rawYaw, minYaw, maxYaw);
        return applyStep(clamped);
    }

    /**
     * Clamps and quantizes a raw Pitch angle against lock status, range bounds, and snap steps.
     *
     * @param rawPitch raw input pitch angle
     * @return constrained pitch angle (degrees)
     */
    public float clampPitch(float rawPitch) {
        if (lockPitch) return 0.0f;
        float clamped = MathUtils.clamp(rawPitch, minPitch, maxPitch);
        return applyStep(clamped);
    }

    /**
     * Clamps and quantizes a raw Roll angle against lock status, range bounds, and snap steps.
     *
     * @param rawRoll raw input roll angle
     * @return constrained roll angle (degrees)
     */
    public float clampRoll(float rawRoll) {
        if (lockRoll) return 0.0f;
        float clamped = MathUtils.clamp(rawRoll, minRoll, maxRoll);
        return applyStep(clamped);
    }

    private float applyStep(float value) {
        if (step <= 0.0f) return value;
        return Math.round(value / step) * step;
    }
}
