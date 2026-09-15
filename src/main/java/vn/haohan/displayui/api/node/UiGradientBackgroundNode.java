/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.node;

import org.bukkit.Color;
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.api.gradient.UiGradientEndpoint;
import vn.haohan.displayui.api.gradient.UiGradientPosition;
import vn.haohan.displayui.api.layout.UiRect;

import java.util.Objects;

/**
 * A rectangular background node that renders a smooth multi-slice gradient color transition.
 * <p>
 * Parameterized by a 2-point {@link UiGradient} (start position/color to end position/color),
 * automatically subdividing into a grid of TextDisplay background slices to recreate a continuous
 * gradient effect on the Minecraft client.
 *
 * @param x           top-left X coordinate (pixels)
 * @param y           top-left Y coordinate (pixels)
 * @param depth       Z-depth layer offset
 * @param width       panel width (pixels)
 * @param height      panel height (pixels)
 * @param gradient    {@link UiGradient} color configuration
 * @param slicesX     horizontal subdivision slice count
 * @param slicesY     vertical subdivision slice count
 * @param doubleSided whether back faces are rendered
 */
public record UiGradientBackgroundNode(
        float x, float y, float depth,
        float width, float height,
        UiGradient gradient,
        int slicesX, int slicesY,
        boolean doubleSided
) implements UiNode {

    /** Default slice count for 1D gradients (horizontal or vertical): 16 slices. */
    public static final int DEFAULT_1D_SLICES = 16;
    /** Default slice count per axis for 2D diagonal gradients: 8x8 slices. */
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

    /**
     * Constructs a gradient node with subdivision slices automatically optimized for gradient direction.
     */
    public UiGradientBackgroundNode(float x, float y, float depth, float width, float height, UiGradient gradient) {
        this(x, y, depth, width, height, gradient, defaultSlicesX(gradient), defaultSlicesY(gradient), false);
    }

    /**
     * Constructs a gradient node from coordinates and two color endpoints.
     *
     * @param x          top-left X coordinate
     * @param y          top-left Y coordinate
     * @param depth      layer Z-depth
     * @param width      panel width
     * @param height     panel height
     * @param startPos   start endpoint position (TOP_LEFT, TOP_RIGHT, etc.)
     * @param startColor start color
     * @param endPos     end endpoint position
     * @param endColor   end color
     */
    public UiGradientBackgroundNode(float x, float y, float depth, float width, float height,
                                    UiGradientPosition startPos, Color startColor,
                                    UiGradientPosition endPos, Color endColor) {
        this(x, y, depth, width, height, UiGradient.of(startPos, startColor, endPos, endColor));
    }

    /**
     * Constructs a gradient node fitted to bounding rectangle {@link UiRect} from two endpoints.
     */
    public UiGradientBackgroundNode(UiRect bounds, float depth, UiGradientEndpoint start, UiGradientEndpoint end) {
        this(Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), UiGradient.of(start, end));
    }

    /**
     * Constructs a gradient node fitted to bounding rectangle {@link UiRect} from {@link UiGradient}.
     */
    public UiGradientBackgroundNode(UiRect bounds, float depth, UiGradient gradient) {
        this(Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), gradient);
    }

    /**
     * Implementation from {@link UiNode#withDoubleSided(boolean)}.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link UiGradientBackgroundNode} instance
     */
    public UiGradientBackgroundNode withDoubleSided(boolean doubleSided) {
        return new UiGradientBackgroundNode(x, y, depth, width, height, gradient, slicesX, slicesY, doubleSided);
    }

    /**
     * Fluent alias for {@link #withDoubleSided(boolean)}.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link UiGradientBackgroundNode} instance
     */
    public UiGradientBackgroundNode doubleSided(boolean doubleSided) {
        return withDoubleSided(doubleSided);
    }

    /**
     * Returns a copy with an updated Z-depth offset.
     *
     * @param newDepth new Z-depth
     * @return a new {@link UiGradientBackgroundNode} instance
     */
    public UiGradientBackgroundNode atDepth(float newDepth) {
        return new UiGradientBackgroundNode(x, y, newDepth, width, height, gradient, slicesX, slicesY, doubleSided);
    }

    /**
     * Returns a copy with custom horizontal and vertical subdivision slices.
     *
     * @param slicesX horizontal slice count (> 0)
     * @param slicesY vertical slice count (> 0)
     * @return a new {@link UiGradientBackgroundNode} instance
     */
    public UiGradientBackgroundNode withGrid(int slicesX, int slicesY) {
        return new UiGradientBackgroundNode(x, y, depth, width, height, gradient, slicesX, slicesY, doubleSided);
    }

    /**
     * Returns a copy with a total slice count automatically distributed based on gradient direction.
     *
     * @param slices slice count
     * @return a new {@link UiGradientBackgroundNode} instance
     */
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
     * Computes the interpolated color for the display slice entity at a given cell index.
     *
     * @param index slice cell entity index
     * @return interpolated {@link Color} at cell center
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

    /**
     * Determines optimal default X-axis slice count based on gradient orientation.
     *
     * @param gradient gradient definition
     * @return recommended X-slice count
     */
    public static int defaultSlicesX(UiGradient gradient) {
        if (gradient.isVertical()) return 1;
        if (gradient.isHorizontal()) return DEFAULT_1D_SLICES;
        return DEFAULT_2D_SLICES;
    }

    /**
     * Determines optimal default Y-axis slice count based on gradient orientation.
     *
     * @param gradient gradient definition
     * @return recommended Y-slice count
     */
    public static int defaultSlicesY(UiGradient gradient) {
        if (gradient.isHorizontal()) return 1;
        if (gradient.isVertical()) return DEFAULT_1D_SLICES;
        return DEFAULT_2D_SLICES;
    }
}
