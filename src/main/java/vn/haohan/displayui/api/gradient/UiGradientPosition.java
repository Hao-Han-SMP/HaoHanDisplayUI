/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.gradient;

import org.bukkit.Color;

/**
 * Normalized 2D positions for UI gradient endpoints.
 * Coordinates range from u in [0.0, 1.0] (left to right) and v in [0.0, 1.0] (top to bottom).
 */
public enum UiGradientPosition {
    TOP_LEFT(0.0f, 0.0f),
    CENTER_LEFT(0.0f, 0.5f),
    BOTTOM_LEFT(0.0f, 1.0f),
    TOP_RIGHT(1.0f, 0.0f),
    CENTER_RIGHT(1.0f, 0.5f),
    BOTTOM_RIGHT(1.0f, 1.0f),
    CENTER_TOP(0.5f, 0.0f),
    CENTER_BOTTOM(0.5f, 1.0f),
    CENTER(0.5f, 0.5f);

    private final float u;
    private final float v;

    UiGradientPosition(float u, float v) {
        this.u = u;
        this.v = v;
    }

    public float u() {
        return u;
    }

    public float v() {
        return v;
    }

    public UiGradientEndpoint withColor(Color color) {
        return UiGradientEndpoint.of(this, color);
    }
}
