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
package vn.haohan.displayui.runtime;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.event.server.PluginDisableEvent;

public final class UiInteractionListener implements Listener {
    private final DisplayUiServiceImpl service;

    public UiInteractionListener(DisplayUiServiceImpl service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() == Action.LEFT_CLICK_AIR
                || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (service.handleLeftClick(event.getPlayer())) {
                event.setCancelled(true);
            }
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (service.handleRightClick(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        boolean isUiEntity = event.getEntity().getScoreboardTags().contains("hhdui_interaction")
                || event.getEntity().getScoreboardTags().contains("hhdui_scene");
        if (!isUiEntity) return;
        event.setCancelled(true);
        if (event.getDamager() instanceof Player player) {
            service.handleLeftClick(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        service.stopDragging(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHotbarScroll(PlayerItemHeldEvent event) {
        if (event.getNewSlot() == event.getPreviousSlot()) return;
        int prev = event.getPreviousSlot();
        int next = event.getNewSlot();
        // Shortest directed step on circular 9-slot hotbar (handles 0 <-> 8 wrapping)
        int diff = (next - prev + 9) % 9;
        if (diff > 4) diff -= 9;
        int direction = Integer.compare(diff, 0);
        if (direction != 0 && service.handleScroll(event.getPlayer(), direction)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        boolean isUiEntity = event.getRightClicked().getScoreboardTags().contains("hhdui_interaction")
                || event.getRightClicked().getScoreboardTags().contains("hhdui_scene");
        if (!isUiEntity) return;
        if (service.handleRightClick(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityCombust(EntityCombustEvent event) {
        if (event.getEntity().getScoreboardTags().contains("hhdui_scene")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().getScoreboardTags().contains("hhdui_scene")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity().getScoreboardTags().contains("hhdui_scene")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        service.icons().unregisterAll(event.getPlugin());
    }
}
