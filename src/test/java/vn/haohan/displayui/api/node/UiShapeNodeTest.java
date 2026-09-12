/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package vn.haohan.displayui.api.node;

import org.bukkit.Color;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.loader.UiDocumentLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UiShapeNodeTest {

    private static final String[] ALL_SHAPES = {
            "rect", "rounded_rect", "circle", "diamond", "trapezoid", "parallelogram", "slanted",
            "triangle", "right_triangle", "pentagon", "hexagon", "heptagon", "octagon",
            "star3", "star4", "star5", "star6", "arrow_right", "arrow_left",
            "chevron_right", "chevron", "double_arrow", "heart", "cross", "speech_bubble",
            "lightning", "bolt"
    };

    @Test
    void testAllShapesBoundaryAndTriangulation() {
        for (String shapeType : ALL_SHAPES) {
            UiShapeNode node = new UiShapeNode(shapeType, 10, 20, 80, 50, Color.fromRGB(255, 100, 50));
            List<PolylineNode.Point> pts = node.computeBoundaryPoints();
            assertNotNull(pts, "Boundary points null for " + shapeType);
            assertTrue(pts.size() >= 3, "Boundary points < 3 for " + shapeType + ": " + pts.size());

            List<TriangleNode> tris = node.triangulate();
            assertNotNull(tris, "Triangles null for " + shapeType);
            assertTrue(tris.size() >= 1, "Triangles < 1 for " + shapeType + ": " + tris.size());

            List<UiNode> decomposed = node.decomposeToNodes();
            assertNotNull(decomposed, "Decomposition null for " + shapeType);
            assertFalse(decomposed.isEmpty(), "Decomposition empty for " + shapeType);
        }
    }

    @Test
    void testAllShapesWithVariousDimensionsAndRotations() {
        float[][] testSizes = {
                {50, 50},  // Square (width == height)
                {100, 40}, // Landscape (width > height)
                {30, 80},  // Portrait (height > width)
                {2, 2},    // Very small size
                {10, 2},   // Very thin horizontal
                {2, 10}    // Very thin vertical
        };
        float[] testRotations = {0.0f, 30.0f, 45.0f, 90.0f, 180.0f};
        String[] outlineStyles = {"solid", "dashed", "dotted"};

        for (String shapeType : ALL_SHAPES) {
            for (float[] size : testSizes) {
                float w = size[0];
                float h = size[1];
                for (float rot : testRotations) {
                    for (String style : outlineStyles) {
                        UiShapeNode node = UiShapeNode.builder(shapeType, 10, 10, w, h)
                                .color(Color.fromRGB(200, 100, 50))
                                .outline(true)
                                .outlineStyle(style)
                                .outlineThickness(1.5f)
                                .rotation(rot)
                                .cornerRadius(4.0f)
                                .build();

                        List<PolylineNode.Point> pts = node.computeBoundaryPoints();
                        assertNotNull(pts, "Boundary points null for " + shapeType);
                        assertTrue(pts.size() >= 3, "Boundary points < 3 for " + shapeType);

                        for (PolylineNode.Point pt : pts) {
                            assertFalse(Float.isNaN(pt.x()), "NaN in vertex x for " + shapeType);
                            assertFalse(Float.isNaN(pt.y()), "NaN in vertex y for " + shapeType);
                            assertFalse(Float.isInfinite(pt.x()), "Infinity in vertex x for " + shapeType);
                            assertFalse(Float.isInfinite(pt.y()), "Infinity in vertex y for " + shapeType);
                        }

                        List<UiNode> nodes = node.decomposeToNodes();
                        assertNotNull(nodes, "Decomposition null for " + shapeType);
                        assertFalse(nodes.isEmpty(), "Decomposition empty for " + shapeType);
                    }
                }
            }
        }
    }

    @Test
    void testShapeOutlineAlpha255VsAlphaTranslucent() {
        // Both opaque and translucent shapes produce coplanar fill + PolylineNode outline at depth + 0.0001f
        UiShapeNode rectOpaque = new UiShapeNode(
                "rect", 0, 0, 100, 60, 0.001f,
                Color.fromARGB(255, 20, 30, 40),
                true, Color.fromRGB(255, 255, 0), 4.0f, "solid", 0, false);
        List<UiNode> opaqueNodes = rectOpaque.decomposeToNodes();
        assertEquals(2, opaqueNodes.size());
        assertTrue(opaqueNodes.get(0) instanceof UiBackgroundNode);
        assertTrue(opaqueNodes.get(1) instanceof PolylineNode);
        PolylineNode opaqueOutline = (PolylineNode) opaqueNodes.get(1);
        assertTrue(opaqueOutline.closed());
        assertEquals(4.0f, opaqueOutline.thickness(), 0.01f);
        assertEquals(0.0011f, opaqueOutline.depth(), 0.00001f);

        UiShapeNode rectTranslucent = new UiShapeNode(
                "rect", 0, 0, 100, 60, 0.001f,
                Color.fromARGB(150, 20, 30, 40),
                true, Color.fromRGB(255, 255, 0), 4.0f, "solid", 0, false);
        List<UiNode> transNodes = rectTranslucent.decomposeToNodes();
        assertEquals(2, transNodes.size());
        assertTrue(transNodes.get(0) instanceof UiBackgroundNode);
        assertTrue(transNodes.get(1) instanceof PolylineNode);
        PolylineNode polyOutline = (PolylineNode) transNodes.get(1);
        assertTrue(polyOutline.closed());
        assertEquals(4.0f, polyOutline.thickness(), 0.01f);
        assertEquals(0.0011f, polyOutline.depth(), 0.00001f);
    }

    @Test
    void testJsonLoaderWithShapeNodes() {
        String json = """
        {
          "version": "1.0",
          "name": "test_shapes_panel",
          "canvasWidth": 256,
          "canvasHeight": 192,
          "nodes": [
            {
              "type": "shape",
              "shapeType": "star5",
              "x": 20,
              "y": 30,
              "width": 60,
              "height": 60,
              "color": "#ffd700",
              "alpha": 255,
              "outline": true,
              "outlineColor": "#ff0000",
              "outlineThickness": 2,
              "outlineStyle": "solid",
              "depth": 0.002
            },
            {
              "type": "shape",
              "shapeType": "hexagon",
              "x": 100,
              "y": 40,
              "width": 50,
              "height": 50,
              "color": "#00b4d8",
              "alpha": 180,
              "outline": true,
              "outlineColor": "#ffffff",
              "outlineThickness": 3,
              "outlineStyle": "solid"
            },
            {
              "type": "parallelogram",
              "x": 10,
              "y": 120,
              "width": 80,
              "height": 25,
              "color": "#e63946"
            },
            {
              "type": "triangle",
              "x": 120,
              "y": 120,
              "width": 40,
              "height": 40,
              "color": "#2a9d8f"
            }
          ]
        }
        """;

        UiDocument doc = UiDocumentLoader.loadFromString(json);
        assertNotNull(doc);
        assertEquals(4, doc.nodes().size());
        assertTrue(doc.nodes().get(0) instanceof UiShapeNode);
        UiShapeNode star = (UiShapeNode) doc.nodes().get(0);
        assertEquals("star5", star.shapeType());
        assertTrue(star.outline());
        assertEquals(2.0f, star.outlineThickness(), 0.01f);

        assertTrue(doc.nodes().get(1) instanceof UiShapeNode);
        UiShapeNode hex = (UiShapeNode) doc.nodes().get(1);
        assertEquals("hexagon", hex.shapeType());
        assertEquals(180, hex.color().getAlpha());

        assertTrue(doc.nodes().get(2) instanceof ParallelogramNode);
        assertTrue(doc.nodes().get(3) instanceof TriangleNode);
    }

    @Test
    void testUsersExportedJson() {
        String json = """
{
  "version": "1.0",
  "name": "untitled",
  "canvasWidth": 256,
  "canvasHeight": 192,
  "nodes": [
    {
      "type": "background",
      "id": "bg_1",
      "x": 52,
      "y": 58,
      "depth": 0.001,
      "width": 2,
      "height": 60,
      "color": "#2b5aff",
      "alpha": 210,
      "doubleSided": false
    },
    {
      "type": "background",
      "id": "bg_2",
      "x": 54,
      "y": 58,
      "depth": 0.001,
      "width": 80,
      "height": 60,
      "color": "#1a2035",
      "alpha": 210,
      "doubleSided": false
    },
    {
      "type": "text",
      "id": "txt_1",
      "text": "Hi world",
      "boxX": 56,
      "boxY": 60,
      "depth": 0.002,
      "width": 20,
      "height": 8,
      "fontSize": 5,
      "contentWidth": 114,
      "alignment": "CENTER",
      "verticalAlignment": "MIDDLE",
      "leftOffset": 0,
      "rightOffset": 0,
      "verticalOffset": 0,
      "shadow": true,
      "seeThrough": false,
      "doubleSided": true
    },
    {
      "type": "shape",
      "shapeType": "rect",
      "id": "shape_1",
      "x": 112,
      "y": 106,
      "depth": 0.001,
      "width": 18,
      "height": 8,
      "color": "#ffffff",
      "alpha": 210,
      "cornerRadius": 6,
      "outline": true,
      "outlineColor": "#000000",
      "outlineAlpha": 255,
      "outlineThickness": 1,
      "outlineStyle": "solid",
      "doubleSided": false
    },
    {
      "type": "item",
      "id": "item_1",
      "material": "DIAMOND_SWORD",
      "x": 66,
      "y": 94,
      "depth": 0.003,
      "scale": 0.8,
      "transform": "FIXED",
      "doubleSided": false
    },
    {
      "type": "block",
      "id": "blk_3",
      "material": "STONE",
      "x": 78,
      "y": 88,
      "depth": 0.004,
      "width": 18,
      "height": 10,
      "thickness": 1,
      "doubleSided": false
    },
    {
      "type": "shape",
      "shapeType": "cross",
      "id": "shape_4",
      "x": 124,
      "y": 60,
      "depth": 0.001,
      "width": 8,
      "height": 8,
      "color": "#ff0000",
      "alpha": 210,
      "cornerRadius": 6,
      "outline": true,
      "outlineColor": "#4f8ef7",
      "outlineAlpha": 255,
      "outlineThickness": 0,
      "outlineStyle": "solid",
      "doubleSided": false,
      "rotation": 44
    },
    {
      "type": "shape",
      "shapeType": "chevron_right",
      "id": "shape_1",
      "x": 86,
      "y": 70,
      "depth": 0.001,
      "width": 8,
      "height": 10,
      "color": "#1a2035",
      "alpha": 210,
      "cornerRadius": 6,
      "rotation": 0,
      "outline": true,
      "outlineColor": "#4f8ef7",
      "outlineAlpha": 255,
      "outlineThickness": 2,
      "outlineStyle": "solid",
      "doubleSided": false
    },
    {
      "type": "shape",
      "shapeType": "heart",
      "id": "shape_2",
      "x": 106,
      "y": 74,
      "depth": 0.001,
      "width": 22,
      "height": 18,
      "color": "#1a2035",
      "alpha": 210,
      "cornerRadius": 6,
      "rotation": 0,
      "outline": true,
      "outlineColor": "#4f8ef7",
      "outlineAlpha": 255,
      "outlineThickness": 1,
      "outlineStyle": "solid",
      "doubleSided": false
    },
    {
      "type": "shape",
      "shapeType": "speech_bubble",
      "id": "shape_3",
      "x": 138,
      "y": 38,
      "depth": 0.001,
      "width": 36,
      "height": 18,
      "color": "#1a2035",
      "alpha": 210,
      "cornerRadius": 6,
      "rotation": 0,
      "outline": true,
      "outlineColor": "#4f8ef7",
      "outlineAlpha": 255,
      "outlineThickness": 1,
      "outlineStyle": "dashed",
      "doubleSided": false
    },
    {
      "type": "shape",
      "shapeType": "rounded_rect",
      "id": "shape_1",
      "x": 144,
      "y": 74,
      "depth": 0.001,
      "width": 64,
      "height": 50,
      "color": "#1a2035",
      "alpha": 210,
      "cornerRadius": 6,
      "rotation": 0,
      "outline": true,
      "outlineColor": "#4f8ef7",
      "outlineAlpha": 255,
      "outlineThickness": 1,
      "outlineStyle": "solid",
      "doubleSided": false
    }
  ],
  "buttons": []
}
        """;
        UiDocument doc = UiDocumentLoader.loadFromString(json);
        assertNotNull(doc);
        assertEquals(11, doc.nodes().size());

        for (UiNode node : doc.nodes()) {
            if (node instanceof UiShapeNode shapeNode) {
                List<UiNode> subNodes = shapeNode.decomposeToNodes();
                for (UiNode sub : subNodes) {
                    if (sub instanceof TriangleNode tri) {
                        org.joml.Vector3f p1 = new org.joml.Vector3f(tri.x1(), tri.y1(), 0);
                        org.joml.Vector3f p2 = new org.joml.Vector3f(tri.x2(), tri.y2(), 0);
                        org.joml.Vector3f p3 = new org.joml.Vector3f(tri.x3(), tri.y3(), 0);
                        vn.haohan.displayui.api.shape.DisplayShapeMath.computeTriangleTRS(p1, p2, p3);
                    }
                }
            }
        }
    }

    @Test
    void testDashedAndDottedOutlines() {
        UiShapeNode dashed = UiShapeNode.builder("rect", 0, 0, 40, 20)
                .color(Color.fromRGB(200, 100, 50))
                .outline(true)
                .outlineStyle("dashed")
                .outlineThickness(2.0f)
                .build();
        List<UiNode> dashedNodes = dashed.decomposeToNodes();
        assertTrue(dashedNodes.size() > 2, "Dashed outline should produce multiple line segments");
        long lineCount = dashedNodes.stream().filter(n -> n instanceof LineNode).count();
        assertTrue(lineCount >= 4, "Should have line nodes for dashed segments");

        UiShapeNode dotted = UiShapeNode.builder("circle", 0, 0, 30, 30)
                .color(Color.fromRGB(50, 150, 200))
                .outline(true)
                .outlineStyle("dotted")
                .outlineThickness(1.5f)
                .build();
        List<UiNode> dottedNodes = dotted.decomposeToNodes();
        assertTrue(dottedNodes.size() > 5, "Dotted circle outline should produce multiple dotted segments");
    }

    @Test
    void testUiShapeNodeBuilder() {
        UiShapeNode node = UiShapeNode.builder("star5", 10, 20, 30, 40)
                .depth(0.005f)
                .color(Color.fromRGB(255, 200, 0))
                .outline(true)
                .outlineColor(Color.fromRGB(0, 255, 255))
                .outlineThickness(3.0f)
                .outlineStyle("dashed")
                .rotation(45.0f)
                .cornerRadius(5.0f)
                .doubleSided(true)
                .build();

        assertEquals("star5", node.shapeType());
        assertEquals(10, node.x());
        assertEquals(20, node.y());
        assertEquals(30, node.width());
        assertEquals(40, node.height());
        assertEquals(0.005f, node.depth(), 0.0001f);
        assertTrue(node.outline());
        assertEquals(3.0f, node.outlineThickness(), 0.0001f);
        assertEquals("dashed", node.outlineStyle());
        assertEquals(45.0f, node.rotation(), 0.0001f);
        assertTrue(node.doubleSided());
    }

    @Test
    void testGeometricShapesDemoPageBuildAndClick() {
        vn.haohan.displayui.demo.pages.GeometricShapesDemoPage page = new vn.haohan.displayui.demo.pages.GeometricShapesDemoPage();
        vn.haohan.displayui.demo.DemoContext context = new vn.haohan.displayui.demo.DemoContext(java.util.UUID.randomUUID());

        UiDocument.Builder builder = UiDocument.builder();
        page.build(builder, context);
        UiDocument doc = builder.build();

        assertNotNull(doc);
        assertTrue(doc.nodes().size() >= 24, "Should build at least 24 shape nodes + labels");

        // Test outline toggle
        boolean prevOutline = context.shapeOutline();
        assertTrue(page.onClick(context, "shape_outline_toggle", null));
        assertEquals(!prevOutline, context.shapeOutline());

        // Test style cycle
        context.lineStyle("solid");
        assertTrue(page.onClick(context, "shape_style_cycle", null));
        assertEquals("dashed", context.lineStyle());
        assertTrue(page.onClick(context, "shape_style_cycle", null));
        assertEquals("dotted", context.lineStyle());
        assertTrue(page.onClick(context, "shape_style_cycle", null));
        assertEquals("solid", context.lineStyle());

        // Test thickness cycle
        context.lineThickness(2.0f);
        assertTrue(page.onClick(context, "shape_thick_cycle", null));
        assertEquals(3.0f, context.lineThickness(), 0.01f);

        // Verify all shapes decompose and all background subnodes produce valid TRS without NPE
        for (UiNode node : doc.nodes()) {
            if (node instanceof UiShapeNode shapeNode) {
                List<UiNode> subNodes = shapeNode.decomposeToNodes();
                for (UiNode sub : subNodes) {
                    if (sub instanceof UiBackgroundNode bg) {
                        float pixels = 100.0f;
                        org.joml.Vector3f p1 = new org.joml.Vector3f(bg.x() / pixels, -(bg.y() + bg.height()) / pixels, 0);
                        org.joml.Vector3f p2 = new org.joml.Vector3f((bg.x() + bg.width()) / pixels, -(bg.y() + bg.height()) / pixels, 0);
                        org.joml.Vector3f p3 = new org.joml.Vector3f(bg.x() / pixels, -bg.y() / pixels, 0);
                        vn.haohan.displayui.api.shape.TRSResult trs = vn.haohan.displayui.api.shape.DisplayShapeMath.computeParallelogramTRS(p1, p2, p3);
                        assertNotNull(trs, "TRS should never be null for background node: " + bg.width() + "x" + bg.height());
                        assertNotNull(trs.translation());
                    }
                }
            }
        }
    }

    @Test
    void testDoubleSidedVisibilityForShapes() {
        for (String shapeType : ALL_SHAPES) {
            UiShapeNode node = UiShapeNode.builder(shapeType, 10, 10, 50, 40)
                    .color(Color.fromRGB(200, 100, 50))
                    .outline(true)
                    .outlineThickness(2.0f)
                    .doubleSided(true)
                    .build();

            List<UiNode> subNodes = node.decomposeToNodes();
            assertFalse(subNodes.isEmpty(), "Subnodes empty for " + shapeType);

            int totalFront = 0;
            int totalBack = 0;
            for (UiNode sub : subNodes) {
                if (sub instanceof UiBackgroundNode || sub instanceof LineNode || sub instanceof ParallelogramNode) {
                    // 2 displays: index 0 is front, index 1 is back
                    assertFalse(vn.haohan.displayui.runtime.scene.visibility.UiSceneVisibilityPolicy.isBackDisplay(sub, 2, 0));
                    assertTrue(vn.haohan.displayui.runtime.scene.visibility.UiSceneVisibilityPolicy.isBackDisplay(sub, 2, 1));
                    totalFront++;
                    totalBack++;
                } else if (sub instanceof TriangleNode) {
                    assertFalse(vn.haohan.displayui.runtime.scene.visibility.UiSceneVisibilityPolicy.isBackDisplay(sub, 6, 0));
                    assertFalse(vn.haohan.displayui.runtime.scene.visibility.UiSceneVisibilityPolicy.isBackDisplay(sub, 6, 2));
                    assertTrue(vn.haohan.displayui.runtime.scene.visibility.UiSceneVisibilityPolicy.isBackDisplay(sub, 6, 3));
                    assertTrue(vn.haohan.displayui.runtime.scene.visibility.UiSceneVisibilityPolicy.isBackDisplay(sub, 6, 5));
                    totalFront += 3;
                    totalBack += 3;
                } else if (sub instanceof PolylineNode poly) {
                    int pts = poly.points().size();
                    int segs = pts < 2 ? 1 : (pts - 1 + (poly.closed() && pts > 2 ? 1 : 0));
                    for (int k = 0; k < segs * 2; k++) {
                        boolean isBack = vn.haohan.displayui.runtime.scene.visibility.UiSceneVisibilityPolicy.isBackDisplay(sub, segs * 2, k);
                        if ((k & 1) == 1) {
                            assertTrue(isBack, "Odd index should be back display for polyline segment " + k);
                            totalBack++;
                        } else {
                            assertFalse(isBack, "Even index should be front display for polyline segment " + k);
                            totalFront++;
                        }
                    }
                }
            }
            assertTrue(totalFront > 0, "Shape " + shapeType + " should have front displays");
            assertTrue(totalBack > 0, "Shape " + shapeType + " should have back displays");
            assertEquals(totalFront, totalBack, "Shape " + shapeType + " should have equal front and back display counts");
        }
    }

    @Test
    void testAllShapesDecomposedPrimitivesAreStrictlyCoplanar() {
        float expectedDepth = 0.005f;
        for (String shapeType : ALL_SHAPES) {
            UiShapeNode shape = UiShapeNode.builder(shapeType, 20, 30, 80, 50)
                    .depth(expectedDepth)
                    .color(Color.fromRGB(100, 150, 200))
                    .outline(true)
                    .outlineThickness(2.0f)
                    .build();

            List<UiNode> primitives = shape.decomposeToNodes();
            assertFalse(primitives.isEmpty(), "Shape " + shapeType + " decomposition should not be empty");

            for (UiNode prim : primitives) {
                if (prim instanceof PolylineNode poly) {
                    // Outline must be at expectedDepth + 0.0001f
                    assertEquals(expectedDepth + 0.0001f, poly.depth(), 1e-6f,
                            "Outline polyline of " + shapeType + " must be at D + 0.0001f");
                } else if (prim instanceof UiBackgroundNode bg) {
                    // Fill background must be at expectedDepth
                    assertEquals(expectedDepth, bg.depth(), 1e-6f,
                            "Fill background primitive of " + shapeType + " must be at D");
                } else if (prim instanceof TriangleNode tri) {
                    // Fill triangle must be at expectedDepth
                    assertEquals(expectedDepth, tri.depth(), 1e-6f,
                            "Fill triangle primitive of " + shapeType + " must be at D");
                } else if (prim instanceof ParallelogramNode para) {
                    // Fill parallelogram must be at expectedDepth
                    assertEquals(expectedDepth, para.depth(), 1e-6f,
                            "Fill parallelogram primitive of " + shapeType + " must be at D");
                }
            }
        }
    }

    @Test
    void testSlantedParallelogramVerticesValid() {
        UiShapeNode slanted = UiShapeNode.builder("slanted", 10, 20, 60, 30)
                .depth(0.002f)
                .color(Color.AQUA)
                .build();
        List<UiNode> nodes = slanted.decomposeToNodes();
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof ParallelogramNode);
        ParallelogramNode p = (ParallelogramNode) nodes.get(0);

        // Edge 1: (x1, y1) to (x2, y2)
        // Edge 2: (x1, y1) to (x3, y3)
        // Corner 4 = P1 + (P2 - P1) + (P3 - P1) = P2 + P3 - P1
        float p4x = p.x2() + p.x3() - p.x1();
        float p4y = p.y2() + p.y3() - p.y1();

        // Check that all 4 corners stay within the shape bounding box [10, 70] x [20, 50]
        assertTrue(p.x1() >= 10.0f && p.x1() <= 70.0f, "P1 x out of bounds: " + p.x1());
        assertTrue(p.x2() >= 10.0f && p.x2() <= 70.0f, "P2 x out of bounds: " + p.x2());
        assertTrue(p.x3() >= 10.0f && p.x3() <= 70.0f, "P3 x out of bounds: " + p.x3());
        assertTrue(p4x >= 10.0f && p4x <= 70.0f, "P4 x out of bounds: " + p4x);
        assertEquals(20.0f, p.y1(), 1e-6f);
        assertEquals(20.0f, p.y2(), 1e-6f);
        assertEquals(50.0f, p.y3(), 1e-6f);
        assertEquals(50.0f, p4y, 1e-6f);
    }

    @Test
    void testShapeScaledUniformlyAroundCenter() {
        UiShapeNode arrow = UiShapeNode.builder("arrow_right", 10, 20, 40, 20)
                .depth(0.002f)
                .outline(true)
                .outlineThickness(2.0f)
                .build();

        // Center is (10 + 20, 20 + 10) = (30, 30)
        UiShapeNode scaled = arrow.scaled(2.0f);
        assertEquals(80.0f, scaled.width(), 1e-6f);
        assertEquals(40.0f, scaled.height(), 1e-6f);
        // New X should be 30 - 40 = -10, new Y should be 30 - 20 = 10
        assertEquals(-10.0f, scaled.x(), 1e-6f);
        assertEquals(10.0f, scaled.y(), 1e-6f);
        assertEquals(4.0f, scaled.outlineThickness(), 1e-6f);

        // Center must remain identical
        assertEquals(30.0f, scaled.x() + scaled.width() * 0.5f, 1e-6f);
        assertEquals(30.0f, scaled.y() + scaled.height() * 0.5f, 1e-6f);
    }

    @Test
    void testCompositeShapesDecompositionWithinBounds() {
        String[] compositeTypes = {"arrow_right", "arrow_left", "cross", "trapezoid", "double_arrow"};
        for (String type : compositeTypes) {
            UiShapeNode node = UiShapeNode.builder(type, 10, 20, 50, 30)
                    .outline(true)
                    .outlineThickness(1.0f)
                    .build();

            List<UiNode> primitives = node.decomposeToNodes();
            assertFalse(primitives.isEmpty(), "Decomposition empty for " + type);

            for (UiNode sub : primitives) {
                if (sub instanceof UiBackgroundNode bg) {
                    assertTrue(bg.x() >= 10.0f - 1e-4f, type + " bg x too small: " + bg.x());
                    assertTrue(bg.x() + bg.width() <= 60.0f + 1e-4f, type + " bg x+w too large: " + (bg.x() + bg.width()));
                    assertTrue(bg.y() >= 20.0f - 1e-4f, type + " bg y too small: " + bg.y());
                    assertTrue(bg.y() + bg.height() <= 50.0f + 1e-4f, type + " bg y+h too large: " + (bg.y() + bg.height()));
                } else if (sub instanceof TriangleNode tri) {
                    for (float vx : new float[]{tri.x1(), tri.x2(), tri.x3()}) {
                        assertTrue(vx >= 10.0f - 1e-4f && vx <= 60.0f + 1e-4f, type + " tri vx out of bounds: " + vx);
                    }
                    for (float vy : new float[]{tri.y1(), tri.y2(), tri.y3()}) {
                        assertTrue(vy >= 20.0f - 1e-4f && vy <= 50.0f + 1e-4f, type + " tri vy out of bounds: " + vy);
                    }
                }
            }
        }
    }
}
