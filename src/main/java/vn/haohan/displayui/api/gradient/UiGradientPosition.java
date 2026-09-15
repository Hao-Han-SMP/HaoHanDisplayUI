/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.gradient;

import org.bukkit.Color;

/**
 * Normalized 2D gradient endpoint anchor positions.
 * <p>
 * Horizontal axis {@code u} spans [0.0, 1.0] (left to right) and vertical axis {@code v} spans [0.0, 1.0] (top to bottom).
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

    /**
     * Normalized horizontal coordinate {@code u} (0.0f on the left to 1.0f on the right).
     *
     * @return u coordinate
     */
    public float u() {
        return u;
    }

    /**
     * Normalized vertical coordinate {@code v} (0.0f on the top edge to 1.0f on the bottom edge).
     *
     * @return v coordinate
     */
    public float v() {
        return v;
    }

    /**
     * Combines this normalized anchor position with a specified color to create a {@link UiGradientEndpoint}.
     *
     * @param color endpoint color
     * @return a new {@link UiGradientEndpoint} instance
     */
    public UiGradientEndpoint withColor(Color color) {
        return UiGradientEndpoint.of(this, color);
    }
}
