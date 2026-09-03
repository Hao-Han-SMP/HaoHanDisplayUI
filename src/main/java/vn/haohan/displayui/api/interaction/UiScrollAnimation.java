package vn.haohan.displayui.api.interaction;

import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.animation.UiAnimation;

import java.util.List;

/**
 * Creates the per-node animation frame used when a scroll list changes.
 * Return one animation per document node; an empty list disables scrolling
 * animation for that change. The engine still owns timing and display updates.
 */
@FunctionalInterface
public interface UiScrollAnimation {
    List<UiAnimation> create(UiDocument document, UiScrollList list, int direction);

    static UiScrollAnimation none() {
        return (document, list, direction) -> List.of();
    }
}
