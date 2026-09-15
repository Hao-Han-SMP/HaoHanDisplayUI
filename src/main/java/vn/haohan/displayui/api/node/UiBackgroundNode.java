/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.node;

import org.bukkit.Color;
import vn.haohan.displayui.api.layout.UiRect;
import java.util.Objects;

/**
 * A solid or translucent flat colored background panel node rendered using
 * Minecraft's background-only TextDisplay entity.
 *
 * @param x           top-left X coordinate (pixels)
 * @param y           top-left Y coordinate (pixels)
 * @param depth       Z-depth layer offset
 * @param width       panel width (pixels, > 0)
 * @param height      panel height (pixels, > 0)
 * @param background  Bukkit {@link Color} background tint (supports alpha transparency)
 * @param doubleSided whether back faces are rendered
 */
public record UiBackgroundNode(float x, float y, float depth,
                               float width, float height, Color background,
                               boolean doubleSided)
        implements UiNode {
    public UiBackgroundNode {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException(
                "background dimensions must be positive");
        background = Objects.requireNonNull(background, "background");
    }

    /**
     * Constructs a single-sided background node (doubleSided = false).
     */
    public UiBackgroundNode(float x, float y, float depth, float width, float height, Color background) {
        this(x, y, depth, width, height, background, false);
    }

    /**
     * Constructs a background node fitted to bounding rectangle {@link UiRect}.
     *
     * @param bounds     bounding rectangle
     * @param depth      layer Z-depth
     * @param background background tint color
     */
    public UiBackgroundNode(UiRect bounds, float depth, Color background) {
        this(Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), background, false);
    }

    /**
     * Constructs a background node fitted to bounding rectangle {@link UiRect} with double-sided rendering flag.
     *
     * @param bounds      bounding rectangle
     * @param depth       layer Z-depth
     * @param background  background tint color
     * @param doubleSided whether back faces are rendered
     */
    public UiBackgroundNode(UiRect bounds, float depth, Color background, boolean doubleSided) {
        this(Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), background, doubleSided);
    }

    /**
     * Implementation from {@link UiNode#withDoubleSided(boolean)}.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link UiBackgroundNode} instance
     */
    public UiBackgroundNode withDoubleSided(boolean doubleSided) {
        return new UiBackgroundNode(x, y, depth, width, height, background, doubleSided);
    }

    /**
     * Fluent alias for {@link #withDoubleSided(boolean)}.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link UiBackgroundNode} instance
     */
    public UiBackgroundNode doubleSided(boolean doubleSided) {
        return withDoubleSided(doubleSided);
    }

    /**
     * Returns a copy with an updated layer Z-depth.
     *
     * @param newDepth new Z-depth
     * @return a new {@link UiBackgroundNode} instance
     */
    public UiBackgroundNode atDepth(float newDepth) {
        return new UiBackgroundNode(x, y, newDepth, width, height, background, doubleSided);
    }

    /**
     * Returns a copy with an updated background color.
     *
     * @param newBackground new background color
     * @return a new {@link UiBackgroundNode} instance
     */
    public UiBackgroundNode background(Color newBackground) {
        return new UiBackgroundNode(x, y, depth, width, height, newBackground, doubleSided);
    }

    /**
     * Utility factory creating a gradient background node {@link UiGradientBackgroundNode}.
     */
    public static UiGradientBackgroundNode gradient(float x, float y, float depth, float width, float height,
                                                    vn.haohan.displayui.api.gradient.UiGradientPosition startPos, Color startColor,
                                                    vn.haohan.displayui.api.gradient.UiGradientPosition endPos, Color endColor) {
        return new UiGradientBackgroundNode(x, y, depth, width, height, startPos, startColor, endPos, endColor);
    }

    /**
     * Utility factory creating a gradient background node from {@link vn.haohan.displayui.api.gradient.UiGradient}.
     */
    public static UiGradientBackgroundNode gradient(float x, float y, float depth, float width, float height,
                                                    vn.haohan.displayui.api.gradient.UiGradient gradient) {
        return new UiGradientBackgroundNode(x, y, depth, width, height, gradient);
    }

    /**
     * Utility factory creating a gradient background node fitted to {@link UiRect} from two endpoints.
     */
    public static UiGradientBackgroundNode gradient(UiRect bounds, float depth,
                                                    vn.haohan.displayui.api.gradient.UiGradientEndpoint start,
                                                    vn.haohan.displayui.api.gradient.UiGradientEndpoint end) {
        return new UiGradientBackgroundNode(bounds, depth, start, end);
    }

    /**
     * Utility factory creating a gradient background node fitted to {@link UiRect} from {@link vn.haohan.displayui.api.gradient.UiGradient}.
     */
    public static UiGradientBackgroundNode gradient(UiRect bounds, float depth,
                                                    vn.haohan.displayui.api.gradient.UiGradient gradient) {
        return new UiGradientBackgroundNode(bounds, depth, gradient);
    }
}
