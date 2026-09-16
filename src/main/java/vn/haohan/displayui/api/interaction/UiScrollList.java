/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.interaction;

import net.kyori.adventure.text.Component;
import vn.haohan.displayui.utils.MathUtils;

/**
 * Scrollable list viewport control supporting mouse-wheel or interaction offset sliding.
 * <p>
 * Manages scroll offset index and visible container bounds.
 *
 * @param id          unique control identifier
 * @param x           top-left X coordinate in UI pixels
 * @param y           top-left Y coordinate in UI pixels
 * @param width       viewport width in UI pixels
 * @param height      viewport height in UI pixels
 * @param maxOffset   maximum permissible scroll offset (>= 0)
 * @param offset      current scroll offset
 * @param step        scroll step increment (>= 1)
 * @param description tooltip or label component
 * @param hitSlop     expanded raycast hit margin in UI pixels
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
        offset = MathUtils.clamp(offset, 0, maxOffset);
        description = description == null ? Component.empty() : description;
    }

    /**
     * Constructs a scroll list with a default step increment of 1.
     *
     * @param id          unique control identifier
     * @param x           top-left X in UI pixels
     * @param y           top-left Y in UI pixels
     * @param width       viewport width in UI pixels
     * @param height      viewport height in UI pixels
     * @param maxOffset   maximum scroll offset
     * @param offset      initial scroll offset
     * @param description tooltip or label component
     */
    public UiScrollList(String id, float x, float y, float width, float height,
                        int maxOffset, int offset, Component description) {
        this(id, x, y, width, height, maxOffset, offset, 1, description, 0.0f);
    }

    /**
     * Creates a copy of this scroll list with an updated offset (clamped to [0, maxOffset]).
     *
     * @param nextOffset new scroll offset
     * @return a new {@link UiScrollList} instance
     */
    public UiScrollList withOffset(int nextOffset) {
        return new UiScrollList(id, x, y, width, height, maxOffset,
                nextOffset, step, description, hitSlop);
    }

    /**
     * Creates a copy of this scroll list with a modified step increment.
     *
     * @param nextStep new step increment (> 0)
     * @return a new {@link UiScrollList} instance
     */
    public UiScrollList step(int nextStep) {
        return new UiScrollList(id, x, y, width, height, maxOffset,
                offset, nextStep, description, hitSlop);
    }

    /**
     * Creates a copy of this scroll list with modified raycast hit slop margin.
     *
     * @param pixels hit margin expansion in UI pixels
     * @return a new {@link UiScrollList} instance
     */
    public UiScrollList hitSlop(float pixels) {
        return new UiScrollList(id, x, y, width, height, maxOffset,
                offset, step, description, pixels);
    }
}

