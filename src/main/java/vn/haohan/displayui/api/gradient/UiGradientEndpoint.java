/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.gradient;

import org.bukkit.Color;

import java.util.Objects;

/**
 * An endpoint defining one end of a gradient: a normalized position (u, v) and a color.
 * Coordinates typically range from 0.0 (left/top) to 1.0 (right/bottom).
 */
public record UiGradientEndpoint(float u, float v, Color color) {
    public UiGradientEndpoint {
        color = Objects.requireNonNull(color, "color");
    }

    public static UiGradientEndpoint of(UiGradientPosition position, Color color) {
        Objects.requireNonNull(position, "position");
        return new UiGradientEndpoint(position.u(), position.v(), color);
    }

    public static UiGradientEndpoint of(float u, float v, Color color) {
        return new UiGradientEndpoint(u, v, color);
    }
}
