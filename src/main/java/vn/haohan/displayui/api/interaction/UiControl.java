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

import net.kyori.adventure.text.Component;

/**
 * Interactive control interface for UI elements (buttons, sliders, checkboxes, scroll lists).
 * <p>
 * Defines layout bounds and hit tolerance margins ({@link #hitSlop()}) to evaluate
 * player raycast intersection.
 */
public sealed interface UiControl permits UiButton, UiSlider, UiCheckbox, UiScrollList {

    /**
     * Returns the unique identifier of this control in the UI document.
     *
     * @return unique control identifier string
     */
    String id();

    /**
     * Returns the top-left X coordinate in UI pixels.
     *
     * @return X coordinate
     */
    float x();

    /**
     * Returns the top-left Y coordinate in UI pixels.
     *
     * @return Y coordinate
     */
    float y();

    /**
     * Returns the width of the control in UI pixels.
     *
     * @return width
     */
    float width();

    /**
     * Returns the height of the control in UI pixels.
     *
     * @return height
     */
    float height();

    /**
     * Returns the tooltip or display description of this control.
     *
     * @return description {@link Component}
     */
    Component description();

    /**
     * Returns the raycast hit margin expansion in UI pixels for easier clicking.
     *
     * @return hit-slop margin in UI pixels
     */
    float hitSlop();

    /**
     * Tests whether a local canvas coordinate falls within this control's bounding box plus hit slop.
     *
     * @param localX local X coordinate in UI pixels
     * @param localY local Y coordinate in UI pixels
     * @return {@code true} if inside interaction area; {@code false} otherwise
     */
    default boolean contains(float localX, float localY) {
        return localX >= x() - hitSlop() && localX <= x() + width() + hitSlop()
                && localY >= y() - hitSlop() && localY <= y() + height() + hitSlop();
    }
}

