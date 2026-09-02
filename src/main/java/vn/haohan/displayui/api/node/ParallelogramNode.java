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
 * A renderable 2D parallelogram / quad or slanted card defined by 3 vertices.
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

    public ParallelogramNode(float x1, float y1, float x2, float y2, float x3, float y3, Color color) {
        this(x1, y1, x2, y2, x3, y3, 0.0f, color, false);
    }

    /**
     * Creates a slanted card / badge with a horizontal skew offset.
     *
     * @param x top-left origin X
     * @param y top-left origin Y
     * @param width width of the card
     * @param height height of the card
     * @param skewX horizontal shear offset in pixels
     * @param color background color
     */
    public static ParallelogramNode slanted(float x, float y, float width, float height, float skewX, Color color) {
        return new ParallelogramNode(x, y, x + width, y, x + skewX, y + height, 0.0f, color, false);
    }

    public static ParallelogramNode slanted(float x, float y, float width, float height, float skewX, Color color, boolean doubleSided) {
        return new ParallelogramNode(x, y, x + width, y, x + skewX, y + height, 0.0f, color, doubleSided);
    }

    @Override
    public float x() {
        return Math.min(x1, Math.min(x2, x3));
    }

    @Override
    public float y() {
        return Math.min(y1, Math.min(y2, y3));
    }

    public ParallelogramNode atDepth(float depth) {
        return new ParallelogramNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    public ParallelogramNode withColor(Color color) {
        return new ParallelogramNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    public ParallelogramNode doubleSided(boolean doubleSided) {
        return new ParallelogramNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    @Override
    public ParallelogramNode withDoubleSided(boolean doubleSided) {
        return doubleSided(doubleSided);
    }
}
