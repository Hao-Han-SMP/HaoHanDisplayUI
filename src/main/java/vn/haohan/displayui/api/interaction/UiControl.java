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

/** A value-changing interactive region in logical-pixel screen space. */
public sealed interface UiControl permits UiButton, UiSlider, UiCheckbox, UiScrollList {
    String id();
    float x();
    float y();
    float width();
    float height();
    Component description();
    float hitSlop();

    default boolean contains(float localX, float localY) {
        return localX >= x() - hitSlop() && localX <= x() + width() + hitSlop()
                && localY >= y() - hitSlop() && localY <= y() + height() + hitSlop();
    }
}
