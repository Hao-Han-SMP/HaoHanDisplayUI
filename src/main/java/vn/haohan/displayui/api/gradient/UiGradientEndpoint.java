/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.gradient;

import org.bukkit.Color;

import java.util.Objects;

/**
 * Defines an endpoint of a color gradient: comprising normalized 2D coordinates (u, v) and a Bukkit {@link Color}.
 * <p>
 * Normalized coordinates typically range from {@code 0.0f} (left / top) to {@code 1.0f} (right / bottom).
 *
 * @param u     normalized horizontal coordinate [0.0f - 1.0f]
 * @param v     normalized vertical coordinate [0.0f - 1.0f]
 * @param color color at this endpoint
 */
public record UiGradientEndpoint(float u, float v, Color color) {
    public UiGradientEndpoint {
        color = Objects.requireNonNull(color, "color");
    }

    /**
     * Creates an endpoint from a preset position {@link UiGradientPosition} and a color.
     *
     * @param position preset anchor position
     * @param color    endpoint color
     * @return a new {@link UiGradientEndpoint} instance
     */
    public static UiGradientEndpoint of(UiGradientPosition position, Color color) {
        Objects.requireNonNull(position, "position");
        return new UiGradientEndpoint(position.u(), position.v(), color);
    }

    /**
     * Creates an endpoint from arbitrary normalized coordinates (u, v) and a color.
     *
     * @param u     normalized horizontal coordinate
     * @param v     normalized vertical coordinate
     * @param color endpoint color
     * @return a new {@link UiGradientEndpoint} instance
     */
    public static UiGradientEndpoint of(float u, float v, Color color) {
        return new UiGradientEndpoint(u, v, color);
    }
}
