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
package vn.haohan.displayui.api.text;

/**
 * Optical horizontal offset presets for Minecraft typography rendering nuances.
 * <p>
 * In Minecraft, visual glyph bounds (italic slant, bold shadows, gradient color tags)
 * can deviate slightly from the geometric layout box. These optical presets compensate for those micro-alignments.
 */
public enum UiTextOpticalPreset {
    /** Compensation offset for italicized text. */
    ITALIC(-1.0f),
    /** No offset adjustment (standard plain text). */
    PLAIN(0.0f),
    /** Compensation offset for gradient text runs. */
    GRADIENT(1.0f),
    /** Compensation offset for bold typeface styling. */
    BOLD(2.0f),
    /** Compensation offset for bold text with gradient styling. */
    BOLD_GRADIENT(3.0f);

    private final float xOffset;

    UiTextOpticalPreset(float xOffset) {
        this.xOffset = xOffset;
    }

    /**
     * Returns the horizontal optical compensation offset in UI pixels.
     *
     * @return X-offset adjustment in UI pixels
     */
    public float xOffset() {
        return xOffset;
    }
}

