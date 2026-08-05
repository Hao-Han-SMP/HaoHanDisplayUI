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

    /** Fixed in world space with no local angle correction. */
    public static UiCameraTransform fixed() {
        return new UiCameraTransform(true, true, true, 0, 0, 0);
    }

    /** Follows camera yaw and pitch. Minecraft cameras do not expose roll. */
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
