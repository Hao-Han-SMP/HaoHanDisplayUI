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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A series of connected line segments forming a polyline (for graphs, charts, tech trees, or custom outlines).
 */
public record PolylineNode(
        List<Point> points,
        float thickness,
        float depth,
        Color color,
        boolean doubleSided,
        boolean closed
) implements UiNode {
    public record Point(float x, float y) {}

    public PolylineNode {
        Objects.requireNonNull(points, "points");
        Objects.requireNonNull(color, "color");
        if (points.size() < 2) throw new IllegalArgumentException("polyline requires at least 2 points");
        if (thickness <= 0.0f) throw new IllegalArgumentException("thickness must be positive");
        points = List.copyOf(points);
    }

    public PolylineNode(List<Point> points, float thickness, float depth, Color color, boolean doubleSided) {
        this(points, thickness, depth, color, doubleSided, false);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public float x() {
        return points.stream().map(Point::x).min(Float::compare).orElse(0.0f);
    }

    @Override
    public float y() {
        return points.stream().map(Point::y).min(Float::compare).orElse(0.0f);
    }

    public PolylineNode doubleSided(boolean doubleSided) {
        return new PolylineNode(points, thickness, depth, color, doubleSided, closed);
    }

    @Override
    public PolylineNode withDoubleSided(boolean doubleSided) {
        return doubleSided(doubleSided);
    }

    public List<LineNode> toLineNodes() {
        List<LineNode> lines = new ArrayList<>(points.size());
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            lines.add(new LineNode(p1.x(), p1.y(), p2.x(), p2.y(), thickness, depth, color, doubleSided, 0.0f));
        }
        if (closed && points.size() > 2) {
            Point p1 = points.getLast();
            Point p2 = points.getFirst();
            lines.add(new LineNode(p1.x(), p1.y(), p2.x(), p2.y(), thickness, depth, color, doubleSided, 0.0f));
        }
        return lines;
    }

    public static final class Builder {
        private final List<Point> points = new ArrayList<>();
        private float thickness = 2.0f;
        private float depth = 0.001f;
        private Color color = Color.WHITE;
        private boolean doubleSided = false;
        private boolean closed = false;

        public Builder add(float x, float y) {
            points.add(new Point(x, y));
            return this;
        }

        public Builder thickness(float thickness) {
            this.thickness = thickness;
            return this;
        }

        public Builder depth(float depth) {
            this.depth = depth;
            return this;
        }

        public Builder color(Color color) {
            this.color = Objects.requireNonNull(color, "color");
            return this;
        }

        public Builder doubleSided(boolean doubleSided) {
            this.doubleSided = doubleSided;
            return this;
        }

        public Builder closed(boolean closed) {
            this.closed = closed;
            return this;
        }

        public PolylineNode build() {
            return new PolylineNode(points, thickness, depth, color, doubleSided, closed);
        }
    }
}
