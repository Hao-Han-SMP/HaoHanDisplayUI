package vn.haohan.displayui.api.interaction;

import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.animation.Easings;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiBackgroundNode;
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

/** Reusable scroll animation presets. The engine does not enable one by default. */
public final class UiScrollAnimations {
    private UiScrollAnimations() {}

    /** The former demo animation: slide rows, scale/fade entering rows, and stagger them. */
    public static UiScrollAnimation slide() {
        return UiScrollAnimations::createSlide;
    }

    private static List<UiAnimation> createSlide(UiDocument document, UiScrollList list, int direction) {
        if (direction == 0 || document.nodes().isEmpty()) return List.of();
        float rowHeight = list.height() / 4.0f;
        UiAnimation.Direction movement = direction > 0
                ? UiAnimation.Direction.BOTTOM : UiAnimation.Direction.TOP;
        List<UiAnimation> result = new ArrayList<>(document.nodes().size());
        for (UiNode node : document.nodes()) {
            boolean inside = isInViewport(node, list);
            if (inside) {
                int row = MathUtils.clamp((int) Math.floor((node.y() - list.y()) / rowHeight), 0, 3);
                boolean entering = direction > 0 ? row == 3 : row == 0;
                result.add(UiAnimation.builder().durationTicks(11).delayTicks(direction > 0 ? row : 3 - row)
                        .easing(Easings.OutCubic).offset(movement, 18.0f)
                        .scale(entering ? 0.25f : 1.0f, 1.0f)
                        .opacity(entering ? 0.0f : 1.0f, 1.0f).build());
            } else {
                result.add(UiAnimation.builder().durationTicks(11).easing(Easings.OutCubic).build());
            }
        }
        return List.copyOf(result);
    }

    private static boolean isInViewport(UiNode node, UiScrollList list) {
        if (node instanceof UiBackgroundNode || node instanceof UiGradientBackgroundNode) return false;
        if (node instanceof BlockNode block && (block.width() > list.width() - 5.0f
                || block.x() >= list.x() + list.width() - 15.0f)) return false;
        if (node instanceof AlignedTextNode text && (text.width() > list.width() - 10.0f
                || text.boxX() >= list.x() + list.width() - 15.0f)) return false;
        float right = list.x() + list.width() - 15.0f;
        return node.x() >= list.x() - 2.0f && node.x() < right
                && node.y() >= list.y() - 0.5f
                && node.y() < list.y() + list.height() - 0.5f;
    }
}
