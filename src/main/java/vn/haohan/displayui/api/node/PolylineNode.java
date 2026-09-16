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
 * A series of connected straight line segments (polyline) forming open paths or closed polygon outlines,
 * commonly used for charts/graphs, technology trees, or customized UI borders.
 *
 * @param points      ordered list of consecutive vertex coordinates
 * @param thickness   stroke thickness (pixels, > 0)
 * @param depth       Z-depth layer offset
 * @param color       Bukkit {@link Color} of the line segments
 * @param doubleSided whether back faces are rendered
 * @param closed      if {@code true}, connects the last point back to the first point
 */
public record PolylineNode(
        List<Point> points,
        float thickness,
        float depth,
        Color color,
        boolean doubleSided,
        boolean closed
) implements UiNode {
    /**
     * A 2D coordinate point on the UI canvas in pixels.
     *
     * @param x X coordinate
     * @param y Y coordinate
     */
    public record Point(float x, float y) {}

    public PolylineNode {
        Objects.requireNonNull(points, "points");
        Objects.requireNonNull(color, "color");
        if (points.size() < 2) throw new IllegalArgumentException("polyline requires at least 2 points");
        if (thickness <= 0.0f) throw new IllegalArgumentException("thickness must be positive");
        points = List.copyOf(points);
    }

    /**
     * Constructs an open polyline (closed = false).
     */
    public PolylineNode(List<Point> points, float thickness, float depth, Color color, boolean doubleSided) {
        this(points, thickness, depth, color, doubleSided, false);
    }

    /**
     * Creates a builder to construct {@link PolylineNode} instances fluently.
     *
     * @return a new {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the minimum X coordinate among all points in the polyline.
     *
     * @return minimum X coordinate
     */
    @Override
    public float x() {
        return points.stream().map(Point::x).min(Float::compare).orElse(0.0f);
    }

    /**
     * Returns the minimum Y coordinate among all points in the polyline.
     *
     * @return minimum Y coordinate
     */
    @Override
    public float y() {
        return points.stream().map(Point::y).min(Float::compare).orElse(0.0f);
    }

    /**
     * Returns a copy with updated double-sided rendering state.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link PolylineNode} instance
     */
    public PolylineNode doubleSided(boolean doubleSided) {
        return new PolylineNode(points, thickness, depth, color, doubleSided, closed);
    }

    /**
     * Implementation from {@link UiNode#withDoubleSided(boolean)}.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link PolylineNode} instance
     */
    @Override
    public PolylineNode withDoubleSided(boolean doubleSided) {
        return doubleSided(doubleSided);
    }

    /**
     * Decomposes this polyline into a list of independent {@link LineNode} segment instances.
     *
     * @return list of corresponding {@link LineNode} objects
     */
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

    /**
     * Builder utility for constructing {@link PolylineNode} instances step by step.
     */
    public static final class Builder {
        private final List<Point> points = new ArrayList<>();
        private float thickness = 2.0f;
        private float depth = 0.001f;
        private Color color = Color.WHITE;
        private boolean doubleSided = false;
        private boolean closed = false;

        /**
         * Appends a vertex point to the polyline sequence.
         *
         * @param x X coordinate (pixels)
         * @param y Y coordinate (pixels)
         * @return this builder
         */
        public Builder add(float x, float y) {
            points.add(new Point(x, y));
            return this;
        }

        /**
         * Sets stroke thickness.
         *
         * @param thickness stroke thickness (pixels)
         * @return this builder
         */
        public Builder thickness(float thickness) {
            this.thickness = thickness;
            return this;
        }

        /**
         * Sets Z-depth layer offset.
         *
         * @param depth layer depth
         * @return this builder
         */
        public Builder depth(float depth) {
            this.depth = depth;
            return this;
        }

        /**
         * Sets stroke color.
         *
         * @param color Bukkit {@link Color}
         * @return this builder
         */
        public Builder color(Color color) {
            this.color = Objects.requireNonNull(color, "color");
            return this;
        }

        /**
         * Sets double-sided rendering flag.
         *
         * @param doubleSided {@code true} to render double-sided
         * @return this builder
         */
        public Builder doubleSided(boolean doubleSided) {
            this.doubleSided = doubleSided;
            return this;
        }

        /**
         * Sets whether the last vertex connects back to the first vertex.
         *
         * @param closed {@code true} if closed polygon
         * @return this builder
         */
        public Builder closed(boolean closed) {
            this.closed = closed;
            return this;
        }

        /**
         * Builds the configured {@link PolylineNode}.
         *
         * @return a new {@link PolylineNode} instance
         */
        public PolylineNode build() {
            return new PolylineNode(points, thickness, depth, color, doubleSided, closed);
        }
    }
}
