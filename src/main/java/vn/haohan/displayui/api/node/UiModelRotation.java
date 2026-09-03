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
 * Configuration and constraints for rotating a 3D entity model in UI.
 * Allows locking individual axes (yaw, pitch, roll), clamping angle ranges,
 * quantizing rotation steps, and choosing interactive rotation modes.
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
    public enum Mode {
        /** Tilts/rotates smoothly following the player's cursor relative to model center. */
        CURSOR_TRACKING,
        /** Continuously spins while the player is hovering over the model. */
        HOVER_SPIN,
        /** Continuously spins whether hovered or not. */
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

    public static UiModelRotation yawOnly() {
        return defaults().withLockPitch(true).withLockRoll(true);
    }

    public static UiModelRotation pitchOnly() {
        return defaults().withLockYaw(true).withLockPitch(false).withLockRoll(true);
    }

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

    public UiModelRotation withMode(Mode nextMode) {
        return new UiModelRotation(nextMode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, minPitch, maxPitch, minRoll, maxRoll,
                step, sensitivity, autoSpinSpeed);
    }

    public UiModelRotation withLocks(boolean yaw, boolean pitch, boolean roll) {
        return new UiModelRotation(mode, yaw, pitch, roll,
                minYaw, maxYaw, minPitch, maxPitch, minRoll, maxRoll,
                step, sensitivity, autoSpinSpeed);
    }

    public UiModelRotation withLockYaw(boolean locked) {
        return withLocks(locked, lockPitch, lockRoll);
    }

    public UiModelRotation withLockPitch(boolean locked) {
        return withLocks(lockYaw, locked, lockRoll);
    }

    public UiModelRotation withLockRoll(boolean locked) {
        return withLocks(lockYaw, lockPitch, locked);
    }

    public UiModelRotation withYawRange(float min, float max) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                min, max, minPitch, maxPitch, minRoll, maxRoll,
                step, sensitivity, autoSpinSpeed);
    }

    public UiModelRotation withPitchRange(float min, float max) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, min, max, minRoll, maxRoll,
                step, sensitivity, autoSpinSpeed);
    }

    public UiModelRotation withRollRange(float min, float max) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, minPitch, maxPitch, min, max,
                step, sensitivity, autoSpinSpeed);
    }

    public UiModelRotation withStep(float stepAngle) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, minPitch, maxPitch, minRoll, maxRoll,
                stepAngle, sensitivity, autoSpinSpeed);
    }

    public UiModelRotation withSensitivity(float factor) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, minPitch, maxPitch, minRoll, maxRoll,
                step, factor, autoSpinSpeed);
    }

    public UiModelRotation withAutoSpinSpeed(float speed) {
        return new UiModelRotation(mode, lockYaw, lockPitch, lockRoll,
                minYaw, maxYaw, minPitch, maxPitch, minRoll, maxRoll,
                step, sensitivity, speed);
    }

    public UiModelRotation withAutoSpin(float degreesPerTick) {
        return withMode(Mode.AUTO_SPIN).withLockYaw(false).withAutoSpinSpeed(degreesPerTick);
    }

    public UiModelRotation withHoverSpin(float degreesPerTick) {
        return withMode(Mode.HOVER_SPIN).withLockYaw(false).withAutoSpinSpeed(degreesPerTick);
    }

    public float clampYaw(float rawYaw) {
        if (lockYaw) return 0.0f;
        if (mode == Mode.AUTO_SPIN || mode == Mode.HOVER_SPIN || (minYaw <= -360.0f && maxYaw >= 360.0f)) {
            return applyStep(MathUtils.normalizeDegrees(rawYaw));
        }
        float clamped = MathUtils.clamp(rawYaw, minYaw, maxYaw);
        return applyStep(clamped);
    }

    public float clampPitch(float rawPitch) {
        if (lockPitch) return 0.0f;
        float clamped = MathUtils.clamp(rawPitch, minPitch, maxPitch);
        return applyStep(clamped);
    }

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
