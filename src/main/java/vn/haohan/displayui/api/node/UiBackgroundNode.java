/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.node;

import org.bukkit.Color;
import vn.haohan.displayui.api.layout.UiRect;
import java.util.Objects;

/** A translucent panel rendered by a background-only TextDisplay. */
public record UiBackgroundNode(float x, float y, float depth,
                               float width, float height, Color background,
                               boolean doubleSided)
        implements UiNode {
    public UiBackgroundNode {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException(
                "background dimensions must be positive");
        background = Objects.requireNonNull(background, "background");
    }

    public UiBackgroundNode(float x, float y, float depth, float width, float height, Color background) {
        this(x, y, depth, width, height, background, false);
    }

    public UiBackgroundNode(UiRect bounds, float depth, Color background) {
        this(Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), background, false);
    }

    public UiBackgroundNode(UiRect bounds, float depth, Color background, boolean doubleSided) {
        this(Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), background, doubleSided);
    }

    public UiBackgroundNode withDoubleSided(boolean doubleSided) {
        return new UiBackgroundNode(x, y, depth, width, height, background, doubleSided);
    }

    public UiBackgroundNode doubleSided(boolean doubleSided) {
        return withDoubleSided(doubleSided);
    }

    public UiBackgroundNode atDepth(float newDepth) {
        return new UiBackgroundNode(x, y, newDepth, width, height, background, doubleSided);
    }

    public UiBackgroundNode background(Color newBackground) {
        return new UiBackgroundNode(x, y, depth, width, height, newBackground, doubleSided);
    }

    public static UiGradientBackgroundNode gradient(float x, float y, float depth, float width, float height,
                                                    vn.haohan.displayui.api.gradient.UiGradientPosition startPos, Color startColor,
                                                    vn.haohan.displayui.api.gradient.UiGradientPosition endPos, Color endColor) {
        return new UiGradientBackgroundNode(x, y, depth, width, height, startPos, startColor, endPos, endColor);
    }

    public static UiGradientBackgroundNode gradient(float x, float y, float depth, float width, float height,
                                                    vn.haohan.displayui.api.gradient.UiGradient gradient) {
        return new UiGradientBackgroundNode(x, y, depth, width, height, gradient);
    }

    public static UiGradientBackgroundNode gradient(UiRect bounds, float depth,
                                                    vn.haohan.displayui.api.gradient.UiGradientEndpoint start,
                                                    vn.haohan.displayui.api.gradient.UiGradientEndpoint end) {
        return new UiGradientBackgroundNode(bounds, depth, start, end);
    }

    public static UiGradientBackgroundNode gradient(UiRect bounds, float depth,
                                                    vn.haohan.displayui.api.gradient.UiGradient gradient) {
        return new UiGradientBackgroundNode(bounds, depth, gradient);
    }
}
