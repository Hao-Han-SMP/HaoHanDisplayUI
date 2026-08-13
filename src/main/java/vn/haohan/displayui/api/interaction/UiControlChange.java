/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.interaction;

import vn.haohan.displayui.api.UiHandle;
import org.bukkit.entity.Player;

/** Immutable value-change payload for sliders and checkboxes. */
public record UiControlChange(
        UiHandle handle,
        UiControl control,
        Player player,
        double oldValue,
        double value,
        float localX,
        float localY,
        double distance
) {
    public boolean checked() { return value >= 0.5; }
}
