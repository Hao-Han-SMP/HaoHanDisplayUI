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
package vn.haohan.displayui.api.interaction.event;

import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.interaction.UiButton;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit event fired when a player clicks an interactive button ({@link UiButton}) on a Display UI.
 * <p>
 * Dispatched before internal callback handlers are invoked. If cancelled, internal handlers
 * and associated button actions ({@link vn.haohan.displayui.api.interaction.UiButtonAction}) are suppressed.
 */
public final class UiButtonClickEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final UiHandle handle;
    private final UiButton button;
    private final Player player;
    private final float localX;
    private final float localY;
    private final double distance;
    private boolean cancelled;

    /**
     * Constructs a button click event.
     *
     * @param handle   UI instance handle {@link UiHandle}
     * @param button   clicked button {@link UiButton}
     * @param player   interacting player {@link Player}
     * @param localX   local canvas X-coordinate at click point in UI pixels
     * @param localY   local canvas Y-coordinate at click point in UI pixels
     * @param distance raycast distance from player eye location in blocks
     */
    public UiButtonClickEvent(UiHandle handle, UiButton button, Player player,
                              float localX, float localY, double distance) {
        this.handle = handle;
        this.button = button;
        this.player = player;
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
     * Returns the target interactive button clicked.
     *
     * @return clicked {@link UiButton}
     */
    public UiButton getButton() { return button; }

    /**
     * Returns the player who clicked the button.
     *
     * @return interacting {@link Player}
     */
    public Player getPlayer() { return player; }

    /**
     * Returns the local canvas X-coordinate at the click point in UI pixels.
     *
     * @return local X coordinate
     */
    public float getLocalX() { return localX; }

    /**
     * Returns the local canvas Y-coordinate at the click point in UI pixels.
     *
     * @return local Y coordinate
     */
    public float getLocalY() { return localY; }

    /**
     * Returns the raycast distance from player eye location to click point in blocks.
     *
     * @return distance in blocks
     */
    public double getDistance() { return distance; }

    /**
     * Checks whether this event is cancelled.
     *
     * @return {@code true} if cancelled; {@code false} otherwise
     */
    @Override public boolean isCancelled() { return cancelled; }

    /**
     * Sets the cancellation state of this click event.
     *
     * @param cancelled {@code true} to suppress internal handlers and button actions
     */
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static @NotNull HandlerList getHandlerList() { return HANDLERS; }
}

