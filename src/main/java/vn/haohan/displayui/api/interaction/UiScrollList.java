/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.interaction;

import net.kyori.adventure.text.Component;

/**
 * Scroll-wheel hit area for a list rendered inside a UI document.
 *
 * <p>The control stores only the viewport state. Consumers render the visible
 * rows from {@link #offset()} and rebuild the document when the value changes.
 * This keeps list rendering flexible while the engine owns input handling.</p>
 */
public record UiScrollList(
        String id,
        float x,
        float y,
        float width,
        float height,
        int maxOffset,
        int offset,
        int step,
        Component description,
        float hitSlop
) implements UiControl {
    public UiScrollList {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank");
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("scroll area must be positive");
        if (maxOffset < 0) throw new IllegalArgumentException("maxOffset must be non-negative");
        if (step <= 0) throw new IllegalArgumentException("step must be positive");
        if (hitSlop < 0) throw new IllegalArgumentException("hitSlop must be non-negative");
        offset = Math.max(0, Math.min(maxOffset, offset));
        description = description == null ? Component.empty() : description;
    }

    public UiScrollList(String id, float x, float y, float width, float height,
                        int maxOffset, int offset, Component description) {
        this(id, x, y, width, height, maxOffset, offset, 1, description, 0.0f);
    }

    public UiScrollList withOffset(int nextOffset) {
        return new UiScrollList(id, x, y, width, height, maxOffset,
                nextOffset, step, description, hitSlop);
    }

    public UiScrollList step(int nextStep) {
        return new UiScrollList(id, x, y, width, height, maxOffset,
                offset, nextStep, description, hitSlop);
    }

    public UiScrollList hitSlop(float pixels) {
        return new UiScrollList(id, x, y, width, height, maxOffset,
                offset, step, description, pixels);
    }
}
