/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HaoHanDisplayUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HaoHanDisplayUI. If not, see <https://www.gnu.org/licenses/>.
 */
package vn.haohan.displayui.api.node;

import org.bukkit.Color;

import java.util.Objects;

/**
 * A 2D straight line segment connecting two points (x1, y1) and (x2, y2) on the UI canvas,
 * rendered via a transformed and stretched TextDisplay background entity.
 *
 * @param x1          start point X coordinate (pixels)
 * @param y1          start point Y coordinate (pixels)
 * @param x2          end point X coordinate (pixels)
 * @param y2          end point Y coordinate (pixels)
 * @param thickness   stroke thickness (pixels, > 0)
 * @param depth       Z-depth layer offset
 * @param color       Bukkit {@link Color} of the line
 * @param doubleSided whether back faces are rendered
 * @param roll        supplementary rotation around the Roll axis (degrees)
 */
public record LineNode(
        float x1,
        float y1,
        float x2,
        float y2,
        float thickness,
        float depth,
        Color color,
        boolean doubleSided,
        float roll
) implements UiNode {
    public LineNode {
        Objects.requireNonNull(color, "color");
        if (thickness <= 0.0f) throw new IllegalArgumentException("thickness must be positive");
    }

    /**
     * Constructs a line with roll angle defaulting to 0.0f.
     */
    public LineNode(float x1, float y1, float x2, float y2, float thickness, float depth, Color color, boolean doubleSided) {
        this(x1, y1, x2, y2, thickness, depth, color, doubleSided, 0.0f);
    }

    /**
     * Constructs a line with default depth (0.001f) and single-sided rendering.
     *
     * @param x1        start X coordinate
     * @param y1        start Y coordinate
     * @param x2        end X coordinate
     * @param y2        end Y coordinate
     * @param thickness stroke thickness (pixels)
     * @param color     stroke color
     */
    public LineNode(float x1, float y1, float x2, float y2, float thickness, Color color) {
        this(x1, y1, x2, y2, thickness, 0.001f, color, false, 0.0f);
    }

    /**
     * Constructs a line with default thickness (2.0 pixels) and single-sided rendering.
     *
     * @param x1    start X coordinate
     * @param y1    start Y coordinate
     * @param x2    end X coordinate
     * @param y2    end Y coordinate
     * @param color stroke color
     */
    public LineNode(float x1, float y1, float x2, float y2, Color color) {
        this(x1, y1, x2, y2, 2.0f, 0.001f, color, false, 0.0f);
    }

    /**
     * Returns top-left bounding box X coordinate.
     *
     * @return minimum of x1 and x2
     */
    @Override
    public float x() {
        return Math.min(x1, x2);
    }

    /**
     * Returns top-left bounding box Y coordinate.
     *
     * @return minimum of y1 and y2
     */
    @Override
    public float y() {
        return Math.min(y1, y2);
    }

    /**
     * Returns a copy with updated stroke thickness.
     *
     * @param thickness new stroke thickness (pixels, > 0)
     * @return a new {@link LineNode} instance
     */
    public LineNode withThickness(float thickness) {
        return new LineNode(x1, y1, x2, y2, thickness, depth, color, doubleSided, roll);
    }

    /**
     * Returns a copy with updated layer Z-depth.
     *
     * @param depth new Z-depth
     * @return a new {@link LineNode} instance
     */
    public LineNode atDepth(float depth) {
        return new LineNode(x1, y1, x2, y2, thickness, depth, color, doubleSided, roll);
    }

    /**
     * Returns a copy with updated stroke color.
     *
     * @param color new color
     * @return a new {@link LineNode} instance
     */
    public LineNode withColor(Color color) {
        return new LineNode(x1, y1, x2, y2, thickness, depth, color, doubleSided, roll);
    }

    /**
     * Returns a copy with updated double-sided rendering state.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link LineNode} instance
     */
    public LineNode doubleSided(boolean doubleSided) {
        return new LineNode(x1, y1, x2, y2, thickness, depth, color, doubleSided, roll);
    }

    /**
     * Implementation from {@link UiNode#withDoubleSided(boolean)}.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link LineNode} instance
     */
    @Override
    public LineNode withDoubleSided(boolean doubleSided) {
        return doubleSided(doubleSided);
    }

    /**
     * Returns a copy with an updated Roll angle.
     *
     * @param roll supplementary roll angle (degrees)
     * @return a new {@link LineNode} instance
     */
    public LineNode withRoll(float roll) {
        return new LineNode(x1, y1, x2, y2, thickness, depth, color, doubleSided, roll);
    }
}
