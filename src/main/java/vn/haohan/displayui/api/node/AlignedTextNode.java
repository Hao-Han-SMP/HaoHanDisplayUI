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
 * A text node laid out and aligned inside a 2D rectangular bounding box (similar to drawAlignedString).
 * <p>
 * Supports horizontal alignment (LEFT, CENTER, RIGHT), vertical alignment (TOP, CENTER, BOTTOM),
 * font scaling, drop shadows, see-through projection through obstacles, and optical kerning tweaks.
 *
 * @param text              the Adventure {@link Component} text content
 * @param boxX              the X coordinate of the top-left corner of the container (pixels)
 * @param boxY              the Y coordinate of the top-left corner of the container (pixels)
 * @param width             the container width (pixels, > 0)
 * @param height            the container height (pixels, > 0)
 * @param depth             the layer Z-depth (pixels)
 * @param alignment         the horizontal alignment strategy (LEFT, RIGHT, CENTER)
 * @param leftOffset        the left margin padding offset (pixels)
 * @param rightOffset       the right margin padding offset (pixels)
 * @param fontSize          the logical font size (UI pixels, > 0)
 * @param contentWidth      the measured width of the rendered text content (pixels)
 * @param verticalAlignment the vertical alignment strategy (TOP, CENTER, BOTTOM)
 * @param verticalOffset    additional vertical baseline offset (pixels)
 * @param shadow            whether drop shadows are enabled
 * @param seeThrough        whether text is visible through solid blocks
 * @param doubleSided       whether text renders on both front and back faces
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
        boolean seeThrough,
        boolean doubleSided
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

    /**
     * Constructs a basic aligned text node with default depth, font size, and centering.
     *
     * @param text      the text content component
     * @param x         the top-left X coordinate of the container (pixels)
     * @param y         the top-left Y coordinate of the container (pixels)
     * @param width     the container width (pixels)
     * @param height    the container height (pixels)
     * @param alignment the horizontal alignment strategy
     */
    public AlignedTextNode(Component text, float x, float y, float width, float height,
                           UiTextAlignment alignment) {
        this(text, x, y, width, height, 0.002f, alignment,
                0.0f, 0.0f, 10.0f, UiText.estimateWidth(text, 10.0f),
                UiVerticalAlignment.CENTER, 0.0f, false, false, false);
    }

    /**
     * Constructs an aligned text node using bounding rectangle {@link UiRect}.
     *
     * @param text      the text content component
     * @param bounds    the bounding rectangle
     * @param alignment the horizontal alignment strategy
     */
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
                verticalOffset, shadow, seeThrough, false);
    }

    public AlignedTextNode(Component text, float boxX, float boxY, float width,
                           float height, float depth, UiTextAlignment alignment,
                           float leftOffset, float rightOffset, float fontSize,
                           float contentWidth, UiVerticalAlignment verticalAlignment,
                           float verticalOffset, boolean shadow, boolean seeThrough) {
        this(text, boxX, boxY, width, height, depth, alignment, leftOffset,
                rightOffset, fontSize, contentWidth, verticalAlignment,
                verticalOffset, shadow, seeThrough, false);
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

    /**
     * Sets asymmetric horizontal padding offsets for left and right edges.
     *
     * @param left  the left offset padding (pixels)
     * @param right the right offset padding (pixels)
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode offsets(float left, float right) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, left, right, fontSize, contentWidth,
                verticalAlignment, verticalOffset, shadow, seeThrough, doubleSided);
    }

    /**
     * Sets symmetric horizontal padding offset for both left and right edges.
     *
     * @param bothSides the offset padding applied to both sides (pixels)
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode offset(float bothSides) {
        return offsets(bothSides, bothSides);
    }

    /**
     * Nudges the container X coordinate by a given amount of pixels.
     *
     * @param pixels the amount of pixels to shift along the X-axis
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode nudgeX(float pixels) {
        return new AlignedTextNode(text, boxX + pixels, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                verticalAlignment, verticalOffset, shadow, seeThrough, doubleSided);
    }

    /**
     * Applies an optical alignment preset {@link UiTextOpticalPreset}.
     *
     * @param preset the optical compensation preset
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode opticalPreset(UiTextOpticalPreset preset) {
        return nudgeX(Objects.requireNonNull(preset, "preset").xOffset());
    }

    /**
     * Updates the font size and recalculates the estimated content width.
     *
     * @param size the new font size in logical pixels
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode fontSize(float size) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, size,
                UiText.estimateWidth(text, size), verticalAlignment, verticalOffset,
                shadow, seeThrough, doubleSided);
    }

    /**
     * Manually overrides the measured content width.
     *
     * @param width the content width (pixels)
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode contentWidth(float width) {
        if (width <= 0.0f) throw new IllegalArgumentException("contentWidth must be positive");
        return new AlignedTextNode(text, boxX, boxY, this.width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, width,
                verticalAlignment, verticalOffset, shadow, seeThrough, doubleSided);
    }

    /**
     * Sets the vertical alignment strategy (TOP, CENTER, BOTTOM).
     *
     * @param verticalAlignment the vertical alignment
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode verticalAlignment(UiVerticalAlignment verticalAlignment) {
        return align(verticalAlignment);
    }

    /**
     * Sets an additional vertical baseline offset.
     *
     * @param offset the vertical offset (pixels)
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode verticalOffset(float offset) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                verticalAlignment, offset, shadow, seeThrough, doubleSided);
    }

    /**
     * Toggles whether the text casts a drop shadow.
     *
     * @param shadow {@code true} to enable drop shadow
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode shadowed(boolean shadow) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                verticalAlignment, verticalOffset, shadow, seeThrough, doubleSided);
    }

    /**
     * Toggles whether the text is visible through solid geometry.
     *
     * @param seeThrough {@code true} to enable see-through mode
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode seeThrough(boolean seeThrough) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                verticalAlignment, verticalOffset, shadow, seeThrough, doubleSided);
    }

    /**
     * Sets the layer Z-depth of the text node.
     *
     * @param depth the layer depth in pixels
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode atDepth(float depth) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                verticalAlignment, verticalOffset, shadow, seeThrough, doubleSided);
    }

    /**
     * Sets the vertical alignment.
     *
     * @param verticalAlignment the vertical alignment
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode align(UiVerticalAlignment verticalAlignment) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                Objects.requireNonNull(verticalAlignment, "verticalAlignment"),
                verticalOffset, shadow, seeThrough, doubleSided);
    }

    /** Aligns text to the top edge. */
    public AlignedTextNode top() { return align(UiVerticalAlignment.TOP); }
    /** Aligns text to the vertical center. */
    public AlignedTextNode centerVertical() { return align(UiVerticalAlignment.CENTER); }
    /** Aligns text to the bottom edge. */
    public AlignedTextNode bottom() { return align(UiVerticalAlignment.BOTTOM); }

    /**
     * Toggles whether the text renders on both front and back faces.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode withDoubleSided(boolean doubleSided) {
        return new AlignedTextNode(text, boxX, boxY, width, height, depth,
                alignment, leftOffset, rightOffset, fontSize, contentWidth,
                verticalAlignment, verticalOffset, shadow, seeThrough, doubleSided);
    }

    /**
     * Toggles whether the text renders on both front and back faces.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new updated {@link AlignedTextNode}
     */
    public AlignedTextNode doubleSided(boolean doubleSided) {
        return withDoubleSided(doubleSided);
    }

    /** Returns the width of the bounding box. */
    public float boxWidth() { return width; }
    /** Returns the height of the bounding box. */
    public float boxHeight() { return height; }
}

