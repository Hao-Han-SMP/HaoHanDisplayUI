/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.api.interaction.event;

import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.interaction.UiControl;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Fired before a slider or checkbox value is committed. */
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

    public UiHandle getHandle() { return handle; }
    public UiControl getControl() { return control; }
    public Player getPlayer() { return player; }
    public double getOldValue() { return oldValue; }
    public double getValue() { return value; }
    public float getLocalX() { return localX; }
    public float getLocalY() { return localY; }
    public double getDistance() { return distance; }
    public boolean isChecked() { return value >= 0.5; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static @NotNull HandlerList getHandlerList() { return HANDLERS; }
}
