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
package vn.haohan.displayui.api.view;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Audience visibility filter controlling which players can perceive and interact with a Display UI.
 */
@FunctionalInterface
public interface UiAudience {

    /**
     * Evaluates whether a player is permitted to view this UI instance.
     *
     * @param player player to test for visibility
     * @return {@code true} if visible; {@code false} if hidden
     */
    boolean canView(Player player);

    /**
     * Creates an audience permitting all players within view range to see the UI.
     *
     * @return public {@link UiAudience}
     */
    static UiAudience all() {
        return player -> true;
    }

    /**
     * Creates an audience restricted exclusively to a set of player UUIDs.
     *
     * @param viewers set of permitted player {@link UUID}s
     * @return restricted {@link UiAudience}
     * @throws NullPointerException if {@code viewers} is {@code null}
     */
    static UiAudience only(Set<UUID> viewers) {
        Set<UUID> immutable = Set.copyOf(viewers);
        return player -> immutable.contains(player.getUniqueId());
    }
}

