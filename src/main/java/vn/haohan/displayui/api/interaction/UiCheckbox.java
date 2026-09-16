/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package vn.haohan.displayui.api.interaction;

import vn.haohan.displayui.api.layout.UiRect;
import net.kyori.adventure.text.Component;

import java.util.Objects;

/**
 * Interactive toggle checkbox control with boolean state.
 *
 * @param id          unique control identifier ([a-z0-9_.-]+)
 * @param x           top-left X coordinate in UI pixels
 * @param y           top-left Y coordinate in UI pixels
 * @param width       width of interactive hit area in UI pixels
 * @param height      height of interactive hit area in UI pixels
 * @param checked     current toggle state: {@code true} if checked, {@code false} if unchecked
 * @param description tooltip or label component
 * @param hitSlop     expanded raycast hit margin in UI pixels
 */
public record UiCheckbox(
        String id,
        float x,
        float y,
        float width,
        float height,
        boolean checked,
        Component description,
        float hitSlop
) implements UiControl {
    public UiCheckbox {
        Objects.requireNonNull(id, "id");
        if (!id.matches("[a-z0-9_.-]+")) {
            throw new IllegalArgumentException("control id must contain only [a-z0-9_.-]");
        }
        Objects.requireNonNull(description, "description");
        if (!Float.isFinite(width) || !Float.isFinite(height)
                || width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("control dimensions must be positive and finite");
        }
        if (!Float.isFinite(hitSlop) || hitSlop < 0.0f) {
            throw new IllegalArgumentException("checkbox hitSlop must be finite and non-negative");
        }
    }

    /**
     * Constructs a basic checkbox with toggle state.
     *
     * @param id      unique control identifier
     * @param x       top-left X in UI pixels
     * @param y       top-left Y in UI pixels
     * @param width   width in UI pixels
     * @param height  height in UI pixels
     * @param checked initial toggle state
     */
    public UiCheckbox(String id, float x, float y, float width, float height,
                      boolean checked) {
        this(id, x, y, width, height, checked, Component.empty(), 0.0f);
    }

    /**
     * Constructs a checkbox with a description component.
     *
     * @param id          unique control identifier
     * @param x           top-left X in UI pixels
     * @param y           top-left Y in UI pixels
     * @param width       width in UI pixels
     * @param height      height in UI pixels
     * @param checked     initial toggle state
     * @param description tooltip or label component
     */
    public UiCheckbox(String id, float x, float y, float width, float height,
                      boolean checked, Component description) {
        this(id, x, y, width, height, checked,
                Objects.requireNonNull(description, "description"), 0.0f);
    }

    /**
     * Creates a copy of this checkbox with a modified description component.
     *
     * @param nextDescription new description component
     * @return a new {@link UiCheckbox} instance
     */
    public UiCheckbox describedBy(Component nextDescription) {
        return new UiCheckbox(id, x, y, width, height, checked,
                Objects.requireNonNull(nextDescription, "description"), hitSlop);
    }

    /**
     * Creates a copy of this checkbox with modified raycast hit slop margin.
     *
     * @param pixels hit margin expansion in UI pixels
     * @return a new {@link UiCheckbox} instance
     */
    public UiCheckbox hitSlop(float pixels) {
        return new UiCheckbox(id, x, y, width, height, checked, description, pixels);
    }

    /**
     * Creates a copy of this checkbox with an updated toggle state.
     *
     * @param nextChecked new checked state
     * @return a new {@link UiCheckbox} instance
     */
    public UiCheckbox checked(boolean nextChecked) {
        return new UiCheckbox(id, x, y, width, height, nextChecked,
                description, hitSlop);
    }

    /**
     * Returns the bounding layout rectangle {@link UiRect} for the checkbox indicator icon.
     *
     * @return bounding {@link UiRect}
     */
    public UiRect indicatorRect() {
        return new UiRect(x, y, width, height);
    }
}

