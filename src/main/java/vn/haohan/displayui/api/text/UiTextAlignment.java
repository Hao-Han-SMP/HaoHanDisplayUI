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

import org.bukkit.entity.TextDisplay;

/**
 * Horizontal text alignment options for 2D UI text nodes and TextDisplay entities.
 */
public enum UiTextAlignment {
    /** Left alignment. */
    LEFT,
    /** Right alignment. */
    RIGHT,
    /** Center alignment. */
    CENTER;

    /**
     * Calculates the local horizontal offset within a bounding box given the measured text width.
     *
     * @param boxWidth     total container box width in UI pixels
     * @param contentWidth measured rendered width of the text content in UI pixels
     * @return relative X-offset relative to the container origin
     */
    public float calculateOffset(float boxWidth, float contentWidth) {
        return switch (this) {
            case LEFT -> contentWidth * 0.5f;
            case RIGHT -> boxWidth - contentWidth * 0.5f;
            case CENTER -> boxWidth * 0.5f;
        };
    }

    /**
     * Converts this alignment into the corresponding Bukkit {@link TextDisplay.TextAlignment}.
     *
     * @return equivalent Bukkit TextDisplay text alignment enum
     */
    public TextDisplay.TextAlignment toBukkit() {
        return switch (this) {
            case LEFT -> TextDisplay.TextAlignment.LEFT;
            case RIGHT -> TextDisplay.TextAlignment.RIGHT;
            case CENTER -> TextDisplay.TextAlignment.CENTER;
        };
    }
}

