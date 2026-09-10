/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.node;

import org.bukkit.Color;
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.api.gradient.UiGradientEndpoint;
import vn.haohan.displayui.api.gradient.UiGradientPosition;
import vn.haohan.displayui.api.layout.UiRect;

import java.util.Objects;

/**
 * A rectangular background panel displaying a continuous multi-slice color gradient.
 * Parameterized by a 2-endpoint UiGradient (start position/color to end position/color).
 */
public record UiGradientBackgroundNode(
        float x, float y, float depth,
        float width, float height,
        UiGradient gradient,
        int slicesX, int slicesY,
        boolean doubleSided
) implements UiNode {

    public static final int DEFAULT_1D_SLICES = 16;
    public static final int DEFAULT_2D_SLICES = 8;

    public UiGradientBackgroundNode {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("gradient background dimensions must be positive");
        }
        if (slicesX <= 0 || slicesY <= 0) {
            throw new IllegalArgumentException("slices count must be positive");
        }
        gradient = Objects.requireNonNull(gradient, "gradient");
    }

    public UiGradientBackgroundNode(float x, float y, float depth, float width, float height, UiGradient gradient) {
        this(x, y, depth, width, height, gradient, defaultSlicesX(gradient), defaultSlicesY(gradient), false);
    }

    public UiGradientBackgroundNode(float x, float y, float depth, float width, float height,
                                    UiGradientPosition startPos, Color startColor,
                                    UiGradientPosition endPos, Color endColor) {
        this(x, y, depth, width, height, UiGradient.of(startPos, startColor, endPos, endColor));
    }

    public UiGradientBackgroundNode(UiRect bounds, float depth, UiGradientEndpoint start, UiGradientEndpoint end) {
        this(Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), UiGradient.of(start, end));
    }

    public UiGradientBackgroundNode(UiRect bounds, float depth, UiGradient gradient) {
        this(Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), gradient);
    }

    public UiGradientBackgroundNode withDoubleSided(boolean doubleSided) {
        return new UiGradientBackgroundNode(x, y, depth, width, height, gradient, slicesX, slicesY, doubleSided);
    }

    public UiGradientBackgroundNode doubleSided(boolean doubleSided) {
        return withDoubleSided(doubleSided);
    }

    public UiGradientBackgroundNode atDepth(float newDepth) {
        return new UiGradientBackgroundNode(x, y, newDepth, width, height, gradient, slicesX, slicesY, doubleSided);
    }

    public UiGradientBackgroundNode withGrid(int slicesX, int slicesY) {
        return new UiGradientBackgroundNode(x, y, depth, width, height, gradient, slicesX, slicesY, doubleSided);
    }

    public UiGradientBackgroundNode withSlices(int slices) {
        if (gradient.isHorizontal()) {
            return withGrid(slices, 1);
        } else if (gradient.isVertical()) {
            return withGrid(1, slices);
        } else {
            return withGrid(slices, slices);
        }
    }

    /**
     * Calculates the color for the display entity at index i (front or back).
     */
    public Color colorForDisplayIndex(int index) {
        int totalCells = slicesX * slicesY;
        if (totalCells <= 0) return gradient.start().color();
        int cellIndex = index % totalCells;
        int row = cellIndex / slicesX;
        int col = cellIndex % slicesX;
        float u = (col + 0.5f) / slicesX;
        float v = (row + 0.5f) / slicesY;
        return gradient.evaluate(u, v);
    }

    public static int defaultSlicesX(UiGradient gradient) {
        if (gradient.isVertical()) return 1;
        if (gradient.isHorizontal()) return DEFAULT_1D_SLICES;
        return DEFAULT_2D_SLICES;
    }

    public static int defaultSlicesY(UiGradient gradient) {
        if (gradient.isHorizontal()) return 1;
        if (gradient.isVertical()) return DEFAULT_1D_SLICES;
        return DEFAULT_2D_SLICES;
    }
}
