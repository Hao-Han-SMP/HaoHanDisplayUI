/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.interaction;

/**
 * Listener interface receiving value change callbacks from interactive controls (sliders, checkboxes, etc.).
 */
@FunctionalInterface
public interface UiControlChangeHandler {

    /**
     * Invoked when the state or numeric value of a control changes.
     *
     * @param change event object describing old and new values and the interacting player
     */
    void onChange(UiControlChange change);
}

