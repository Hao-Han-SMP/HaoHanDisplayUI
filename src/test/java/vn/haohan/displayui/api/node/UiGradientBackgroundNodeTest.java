/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.node;

import org.bukkit.Color;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.api.gradient.UiGradientPosition;
import vn.haohan.displayui.api.interaction.UiButton;

import static org.junit.jupiter.api.Assertions.*;

class UiGradientBackgroundNodeTest {

    @Test
    void testDefaultSlicesSmartSelection() {
        Color c1 = Color.fromRGB(255, 0, 0);
        Color c2 = Color.fromRGB(0, 0, 255);

        // Horizontal gradient: default 16x1
        UiGradientBackgroundNode hNode = new UiGradientBackgroundNode(
                0, 0, 0, 100, 50,
                UiGradient.horizontal(c1, c2)
        );
        assertEquals(16, hNode.slicesX());
        assertEquals(1, hNode.slicesY());

        // Vertical gradient: default 1x16
        UiGradientBackgroundNode vNode = new UiGradientBackgroundNode(
                0, 0, 0, 100, 50,
                UiGradient.vertical(c1, c2)
        );
        assertEquals(1, vNode.slicesX());
        assertEquals(16, vNode.slicesY());

        // Diagonal gradient: default 8x8
        UiGradientBackgroundNode dNode = new UiGradientBackgroundNode(
                0, 0, 0, 100, 50,
                UiGradient.diagonal(c1, c2)
        );
        assertEquals(8, dNode.slicesX());
        assertEquals(8, dNode.slicesY());
    }

    @Test
    void testWithGridAndWithSlices() {
        Color c1 = Color.fromRGB(10, 10, 10);
        Color c2 = Color.fromRGB(20, 20, 20);
        UiGradient hGrad = UiGradient.horizontal(c1, c2);

        UiGradientBackgroundNode node = new UiGradientBackgroundNode(10, 20, 0, 80, 40, hGrad)
                .withGrid(12, 3);
        assertEquals(12, node.slicesX());
        assertEquals(3, node.slicesY());

        UiGradientBackgroundNode slicedH = node.withSlices(20);
        assertEquals(20, slicedH.slicesX());
        assertEquals(1, slicedH.slicesY());

        UiGradient diagGrad = UiGradient.diagonal(c1, c2);
        UiGradientBackgroundNode diagNode = new UiGradientBackgroundNode(0, 0, 0, 50, 50, diagGrad)
                .withSlices(10);
        assertEquals(10, diagNode.slicesX());
        assertEquals(10, diagNode.slicesY());
    }

    @Test
    void testColorForDisplayIndex() {
        Color left = Color.fromRGB(255, 0, 0);
        Color right = Color.fromRGB(0, 255, 0);
        UiGradientBackgroundNode node = new UiGradientBackgroundNode(
                0, 0, 0, 100, 50,
                UiGradientPosition.CENTER_LEFT, left,
                UiGradientPosition.CENTER_RIGHT, right
        ).withGrid(2, 1);

        // 2 slices horizontally:
        // slice 0 center: u = 0.25 (75% left, 25% right)
        // slice 1 center: u = 0.75 (25% left, 75% right)
        Color col0 = node.colorForDisplayIndex(0);
        Color col1 = node.colorForDisplayIndex(1);

        assertEquals(191, col0.getRed());
        assertEquals(64, col0.getGreen());

        assertEquals(64, col1.getRed());
        assertEquals(191, col1.getGreen());

        // Back-face wrap around: index 2 should equal index 0, index 3 should equal index 1
        assertEquals(col0, node.colorForDisplayIndex(2));
        assertEquals(col1, node.colorForDisplayIndex(3));
    }

    @Test
    void testButtonHitboxIntegration() {
        UiGradientBackgroundNode node = new UiGradientBackgroundNode(
                15.0f, 25.0f, 0.5f, 120.0f, 60.0f,
                UiGradient.horizontal(Color.WHITE, Color.BLACK)
        );

        UiButton button = UiButton.forNode("bg_btn", node);
        assertEquals(15.0f, button.x());
        assertEquals(25.0f, button.y());
        assertEquals(120.0f, button.width());
        assertEquals(60.0f, button.height());
    }

    @Test
    void testUiDocumentBuilderIntegration() {
        UiDocument doc = UiDocument.builder()
                .gradientBackground(10, 20, 0.1f, 100, 50,
                        UiGradientPosition.TOP_LEFT, Color.RED,
                        UiGradientPosition.BOTTOM_RIGHT, Color.BLUE)
                .build();

        assertEquals(1, doc.nodes().size());
        assertTrue(doc.nodes().getFirst() instanceof UiGradientBackgroundNode);
        UiGradientBackgroundNode node = (UiGradientBackgroundNode) doc.nodes().getFirst();
        assertEquals(10.0f, node.x());
        assertEquals(20.0f, node.y());
        assertEquals(100.0f, node.width());
        assertEquals(50.0f, node.height());
    }

    @Test
    void testValidations() {
        Color c = Color.RED;
        assertThrows(IllegalArgumentException.class, () ->
                new UiGradientBackgroundNode(0, 0, 0, -10, 50, UiGradient.horizontal(c, c)));
        assertThrows(IllegalArgumentException.class, () ->
                new UiGradientBackgroundNode(0, 0, 0, 10, 0, UiGradient.horizontal(c, c)));
        assertThrows(NullPointerException.class, () ->
                new UiGradientBackgroundNode(0, 0, 0, 10, 10, (UiGradient) null));
    }
}
