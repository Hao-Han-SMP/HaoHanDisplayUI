/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.runtime.scene.visibility;

import vn.haohan.displayui.api.node.PolylineNode;
import vn.haohan.displayui.api.node.TriangleNode;
import vn.haohan.displayui.api.node.UiNode;

import java.util.List;

/** Pure visibility rules shared by scene culling and display-side selection. */
public final class UiSceneVisibilityPolicy {
    private UiSceneVisibilityPolicy() {
        throw new AssertionError("utility class");
    }

    public static boolean isTwoSided(boolean sceneDoubleSided, List<UiNode> nodes) {
        return sceneDoubleSided || nodes.stream().anyMatch(UiNode::doubleSided);
    }

    public static boolean isDoubleSided(UiNode node, boolean sceneDoubleSided) {
        return node.doubleSided() || sceneDoubleSided;
    }

    public static boolean isBackDisplay(UiNode node, int displayCount, int index) {
        if (node instanceof PolylineNode) return (index & 1) == 1;
        if (node instanceof TriangleNode) return index >= displayCount / 2;
        return index > 0;
    }
}
