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
package vn.haohan.displayui.api.layout;

import org.bukkit.entity.Display;

/**
 * Camera billboard constraints and local Euler angle rotation adjustments for a Display UI.
 * <p>
 * Controls axis locking (X, Y, Z) to keep a UI static in world space or dynamically oriented towards player cameras.
 *
 * @param lockX  locks X-axis pitch (if {@code true}, UI does not tilt up/down with player pitch)
 * @param lockY  locks Y-axis yaw (if {@code true}, UI does not rotate horizontally with player yaw)
 * @param lockZ  locks Z-axis roll
 * @param angleX pitch offset adjustment in degrees
 * @param angleY yaw offset adjustment in degrees
 * @param angleZ roll offset adjustment in degrees
 */
public record UiCameraTransform(
        boolean lockX,
        boolean lockY,
        boolean lockZ,
        float angleX,
        float angleY,
        float angleZ
) {
    public UiCameraTransform {
        if (!Float.isFinite(angleX) || !Float.isFinite(angleY)
                || !Float.isFinite(angleZ)) {
            throw new IllegalArgumentException("camera angles must be finite");
        }
    }

    /**
     * Creates a fully fixed camera transform (FIXED billboard, all axes locked).
     *
     * @return static {@link UiCameraTransform}
     */
    public static UiCameraTransform fixed() {
        return new UiCameraTransform(true, true, true, 0, 0, 0);
    }

    /**
     * Creates a camera transform dynamically facing the player's view (CENTER billboard).
     *
     * @return camera-facing {@link UiCameraTransform}
     */
    public static UiCameraTransform cameraFacing() {
        return new UiCameraTransform(false, false, true, 0, 0, 0);
    }

    /**
     * Creates a copy of this transform with modified axis locks.
     *
     * @param x whether X-axis is locked
     * @param y whether Y-axis is locked
     * @param z whether Z-axis is locked
     * @return a new {@link UiCameraTransform} instance
     */
    public UiCameraTransform locks(boolean x, boolean y, boolean z) {
        return new UiCameraTransform(x, y, z, angleX, angleY, angleZ);
    }

    /**
     * Creates a copy of this transform with X-axis lock modified.
     *
     * @param locked {@code true} to lock X-axis
     * @return a new {@link UiCameraTransform} instance
     */
    public UiCameraTransform lockX(boolean locked) {
        return locks(locked, lockY, lockZ);
    }

    /**
     * Creates a copy of this transform with Y-axis lock modified.
     *
     * @param locked {@code true} to lock Y-axis
     * @return a new {@link UiCameraTransform} instance
     */
    public UiCameraTransform lockY(boolean locked) {
        return locks(lockX, locked, lockZ);
    }

    /**
     * Creates a copy of this transform with Z-axis lock modified.
     *
     * @param locked {@code true} to lock Z-axis
     * @return a new {@link UiCameraTransform} instance
     */
    public UiCameraTransform lockZ(boolean locked) {
        return locks(lockX, lockY, locked);
    }

    /**
     * Creates a copy of this transform with all three Euler rotation angles modified.
     *
     * @param xDegrees pitch adjustment in degrees
     * @param yDegrees yaw adjustment in degrees
     * @param zDegrees roll adjustment in degrees
     * @return a new {@link UiCameraTransform} instance
     */
    public UiCameraTransform angles(float xDegrees, float yDegrees, float zDegrees) {
        return new UiCameraTransform(lockX, lockY, lockZ,
                xDegrees, yDegrees, zDegrees);
    }

    /**
     * Creates a copy of this transform with modified Y-axis yaw angle.
     *
     * @param degrees yaw adjustment in degrees
     * @return a new {@link UiCameraTransform} instance
     */
    public UiCameraTransform angle(float degrees) {
        return angles(angleX, degrees, angleZ);
    }

    /**
     * Creates a copy of this transform with modified X-axis pitch angle.
     *
     * @param degrees pitch adjustment in degrees
     * @return a new {@link UiCameraTransform} instance
     */
    public UiCameraTransform angleX(float degrees) {
        return angles(degrees, angleY, angleZ);
    }

    /**
     * Creates a copy of this transform with modified Y-axis yaw angle.
     *
     * @param degrees yaw adjustment in degrees
     * @return a new {@link UiCameraTransform} instance
     */
    public UiCameraTransform angleY(float degrees) {
        return angles(angleX, degrees, angleZ);
    }

    /**
     * Creates a copy of this transform with modified Z-axis roll angle.
     *
     * @param degrees roll adjustment in degrees
     * @return a new {@link UiCameraTransform} instance
     */
    public UiCameraTransform angleZ(float degrees) {
        return angles(angleX, angleY, degrees);
    }

    /**
     * Maps the axis locking configuration to Bukkit's {@link Display.Billboard} enum.
     *
     * @return corresponding {@link Display.Billboard} mode (FIXED, VERTICAL, HORIZONTAL, CENTER)
     */
    public Display.Billboard billboard() {
        if (lockX && lockY) return Display.Billboard.FIXED;
        if (lockX) return Display.Billboard.VERTICAL;
        if (lockY) return Display.Billboard.HORIZONTAL;
        return Display.Billboard.CENTER;
    }
}

