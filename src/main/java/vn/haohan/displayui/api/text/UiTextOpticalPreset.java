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
 * Manual optical X corrections for Minecraft text whose visible glyph edge
 * differs from its measured layout edge.
 */
public enum UiTextOpticalPreset {
    ITALIC(-1.0f),
    PLAIN(0.0f),
    GRADIENT(1.0f),
    BOLD(2.0f),
    BOLD_GRADIENT(3.0f);

    private final float xOffset;

    UiTextOpticalPreset(float xOffset) {
        this.xOffset = xOffset;
    }

    public float xOffset() {
        return xOffset;
    }
}
