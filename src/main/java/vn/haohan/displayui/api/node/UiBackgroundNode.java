/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.node;

import org.bukkit.Color;
import vn.haohan.displayui.api.layout.UiRect;
import java.util.Objects;

/** A translucent panel rendered by a background-only TextDisplay. */
public record UiBackgroundNode(float x, float y, float depth,
                               float width, float height, Color background)
        implements UiNode {
    public UiBackgroundNode {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException(
                "background dimensions must be positive");
        background = Objects.requireNonNull(background, "background");
    }

    public UiBackgroundNode(UiRect bounds, float depth, Color background) {
        this(Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), background);
    }
}
