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
 * A 2D parallelogram or slanted card/badge node defined by 3 vertices on the UI canvas.
 *
 * @param x1          vertex 1 X coordinate
 * @param y1          vertex 1 Y coordinate
 * @param x2          vertex 2 X coordinate
 * @param y2          vertex 2 Y coordinate
 * @param x3          vertex 3 X coordinate
 * @param y3          vertex 3 Y coordinate
 * @param depth       Z-depth layer offset
 * @param color       fill {@link Color}
 * @param doubleSided whether back faces are rendered
 */
public record ParallelogramNode(
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
    public ParallelogramNode {
        Objects.requireNonNull(color, "color");
    }

    /**
     * Constructs a parallelogram with depth 0.0f and single-sided rendering.
     */
    public ParallelogramNode(float x1, float y1, float x2, float y2, float x3, float y3, Color color) {
        this(x1, y1, x2, y2, x3, y3, 0.0f, color, false);
    }

    /**
     * Factory creating a slanted card / badge with a horizontal skew offset.
     *
     * @param x      top-left X coordinate (pixels)
     * @param y      top-left Y coordinate (pixels)
     * @param width  card width (pixels)
     * @param height card height (pixels)
     * @param skewX  horizontal shearing skew offset (pixels)
     * @param color  fill color
     * @return a new slanted {@link ParallelogramNode} instance
     */
    public static ParallelogramNode slanted(float x, float y, float width, float height, float skewX, Color color) {
        return new ParallelogramNode(x, y, x + width, y, x + skewX, y + height, 0.0f, color, false);
    }

    /**
     * Factory creating a slanted card / badge with double-sided rendering support.
     *
     * @param x           top-left X coordinate (pixels)
     * @param y           top-left Y coordinate (pixels)
     * @param width       card width (pixels)
     * @param height      card height (pixels)
     * @param skewX       horizontal shearing skew offset (pixels)
     * @param color       fill color
     * @param doubleSided whether back faces are rendered
     * @return a new slanted {@link ParallelogramNode} instance
     */
    public static ParallelogramNode slanted(float x, float y, float width, float height, float skewX, Color color, boolean doubleSided) {
        return new ParallelogramNode(x, y, x + width, y, x + skewX, y + height, 0.0f, color, doubleSided);
    }

    /**
     * Returns the minimum X coordinate among all vertices.
     *
     * @return minimum X coordinate
     */
    @Override
    public float x() {
        return Math.min(x1, Math.min(x2, x3));
    }

    /**
     * Returns the minimum Y coordinate among all vertices.
     *
     * @return minimum Y coordinate
     */
    @Override
    public float y() {
        return Math.min(y1, Math.min(y2, x3 == y3 ? y1 : y2));
    }

    /**
     * Returns a copy with updated layer Z-depth.
     *
     * @param depth new Z-depth
     * @return a new {@link ParallelogramNode} instance
     */
    public ParallelogramNode atDepth(float depth) {
        return new ParallelogramNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    /**
     * Returns a copy with an updated fill color.
     *
     * @param color new color
     * @return a new {@link ParallelogramNode} instance
     */
    public ParallelogramNode withColor(Color color) {
        return new ParallelogramNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    /**
     * Returns a copy with updated double-sided rendering state.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link ParallelogramNode} instance
     */
    public ParallelogramNode doubleSided(boolean doubleSided) {
        return new ParallelogramNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    /**
     * Implementation from {@link UiNode#withDoubleSided(boolean)}.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link ParallelogramNode} instance
     */
    @Override
    public ParallelogramNode withDoubleSided(boolean doubleSided) {
        return doubleSided(doubleSided);
    }
}
