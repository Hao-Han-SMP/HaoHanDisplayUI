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
package vn.haohan.displayui.api;

import vn.haohan.displayui.api.layout.UiCameraTransform;

/** Runtime behavior shared by every node in a display group. */
public record UiOptions(
        float pixelsPerBlock,
        double maxDistance,
        boolean requireFront,
        float viewRange,
        String scoreboardTag,
        UiCameraTransform cameraTransform,
        String clickSound,
        float clickSoundVolume,
        float clickSoundPitch,
        boolean cullItemBackfaces,
        boolean doubleSided,
        boolean mirrorSide
) {
    public UiOptions {
        if (pixelsPerBlock <= 0.0f) throw new IllegalArgumentException("pixelsPerBlock must be positive");
        if (maxDistance <= 0.0) throw new IllegalArgumentException("maxDistance must be positive");
        if (viewRange <= 0.0f) throw new IllegalArgumentException("viewRange must be positive");
        if (scoreboardTag == null || scoreboardTag.isBlank()) scoreboardTag = "haohan_display_ui";
        if (cameraTransform == null) cameraTransform = UiCameraTransform.fixed();
        if (clickSound != null && clickSound.isBlank()) clickSound = null;
        if (!Float.isFinite(clickSoundVolume) || clickSoundVolume < 0.0f) {
            throw new IllegalArgumentException("clickSoundVolume must be finite and non-negative");
        }
        if (!Float.isFinite(clickSoundPitch) || clickSoundPitch < 0.0f) {
            throw new IllegalArgumentException("clickSoundPitch must be finite and non-negative");
        }
    }

    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                UiCameraTransform.fixed(), "minecraft:ui.button.click", 0.7f, 1.0f, true, false, false);
    }

    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag, UiCameraTransform cameraTransform) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                cameraTransform, "minecraft:ui.button.click", 0.7f, 1.0f, true, false, false);
    }

    /** Backward-compatible constructor from before item backface culling and double-sided. */
    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag, UiCameraTransform cameraTransform,
                     String clickSound, float clickSoundVolume, float clickSoundPitch) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                cameraTransform, clickSound, clickSoundVolume, clickSoundPitch, true, false, false);
    }

    /** Backward-compatible constructor with item backface culling flag. */
    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag, UiCameraTransform cameraTransform,
                     String clickSound, float clickSoundVolume, float clickSoundPitch,
                     boolean cullItemBackfaces) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                cameraTransform, clickSound, clickSoundVolume, clickSoundPitch, cullItemBackfaces, false, false);
    }

    /** Backward-compatible constructor with double-sided flag. */
    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag, UiCameraTransform cameraTransform,
                     String clickSound, float clickSoundVolume, float clickSoundPitch,
                     boolean cullItemBackfaces, boolean doubleSided) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                cameraTransform, clickSound, clickSoundVolume, clickSoundPitch, cullItemBackfaces, doubleSided, false);
    }

    public UiOptions withCameraTransform(UiCameraTransform transform) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, transform, clickSound, clickSoundVolume, clickSoundPitch,
                cullItemBackfaces, doubleSided, mirrorSide);
    }

    /** Sets the sound played after a non-cancelled button/control interaction. */
    public UiOptions withClickSound(String sound, float volume, float pitch) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, cameraTransform, sound, volume, pitch, cullItemBackfaces, doubleSided, mirrorSide);
    }

    /** Disables interaction sounds for this scene. */
    public UiOptions withoutClickSound() {
        return withClickSound(null, clickSoundVolume, clickSoundPitch);
    }

    /** Enables software backface culling for fixed ItemDisplay/Icon nodes. */
    public UiOptions withItemBackfaceCulling(boolean enabled) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, cameraTransform, clickSound, clickSoundVolume,
                clickSoundPitch, enabled, doubleSided, mirrorSide);
    }

    /** Sets whether the UI should render and accept interaction from both sides. */
    public UiOptions withDoubleSided(boolean enabled) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, cameraTransform, clickSound, clickSoundVolume,
                clickSoundPitch, cullItemBackfaces, enabled, mirrorSide);
    }

    /** Sets whether the back side should be mirrored for seamless viewing and interaction. */
    public UiOptions withMirrorSide(boolean enabled) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, cameraTransform, clickSound, clickSoundVolume,
                clickSoundPitch, cullItemBackfaces, doubleSided, enabled);
    }

    /** Configures both two-sided rendering and back-side mirroring together. */
    public UiOptions withSides(boolean doubleSided, boolean mirrorSide) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, cameraTransform, clickSound, clickSoundVolume,
                clickSoundPitch, cullItemBackfaces, doubleSided, mirrorSide);
    }

    public static UiOptions defaults() {
        return new UiOptions(40.0f, 8.0, true, 0.15f, "haohan_display_ui",
                UiCameraTransform.fixed(), "minecraft:ui.button.click", 0.7f, 1.0f, true, false, false);
    }
}
