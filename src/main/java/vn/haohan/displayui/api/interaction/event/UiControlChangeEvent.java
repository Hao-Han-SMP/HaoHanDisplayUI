/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.interaction.event;

import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.interaction.UiControl;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit event fired when a player alters the value or state of an interactive control (Slider or Checkbox).
 * <p>
 * Dispatched before the new value is officially committed. If cancelled, the new value
 * is rejected and internal change listeners are suppressed.
 */
public final class UiControlChangeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UiHandle handle;
    private final UiControl control;
    private final Player player;
    private final double oldValue;
    private final double value;
    private final float localX;
    private final float localY;
    private final double distance;
    private boolean cancelled;

    /**
     * Constructs a control change event.
     *
     * @param handle   UI instance handle {@link UiHandle}
     * @param control  modified interactive control {@link UiControl}
     * @param player   interacting player {@link Player}
     * @param oldValue previous control value
     * @param value    new control value
     * @param localX   local canvas X-coordinate in UI pixels
     * @param localY   local canvas Y-coordinate in UI pixels
     * @param distance raycast distance from player eye location in blocks
     */
    public UiControlChangeEvent(UiHandle handle, UiControl control, Player player,
                                 double oldValue, double value, float localX,
                                 float localY, double distance) {
        this.handle = handle;
        this.control = control;
        this.player = player;
        this.oldValue = oldValue;
        this.value = value;
        this.localX = localX;
        this.localY = localY;
        this.distance = distance;
    }

    /**
     * Returns the controlling UI instance handle.
     *
     * @return active {@link UiHandle}
     */
    public UiHandle getHandle() { return handle; }

    /**
     * Returns the interactive control being modified.
     *
     * @return modified {@link UiControl}
     */
    public UiControl getControl() { return control; }

    /**
     * Returns the interacting player.
     *
     * @return interacting {@link Player}
     */
    public Player getPlayer() { return player; }

    /**
     * Returns the previous control value prior to this change.
     *
     * @return old value
     */
    public double getOldValue() { return oldValue; }

    /**
     * Returns the new value being assigned to the control.
     *
     * @return new value
     */
    public double getValue() { return value; }

    /**
     * Returns the local canvas X-coordinate at interaction point in UI pixels.
     *
     * @return local X coordinate
     */
    public float getLocalX() { return localX; }

    /**
     * Returns the local canvas Y-coordinate at interaction point in UI pixels.
     *
     * @return local Y coordinate
     */
    public float getLocalY() { return localY; }

    /**
     * Returns the raycast distance from player eye location to interaction point in blocks.
     *
     * @return distance in blocks
     */
    public double getDistance() { return distance; }

    /**
     * Checks the toggled state if this control is a checkbox.
     *
     * @return {@code true} if {@code value >= 0.5}; {@code false} otherwise
     */
    public boolean isChecked() { return value >= 0.5; }

    /**
     * Checks whether this event is cancelled.
     *
     * @return {@code true} if cancelled; {@code false} otherwise
     */
    @Override public boolean isCancelled() { return cancelled; }

    /**
     * Sets the cancellation state of this event.
     *
     * @param cancelled {@code true} to cancel the value modification
     */
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static @NotNull HandlerList getHandlerList() { return HANDLERS; }
}

