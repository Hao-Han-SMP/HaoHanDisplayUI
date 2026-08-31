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
package vn.haohan.displayui.api.node;

import vn.haohan.displayui.api.layout.UiRect;
import vn.haohan.displayui.api.text.UiText;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.text.UiTextOpticalPreset;
import vn.haohan.displayui.api.text.UiVerticalAlignment;
import net.kyori.adventure.text.Component;

import java.util.Objects;

/**
 * Text laid out inside a logical-pixel rectangle, analogous to a 2D UI
 * drawAlignedString helper. Native client alignment performs text measurement.
 */
public record AlignedTextNode(
        Component text,
        float boxX,
        float boxY,
        float width,
        float height,
        float depth,
        UiTextAlignment alignment,
        float leftOffset,
        float rightOffset,
        float fontSize,
        float contentWidth,
        UiVerticalAlignment verticalAlignment,
        float verticalOffset,
        boolean shadow,
        boolean seeThrough
) implements UiNode {
    public AlignedTextNode {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(alignment, "alignment");
        Objects.requireNonNull(verticalAlignment, "verticalAlignment");
        if (width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("text box dimensions must be positive");
        }
        if (fontSize <= 0.0f) throw new IllegalArgumentException("fontSize must be positive");
        if (contentWidth <= 0.0f) throw new IllegalArgumentException("contentWidth must be positive");
        if (leftOffset < 0.0f || rightOffset < 0.0f) {
            throw new IllegalArgumentException("text offsets cannot be negative");
        }
    }

    public AlignedTextNode(Component text, float x, float y, float width, float height,
                           UiTextAlignment alignment) {
        this(text, x, y, width, height, 0.002f, alignment,
                0.0f, 0.0f, 10.0f, UiText.estimateWidth(text, 10.0f),
                UiVerticalAlignment.CENTER, 0.0f, false, false);
    }

    public AlignedTextNode(Component text, UiRect bounds, UiTextAlignment alignment) {
        this(text, Objects.requireNonNull(bounds, "bounds").x(), bounds.y(),
                bounds.width(), bounds.height(), alignment);
    }

    public AlignedTextNode(Component text, float boxX, float boxY, float width,
                           float height, float depth, UiTextAlignment alignment,
                           float leftOffset, float rightOffset, float fontSize,
                           float contentWidth, float verticalOffset,
                           boolean shadow, boolean seeThrough) {
        this(text, boxX, boxY, width, height, depth, alignment, leftOffset,
                rightOffset, fontSize, contentWidth, UiVerticalAlignment.CENTER,
                verticalOffset, shadow, seeThrough);
    }

    @Override
    public float x() {
        return switch (alignment) {
            case LEFT -> boxX + leftOffset + contentWidth * 0.5f;
            case RIGHT -> boxX + width - rightOffset - contentWidth * 0.5f;
            case CENTER -> boxX + width * 0.5f;
        };
    }

    @Override
    public float y() {
        float contentCenter = switch (verticalAlignment) {
            case TOP -> boxY + fontSize * 0.5f;
            case CENTER -> boxY + height * 0.5f;
            case BOTTOM -> boxY + height - fontSize * 0.5f;
        };
        // In Minecraft TextDisplay, character glyphs sit above the local baseline origin.
        // Offsetting by ~0.60 * fontSize aligns the visual optical center of the
        // text glyphs with the geometric midpoint of adjacent icons and bounding boxes.
        return contentCenter + (fontSize * 0.60f) + verticalOffset;
    }

    public AlignedTextNode offsets(float left, float right) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, left, right, fontSize, contentWidth,
                verticalAlignment, verticalOffset, shadow, seeThrough);
    }

    public AlignedTextNode offset(float bothSides) {
        return offsets(bothSides, bothSides);
    }

    public AlignedTextNode nudgeX(float pixels) {
        return new AlignedTextNode(text, boxX + pixels, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                verticalAlignment, verticalOffset, shadow, seeThrough);
    }

    public AlignedTextNode opticalPreset(UiTextOpticalPreset preset) {
        return nudgeX(Objects.requireNonNull(preset, "preset").xOffset());
    }

    public AlignedTextNode fontSize(float size) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, size,
                UiText.estimateWidth(text, size), verticalAlignment, verticalOffset,
                shadow, seeThrough);
    }

    public AlignedTextNode verticalOffset(float offset) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                verticalAlignment, offset, shadow, seeThrough);
    }

    public AlignedTextNode shadowed(boolean shadow) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                verticalAlignment, verticalOffset, shadow, seeThrough);
    }

    public AlignedTextNode seeThrough(boolean seeThrough) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                verticalAlignment, verticalOffset, shadow, seeThrough);
    }

    public AlignedTextNode atDepth(float depth) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                verticalAlignment, verticalOffset, shadow, seeThrough);
    }

    public AlignedTextNode align(UiVerticalAlignment verticalAlignment) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                Objects.requireNonNull(verticalAlignment, "verticalAlignment"),
                verticalOffset, shadow, seeThrough);
    }

    public AlignedTextNode top() { return align(UiVerticalAlignment.TOP); }
    public AlignedTextNode centerVertical() { return align(UiVerticalAlignment.CENTER); }
    public AlignedTextNode bottom() { return align(UiVerticalAlignment.BOTTOM); }

    public float boxWidth() { return width; }
    public float boxHeight() { return height; }
}
