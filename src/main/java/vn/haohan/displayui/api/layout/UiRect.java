/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package vn.haohan.displayui.api.layout;

import java.util.Objects;

/**
 * Immutable 2D layout rectangle measured in logical UI pixels.
 * <p>
 * The coordinates {@code (x, y)} denote the top-left corner of the rectangle,
 * matching coordinate conventions for icons, text boxes, and interactive hit zones.
 *
 * @param x      top-left X coordinate in UI pixels
 * @param y      top-left Y coordinate in UI pixels
 * @param width  rectangle width in UI pixels (must be > 0)
 * @param height rectangle height in UI pixels (must be > 0)
 */
public record UiRect(float x, float y, float width, float height) {
    public UiRect {
        if (!Float.isFinite(x) || !Float.isFinite(y)
                || !Float.isFinite(width) || !Float.isFinite(height)) {
            throw new IllegalArgumentException("rectangle values must be finite");
        }
        if (width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("rectangle dimensions must be positive");
        }
    }

    /**
     * Creates a rectangle centered at the specified coordinates.
     *
     * @param centerX horizontal center coordinate in UI pixels
     * @param centerY vertical center coordinate in UI pixels
     * @param width   width in UI pixels
     * @param height  height in UI pixels
     * @return centered {@link UiRect}
     */
    public static UiRect centered(float centerX, float centerY,
                                  float width, float height) {
        return new UiRect(centerX - width * 0.5f, centerY - height * 0.5f,
                width, height);
    }

    /**
     * Returns the left boundary X-coordinate (X min).
     *
     * @return left coordinate
     */
    public float left() {
        return x;
    }

    /**
     * Returns the top boundary Y-coordinate (Y min).
     *
     * @return top coordinate
     */
    public float top() {
        return y;
    }

    /**
     * Returns the right boundary X-coordinate (X max = x + width).
     *
     * @return right coordinate
     */
    public float right() {
        return x + width;
    }

    /**
     * Returns the bottom boundary Y-coordinate (Y max = y + height).
     *
     * @return bottom coordinate
     */
    public float bottom() {
        return y + height;
    }

    /**
     * Returns the horizontal center X-coordinate.
     *
     * @return center X
     */
    public float centerX() {
        return x + width * 0.5f;
    }

    /**
     * Returns the vertical center Y-coordinate.
     *
     * @return center Y
     */
    public float centerY() {
        return y + height * 0.5f;
    }

    /**
     * Computes the X-coordinate at a given {@link UiAnchor} within this rectangle.
     *
     * @param anchor anchor point to evaluate
     * @return X-coordinate at anchor
     * @throws NullPointerException if {@code anchor} is {@code null}
     */
    public float anchorX(UiAnchor anchor) {
        return x + width * Objects.requireNonNull(anchor, "anchor").xFactor();
    }

    /**
     * Computes the Y-coordinate at a given {@link UiAnchor} within this rectangle.
     *
     * @param anchor anchor point to evaluate
     * @return Y-coordinate at anchor
     * @throws NullPointerException if {@code anchor} is {@code null}
     */
    public float anchorY(UiAnchor anchor) {
        return y + height * Objects.requireNonNull(anchor, "anchor").yFactor();
    }

    /**
     * Offsets this rectangle by (offsetX, offsetY).
     *
     * @param offsetX horizontal translation in UI pixels
     * @param offsetY vertical translation in UI pixels
     * @return translated {@link UiRect}
     */
    public UiRect translate(float offsetX, float offsetY) {
        return new UiRect(x + offsetX, y + offsetY, width, height);
    }

    /**
     * Uniformly insets all four edges of this rectangle by a padding value.
     *
     * @param pixels padding distance for all edges in UI pixels
     * @return inset {@link UiRect}
     */
    public UiRect inset(float pixels) {
        return inset(pixels, pixels, pixels, pixels);
    }

    /**
     * Insets horizontal and vertical edges independently.
     *
     * @param horizontal padding for left and right edges in UI pixels
     * @param vertical   padding for top and bottom edges in UI pixels
     * @return inset {@link UiRect}
     */
    public UiRect inset(float horizontal, float vertical) {
        return inset(horizontal, vertical, horizontal, vertical);
    }

    /**
     * Insets each edge independently by specified values.
     *
     * @param left   left edge inset in UI pixels
     * @param top    top edge inset in UI pixels
     * @param right  right edge inset in UI pixels
     * @param bottom bottom edge inset in UI pixels
     * @return inset {@link UiRect}
     * @throws IllegalArgumentException if any inset value is negative or non-finite
     */
    public UiRect inset(float left, float top, float right, float bottom) {
        if (left < 0.0f || top < 0.0f || right < 0.0f || bottom < 0.0f
                || !Float.isFinite(left) || !Float.isFinite(top)
                || !Float.isFinite(right) || !Float.isFinite(bottom)) {
            throw new IllegalArgumentException("insets must be finite and non-negative");
        }
        return new UiRect(x + left, y + top,
                width - left - right, height - top - bottom);
    }

    /**
     * Positions a child rectangle by aligning a parent anchor with a child anchor,
     * applying an additional translation offset.
     *
     * @param parentAnchor reference anchor on this rectangle
     * @param childAnchor  reference anchor on the child rectangle
     * @param childWidth   width of the child rectangle in UI pixels
     * @param childHeight  height of the child rectangle in UI pixels
     * @param offsetX      additional horizontal offset in UI pixels
     * @param offsetY      additional vertical offset in UI pixels
     * @return positioned child {@link UiRect}
     * @throws NullPointerException if any anchor is {@code null}
     */
    public UiRect place(UiAnchor parentAnchor, UiAnchor childAnchor,
                        float childWidth, float childHeight,
                        float offsetX, float offsetY) {
        Objects.requireNonNull(parentAnchor, "parentAnchor");
        Objects.requireNonNull(childAnchor, "childAnchor");
        float childX = anchorX(parentAnchor)
                - childWidth * childAnchor.xFactor() + offsetX;
        float childY = anchorY(parentAnchor)
                - childHeight * childAnchor.yFactor() + offsetY;
        return new UiRect(childX, childY, childWidth, childHeight);
    }
}

