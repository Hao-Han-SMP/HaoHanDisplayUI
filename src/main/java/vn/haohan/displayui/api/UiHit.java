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
package vn.haohan.displayui.api;

import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiControl;
import org.bukkit.entity.Player;
import vn.haohan.displayui.runtime.scene.UiScene;

/**
 * Raycast intersection record captured when a player interacts with or clicks a Display UI.
 *
 * @param scene    runtime scene instance {@link UiScene} where the hit occurred
 * @param button   interactive button {@link UiButton} intersected, if any
 * @param control  interactive control {@link UiControl} intersected, if any
 * @param player   interacting {@link Player}
 * @param localX   local canvas X-coordinate at the raycast intersection point in UI pixels
 * @param localY   local canvas Y-coordinate at the raycast intersection point in UI pixels
 * @param distance raycast distance from player eye location to UI canvas plane in blocks
 */
public record UiHit(
        UiScene scene,
        UiButton button,
        UiControl control,
        Player player,
        float localX,
        float localY,
        double distance
) {}

