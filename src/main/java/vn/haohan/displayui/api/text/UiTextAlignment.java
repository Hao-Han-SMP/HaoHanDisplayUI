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
 * Text alignment options for 2D UI text bounding boxes and TextDisplay entities.
 */
public enum UiTextAlignment {
    LEFT,
    RIGHT,
    CENTER;

    /**
     * Calculates the local X offset within a bounding box of given width for content of given width.
     *
     * @param boxWidth width of the container box
     * @param contentWidth measured width of the content
     * @return X offset relative to box origin
     */
    public float calculateOffset(float boxWidth, float contentWidth) {
        return switch (this) {
            case LEFT -> contentWidth * 0.5f;
            case RIGHT -> boxWidth - contentWidth * 0.5f;
            case CENTER -> boxWidth * 0.5f;
        };
    }

    /**
     * Converts to Bukkit's {@link TextDisplay.TextAlignment} representation.
     */
    public TextDisplay.TextAlignment toBukkit() {
        return switch (this) {
            case LEFT -> TextDisplay.TextAlignment.LEFT;
            case RIGHT -> TextDisplay.TextAlignment.RIGHT;
            case CENTER -> TextDisplay.TextAlignment.CENTER;
        };
    }
}
