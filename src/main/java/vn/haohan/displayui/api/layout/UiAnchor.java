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

/**
 * Normalized anchor point within a UI rectangle or bounding box.
 * <p>
 * Defines relative position ratios (ranging from 0.0f to 1.0f) along the X and Y axes
 * for aligning child nodes or pivot points.
 */
public enum UiAnchor {
    /** Top-left corner (0.0, 0.0). */
    TOP_LEFT(0.0f, 0.0f),
    /** Center of top edge (0.5, 0.0). */
    TOP_CENTER(0.5f, 0.0f),
    /** Top-right corner (1.0, 0.0). */
    TOP_RIGHT(1.0f, 0.0f),
    /** Center of left edge (0.0, 0.5). */
    CENTER_LEFT(0.0f, 0.5f),
    /** Center pivot (0.5, 0.5). */
    CENTER(0.5f, 0.5f),
    /** Center of right edge (1.0, 0.5). */
    CENTER_RIGHT(1.0f, 0.5f),
    /** Bottom-left corner (0.0, 1.0). */
    BOTTOM_LEFT(0.0f, 1.0f),
    /** Center of bottom edge (0.5, 1.0). */
    BOTTOM_CENTER(0.5f, 1.0f),
    /** Bottom-right corner (1.0, 1.0). */
    BOTTOM_RIGHT(1.0f, 1.0f);

    private final float xFactor;
    private final float yFactor;

    UiAnchor(float xFactor, float yFactor) {
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }

    /**
     * Returns the horizontal anchor proportion factor (0.0f to 1.0f).
     *
     * @return X-factor ratio
     */
    public float xFactor() {
        return xFactor;
    }

    /**
     * Returns the vertical anchor proportion factor (0.0f to 1.0f).
     *
     * @return Y-factor ratio
     */
    public float yFactor() {
        return yFactor;
    }
}

