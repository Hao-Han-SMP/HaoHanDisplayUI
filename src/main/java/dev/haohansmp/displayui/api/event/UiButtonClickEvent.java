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
package dev.haohansmp.displayui.api.event;

import dev.haohansmp.displayui.api.UiButton;
import dev.haohansmp.displayui.api.UiHandle;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Fired before scene-local callbacks. Cancelling prevents those callbacks. */
public final class UiButtonClickEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final UiHandle handle;
    private final UiButton button;
    private final Player player;
    private final float localX;
    private final float localY;
    private final double distance;
    private boolean cancelled;

    public UiButtonClickEvent(UiHandle handle, UiButton button, Player player,
                              float localX, float localY, double distance) {
        this.handle = handle;
        this.button = button;
        this.player = player;
        this.localX = localX;
        this.localY = localY;
        this.distance = distance;
    }

    public UiHandle getHandle() { return handle; }
    public UiButton getButton() { return button; }
    public Player getPlayer() { return player; }
    public float getLocalX() { return localX; }
    public float getLocalY() { return localY; }
    public double getDistance() { return distance; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static @NotNull HandlerList getHandlerList() { return HANDLERS; }
}
