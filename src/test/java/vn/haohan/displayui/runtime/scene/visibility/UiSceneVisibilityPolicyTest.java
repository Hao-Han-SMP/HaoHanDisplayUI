/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.runtime.scene.visibility;

import org.bukkit.Color;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;

import static org.junit.jupiter.api.Assertions.*;

class UiSceneVisibilityPolicyTest {

    @Test
    void testGradientBackgroundBackDisplayIdentification() {
        UiGradientBackgroundNode node = new UiGradientBackgroundNode(
                0, 0, 0, 100, 50,
                UiGradient.horizontal(Color.RED, Color.BLUE)
        );

        int totalCount = 32; // e.g. 16 front + 16 back displays

        // Front displays (0 to 15) should NOT be back displays
        for (int i = 0; i < 16; i++) {
            assertFalse(UiSceneVisibilityPolicy.isBackDisplay(node, totalCount, i),
                    "Index " + i + " should be front display");
        }

        // Back displays (16 to 31) should be back displays
        for (int i = 16; i < totalCount; i++) {
            assertTrue(UiSceneVisibilityPolicy.isBackDisplay(node, totalCount, i),
                    "Index " + i + " should be back display");
        }
    }

    @Test
    void testIsDoubleSidedRules() {
        UiGradientBackgroundNode singleNode = new UiGradientBackgroundNode(
                0, 0, 0, 100, 50,
                UiGradient.horizontal(Color.RED, Color.BLUE)
        ).withDoubleSided(false);

        UiGradientBackgroundNode doubleNode = singleNode.withDoubleSided(true);

        // When scene is single-sided:
        assertFalse(UiSceneVisibilityPolicy.isDoubleSided(singleNode, false));
        assertTrue(UiSceneVisibilityPolicy.isDoubleSided(doubleNode, false));

        // When scene is double-sided:
        assertTrue(UiSceneVisibilityPolicy.isDoubleSided(singleNode, true));
        assertTrue(UiSceneVisibilityPolicy.isDoubleSided(doubleNode, true));
    }
}
