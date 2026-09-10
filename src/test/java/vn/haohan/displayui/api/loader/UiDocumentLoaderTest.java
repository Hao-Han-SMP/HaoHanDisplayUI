/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.loader;

import org.bukkit.Color;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.gradient.UiGradientPosition;
import vn.haohan.displayui.api.node.UiBackgroundNode;
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;

import static org.junit.jupiter.api.Assertions.*;

class UiDocumentLoaderTest {

    @Test
    void testParseBackground() {
        String json = """
                {
                  "nodes": [
                    {
                      "type": "background",
                      "x": 10,
                      "y": 20,
                      "depth": 0.005,
                      "width": 120,
                      "height": 80,
                      "color": "#ff0000",
                      "alpha": 180,
                      "doubleSided": true
                    }
                  ]
                }
                """;

        UiDocument doc = UiDocumentLoader.loadFromString(json);
        assertEquals(1, doc.nodes().size());
        assertTrue(doc.nodes().get(0) instanceof UiBackgroundNode);

        UiBackgroundNode bg = (UiBackgroundNode) doc.nodes().get(0);
        assertEquals(10f, bg.x());
        assertEquals(20f, bg.y());
        assertEquals(0.005f, bg.depth());
        assertEquals(120f, bg.width());
        assertEquals(80f, bg.height());
        assertTrue(bg.doubleSided());
        assertEquals(Color.fromARGB(180, 255, 0, 0), bg.background());
    }

    @Test
    void testParseGradientBackgroundWithPreset() {
        String json = """
                {
                  "nodes": [
                    {
                      "type": "gradient_background",
                      "x": -50,
                      "y": -30,
                      "width": 100,
                      "height": 60,
                      "preset": "horizontal",
                      "startColor": "#ff0000",
                      "endColor": "#0000ff",
                      "slices": 20,
                      "doubleSided": true
                    }
                  ]
                }
                """;

        UiDocument doc = UiDocumentLoader.loadFromString(json);
        assertEquals(1, doc.nodes().size());
        assertTrue(doc.nodes().get(0) instanceof UiGradientBackgroundNode);

        UiGradientBackgroundNode g = (UiGradientBackgroundNode) doc.nodes().get(0);
        assertEquals(-50f, g.x());
        assertEquals(-30f, g.y());
        assertEquals(100f, g.width());
        assertEquals(60f, g.height());
        assertTrue(g.doubleSided());
        assertTrue(g.gradient().isHorizontal());
        assertEquals(20, g.slicesX());
        assertEquals(1, g.slicesY());
    }

    @Test
    void testParseGradientBackgroundWithPositions() {
        String json = """
                {
                  "nodes": [
                    {
                      "type": "gradientBackground",
                      "x": 0,
                      "y": 0,
                      "width": 80,
                      "height": 40,
                      "startPos": "TOP_LEFT",
                      "endPos": "BOTTOM_RIGHT",
                      "startColor": "#00ff00",
                      "endColor": "#ffff00",
                      "slicesX": 10,
                      "slicesY": 10
                    }
                  ]
                }
                """;

        UiDocument doc = UiDocumentLoader.loadFromString(json);
        assertEquals(1, doc.nodes().size());
        assertTrue(doc.nodes().get(0) instanceof UiGradientBackgroundNode);

        UiGradientBackgroundNode g = (UiGradientBackgroundNode) doc.nodes().get(0);
        assertEquals(80f, g.width());
        assertEquals(40f, g.height());
        assertEquals(UiGradientPosition.TOP_LEFT.u(), g.gradient().start().u());
        assertEquals(UiGradientPosition.TOP_LEFT.v(), g.gradient().start().v());
        assertEquals(UiGradientPosition.BOTTOM_RIGHT.u(), g.gradient().end().u());
        assertEquals(UiGradientPosition.BOTTOM_RIGHT.v(), g.gradient().end().v());
        assertEquals(10, g.slicesX());
        assertEquals(10, g.slicesY());
        assertFalse(g.doubleSided());
    }
}
