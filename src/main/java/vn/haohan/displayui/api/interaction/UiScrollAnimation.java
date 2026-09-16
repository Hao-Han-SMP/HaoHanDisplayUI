package vn.haohan.displayui.api.interaction;

import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.animation.UiAnimation;

import java.util.List;

/**
 * Factory creating node animations when a scrollable list ({@link UiScrollList}) changes scroll position.
 */
@FunctionalInterface
public interface UiScrollAnimation {

    /**
     * Generates a list of node animations applied to document elements during a scroll update.
     *
     * @param document  current UI document
     * @param list      active scroll list control
     * @param direction scroll direction: {@code > 0} for scroll down/forward, {@code < 0} for scroll up/backward, {@code 0} for no change
     * @return list of {@link UiAnimation} objects corresponding to document nodes by index
     */
    List<UiAnimation> create(UiDocument document, UiScrollList list, int direction);

    /**
     * Creates a no-op scroll animation factory (disabling smooth scrolling motion).
     *
     * @return empty {@link UiScrollAnimation}
     */
    static UiScrollAnimation none() {
        return (document, list, direction) -> List.of();
    }
}

