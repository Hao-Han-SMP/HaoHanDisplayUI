/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.gradient;

import org.bukkit.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UiGradientTest {

    @Test
    void testGradientPositionsCoordinates() {
        assertEquals(0.0f, UiGradientPosition.TOP_LEFT.u(), 1e-6f);
        assertEquals(0.0f, UiGradientPosition.TOP_LEFT.v(), 1e-6f);

        assertEquals(0.0f, UiGradientPosition.CENTER_LEFT.u(), 1e-6f);
        assertEquals(0.5f, UiGradientPosition.CENTER_LEFT.v(), 1e-6f);

        assertEquals(0.0f, UiGradientPosition.BOTTOM_LEFT.u(), 1e-6f);
        assertEquals(1.0f, UiGradientPosition.BOTTOM_LEFT.v(), 1e-6f);

        assertEquals(1.0f, UiGradientPosition.TOP_RIGHT.u(), 1e-6f);
        assertEquals(0.0f, UiGradientPosition.TOP_RIGHT.v(), 1e-6f);

        assertEquals(1.0f, UiGradientPosition.CENTER_RIGHT.u(), 1e-6f);
        assertEquals(0.5f, UiGradientPosition.CENTER_RIGHT.v(), 1e-6f);

        assertEquals(1.0f, UiGradientPosition.BOTTOM_RIGHT.u(), 1e-6f);
        assertEquals(1.0f, UiGradientPosition.BOTTOM_RIGHT.v(), 1e-6f);

        assertEquals(0.5f, UiGradientPosition.CENTER_TOP.u(), 1e-6f);
        assertEquals(0.0f, UiGradientPosition.CENTER_TOP.v(), 1e-6f);

        assertEquals(0.5f, UiGradientPosition.CENTER_BOTTOM.u(), 1e-6f);
        assertEquals(1.0f, UiGradientPosition.CENTER_BOTTOM.v(), 1e-6f);

        assertEquals(0.5f, UiGradientPosition.CENTER.u(), 1e-6f);
        assertEquals(0.5f, UiGradientPosition.CENTER.v(), 1e-6f);
    }

    @Test
    void testEndpointCreation() {
        Color red = Color.fromRGB(255, 0, 0);
        UiGradientEndpoint ep1 = UiGradientPosition.TOP_LEFT.withColor(red);
        assertEquals(0.0f, ep1.u());
        assertEquals(0.0f, ep1.v());
        assertEquals(red, ep1.color());

        UiGradientEndpoint ep2 = UiGradientEndpoint.of(0.2f, 0.8f, red);
        assertEquals(0.2f, ep2.u(), 1e-6f);
        assertEquals(0.8f, ep2.v(), 1e-6f);
        assertEquals(red, ep2.color());

        assertThrows(NullPointerException.class, () -> UiGradientEndpoint.of(UiGradientPosition.CENTER, null));
    }

    @Test
    void testHorizontalGradientEvaluation() {
        Color c1 = Color.fromRGB(255, 0, 0);
        Color c2 = Color.fromRGB(0, 0, 255);

        UiGradient gradient = UiGradient.horizontal(c1, c2);
        assertTrue(gradient.isHorizontal());
        assertFalse(gradient.isVertical());

        // Left edge: should be exact c1
        assertEquals(c1, gradient.evaluate(0.0f, 0.0f));
        assertEquals(c1, gradient.evaluate(0.0f, 0.5f));
        assertEquals(c1, gradient.evaluate(0.0f, 1.0f));

        // Right edge: should be exact c2
        assertEquals(c2, gradient.evaluate(1.0f, 0.0f));
        assertEquals(c2, gradient.evaluate(1.0f, 0.5f));
        assertEquals(c2, gradient.evaluate(1.0f, 1.0f));

        // Center column: midpoint color regardless of row
        Color expectedMid = Color.fromRGB(128, 0, 128);
        Color midTop = gradient.evaluate(0.5f, 0.0f);
        Color midCenter = gradient.evaluate(0.5f, 0.5f);
        Color midBottom = gradient.evaluate(0.5f, 1.0f);

        assertEquals(expectedMid, midTop);
        assertEquals(expectedMid, midCenter);
        assertEquals(expectedMid, midBottom);
    }

    @Test
    void testVerticalGradientEvaluation() {
        Color c1 = Color.fromRGB(200, 200, 0);
        Color c2 = Color.fromRGB(0, 200, 200);

        UiGradient gradient = UiGradient.vertical(c1, c2);
        assertFalse(gradient.isHorizontal());
        assertTrue(gradient.isVertical());

        // Top edge: should be exact c1
        assertEquals(c1, gradient.evaluate(0.0f, 0.0f));
        assertEquals(c1, gradient.evaluate(0.5f, 0.0f));
        assertEquals(c1, gradient.evaluate(1.0f, 0.0f));

        // Bottom edge: should be exact c2
        assertEquals(c2, gradient.evaluate(0.0f, 1.0f));
        assertEquals(c2, gradient.evaluate(0.5f, 1.0f));
        assertEquals(c2, gradient.evaluate(1.0f, 1.0f));

        // Center row: midpoint color regardless of column
        Color expectedMid = Color.fromRGB(100, 200, 100);
        assertEquals(expectedMid, gradient.evaluate(0.0f, 0.5f));
        assertEquals(expectedMid, gradient.evaluate(0.5f, 0.5f));
        assertEquals(expectedMid, gradient.evaluate(1.0f, 0.5f));
    }

    @Test
    void testDiagonalGradientEvaluation() {
        Color white = Color.fromRGB(255, 255, 255);
        Color black = Color.fromRGB(0, 0, 0);

        // TOP_LEFT to BOTTOM_RIGHT
        UiGradient gradient = UiGradient.diagonal(white, black);
        assertFalse(gradient.isHorizontal());
        assertFalse(gradient.isVertical());

        assertEquals(white, gradient.evaluate(0.0f, 0.0f));
        assertEquals(black, gradient.evaluate(1.0f, 1.0f));

        // Midpoint along diagonal
        Color mid = gradient.evaluate(0.5f, 0.5f);
        assertEquals(Color.fromRGB(128, 128, 128), mid);

        // Opposite corners (1, 0) and (0, 1) project to t = 0.5
        assertEquals(mid, gradient.evaluate(1.0f, 0.0f));
        assertEquals(mid, gradient.evaluate(0.0f, 1.0f));
    }

    @Test
    void testDiagonalBottomLeftToTopRight() {
        Color green = Color.fromRGB(0, 255, 0);
        Color red = Color.fromRGB(255, 0, 0);

        UiGradient gradient = UiGradient.diagonalBottomLeftToTopRight(green, red);
        assertEquals(green, gradient.evaluate(0.0f, 1.0f));
        assertEquals(red, gradient.evaluate(1.0f, 0.0f));
        assertEquals(Color.fromRGB(128, 128, 0), gradient.evaluate(0.5f, 0.5f));
    }

    @Test
    void testDegenerateGradient() {
        Color c = Color.fromRGB(10, 20, 30);
        UiGradient gradient = UiGradient.of(UiGradientPosition.CENTER, c, UiGradientPosition.CENTER, c);
        assertEquals(c, gradient.evaluate(0.0f, 0.0f));
        assertEquals(c, gradient.evaluate(0.7f, 0.3f));
    }
}
