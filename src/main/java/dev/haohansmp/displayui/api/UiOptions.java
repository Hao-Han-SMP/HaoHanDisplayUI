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

import dev.haohansmp.displayui.api.layout.UiCameraTransform;

/** Runtime behavior shared by every node in a display group. */
public record UiOptions(
        float pixelsPerBlock,
        double maxDistance,
        boolean requireFront,
        float viewRange,
        String scoreboardTag,
        UiCameraTransform cameraTransform
) {
    public UiOptions {
        if (pixelsPerBlock <= 0.0f) throw new IllegalArgumentException("pixelsPerBlock must be positive");
        if (maxDistance <= 0.0) throw new IllegalArgumentException("maxDistance must be positive");
        if (viewRange <= 0.0f) throw new IllegalArgumentException("viewRange must be positive");
        if (scoreboardTag == null || scoreboardTag.isBlank()) scoreboardTag = "haohan_display_ui";
        if (cameraTransform == null) cameraTransform = UiCameraTransform.fixed();
    }

    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                UiCameraTransform.fixed());
    }

    public UiOptions withCameraTransform(UiCameraTransform transform) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, transform);
    }

    public static UiOptions defaults() {
        return new UiOptions(40.0f, 8.0, true, 0.15f, "haohan_display_ui",
                UiCameraTransform.fixed());
    }
}
