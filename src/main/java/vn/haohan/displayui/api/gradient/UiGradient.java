/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.gradient;

import org.bukkit.Color;
import vn.haohan.displayui.utils.ColorUtils;
import vn.haohan.displayui.utils.MathUtils;

import java.util.Objects;

/**
 * Mathematical model of a two-stop linear color gradient.
 * <p>
 * Evaluates interpolated colors at arbitrary normalized (u, v) coordinates using
 * orthogonal projection onto the directional vector between the start and end endpoints.
 *
 * @param start the start gradient endpoint {@link UiGradientEndpoint}
 * @param end the end gradient endpoint {@link UiGradientEndpoint}
 */
public record UiGradient(UiGradientEndpoint start, UiGradientEndpoint end) {
    public UiGradient {
        Objects.requireNonNull(start, "start endpoint cannot be null");
        Objects.requireNonNull(end, "end endpoint cannot be null");
    }

    /**
     * Creates a gradient from two existing endpoints.
     *
     * @param start the starting endpoint
     * @param end the ending endpoint
     * @return a new {@link UiGradient} instance
     */
    public static UiGradient of(UiGradientEndpoint start, UiGradientEndpoint end) {
        return new UiGradient(start, end);
    }

    /**
     * Creates a gradient from predefined positions and colors.
     *
     * @param startPos the start position anchor
     * @param startColor the start color
     * @param endPos the end position anchor
     * @param endColor the end color
     * @return a new {@link UiGradient} instance
     */
    public static UiGradient of(UiGradientPosition startPos, Color startColor,
                                UiGradientPosition endPos, Color endColor) {
        return new UiGradient(
                UiGradientEndpoint.of(startPos, startColor),
                UiGradientEndpoint.of(endPos, endColor)
        );
    }

    /**
     * Creates a gradient from explicit normalized (u, v) coordinates and colors.
     *
     * @param startU horizontal coordinate of the start point (0.0 to 1.0)
     * @param startV vertical coordinate of the start point (0.0 to 1.0)
     * @param startColor the start color
     * @param endU horizontal coordinate of the end point (0.0 to 1.0)
     * @param endV vertical coordinate of the end point (0.0 to 1.0)
     * @param endColor the end color
     * @return a new {@link UiGradient} instance
     */
    public static UiGradient of(float startU, float startV, Color startColor,
                                float endU, float endV, Color endColor) {
        return new UiGradient(
                UiGradientEndpoint.of(startU, startV, startColor),
                UiGradientEndpoint.of(endU, endV, endColor)
        );
    }

    /**
     * Creates a horizontal gradient from left to right.
     *
     * @param left color on the left side
     * @param right color on the right side
     * @return a new horizontal {@link UiGradient}
     */
    public static UiGradient horizontal(Color left, Color right) {
        return of(UiGradientPosition.CENTER_LEFT, left, UiGradientPosition.CENTER_RIGHT, right);
    }

    /**
     * Creates a reverse horizontal gradient from right to left.
     *
     * @param right color on the right side
     * @param left color on the left side
     * @return a new horizontal {@link UiGradient}
     */
    public static UiGradient horizontalReverse(Color right, Color left) {
        return of(UiGradientPosition.CENTER_RIGHT, right, UiGradientPosition.CENTER_LEFT, left);
    }

    /**
     * Creates a vertical gradient from top to bottom.
     *
     * @param top color at the top
     * @param bottom color at the bottom
     * @return a new vertical {@link UiGradient}
     */
    public static UiGradient vertical(Color top, Color bottom) {
        return of(UiGradientPosition.CENTER_TOP, top, UiGradientPosition.CENTER_BOTTOM, bottom);
    }

    /**
     * Creates a reverse vertical gradient from bottom to top.
     *
     * @param bottom color at the bottom
     * @param top color at the top
     * @return a new vertical {@link UiGradient}
     */
    public static UiGradient verticalReverse(Color bottom, Color top) {
        return of(UiGradientPosition.CENTER_BOTTOM, bottom, UiGradientPosition.CENTER_TOP, top);
    }

    /**
     * Creates a diagonal gradient from top-left to bottom-right.
     *
     * @param topLeft color at top-left
     * @param bottomRight color at bottom-right
     * @return a new diagonal {@link UiGradient}
     */
    public static UiGradient diagonal(Color topLeft, Color bottomRight) {
        return of(UiGradientPosition.TOP_LEFT, topLeft, UiGradientPosition.BOTTOM_RIGHT, bottomRight);
    }

    /**
     * Creates a diagonal gradient from bottom-left to top-right.
     *
     * @param bottomLeft color at bottom-left
     * @param topRight color at top-right
     * @return a new diagonal {@link UiGradient}
     */
    public static UiGradient diagonalBottomLeftToTopRight(Color bottomLeft, Color topRight) {
        return of(UiGradientPosition.BOTTOM_LEFT, bottomLeft, UiGradientPosition.TOP_RIGHT, topRight);
    }

    /**
     * Creates a diagonal gradient from top-right to bottom-left.
     *
     * @param topRight color at top-right
     * @param bottomLeft color at bottom-left
     * @return a new diagonal {@link UiGradient}
     */
    public static UiGradient diagonalTopRightToBottomLeft(Color topRight, Color bottomLeft) {
        return of(UiGradientPosition.TOP_RIGHT, topRight, UiGradientPosition.BOTTOM_LEFT, bottomLeft);
    }

    /**
     * Creates a diagonal gradient from bottom-right to top-left.
     *
     * @param bottomRight color at bottom-right
     * @param topLeft color at top-left
     * @return a new diagonal {@link UiGradient}
     */
    public static UiGradient diagonalBottomRightToTopLeft(Color bottomRight, Color topLeft) {
        return of(UiGradientPosition.BOTTOM_RIGHT, bottomRight, UiGradientPosition.TOP_LEFT, topLeft);
    }

    /**
     * Creates an angled gradient from center-left to top-right.
     *
     * @param centerLeft color at center-left
     * @param topRight color at top-right
     * @return a new angled {@link UiGradient}
     */
    public static UiGradient centerLeftToTopRight(Color centerLeft, Color topRight) {
        return of(UiGradientPosition.CENTER_LEFT, centerLeft, UiGradientPosition.TOP_RIGHT, topRight);
    }

    /**
     * Creates a gradient radiating from center to bottom-right.
     *
     * @param center color at center
     * @param bottomRight color at bottom-right
     * @return a new {@link UiGradient}
     */
    public static UiGradient centerToBottomRight(Color center, Color bottomRight) {
        return of(UiGradientPosition.CENTER, center, UiGradientPosition.BOTTOM_RIGHT, bottomRight);
    }

    /**
     * Checks if this gradient is purely horizontal (identical v coordinates).
     *
     * @return {@code true} if the gradient is oriented horizontally
     */
    public boolean isHorizontal() {
        return Math.abs(start.v() - end.v()) < 1e-5f;
    }

    /**
     * Checks if this gradient is purely vertical (identical u coordinates).
     *
     * @return {@code true} if the gradient is oriented vertically
     */
    public boolean isVertical() {
        return Math.abs(start.u() - end.u()) < 1e-5f;
    }

    /**
     * Evaluates and interpolates the gradient color at a normalized coordinate (u, v) in range [0.0, 1.0].
     *
     * @param u normalized horizontal coordinate
     * @param v normalized vertical coordinate
     * @return the interpolated {@link Color}
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
