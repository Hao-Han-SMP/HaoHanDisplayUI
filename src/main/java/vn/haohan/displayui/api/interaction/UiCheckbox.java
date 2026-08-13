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

/** Boolean toggle control. */
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

    public UiCheckbox(String id, float x, float y, float width, float height,
                      boolean checked) {
        this(id, x, y, width, height, checked, Component.empty(), 0.0f);
    }

    public UiCheckbox(String id, float x, float y, float width, float height,
                      boolean checked, Component description) {
        this(id, x, y, width, height, checked,
                Objects.requireNonNull(description, "description"), 0.0f);
    }

    public UiCheckbox describedBy(Component nextDescription) {
        return new UiCheckbox(id, x, y, width, height, checked,
                Objects.requireNonNull(nextDescription, "description"), hitSlop);
    }

    public UiCheckbox hitSlop(float pixels) {
        return new UiCheckbox(id, x, y, width, height, checked, description, pixels);
    }

    public UiCheckbox checked(boolean nextChecked) {
        return new UiCheckbox(id, x, y, width, height, nextChecked,
                description, hitSlop);
    }

    /** Bounds for a custom checkbox indicator. */
    public UiRect indicatorRect() {
        return new UiRect(x, y, width, height);
    }
}
