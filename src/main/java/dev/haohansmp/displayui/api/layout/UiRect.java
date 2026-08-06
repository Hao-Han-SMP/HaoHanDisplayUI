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
package dev.haohansmp.displayui.api.layout;

import java.util.Objects;

/**
 * Immutable rectangle in scene logical pixels. The stored x/y coordinates
 * are its visual top-left corner, matching icon, text-box and hit-zone bounds.
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

    public static UiRect centered(float centerX, float centerY,
                                  float width, float height) {
        return new UiRect(centerX - width * 0.5f, centerY - height * 0.5f,
                width, height);
    }

    public float left() {
        return x;
    }

    public float top() {
        return y;
    }

    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }

    public float centerX() {
        return x + width * 0.5f;
    }

    public float centerY() {
        return y + height * 0.5f;
    }

    public float anchorX(UiAnchor anchor) {
        return x + width * Objects.requireNonNull(anchor, "anchor").xFactor();
    }

    public float anchorY(UiAnchor anchor) {
        return y + height * Objects.requireNonNull(anchor, "anchor").yFactor();
    }

    public UiRect translate(float offsetX, float offsetY) {
        return new UiRect(x + offsetX, y + offsetY, width, height);
    }

    public UiRect inset(float pixels) {
        return inset(pixels, pixels, pixels, pixels);
    }

    public UiRect inset(float horizontal, float vertical) {
        return inset(horizontal, vertical, horizontal, vertical);
    }

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
     * Places a child rectangle by joining one of its anchors to an anchor of
     * this rectangle, then applying a logical-pixel offset.
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
