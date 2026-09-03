/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HaoHanDisplayUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HaoHanDisplayUI. If not, see <https://www.gnu.org/licenses/>.
 */
package vn.haohan.displayui.runtime;

import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiCheckbox;
import vn.haohan.displayui.api.interaction.UiControl;
import vn.haohan.displayui.api.interaction.UiScrollList;
import vn.haohan.displayui.api.interaction.UiSlider;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Keeps interactive control values stable while immutable documents are rebuilt. */
final class UiControlStateStore {
    private final Map<String, UiControl> values = new LinkedHashMap<>();

    void synchronize(UiDocument document) {
        document.controls().forEach(control -> {
            UiControl old = values.get(control.id());
            if (old instanceof UiSlider oldSlider && control instanceof UiSlider slider
                    && sameLayout(oldSlider, slider)) {
                values.put(control.id(), slider.withValue(oldSlider.value()));
            } else if (old instanceof UiCheckbox oldCheckbox && control instanceof UiCheckbox checkbox
                    && sameLayout(oldCheckbox, checkbox)) {
                values.put(control.id(), checkbox.checked(oldCheckbox.checked()));
            } else {
                values.put(control.id(), control);
            }
        });
    }

    UiControl get(String id) { return values.get(id); }
    Collection<UiControl> values() { return values.values(); }
    Map<String, UiControl> snapshot() { return new LinkedHashMap<>(values); }
    boolean isEmpty() { return values.isEmpty(); }
    void clear() { values.clear(); }

    Change change(UiControl control, double nextValue) {
        if (control == null) return null;
        double oldValue = valueOf(control);
        UiControl next = withValue(control, nextValue);
        if (control.equals(next)) return null;
        values.put(control.id(), next);
        return new Change(next, oldValue, nextValue);
    }

    private static double valueOf(UiControl control) {
        return switch (control) {
            case UiButton ignored -> 0.0;
            case UiCheckbox checkbox -> checkbox.checked() ? 1.0 : 0.0;
            case UiSlider slider -> slider.value();
            case UiScrollList scrollList -> scrollList.offset();
        };
    }

    private static UiControl withValue(UiControl control, double value) {
        return switch (control) {
            case UiButton button -> button;
            case UiCheckbox checkbox -> checkbox.checked(value > 0.5);
            case UiSlider slider -> slider.withValue(value);
            case UiScrollList scrollList -> scrollList.withOffset((int) Math.round(value));
        };
    }

    private static boolean sameLayout(UiControl first, UiControl second) {
        return Math.abs(first.x() - second.x()) < 0.01f
                && Math.abs(first.y() - second.y()) < 0.01f
                && Math.abs(first.width() - second.width()) < 0.01f
                && Math.abs(first.height() - second.height()) < 0.01f;
    }

    record Change(UiControl control, double oldValue, double newValue) {}
}
