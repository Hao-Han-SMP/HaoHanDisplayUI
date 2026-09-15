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
 * A 2D triangular surface node defined by 3 vertices (x1, y1), (x2, y2), and (x3, y3) on the UI canvas,
 * rasterized using combined affine sheared TextDisplay background elements.
 *
 * @param x1          first vertex X coordinate (pixels)
 * @param y1          first vertex Y coordinate (pixels)
 * @param x2          second vertex X coordinate (pixels)
 * @param y2          second vertex Y coordinate (pixels)
 * @param x3          third vertex X coordinate (pixels)
 * @param y3          third vertex Y coordinate (pixels)
 * @param depth       Z-depth layer offset
 * @param color       solid fill {@link Color} of the triangle
 * @param doubleSided whether back faces are rendered
 */
public record TriangleNode(
        float x1,
        float y1,
        float x2,
        float y2,
        float x3,
        float y3,
        float depth,
        Color color,
        boolean doubleSided
) implements UiNode {
    public TriangleNode {
        Objects.requireNonNull(color, "color");
    }

    /**
     * Constructs a triangle with default depth (0.001f) and single-sided rendering.
     *
     * @param x1    vertex 1 X coordinate
     * @param y1    vertex 1 Y coordinate
     * @param x2    vertex 2 X coordinate
     * @param y2    vertex 2 Y coordinate
     * @param x3    vertex 3 X coordinate
     * @param y3    vertex 3 Y coordinate
     * @param color fill color
     */
    public TriangleNode(float x1, float y1, float x2, float y2, float x3, float y3, Color color) {
        this(x1, y1, x2, y2, x3, y3, 0.001f, color, false);
    }

    /**
     * Returns the minimum X coordinate among all 3 vertices (left bounding edge).
     *
     * @return minimum X coordinate
     */
    @Override
    public float x() {
        return Math.min(x1, Math.min(x2, x3));
    }

    /**
     * Returns the minimum Y coordinate among all 3 vertices (top bounding edge).
     *
     * @return minimum Y coordinate
     */
    @Override
    public float y() {
        return Math.min(y1, Math.min(y2, y3));
    }

    /**
     * Returns a copy with updated layer Z-depth.
     *
     * @param depth new Z-depth
     * @return a new {@link TriangleNode} instance
     */
    public TriangleNode atDepth(float depth) {
        return new TriangleNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    /**
     * Returns a copy with an updated fill color.
     *
     * @param color new color
     * @return a new {@link TriangleNode} instance
     */
    public TriangleNode withColor(Color color) {
        return new TriangleNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    /**
     * Returns a copy with updated double-sided rendering state.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link TriangleNode} instance
     */
    public TriangleNode doubleSided(boolean doubleSided) {
        return new TriangleNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    /**
     * Implementation from {@link UiNode#withDoubleSided(boolean)}.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link TriangleNode} instance
     */
    @Override
    public TriangleNode withDoubleSided(boolean doubleSided) {
        return doubleSided(doubleSided);
    }
}
