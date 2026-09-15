/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.interaction;

import vn.haohan.displayui.api.UiHandle;
import org.bukkit.entity.Player;

/**
 * Event context record detailing a value modification on an interactive UI control (slider, checkbox, etc.).
 *
 * @param handle   controlling {@link UiHandle} of the UI instance
 * @param control  {@link UiControl} whose value changed
 * @param player   interacting player who triggered the change
 * @param oldValue previous control value prior to change
 * @param value    new control value after change
 * @param localX   local canvas X-coordinate of interaction point in UI pixels
 * @param localY   local canvas Y-coordinate of interaction point in UI pixels
 * @param distance raycast distance from player eye location to UI canvas in blocks
 */
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
    /**
     * Convenience helper evaluating whether the control is in a checked/enabled state.
     *
     * @return {@code true} if {@code value >= 0.5}; {@code false} otherwise
     */
    public boolean checked() { return value >= 0.5; }
}

