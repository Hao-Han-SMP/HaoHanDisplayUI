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
 * A renderable 2D line segment between two UI pixel points, rendered using a transformed TextDisplay.
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

    public LineNode(float x1, float y1, float x2, float y2, float thickness, float depth, Color color, boolean doubleSided) {
        this(x1, y1, x2, y2, thickness, depth, color, doubleSided, 0.0f);
    }

    public LineNode(float x1, float y1, float x2, float y2, float thickness, Color color) {
        this(x1, y1, x2, y2, thickness, 0.001f, color, false, 0.0f);
    }

    public LineNode(float x1, float y1, float x2, float y2, Color color) {
        this(x1, y1, x2, y2, 2.0f, 0.001f, color, false, 0.0f);
    }

    @Override
    public float x() {
        return Math.min(x1, x2);
    }

    @Override
    public float y() {
        return Math.min(y1, y2);
    }

    public LineNode withThickness(float thickness) {
        return new LineNode(x1, y1, x2, y2, thickness, depth, color, doubleSided, roll);
    }

    public LineNode atDepth(float depth) {
        return new LineNode(x1, y1, x2, y2, thickness, depth, color, doubleSided, roll);
    }

    public LineNode withColor(Color color) {
        return new LineNode(x1, y1, x2, y2, thickness, depth, color, doubleSided, roll);
    }

    public LineNode doubleSided(boolean doubleSided) {
        return new LineNode(x1, y1, x2, y2, thickness, depth, color, doubleSided, roll);
    }

    @Override
    public LineNode withDoubleSided(boolean doubleSided) {
        return doubleSided(doubleSided);
    }

    public LineNode withRoll(float roll) {
        return new LineNode(x1, y1, x2, y2, thickness, depth, color, doubleSided, roll);
    }
}
