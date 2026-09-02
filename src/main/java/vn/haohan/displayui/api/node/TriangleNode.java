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
 * A renderable 2D filled triangle between 3 UI pixel points, rasterized using 3 sheared TextDisplay pieces.
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

    public TriangleNode(float x1, float y1, float x2, float y2, float x3, float y3, Color color) {
        this(x1, y1, x2, y2, x3, y3, 0.001f, color, false);
    }

    @Override
    public float x() {
        return Math.min(x1, Math.min(x2, x3));
    }

    @Override
    public float y() {
        return Math.min(y1, Math.min(y2, y3));
    }

    public TriangleNode atDepth(float depth) {
        return new TriangleNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    public TriangleNode withColor(Color color) {
        return new TriangleNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    public TriangleNode doubleSided(boolean doubleSided) {
        return new TriangleNode(x1, y1, x2, y2, x3, y3, depth, color, doubleSided);
    }

    @Override
    public TriangleNode withDoubleSided(boolean doubleSided) {
        return doubleSided(doubleSided);
    }
}
