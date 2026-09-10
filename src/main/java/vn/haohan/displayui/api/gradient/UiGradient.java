/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.gradient;

import org.bukkit.Color;
import vn.haohan.displayui.utils.ColorUtils;
import vn.haohan.displayui.utils.MathUtils;

import java.util.Objects;

/**
 * Mathematical 2-endpoint linear gradient.
 * Calculates color interpolation at any normalized point (u, v) using orthogonal projection
 * onto the vector between start and end endpoints.
 */
public record UiGradient(UiGradientEndpoint start, UiGradientEndpoint end) {
    public UiGradient {
        Objects.requireNonNull(start, "start endpoint cannot be null");
        Objects.requireNonNull(end, "end endpoint cannot be null");
    }

    public static UiGradient of(UiGradientEndpoint start, UiGradientEndpoint end) {
        return new UiGradient(start, end);
    }

    public static UiGradient of(UiGradientPosition startPos, Color startColor,
                                UiGradientPosition endPos, Color endColor) {
        return new UiGradient(
                UiGradientEndpoint.of(startPos, startColor),
                UiGradientEndpoint.of(endPos, endColor)
        );
    }

    public static UiGradient of(float startU, float startV, Color startColor,
                                float endU, float endV, Color endColor) {
        return new UiGradient(
                UiGradientEndpoint.of(startU, startV, startColor),
                UiGradientEndpoint.of(endU, endV, endColor)
        );
    }

    // Common presets parameterized by (color1, color2)
    public static UiGradient horizontal(Color left, Color right) {
        return of(UiGradientPosition.CENTER_LEFT, left, UiGradientPosition.CENTER_RIGHT, right);
    }

    public static UiGradient horizontalReverse(Color right, Color left) {
        return of(UiGradientPosition.CENTER_RIGHT, right, UiGradientPosition.CENTER_LEFT, left);
    }

    public static UiGradient vertical(Color top, Color bottom) {
        return of(UiGradientPosition.CENTER_TOP, top, UiGradientPosition.CENTER_BOTTOM, bottom);
    }

    public static UiGradient verticalReverse(Color bottom, Color top) {
        return of(UiGradientPosition.CENTER_BOTTOM, bottom, UiGradientPosition.CENTER_TOP, top);
    }

    public static UiGradient diagonal(Color topLeft, Color bottomRight) {
        return of(UiGradientPosition.TOP_LEFT, topLeft, UiGradientPosition.BOTTOM_RIGHT, bottomRight);
    }

    public static UiGradient diagonalBottomLeftToTopRight(Color bottomLeft, Color topRight) {
        return of(UiGradientPosition.BOTTOM_LEFT, bottomLeft, UiGradientPosition.TOP_RIGHT, topRight);
    }

    public static UiGradient diagonalTopRightToBottomLeft(Color topRight, Color bottomLeft) {
        return of(UiGradientPosition.TOP_RIGHT, topRight, UiGradientPosition.BOTTOM_LEFT, bottomLeft);
    }

    public static UiGradient diagonalBottomRightToTopLeft(Color bottomRight, Color topLeft) {
        return of(UiGradientPosition.BOTTOM_RIGHT, bottomRight, UiGradientPosition.TOP_LEFT, topLeft);
    }

    public static UiGradient centerLeftToTopRight(Color centerLeft, Color topRight) {
        return of(UiGradientPosition.CENTER_LEFT, centerLeft, UiGradientPosition.TOP_RIGHT, topRight);
    }

    public static UiGradient centerToBottomRight(Color center, Color bottomRight) {
        return of(UiGradientPosition.CENTER, center, UiGradientPosition.BOTTOM_RIGHT, bottomRight);
    }

    public boolean isHorizontal() {
        return Math.abs(start.v() - end.v()) < 1e-5f;
    }

    public boolean isVertical() {
        return Math.abs(start.u() - end.u()) < 1e-5f;
    }

    /**
     * Evaluates the gradient color at normalized coordinates (u, v) in [0.0, 1.0].
     */
    public Color evaluate(float u, float v) {
        float du = end.u() - start.u();
        float dv = end.v() - start.v();
        float lengthSq = du * du + dv * dv;
        if (lengthSq < 1e-7f) {
            return start.color();
        }
        float pu = u - start.u();
        float pv = v - start.v();
        float t = (pu * du + pv * dv) / lengthSq;
        return ColorUtils.interpolate(start.color(), end.color(), MathUtils.clamp(t, 0.0f, 1.0f));
    }
}
