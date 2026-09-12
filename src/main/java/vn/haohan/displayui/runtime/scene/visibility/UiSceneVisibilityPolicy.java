/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.runtime.scene.visibility;

import vn.haohan.displayui.api.node.PolylineNode;
import vn.haohan.displayui.api.node.TriangleNode;
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;
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
        if (displayCount <= 1) return false;
        if (node instanceof vn.haohan.displayui.api.node.UiShapeNode shape) {
            List<UiNode> subNodes = shape.decomposeToNodes();
            int offset = 0;
            for (UiNode sub : subNodes) {
                int subCount = countSubnodeDisplays(sub);
                if (index < offset + subCount) {
                    return isBackDisplay(sub, subCount, index - offset);
                }
                offset += subCount;
            }
            return false;
        }
        if (node instanceof PolylineNode poly) {
            int pts = poly.points().size();
            int segs = pts < 2 ? 1 : (pts - 1 + (poly.closed() && pts > 2 ? 1 : 0));
            if (displayCount <= segs) return false;
            return (index & 1) == 1;
        }
        if (node instanceof TriangleNode) {
            if (displayCount <= 3) return false;
            return index >= displayCount / 2;
        }
        if (node instanceof UiGradientBackgroundNode grad) {
            int slices = grad.slicesX() * grad.slicesY();
            if (displayCount <= slices) return false;
            return index >= displayCount / 2;
        }
        return index > 0;
    }

    private static int countSubnodeDisplays(UiNode node) {
        if (node instanceof PolylineNode poly) {
            int pts = poly.points().size();
            int segs = pts < 2 ? 1 : (pts - 1 + (poly.closed() && pts > 2 ? 1 : 0));
            return segs * 2;
        }
        if (node instanceof TriangleNode) {
            return 6;
        }
        if (node instanceof UiGradientBackgroundNode grad) {
            return grad.slicesX() * grad.slicesY() * 2;
        }
        if (node instanceof vn.haohan.displayui.api.node.UiShapeNode shape) {
            int total = 0;
            for (UiNode sub : shape.decomposeToNodes()) {
                total += countSubnodeDisplays(sub);
            }
            return total;
        }
        return 2;
    }
}
