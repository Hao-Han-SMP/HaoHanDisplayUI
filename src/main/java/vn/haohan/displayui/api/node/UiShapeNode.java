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
 * A versatile 2D shape node that supports geometric primitives, polygons, stars, arrows,
 * symbols, custom outlines, and rotation matching the HaoHan Visual GUI Builder.
 */
public record UiShapeNode(
        String shapeType,
        float x,
        float y,
        float width,
        float height,
        float depth,
        Color color,
        boolean outline,
        Color outlineColor,
        float outlineThickness,
        String outlineStyle,
        float cornerRadius,
        float rotation,
        boolean doubleSided
) implements UiNode {

    public UiShapeNode {
        Objects.requireNonNull(shapeType, "shapeType");
        Objects.requireNonNull(color, "color");
        if (outlineColor == null) outlineColor = Color.fromRGB(255, 255, 255);
        if (outlineStyle == null || outlineStyle.isBlank()) outlineStyle = "solid";
        if (outlineThickness <= 0) outlineThickness = 2.0f;
    }

    public UiShapeNode(String shapeType, float x, float y, float width, float height, Color color) {
        this(shapeType, x, y, width, height, 0.001f, color, false, Color.WHITE, 2.0f, "solid", 6.0f, 0.0f, false);
    }

    public UiShapeNode(String shapeType, float x, float y, float width, float height, float depth,
                       Color color, boolean outline, Color outlineColor, float outlineThickness,
                       String outlineStyle, float cornerRadius, boolean doubleSided) {
        this(shapeType, x, y, width, height, depth, color, outline, outlineColor, outlineThickness, outlineStyle, cornerRadius, 0.0f, doubleSided);
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public UiShapeNode withDoubleSided(boolean doubleSided) {
        return new UiShapeNode(shapeType, x, y, width, height, depth, color, outline, outlineColor, outlineThickness, outlineStyle, cornerRadius, rotation, doubleSided);
    }

    public UiShapeNode withOutline(boolean outline, Color outlineColor, float outlineThickness, String outlineStyle) {
        return new UiShapeNode(shapeType, x, y, width, height, depth, color, outline, outlineColor, outlineThickness, outlineStyle, cornerRadius, rotation, doubleSided);
    }

    public UiShapeNode withRotation(float rotation) {
        return new UiShapeNode(shapeType, x, y, width, height, depth, color, outline, outlineColor, outlineThickness, outlineStyle, cornerRadius, rotation, doubleSided);
    }

    public UiShapeNode atDepth(float depth) {
        return new UiShapeNode(shapeType, x, y, width, height, depth, color, outline, outlineColor, outlineThickness, outlineStyle, cornerRadius, rotation, doubleSided);
    }

    /**
     * Returns a copy of this shape scaled uniformly around its center point.
     */
    public UiShapeNode scaled(float scale) {
        if (Math.abs(scale - 1.0f) < 1e-6f) return this;
        float cx = x + width * 0.5f;
        float cy = y + height * 0.5f;
        float newW = width * scale;
        float newH = height * scale;
        float newX = cx - newW * 0.5f;
        float newY = cy - newH * 0.5f;
        float newRadius = cornerRadius * scale;
        float newThickness = outlineThickness * scale;
        return new UiShapeNode(shapeType, newX, newY, newW, newH, depth, color,
                outline, outlineColor, newThickness, outlineStyle, newRadius, rotation, doubleSided);
    }

    /**
     * Computes the 2D polygon boundary vertices for this shape (including rotation if non-zero).
     */
    public List<PolylineNode.Point> computeBoundaryPoints() {
        List<PolylineNode.Point> pts = new ArrayList<>();
        float w = width;
        float h = height;
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float rx = w * 0.5f;
        float ry = h * 0.5f;

        String type = shapeType.toLowerCase().trim();
        switch (type) {
            case "rect": {
                pts.add(new PolylineNode.Point(x, y));
                pts.add(new PolylineNode.Point(x + w, y));
                pts.add(new PolylineNode.Point(x + w, y + h));
                pts.add(new PolylineNode.Point(x, y + h));
                break;
            }
            case "rounded_rect": {
                float r = Math.max(0, Math.min(cornerRadius, Math.min(rx, ry)));
                if (r <= 0.1f) {
                    pts.add(new PolylineNode.Point(x, y));
                    pts.add(new PolylineNode.Point(x + w, y));
                    pts.add(new PolylineNode.Point(x + w, y + h));
                    pts.add(new PolylineNode.Point(x, y + h));
                } else {
                    int arcSteps = 6;
                    // Top-Right arc (from top edge to right edge)
                    for (int i = 0; i <= arcSteps; i++) {
                        double angle = -Math.PI / 2.0 + (Math.PI / 2.0) * (i / (double) arcSteps);
                        pts.add(new PolylineNode.Point((float) (x + w - r + r * Math.cos(angle)), (float) (y + r + r * Math.sin(angle))));
                    }
                    // Bottom-Right arc (from right edge to bottom edge)
                    for (int i = 1; i <= arcSteps; i++) {
                        double angle = (Math.PI / 2.0) * (i / (double) arcSteps);
                        pts.add(new PolylineNode.Point((float) (x + w - r + r * Math.cos(angle)), (float) (y + h - r + r * Math.sin(angle))));
                    }
                    // Bottom-Left arc (from bottom edge to left edge)
                    for (int i = 1; i <= arcSteps; i++) {
                        double angle = Math.PI / 2.0 + (Math.PI / 2.0) * (i / (double) arcSteps);
                        pts.add(new PolylineNode.Point((float) (x + r + r * Math.cos(angle)), (float) (y + h - r + r * Math.sin(angle))));
                    }
                    // Top-Left arc (from left edge to top edge)
                    for (int i = 1; i < arcSteps; i++) {
                        double angle = Math.PI + (Math.PI / 2.0) * (i / (double) arcSteps);
                        pts.add(new PolylineNode.Point((float) (x + r + r * Math.cos(angle)), (float) (y + r + r * Math.sin(angle))));
                    }
                }
                break;
            }
            case "circle": {
                int sides = 24;
                for (int i = 0; i < sides; i++) {
                    double angle = -Math.PI / 2.0 + (2.0 * Math.PI * i) / sides;
                    pts.add(new PolylineNode.Point((float) (cx + rx * Math.cos(angle)), (float) (cy + ry * Math.sin(angle))));
                }
                break;
            }
            case "diamond": {
                pts.add(new PolylineNode.Point(cx, y));
                pts.add(new PolylineNode.Point(x + w, cy));
                pts.add(new PolylineNode.Point(cx, y + h));
                pts.add(new PolylineNode.Point(x, cy));
                break;
            }
            case "trapezoid": {
                float ins = Math.max(0.0f, Math.min(w * 0.22f, (w - 2.0f) * 0.5f));
                pts.add(new PolylineNode.Point(x + ins, y));
                pts.add(new PolylineNode.Point(x + w - ins, y));
                pts.add(new PolylineNode.Point(x + w, y + h));
                pts.add(new PolylineNode.Point(x, y + h));
                break;
            }
            case "parallelogram":
            case "slanted": {
                float skew = Math.max(0.0f, Math.min(w * 0.25f, (w - 2.0f) * 0.5f));
                pts.add(new PolylineNode.Point(x + skew, y));
                pts.add(new PolylineNode.Point(x + w, y));
                pts.add(new PolylineNode.Point(x + w - skew, y + h));
                pts.add(new PolylineNode.Point(x, y + h));
                break;
            }
            case "triangle": {
                pts.add(new PolylineNode.Point(cx, y));
                pts.add(new PolylineNode.Point(x + w, y + h));
                pts.add(new PolylineNode.Point(x, y + h));
                break;
            }
            case "right_triangle": {
                pts.add(new PolylineNode.Point(x, y));
                pts.add(new PolylineNode.Point(x + w, y + h));
                pts.add(new PolylineNode.Point(x, y + h));
                break;
            }
            case "pentagon":
                pts.addAll(createRegularPolygonPoints(cx, cy, rx, ry, 5, -Math.PI / 2.0));
                break;
            case "hexagon":
                pts.addAll(createRegularPolygonPoints(cx, cy, rx, ry, 6, 0.0));
                break;
            case "heptagon":
                pts.addAll(createRegularPolygonPoints(cx, cy, rx, ry, 7, -Math.PI / 2.0));
                break;
            case "octagon":
                pts.addAll(createRegularPolygonPoints(cx, cy, rx, ry, 8, Math.PI / 8.0));
                break;
            case "star3":
                pts.addAll(createStarPoints(cx, cy, rx, ry, 3, 0.40f, -Math.PI / 2.0));
                break;
            case "star4":
                pts.addAll(createStarPoints(cx, cy, rx, ry, 4, 0.35f, -Math.PI / 2.0));
                break;
            case "star5":
                pts.addAll(createStarPoints(cx, cy, rx, ry, 5, 0.382f, -Math.PI / 2.0));
                break;
            case "star6":
                pts.addAll(createStarPoints(cx, cy, rx, ry, 6, 0.577f, -Math.PI / 2.0));
                break;
            case "arrow_right": {
                float headLen = Math.max(2.0f, Math.min(w * 0.42f, w - 2.0f));
                float shaftH = Math.max(1.0f, Math.min(h * 0.45f, h - 2.0f));
                float shaftTop = cy - shaftH * 0.5f;
                float shaftBottom = cy + shaftH * 0.5f;
                pts.add(new PolylineNode.Point(x, shaftTop));
                pts.add(new PolylineNode.Point(x + w - headLen, shaftTop));
                pts.add(new PolylineNode.Point(x + w - headLen, y));
                pts.add(new PolylineNode.Point(x + w, cy));
                pts.add(new PolylineNode.Point(x + w - headLen, y + h));
                pts.add(new PolylineNode.Point(x + w - headLen, shaftBottom));
                pts.add(new PolylineNode.Point(x, shaftBottom));
                break;
            }
            case "arrow_left": {
                float headLen = Math.max(2.0f, Math.min(w * 0.42f, w - 2.0f));
                float shaftH = Math.max(1.0f, Math.min(h * 0.45f, h - 2.0f));
                float shaftTop = cy - shaftH * 0.5f;
                float shaftBottom = cy + shaftH * 0.5f;
                pts.add(new PolylineNode.Point(x + w, shaftTop));
                pts.add(new PolylineNode.Point(x + headLen, shaftTop));
                pts.add(new PolylineNode.Point(x + headLen, y));
                pts.add(new PolylineNode.Point(x, cy));
                pts.add(new PolylineNode.Point(x + headLen, y + h));
                pts.add(new PolylineNode.Point(x + headLen, shaftBottom));
                pts.add(new PolylineNode.Point(x + w, shaftBottom));
                break;
            }
            case "chevron_right":
            case "chevron": {
                float armW = Math.max(2.0f, Math.min(w * 0.42f, w * 0.5f));
                pts.add(new PolylineNode.Point(x + w - armW, cy));
                pts.add(new PolylineNode.Point(x, y));
                pts.add(new PolylineNode.Point(x + armW, y));
                pts.add(new PolylineNode.Point(x + w, cy));
                pts.add(new PolylineNode.Point(x + armW, y + h));
                pts.add(new PolylineNode.Point(x, y + h));
                break;
            }
            case "double_arrow": {
                float headLen = Math.max(2.0f, Math.min(w * 0.28f, (w - 2.0f) * 0.5f));
                float shaftH = Math.max(1.0f, Math.min(h * 0.40f, h - 2.0f));
                float shaftTop = cy - shaftH * 0.5f;
                float shaftBottom = cy + shaftH * 0.5f;
                pts.add(new PolylineNode.Point(x, cy));
                pts.add(new PolylineNode.Point(x + headLen, y));
                pts.add(new PolylineNode.Point(x + headLen, shaftTop));
                pts.add(new PolylineNode.Point(x + w - headLen, shaftTop));
                pts.add(new PolylineNode.Point(x + w - headLen, y));
                pts.add(new PolylineNode.Point(x + w, cy));
                pts.add(new PolylineNode.Point(x + w - headLen, y + h));
                pts.add(new PolylineNode.Point(x + w - headLen, shaftBottom));
                pts.add(new PolylineNode.Point(x + headLen, shaftBottom));
                pts.add(new PolylineNode.Point(x + headLen, y + h));
                break;
            }
            case "cross": {
                float tw = Math.max(1.0f, Math.min(w * 0.34f, w - 2.0f));
                float th = Math.max(1.0f, Math.min(h * 0.34f, h - 2.0f));
                pts.add(new PolylineNode.Point(cx - tw * 0.5f, y));
                pts.add(new PolylineNode.Point(cx + tw * 0.5f, y));
                pts.add(new PolylineNode.Point(cx + tw * 0.5f, cy - th * 0.5f));
                pts.add(new PolylineNode.Point(x + w, cy - th * 0.5f));
                pts.add(new PolylineNode.Point(x + w, cy + th * 0.5f));
                pts.add(new PolylineNode.Point(cx + tw * 0.5f, cy + th * 0.5f));
                pts.add(new PolylineNode.Point(cx + tw * 0.5f, y + h));
                pts.add(new PolylineNode.Point(cx - tw * 0.5f, y + h));
                pts.add(new PolylineNode.Point(cx - tw * 0.5f, cy + th * 0.5f));
                pts.add(new PolylineNode.Point(x, cy + th * 0.5f));
                pts.add(new PolylineNode.Point(x, cy - th * 0.5f));
                pts.add(new PolylineNode.Point(cx - tw * 0.5f, cy - th * 0.5f));
                break;
            }
            case "heart": {
                int N = 32;
                for (int i = 0; i < N; i++) {
                    double t = -Math.PI + (2.0 * Math.PI * i) / N;
                    double sinT = Math.sin(t);
                    double cosT = Math.cos(t);
                    double hx = 16.0 * sinT * sinT * sinT;
                    double hy = 13.0 * cosT - 5.0 * Math.cos(2.0 * t) - 2.0 * Math.cos(3.0 * t) - Math.cos(4.0 * t);
                    float px = (float) (cx + (hx / 16.0) * rx);
                    float py = (float) (y + h - ((hy + 17.0) / 29.0) * h);
                    pts.add(new PolylineNode.Point(px, py));
                }
                break;
            }
            case "speech_bubble": {
                float bH = h * 0.76f;
                pts.add(new PolylineNode.Point(x, y));
                pts.add(new PolylineNode.Point(x + w, y));
                pts.add(new PolylineNode.Point(x + w, y + bH));
                pts.add(new PolylineNode.Point(x + w * 0.38f, y + bH));
                pts.add(new PolylineNode.Point(x + w * 0.10f, y + h));
                pts.add(new PolylineNode.Point(x + w * 0.18f, y + bH));
                pts.add(new PolylineNode.Point(x, y + bH));
                break;
            }
            case "lightning":
            case "bolt": {
                pts.add(new PolylineNode.Point(x + w * 0.58f, y));
                pts.add(new PolylineNode.Point(x + w * 0.15f, y + h * 0.52f));
                pts.add(new PolylineNode.Point(x + w * 0.48f, y + h * 0.52f));
                pts.add(new PolylineNode.Point(x + w * 0.38f, y + h));
                pts.add(new PolylineNode.Point(x + w * 0.85f, y + h * 0.44f));
                pts.add(new PolylineNode.Point(x + w * 0.52f, y + h * 0.44f));
                break;
            }
            default: { // default rectangle
                pts.add(new PolylineNode.Point(x, y));
                pts.add(new PolylineNode.Point(x + w, y));
                pts.add(new PolylineNode.Point(x + w, y + h));
                pts.add(new PolylineNode.Point(x, y + h));
                break;
            }
        }

        // Apply 2D rotation around center if present
        if (Math.abs(rotation) > 1e-4f) {
            double rad = Math.toRadians(rotation);
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);
            List<PolylineNode.Point> rotated = new ArrayList<>(pts.size());
            for (PolylineNode.Point pt : pts) {
                float dx = pt.x() - cx;
                float dy = pt.y() - cy;
                float rxPt = (float) (cx + dx * cos - dy * sin);
                float ryPt = (float) (cy + dx * sin + dy * cos);
                rotated.add(new PolylineNode.Point(rxPt, ryPt));
            }
            return rotated;
        }

        return pts;
    }

    private static List<PolylineNode.Point> createRegularPolygonPoints(float cx, float cy, float rx, float ry, int sides, double startAngle) {
        List<PolylineNode.Point> list = new ArrayList<>(sides);
        double step = (2.0 * Math.PI) / sides;
        for (int i = 0; i < sides; i++) {
            double a = startAngle + i * step;
            list.add(new PolylineNode.Point((float) (cx + rx * Math.cos(a)), (float) (cy + ry * Math.sin(a))));
        }
        return list;
    }

    private static List<PolylineNode.Point> createStarPoints(float cx, float cy, float rx, float ry, int points, float innerRatio, double startAngle) {
        List<PolylineNode.Point> list = new ArrayList<>(points * 2);
        double step = Math.PI / points;
        for (int i = 0; i < points * 2; i++) {
            double a = startAngle + i * step;
            float rrx = (i % 2 == 0) ? rx : rx * innerRatio;
            float rry = (i % 2 == 0) ? ry : ry * innerRatio;
            list.add(new PolylineNode.Point((float) (cx + rrx * Math.cos(a)), (float) (cy + rry * Math.sin(a))));
        }
        return list;
    }

    /**
     * Triangulates the shape boundary using the robust Ear-Clipping algorithm.
     */
    public List<TriangleNode> triangulate() {
        List<PolylineNode.Point> vertices = computeBoundaryPoints();
        List<TriangleNode> triangles = new ArrayList<>();
        int n = vertices.size();
        if (n < 3) return triangles;
        if (n == 3) {
            triangles.add(new TriangleNode(
                    vertices.get(0).x(), vertices.get(0).y(),
                    vertices.get(1).x(), vertices.get(1).y(),
                    vertices.get(2).x(), vertices.get(2).y(),
                    depth, color, doubleSided));
            return triangles;
        }

        // Polygon area orientation check
        double area = 0.0;
        for (int i = 0; i < n; i++) {
            PolylineNode.Point p1 = vertices.get(i);
            PolylineNode.Point p2 = vertices.get((i + 1) % n);
            area += (p1.x() * p2.y() - p2.x() * p1.y());
        }
        boolean isCCW = area > 0;

        List<PolylineNode.Point> poly = new ArrayList<>(vertices);
        int iterations = 0;
        int maxIterations = n * n * 2;

        while (poly.size() > 3 && iterations++ < maxIterations) {
            boolean earFound = false;
            int count = poly.size();
            for (int i = 0; i < count; i++) {
                int prev = (i - 1 + count) % count;
                int next = (i + 1) % count;
                PolylineNode.Point a = poly.get(prev);
                PolylineNode.Point b = poly.get(i);
                PolylineNode.Point c = poly.get(next);

                // Convexity check
                double cross = (b.x() - a.x()) * (c.y() - a.y()) - (b.y() - a.y()) * (c.x() - a.x());
                if ((isCCW && cross <= 1e-7) || (!isCCW && cross >= -1e-7)) {
                    continue; // reflex vertex, not an ear
                }

                // Check if any other vertex lies inside triangle (a, b, c)
                boolean hasInside = false;
                for (int j = 0; j < count; j++) {
                    if (j == prev || j == i || j == next) continue;
                    PolylineNode.Point p = poly.get(j);
                    if (isPointInsideTriangle(p, a, b, c)) {
                        hasInside = true;
                        break;
                    }
                }

                if (!hasInside) {
                    double triArea = Math.abs(a.x() * (b.y() - c.y()) + b.x() * (c.y() - a.y()) + c.x() * (a.y() - b.y()));
                    if (triArea > 0.01) {
                        triangles.add(new TriangleNode(a.x(), a.y(), b.x(), b.y(), c.x(), c.y(), depth, color, doubleSided));
                    }
                    poly.remove(i);
                    earFound = true;
                    break;
                }
            }

            if (!earFound) {
                // Fallback: fan triangulation from vertex 0
                break;
            }
        }

        if (poly.size() >= 3) {
            for (int i = 1; i < poly.size() - 1; i++) {
                PolylineNode.Point p0 = poly.get(0);
                PolylineNode.Point pi = poly.get(i);
                PolylineNode.Point pi1 = poly.get(i + 1);
                double triArea = Math.abs(p0.x() * (pi.y() - pi1.y()) + pi.x() * (pi1.y() - p0.y()) + pi1.x() * (p0.y() - pi.y()));
                if (triArea > 0.01) {
                    triangles.add(new TriangleNode(
                            p0.x(), p0.y(),
                            pi.x(), pi.y(),
                            pi1.x(), pi1.y(),
                            depth, color, doubleSided));
                }
            }
        }

        return triangles;
    }

    private static boolean isPointInsideTriangle(PolylineNode.Point p, PolylineNode.Point a, PolylineNode.Point b, PolylineNode.Point c) {
        double d1 = sign(p, a, b);
        double d2 = sign(p, b, c);
        double d3 = sign(p, c, a);
        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);
        return !(hasNeg && hasPos);
    }

    private static double sign(PolylineNode.Point p1, PolylineNode.Point p2, PolylineNode.Point p3) {
        return (p1.x() - p3.x()) * (p2.y() - p3.y()) - (p2.x() - p3.x()) * (p1.y() - p3.y());
    }

    /**
     * Decomposes this shape node into standard elemental UiNodes (UiBackgroundNode, TriangleNode, ParallelogramNode, LineNode).
     */
    public List<UiNode> decomposeToNodes() {
        List<UiNode> nodes = new ArrayList<>();
        String type = shapeType.toLowerCase().trim();
        boolean hasRotation = Math.abs(rotation) > 1e-4f;
        float cx = x + width * 0.5f;
        float cy = y + height * 0.5f;
        float rx = width * 0.5f;
        float ry = height * 0.5f;

        // 1. Fill rendering — Optimized assembly from fundamental primitives (rect, triangle, parallelogram) or triangulation
        if (!hasRotation && type.equals("rect")) {
            if (width > 0.001f && height > 0.001f) {
                nodes.add(new UiBackgroundNode(x, y, depth, width, height, color, doubleSided));
            }
        } else if (!hasRotation && type.equals("rounded_rect")) {
            float r = Math.max(0, Math.min(cornerRadius, Math.min(rx, ry)));
            if (r <= 0.5f || width <= 2.0f || height <= 2.0f) {
                if (width > 0.001f && height > 0.001f) {
                    nodes.add(new UiBackgroundNode(x, y, depth, width, height, color, doubleSided));
                }
            } else {
                int cornerSteps = 8;
                float stepH = r / cornerSteps;

                // 1. Top rounded corner slices
                for (int i = 0; i < cornerSteps; i++) {
                    float sliceY = y + i * stepH;
                    float midY = (i + 0.5f) * stepH;
                    float d = r - midY;
                    float indent = (float) (r - Math.sqrt(Math.max(0, r * r - d * d)));
                    float sliceW = width - 2.0f * indent;
                    if (sliceW > 0.05f && stepH > 0.05f) {
                        nodes.add(new UiBackgroundNode(x + indent, sliceY, depth, sliceW, stepH, color, doubleSided));
                    }
                }

                // 2. Middle main rectangular body
                float middleH = height - 2.0f * r;
                if (middleH > 0.05f) {
                    nodes.add(new UiBackgroundNode(x, y + r, depth, width, middleH, color, doubleSided));
                }

                // 3. Bottom rounded corner slices
                for (int i = 0; i < cornerSteps; i++) {
                    float sliceY = y + height - r + i * stepH;
                    float midY = (i + 0.5f) * stepH;
                    float d = midY;
                    float indent = (float) (r - Math.sqrt(Math.max(0, r * r - d * d)));
                    float sliceW = width - 2.0f * indent;
                    if (sliceW > 0.05f && stepH > 0.05f) {
                        nodes.add(new UiBackgroundNode(x + indent, sliceY, depth, sliceW, stepH, color, doubleSided));
                    }
                }
            }
        } else if (!hasRotation && type.equals("circle") && width > 4.0f && height > 4.0f) {
            int steps = 20;
            float stepH = height / steps;
            for (int i = 0; i < steps; i++) {
                float sliceY = y + i * stepH;
                float midY = sliceY + stepH * 0.5f;
                float dy = (midY - cy) / ry;
                if (Math.abs(dy) <= 1.0f) {
                    float sliceW = (float) (2.0f * rx * Math.sqrt(Math.max(0, 1.0f - dy * dy)));
                    float sliceX = cx - sliceW * 0.5f;
                    if (sliceW > 0.05f && stepH > 0.05f) {
                        nodes.add(new UiBackgroundNode(sliceX, sliceY, depth, sliceW, stepH, color, doubleSided));
                    }
                }
            }
        } else if (!hasRotation && type.equals("diamond") && width > 1.0f && height > 1.0f) {
            // Exactly 2 triangles (top & bottom)
            nodes.add(new TriangleNode(cx, y, x, cy, x + width, cy, depth, color, doubleSided));
            nodes.add(new TriangleNode(cx, y + height, x, cy, x + width, cy, depth, color, doubleSided));
        } else if (!hasRotation && type.equals("trapezoid") && width > 4.0f && height > 1.0f) {
            // 1 core rect + 2 side triangles (0% overlap)
            float ins = Math.max(0.0f, Math.min(width * 0.22f, (width - 2.0f) * 0.5f));
            float coreW = width - 2.0f * ins;
            if (coreW > 0.05f) {
                nodes.add(new UiBackgroundNode(x + ins, y, depth, coreW, height, color, doubleSided));
            }
            if (ins > 0.05f) {
                nodes.add(new TriangleNode(x + ins, y, x, y + height, x + ins, y + height, depth, color, doubleSided));
                nodes.add(new TriangleNode(x + width - ins, y, x + width, y + height, x + width - ins, y + height, depth, color, doubleSided));
            }
        } else if (!hasRotation && (type.equals("parallelogram") || type.equals("slanted")) && width > 2.0f && height > 1.0f) {
            float skew = Math.max(0.0f, Math.min(width * 0.25f, (width - 1.0f) * 0.5f));
            nodes.add(new ParallelogramNode(x + skew, y, x + width, y, x, y + height, depth, color, doubleSided));
        } else if (!hasRotation && type.equals("triangle") && width > 0.5f && height > 0.5f) {
            nodes.add(new TriangleNode(cx, y, x + width, y + height, x, y + height, depth, color, doubleSided));
        } else if (!hasRotation && type.equals("right_triangle") && width > 0.5f && height > 0.5f) {
            nodes.add(new TriangleNode(x, y, x + width, y + height, x, y + height, depth, color, doubleSided));
        } else if (!hasRotation && type.equals("arrow_right") && width > 4.0f && height > 2.0f) {
            // 1 shaft rect + 1 head triangle (0% overlap)
            float headLen = Math.max(2.0f, Math.min(width * 0.42f, width - 2.0f));
            float shaftH = Math.max(1.0f, Math.min(height * 0.45f, height - 1.0f));
            float shaftW = width - headLen;
            float shaftTop = cy - shaftH * 0.5f;
            if (shaftW > 0.05f && shaftH > 0.05f) {
                nodes.add(new UiBackgroundNode(x, shaftTop, depth, shaftW, shaftH, color, doubleSided));
            }
            nodes.add(new TriangleNode(x + width - headLen, y, x + width - headLen, y + height, x + width, cy, depth, color, doubleSided));
        } else if (!hasRotation && type.equals("arrow_left") && width > 4.0f && height > 2.0f) {
            // 1 shaft rect + 1 head triangle (0% overlap)
            float headLen = Math.max(2.0f, Math.min(width * 0.42f, width - 2.0f));
            float shaftH = Math.max(1.0f, Math.min(height * 0.45f, height - 1.0f));
            float shaftW = width - headLen;
            float shaftTop = cy - shaftH * 0.5f;
            if (shaftW > 0.05f && shaftH > 0.05f) {
                nodes.add(new UiBackgroundNode(x + headLen, shaftTop, depth, shaftW, shaftH, color, doubleSided));
            }
            nodes.add(new TriangleNode(x + headLen, y, x + headLen, y + height, x, cy, depth, color, doubleSided));
        } else if (!hasRotation && type.equals("double_arrow") && width > 6.0f && height > 2.0f) {
            // 1 shaft rect + 2 head triangles (0% overlap)
            float headLen = Math.max(2.0f, Math.min(width * 0.28f, (width - 2.0f) * 0.5f));
            float shaftH = Math.max(1.0f, Math.min(height * 0.40f, height - 1.0f));
            float shaftW = width - 2.0f * headLen;
            float shaftTop = cy - shaftH * 0.5f;
            nodes.add(new TriangleNode(x, cy, x + headLen, y, x + headLen, y + height, depth, color, doubleSided));
            if (shaftW > 0.05f && shaftH > 0.05f) {
                nodes.add(new UiBackgroundNode(x + headLen, shaftTop, depth, shaftW, shaftH, color, doubleSided));
            }
            nodes.add(new TriangleNode(x + width, cy, x + width - headLen, y, x + width - headLen, y + height, depth, color, doubleSided));
        } else if (!hasRotation && type.equals("cross") && width > 3.0f && height > 3.0f) {
            // 3 non-overlapping rects: vertical bar + left arm + right arm (0% overlap for alpha)
            float tw = Math.max(1.0f, Math.min(width * 0.34f, width - 1.0f));
            float th = Math.max(1.0f, Math.min(height * 0.34f, height - 1.0f));
            float armW = (width - tw) * 0.5f;
            nodes.add(new UiBackgroundNode(cx - tw * 0.5f, y, depth, tw, height, color, doubleSided));
            if (armW > 0.05f && th > 0.05f) {
                nodes.add(new UiBackgroundNode(x, cy - th * 0.5f, depth, armW, th, color, doubleSided));
                nodes.add(new UiBackgroundNode(cx + tw * 0.5f, cy - th * 0.5f, depth, armW, th, color, doubleSided));
            }
        } else if (!hasRotation && type.equals("star6") && width > 2.0f && height > 2.0f) {
            // 2 overlapping equilateral triangles
            List<PolylineNode.Point> up = createRegularPolygonPoints(cx, cy, rx, ry, 3, -Math.PI / 2.0);
            List<PolylineNode.Point> down = createRegularPolygonPoints(cx, cy, rx, ry, 3, Math.PI / 2.0);
            nodes.add(new TriangleNode(up.get(0).x(), up.get(0).y(), up.get(1).x(), up.get(1).y(), up.get(2).x(), up.get(2).y(), depth, color, doubleSided));
            nodes.add(new TriangleNode(down.get(0).x(), down.get(0).y(), down.get(1).x(), down.get(1).y(), down.get(2).x(), down.get(2).y(), depth, color, doubleSided));
        } else if (!hasRotation && (type.equals("pentagon") || type.equals("hexagon") || type.equals("heptagon") || type.equals("octagon")
                || type.equals("star3") || type.equals("star4") || type.equals("star5"))) {
            // Fast, exact Triangle Fan from center for convex regular polygons and stars
            List<PolylineNode.Point> pts = computeBoundaryPoints();
            for (int i = 0; i < pts.size(); i++) {
                PolylineNode.Point p1 = pts.get(i);
                PolylineNode.Point p2 = pts.get((i + 1) % pts.size());
                double triArea = Math.abs(cx * (p1.y() - p2.y()) + p1.x() * (p2.y() - cy) + p2.x() * (cy - p1.y()));
                if (triArea > 0.001) {
                    nodes.add(new TriangleNode(cx, cy, p1.x(), p1.y(), p2.x(), p2.y(), depth, color, doubleSided));
                }
            }
        } else {
            nodes.addAll(triangulate());
        }

        // If decomposition yielded nothing (e.g. tiny shape with sub-threshold triangles), ensure at least 1 fallback element
        if (nodes.isEmpty()) {
            if (width > 0.001f && height > 0.001f) {
                nodes.add(new UiBackgroundNode(x, y, depth, width, height, color, doubleSided));
            }
        }

        // 2. Outline rendering
        if (outline && outlineThickness > 0) {
            String style = outlineStyle != null ? outlineStyle.toLowerCase().trim() : "solid";
            if ("dashed".equals(style) || "dotted".equals(style)) {
                List<PolylineNode.Point> boundary = computeBoundaryPoints();
                float dashLen = "dotted".equals(style) ? Math.max(2.5f, outlineThickness * 1.5f) : 6.0f;
                float gapLen = "dotted".equals(style) ? Math.max(3.0f, outlineThickness * 2.0f) : 4.0f;
                nodes.addAll(createDashedOutlineNodes(boundary, dashLen, gapLen, outlineThickness, depth + 0.0001f, outlineColor, doubleSided));
            } else {
                List<PolylineNode.Point> boundary = computeBoundaryPoints();
                nodes.add(new PolylineNode(boundary, outlineThickness, depth + 0.0001f, outlineColor, doubleSided, true));
            }
        }

        return nodes;
    }

    private static List<UiNode> createDashedOutlineNodes(
            List<PolylineNode.Point> boundary, float dashLen, float gapLen,
            float thickness, float depth, Color outlineColor, boolean doubleSided) {
        List<UiNode> nodes = new ArrayList<>();
        int n = boundary.size();
        if (n < 2) return nodes;

        for (int i = 0; i < n; i++) {
            PolylineNode.Point p1 = boundary.get(i);
            PolylineNode.Point p2 = boundary.get((i + 1) % n);
            float dx = p2.x() - p1.x();
            float dy = p2.y() - p1.y();
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length < 1e-4f) continue;

            float ux = dx / length;
            float uy = dy / length;
            float current = 0.0f;

            while (current < length) {
                float end = Math.min(current + dashLen, length);
                float x1 = p1.x() + ux * current;
                float y1 = p1.y() + uy * current;
                float x2 = p1.x() + ux * end;
                float y2 = p1.y() + uy * end;
                nodes.add(new LineNode(x1, y1, x2, y2, thickness, depth, outlineColor, doubleSided, 0.0f));
                current += dashLen + gapLen;
            }
        }
        return nodes;
    }

    public static Builder builder(String shapeType, float x, float y, float width, float height) {
        return new Builder(shapeType, x, y, width, height);
    }

    public static final class Builder {
        private String shapeType;
        private float x;
        private float y;
        private float width;
        private float height;
        private float depth = 0.001f;
        private Color color = Color.WHITE;
        private boolean outline = false;
        private Color outlineColor = Color.WHITE;
        private float outlineThickness = 2.0f;
        private String outlineStyle = "solid";
        private float cornerRadius = 6.0f;
        private float rotation = 0.0f;
        private boolean doubleSided = false;

        public Builder(String shapeType, float x, float y, float width, float height) {
            this.shapeType = shapeType;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Builder depth(float depth) { this.depth = depth; return this; }
        public Builder color(Color color) { this.color = color; return this; }
        public Builder outline(boolean outline) { this.outline = outline; return this; }
        public Builder outlineColor(Color outlineColor) { this.outlineColor = outlineColor; return this; }
        public Builder outlineThickness(float outlineThickness) { this.outlineThickness = outlineThickness; return this; }
        public Builder outlineStyle(String outlineStyle) { this.outlineStyle = outlineStyle; return this; }
        public Builder cornerRadius(float cornerRadius) { this.cornerRadius = cornerRadius; return this; }
        public Builder rotation(float rotation) { this.rotation = rotation; return this; }
        public Builder doubleSided(boolean doubleSided) { this.doubleSided = doubleSided; return this; }

        public UiShapeNode build() {
            return new UiShapeNode(shapeType, x, y, width, height, depth, color, outline, outlineColor, outlineThickness, outlineStyle, cornerRadius, rotation, doubleSided);
        }
    }
}
