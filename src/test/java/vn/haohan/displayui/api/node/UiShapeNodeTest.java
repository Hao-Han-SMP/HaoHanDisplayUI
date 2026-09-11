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
            "rect", "rounded_rect", "circle", "diamond", "trapezoid", "parallelogram",
            "triangle", "right_triangle", "pentagon", "hexagon", "heptagon", "octagon",
            "star3", "star4", "star5", "star6", "arrow_right", "arrow_left",
            "chevron_right", "double_arrow", "heart", "cross", "speech_bubble"
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
    void testShapeOutlineAlpha255VsAlphaTranslucent() {
        // Alpha = 255: Rect outline creates an expanded background behind
        UiShapeNode rectOpaque = new UiShapeNode(
                "rect", 0, 0, 100, 60, 0.001f,
                Color.fromARGB(255, 20, 30, 40),
                true, Color.fromRGB(255, 255, 0), 4.0f, "solid", 0, false);
        List<UiNode> opaqueNodes = rectOpaque.decomposeToNodes();
        assertEquals(2, opaqueNodes.size());
        assertTrue(opaqueNodes.get(0) instanceof UiBackgroundNode);
        UiBackgroundNode bgOutline = (UiBackgroundNode) opaqueNodes.get(0);
        assertEquals(108.0f, bgOutline.width(), 0.01f);
        assertEquals(68.0f, bgOutline.height(), 0.01f);

        // Alpha < 255: Rect outline creates a closed PolylineNode outline
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
    }
}
