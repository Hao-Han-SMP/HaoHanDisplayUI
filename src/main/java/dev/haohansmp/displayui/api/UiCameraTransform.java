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
package dev.haohansmp.displayui.api;

import org.bukkit.entity.Display;

/**
 * Camera-facing constraints and local Euler-angle corrections for a UI scene.
 * Locked axes remain fixed relative to the scene origin.
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

    public static UiCameraTransform fixed() {
        return new UiCameraTransform(true, true, true, 0, 0, 0);
    }

    public static UiCameraTransform cameraFacing() {
        return new UiCameraTransform(false, false, true, 0, 0, 0);
    }

    public UiCameraTransform locks(boolean x, boolean y, boolean z) {
        return new UiCameraTransform(x, y, z, angleX, angleY, angleZ);
    }

    public UiCameraTransform lockX(boolean locked) {
        return locks(locked, lockY, lockZ);
    }

    public UiCameraTransform lockY(boolean locked) {
        return locks(lockX, locked, lockZ);
    }

    public UiCameraTransform lockZ(boolean locked) {
        return locks(lockX, lockY, locked);
    }

    public UiCameraTransform angles(float xDegrees, float yDegrees, float zDegrees) {
        return new UiCameraTransform(lockX, lockY, lockZ,
                xDegrees, yDegrees, zDegrees);
    }

    public UiCameraTransform angle(float degrees) {
        return angles(angleX, degrees, angleZ);
    }

    public UiCameraTransform angleX(float degrees) {
        return angles(degrees, angleY, angleZ);
    }

    public UiCameraTransform angleY(float degrees) {
        return angles(angleX, degrees, angleZ);
    }

    public UiCameraTransform angleZ(float degrees) {
        return angles(angleX, angleY, degrees);
    }

    public Display.Billboard billboard() {
        if (lockX && lockY) return Display.Billboard.FIXED;
        if (lockX) return Display.Billboard.VERTICAL;
        if (lockY) return Display.Billboard.HORIZONTAL;
        return Display.Billboard.CENTER;
    }
}
