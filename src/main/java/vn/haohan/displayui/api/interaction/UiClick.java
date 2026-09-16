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
package vn.haohan.displayui.api.interaction;

import vn.haohan.displayui.api.UiHandle;
import org.bukkit.entity.Player;

/**
 * Event context record passed to local UI click listeners.
 *
 * @param handle   controlling {@link UiHandle} of the clicked UI instance
 * @param button   interactive button {@link UiButton} triggered
 * @param player   player who clicked the button
 * @param localX   local canvas X-coordinate at click point in UI pixels
 * @param localY   local canvas Y-coordinate at click point in UI pixels
 * @param distance raycast distance from player eye location to UI canvas in blocks
 */
public record UiClick(
        UiHandle handle,
        UiButton button,
        Player player,
        float localX,
        float localY,
        double distance
) {}

