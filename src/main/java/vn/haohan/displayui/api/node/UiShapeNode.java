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
                int sides = 20;
                for (int i = 0; i < sides; i++) {
                    double angle = (2.0 * Math.PI * i) / sides;
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
                float ins = w * 0.22f;
                pts.add(new PolylineNode.Point(x + ins, y));
                pts.add(new PolylineNode.Point(x + w - ins, y));
                pts.add(new PolylineNode.Point(x + w, y + h));
                pts.add(new PolylineNode.Point(x, y + h));
                break;
            }
            case "parallelogram": {
                float sl = w * 0.22f;
                pts.add(new PolylineNode.Point(x + sl, y));
                pts.add(new PolylineNode.Point(x + w, y));
                pts.add(new PolylineNode.Point(x + w - sl, y + h));
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
                pts.addAll(createRegularPolygonPoints(cx, cy, rx, ry, 6, 0));
                break;
            case "heptagon":
                pts.addAll(createRegularPolygonPoints(cx, cy, rx, ry, 7, -Math.PI / 2.0));
                break;
            case "octagon":
                pts.addAll(createRegularPolygonPoints(cx, cy, rx, ry, 8, Math.PI / 8.0));
                break;
            case "star3":
                pts.addAll(createStarPoints(cx, cy, rx, ry, 3, 0.45f, -Math.PI / 2.0));
                break;
            case "star4":
                pts.addAll(createStarPoints(cx, cy, rx, ry, 4, 0.4f, -Math.PI / 2.0));
                break;
            case "star5":
                pts.addAll(createStarPoints(cx, cy, rx, ry, 5, 0.42f, -Math.PI / 2.0));
                break;
            case "star6":
                pts.addAll(createStarPoints(cx, cy, rx, ry, 6, 0.48f, -Math.PI / 2.0));
                break;
            case "arrow_right": {
                float aw = w * 0.45f, ah = h * 0.26f;
                pts.add(new PolylineNode.Point(x, y + ah));
                pts.add(new PolylineNode.Point(x + w - aw, y + ah));
                pts.add(new PolylineNode.Point(x + w - aw, y));
                pts.add(new PolylineNode.Point(x + w, cy));
                pts.add(new PolylineNode.Point(x + w - aw, y + h));
                pts.add(new PolylineNode.Point(x + w - aw, y + h - ah));
                pts.add(new PolylineNode.Point(x, y + h - ah));
                break;
            }
            case "arrow_left": {
                float aw = w * 0.45f, ah = h * 0.26f;
                pts.add(new PolylineNode.Point(x + w, y + ah));
                pts.add(new PolylineNode.Point(x + aw, y + ah));
                pts.add(new PolylineNode.Point(x + aw, y));
                pts.add(new PolylineNode.Point(x, cy));
                pts.add(new PolylineNode.Point(x + aw, y + h));
                pts.add(new PolylineNode.Point(x + aw, y + h - ah));
                pts.add(new PolylineNode.Point(x + w, y + h - ah));
                break;
            }
            case "chevron_right": {
                pts.add(new PolylineNode.Point(x, y));
                pts.add(new PolylineNode.Point(x + w * 0.55f, cy));
                pts.add(new PolylineNode.Point(x, y + h));
                pts.add(new PolylineNode.Point(x + w * 0.45f, y + h));
                pts.add(new PolylineNode.Point(x + w, cy));
                pts.add(new PolylineNode.Point(x + w * 0.45f, y));
                break;
            }
            case "double_arrow": {
                float aw = w * 0.3f, ah = h * 0.26f;
                pts.add(new PolylineNode.Point(x + aw, y));
                pts.add(new PolylineNode.Point(x + aw, y + ah));
                pts.add(new PolylineNode.Point(x + w - aw, y + ah));
                pts.add(new PolylineNode.Point(x + w - aw, y));
                pts.add(new PolylineNode.Point(x + w, cy));
                pts.add(new PolylineNode.Point(x + w - aw, y + h));
                pts.add(new PolylineNode.Point(x + w - aw, y + h - ah));
                pts.add(new PolylineNode.Point(x + aw, y + h - ah));
                pts.add(new PolylineNode.Point(x + aw, y + h));
                pts.add(new PolylineNode.Point(x, cy));
                break;
            }
            case "cross": {
                float tw = w * 0.3f, th = h * 0.3f;
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
            case "speech_bubble": {
                float bH = h * 0.78f;
                pts.add(new PolylineNode.Point(x, y));
                pts.add(new PolylineNode.Point(x + w, y));
                pts.add(new PolylineNode.Point(x + w, y + bH));
                pts.add(new PolylineNode.Point(x + w * 0.45f, y + bH));
                pts.add(new PolylineNode.Point(x + w * 0.25f, y + h));
                pts.add(new PolylineNode.Point(x + w * 0.28f, y + bH));
                pts.add(new PolylineNode.Point(x, y + bH));
                break;
            }
            case "heart": {
                pts.add(new PolylineNode.Point(cx, y + h));
                pts.add(new PolylineNode.Point(x + w * 0.15f, y + h * 0.65f));
                pts.add(new PolylineNode.Point(x, y + h * 0.35f));
                pts.add(new PolylineNode.Point(x + w * 0.15f, y + h * 0.1f));
                pts.add(new PolylineNode.Point(x + w * 0.35f, y));
                pts.add(new PolylineNode.Point(cx, y + h * 0.22f));
                pts.add(new PolylineNode.Point(x + w * 0.65f, y));
                pts.add(new PolylineNode.Point(x + w * 0.85f, y + h * 0.1f));
                pts.add(new PolylineNode.Point(x + w, y + h * 0.35f));
                pts.add(new PolylineNode.Point(x + w * 0.85f, y + h * 0.65f));
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
                    triangles.add(new TriangleNode(a.x(), a.y(), b.x(), b.y(), c.x(), c.y(), depth, color, doubleSided));
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
                triangles.add(new TriangleNode(
                        poly.get(0).x(), poly.get(0).y(),
                        poly.get(i).x(), poly.get(i).y(),
                        poly.get(i + 1).x(), poly.get(i + 1).y(),
                        depth, color, doubleSided));
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
     * Decomposes this shape node into standard elemental UiNodes (UiBackgroundNode, TriangleNode, PolylineNode).
     */
    public List<UiNode> decomposeToNodes() {
        List<UiNode> nodes = new ArrayList<>();
        String type = shapeType.toLowerCase().trim();
        boolean hasRotation = Math.abs(rotation) > 1e-4f;

        // 1. Fill rendering
        if (!hasRotation && type.equals("rect")) {
            nodes.add(new UiBackgroundNode(x, y, depth, width, height, color, doubleSided));
        } else if (!hasRotation && type.equals("rounded_rect")) {
            float rx = width * 0.5f;
            float ry = height * 0.5f;
            float r = Math.max(0, Math.min(cornerRadius, Math.min(rx, ry)));
            if (r <= 0.5f) {
                nodes.add(new UiBackgroundNode(x, y, depth, width, height, color, doubleSided));
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
                    if (sliceW > 0.05f) {
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
                    if (sliceW > 0.05f) {
                        nodes.add(new UiBackgroundNode(x + indent, sliceY, depth, sliceW, stepH, color, doubleSided));
                    }
                }
            }
        } else if (!hasRotation && type.equals("circle")) {
            int steps = 20;
            float rx = width * 0.5f;
            float ry = height * 0.5f;
            float cx = x + rx;
            float cy = y + ry;
            float stepH = height / steps;
            for (int i = 0; i < steps; i++) {
                float sliceY = y + i * stepH;
                float midY = sliceY + stepH * 0.5f;
                float dy = (midY - cy) / ry;
                if (Math.abs(dy) <= 1.0f) {
                    float sliceW = (float) (2.0f * rx * Math.sqrt(Math.max(0, 1.0f - dy * dy)));
                    float sliceX = cx - sliceW * 0.5f;
                    if (sliceW > 0.05f) {
                        nodes.add(new UiBackgroundNode(sliceX, sliceY, depth, sliceW, stepH, color, doubleSided));
                    }
                }
            }
        } else if (!hasRotation && type.equals("parallelogram")) {
            float sl = width * 0.22f;
            nodes.add(new ParallelogramNode(x + sl, y, x + width, y, x + width - sl, y + height, depth, color, doubleSided));
        } else if (!hasRotation && type.equals("triangle")) {
            float cx = x + width * 0.5f;
            nodes.add(new TriangleNode(cx, y, x + width, y + height, x, y + height, depth, color, doubleSided));
        } else if (!hasRotation && type.equals("right_triangle")) {
            nodes.add(new TriangleNode(x, y, x + width, y + height, x, y + height, depth, color, doubleSided));
        } else {
            nodes.addAll(triangulate());
        }

        // 2. Outline rendering
        if (outline && outlineThickness > 0) {
            if (!hasRotation && color.getAlpha() == 255 && type.equals("rect")) {
                // Alpha = 255 for plain rect: Single expanded background behind the main shape
                nodes.add(0, new UiBackgroundNode(
                        x - outlineThickness, y - outlineThickness,
                        depth - 0.0001f,
                        width + 2.0f * outlineThickness, height + 2.0f * outlineThickness,
                        outlineColor, doubleSided));
            } else if (!hasRotation && type.equals("rounded_rect")) {
                float rx = width * 0.5f;
                float ry = height * 0.5f;
                float r = Math.max(0, Math.min(cornerRadius, Math.min(rx, ry)));
                nodes.addAll(createRoundedRectOutlineNodes(x, y, width, height, r, outlineThickness, depth + 0.0001f, outlineColor, doubleSided));
            } else {
                List<PolylineNode.Point> boundary = computeBoundaryPoints();
                nodes.add(new PolylineNode(boundary, outlineThickness, depth + 0.0001f, outlineColor, doubleSided, true));
            }
        }

        return nodes;
    }

    private static List<UiNode> createRoundedRectOutlineNodes(
            float x, float y, float width, float height, float r,
            float thickness, float depth, Color outlineColor, boolean doubleSided) {
        List<UiNode> nodes = new ArrayList<>();
        if (thickness <= 0.001f) return nodes;

        if (r <= 0.5f) {
            nodes.add(new UiBackgroundNode(x - thickness, y - thickness, depth, width + 2.0f * thickness, thickness, outlineColor, doubleSided));
            nodes.add(new UiBackgroundNode(x - thickness, y + height, depth, width + 2.0f * thickness, thickness, outlineColor, doubleSided));
            nodes.add(new UiBackgroundNode(x - thickness, y, depth, thickness, height, outlineColor, doubleSided));
            nodes.add(new UiBackgroundNode(x + width, y, depth, thickness, height, outlineColor, doubleSided));
            return nodes;
        }

        // Top, Bottom, Left, Right straight bars
        if (width - 2.0f * r > 0.05f) {
            nodes.add(new UiBackgroundNode(x + r, y - thickness, depth, width - 2.0f * r, thickness, outlineColor, doubleSided));
            nodes.add(new UiBackgroundNode(x + r, y + height, depth, width - 2.0f * r, thickness, outlineColor, doubleSided));
        }
        if (height - 2.0f * r > 0.05f) {
            nodes.add(new UiBackgroundNode(x - thickness, y + r, depth, thickness, height - 2.0f * r, outlineColor, doubleSided));
            nodes.add(new UiBackgroundNode(x + width, y + r, depth, thickness, height - 2.0f * r, outlineColor, doubleSided));
        }

        int steps = 8;
        float rOut = r + thickness;
        float stepH = rOut / steps;

        // Top corners
        for (int i = 0; i < steps; i++) {
            float sliceY = y - thickness + i * stepH;
            float midY = (i + 0.5f) * stepH;
            float dOut = rOut - midY;
            float indOut = (float) (rOut - Math.sqrt(Math.max(0, rOut * rOut - dOut * dOut)));

            float indIn;
            if (midY < thickness) {
                indIn = r + thickness;
            } else {
                float dIn = r - (midY - thickness);
                indIn = (float) (r - Math.sqrt(Math.max(0, r * r - dIn * dIn)));
            }

            float leftX = x - thickness + indOut;
            float leftW = (x + indIn) - leftX;
            if (leftW > 0.05f) {
                nodes.add(new UiBackgroundNode(leftX, sliceY, depth, leftW, stepH, outlineColor, doubleSided));
            }

            float rightX = x + width - indIn;
            float rightEnd = x + width + thickness - indOut;
            float rightW = rightEnd - rightX;
            if (rightW > 0.05f) {
                nodes.add(new UiBackgroundNode(rightX, sliceY, depth, rightW, stepH, outlineColor, doubleSided));
            }
        }

        // Bottom corners
        for (int i = 0; i < steps; i++) {
            float sliceY = y + height - r + i * stepH;
            float midY = (i + 0.5f) * stepH;
            float dOut = midY;
            float indOut = (float) (rOut - Math.sqrt(Math.max(0, rOut * rOut - dOut * dOut)));

            float indIn;
            if (midY > r) {
                indIn = r + thickness;
            } else {
                float dIn = midY;
                indIn = (float) (r - Math.sqrt(Math.max(0, r * r - dIn * dIn)));
            }

            float leftX = x - thickness + indOut;
            float leftW = (x + indIn) - leftX;
            if (leftW > 0.05f) {
                nodes.add(new UiBackgroundNode(leftX, sliceY, depth, leftW, stepH, outlineColor, doubleSided));
            }

            float rightX = x + width - indIn;
            float rightEnd = x + width + thickness - indOut;
            float rightW = rightEnd - rightX;
            if (rightW > 0.05f) {
                nodes.add(new UiBackgroundNode(rightX, sliceY, depth, rightW, stepH, outlineColor, doubleSided));
            }
        }

        return nodes;
    }
}
