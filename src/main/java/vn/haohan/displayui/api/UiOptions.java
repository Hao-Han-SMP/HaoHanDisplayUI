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

/**
 * Configuration options governing the rendering scale, interaction bounds, and behaviors of a Display UI.
 * <p>
 * Defines pixel-to-block conversion ratio, maximum player interaction distance, view range,
 * click feedback sound, camera billboard transformation, and double-sided rendering flags.
 *
 * @param pixelsPerBlock     number of UI canvas pixels per Minecraft world block (default 40.0f)
 * @param maxDistance        maximum raycast reach in blocks for player clicks (default 12.0)
 * @param requireFront       whether clicks are restricted to players facing the front of the UI
 * @param viewRange          display entity render distance multiplier (default 0.15f)
 * @param scoreboardTag      scoreboard tag applied to spawned display entities (default "haohan_display_ui")
 * @param cameraTransform    camera orientation mode (FIXED or BILLBOARD)
 * @param clickSound         sound key played on button clicks (e.g., "minecraft:ui.button.click"), or null if disabled
 * @param clickSoundVolume   volume of the click sound (>= 0.0f)
 * @param clickSoundPitch    pitch of the click sound (>= 0.0f)
 * @param cullItemBackfaces  whether backfaces of ItemDisplay nodes are culled to boost rendering performance
 * @param doubleSided        whether the UI renders and accepts clicks from both front and back faces
 * @param mirrorSide         whether the reverse face horizontally flips coordinates so text isn't reversed
 */
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

    /**
     * Constructs options with primary geometry parameters and default sound/camera behavior.
     *
     * @param pixelsPerBlock number of pixels per block
     * @param maxDistance    maximum interaction raycast reach
     * @param requireFront   whether front-facing position is required
     * @param viewRange      display entity view range multiplier
     * @param scoreboardTag  scoreboard tag identifying spawned entities
     */
    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                UiCameraTransform.fixed(), "minecraft:ui.button.click", 0.7f, 1.0f, true, false, false);
    }

    /**
     * Constructs options with camera transform mode and default sound parameters.
     *
     * @param pixelsPerBlock  number of pixels per block
     * @param maxDistance     maximum interaction raycast reach
     * @param requireFront    whether front-facing position is required
     * @param viewRange       display entity view range multiplier
     * @param scoreboardTag   scoreboard tag identifying spawned entities
     * @param cameraTransform billboard / camera transformation mode
     */
    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag, UiCameraTransform cameraTransform) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                cameraTransform, "minecraft:ui.button.click", 0.7f, 1.0f, true, false, false);
    }

    /**
     * Constructs options with customized click sound feedback parameters.
     *
     * @param pixelsPerBlock   number of pixels per block
     * @param maxDistance      maximum interaction raycast reach
     * @param requireFront     whether front-facing position is required
     * @param viewRange        display entity view range multiplier
     * @param scoreboardTag    scoreboard tag identifying spawned entities
     * @param cameraTransform  billboard / camera transformation mode
     * @param clickSound       sound key string
     * @param clickSoundVolume sound volume
     * @param clickSoundPitch  sound pitch
     */
    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag, UiCameraTransform cameraTransform,
                     String clickSound, float clickSoundVolume, float clickSoundPitch) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                cameraTransform, clickSound, clickSoundVolume, clickSoundPitch, true, false, false);
    }

    /**
     * Constructs options with item display backface culling toggle.
     *
     * @param pixelsPerBlock    number of pixels per block
     * @param maxDistance       maximum interaction raycast reach
     * @param requireFront      whether front-facing position is required
     * @param viewRange         display entity view range multiplier
     * @param scoreboardTag     scoreboard tag identifying spawned entities
     * @param cameraTransform   billboard / camera transformation mode
     * @param clickSound        sound key string
     * @param clickSoundVolume  sound volume
     * @param clickSoundPitch   sound pitch
     * @param cullItemBackfaces whether backfaces of ItemDisplays are culled
     */
    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag, UiCameraTransform cameraTransform,
                     String clickSound, float clickSoundVolume, float clickSoundPitch,
                     boolean cullItemBackfaces) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                cameraTransform, clickSound, clickSoundVolume, clickSoundPitch, cullItemBackfaces, false, false);
    }

    /**
     * Constructs options with double-sided rendering toggle.
     *
     * @param pixelsPerBlock    number of pixels per block
     * @param maxDistance       maximum interaction raycast reach
     * @param requireFront      whether front-facing position is required
     * @param viewRange         display entity view range multiplier
     * @param scoreboardTag     scoreboard tag identifying spawned entities
     * @param cameraTransform   billboard / camera transformation mode
     * @param clickSound        sound key string
     * @param clickSoundVolume  sound volume
     * @param clickSoundPitch   sound pitch
     * @param cullItemBackfaces whether backfaces of ItemDisplays are culled
     * @param doubleSided       whether double-sided rendering is enabled
     */
    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag, UiCameraTransform cameraTransform,
                     String clickSound, float clickSoundVolume, float clickSoundPitch,
                     boolean cullItemBackfaces, boolean doubleSided) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                cameraTransform, clickSound, clickSoundVolume, clickSoundPitch, cullItemBackfaces, doubleSided, false);
    }

    /**
     * Creates a copy of these options with a different camera transform mode.
     *
     * @param transform new camera transformation mode
     * @return a new {@link UiOptions} instance
     */
    public UiOptions withCameraTransform(UiCameraTransform transform) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, transform, clickSound, clickSoundVolume, clickSoundPitch,
                cullItemBackfaces, doubleSided, mirrorSide);
    }

    /**
     * Creates a copy of these options with customized click sound feedback.
     *
     * @param sound  Minecraft sound key (e.g. "minecraft:ui.button.click")
     * @param volume sound volume
     * @param pitch  sound pitch
     * @return a new {@link UiOptions} instance
     */
    public UiOptions withClickSound(String sound, float volume, float pitch) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, cameraTransform, sound, volume, pitch, cullItemBackfaces, doubleSided, mirrorSide);
    }

    /**
     * Creates a copy of these options with click sound feedback disabled.
     *
     * @return a new {@link UiOptions} instance without click sounds
     */
    public UiOptions withoutClickSound() {
        return withClickSound(null, clickSoundVolume, clickSoundPitch);
    }

    /**
     * Creates a copy of these options with item backface culling enabled or disabled.
     *
     * @param enabled {@code true} to cull item backfaces
     * @return a new {@link UiOptions} instance
     */
    public UiOptions withItemBackfaceCulling(boolean enabled) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, cameraTransform, clickSound, clickSoundVolume,
                clickSoundPitch, enabled, doubleSided, mirrorSide);
    }

    /**
     * Creates a copy of these options with double-sided rendering enabled or disabled.
     *
     * @param enabled {@code true} to render both front and back faces
     * @return a new {@link UiOptions} instance
     */
    public UiOptions withDoubleSided(boolean enabled) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, cameraTransform, clickSound, clickSoundVolume,
                clickSoundPitch, cullItemBackfaces, enabled, mirrorSide);
    }

    /**
     * Creates a copy of these options with reverse side horizontal mirroring configured.
     *
     * @param enabled {@code true} to flip coordinates horizontally on the back face
     * @return a new {@link UiOptions} instance
     */
    public UiOptions withMirrorSide(boolean enabled) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, cameraTransform, clickSound, clickSoundVolume,
                clickSoundPitch, cullItemBackfaces, doubleSided, enabled);
    }

    /**
     * Creates a copy of these options configuring both double-sided rendering and back-side mirroring.
     *
     * @param doubleSided whether double-sided rendering is enabled
     * @param mirrorSide  whether horizontal mirroring is enabled on the reverse face
     * @return a new {@link UiOptions} instance
     */
    public UiOptions withSides(boolean doubleSided, boolean mirrorSide) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, cameraTransform, clickSound, clickSoundVolume,
                clickSoundPitch, cullItemBackfaces, doubleSided, mirrorSide);
    }

    /**
     * Returns default production-ready options for typical Display UI installations.
     *
     * @return standard {@link UiOptions}
     */
    public static UiOptions defaults() {
        return new UiOptions(40.0f, 12.0, true, 0.15f, "haohan_display_ui",
                UiCameraTransform.fixed(), "minecraft:ui.button.click", 0.7f, 1.0f, true, false, false);
    }
}

