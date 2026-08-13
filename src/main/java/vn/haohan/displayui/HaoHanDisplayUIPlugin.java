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
package vn.haohan.displayui;

import vn.haohan.displayui.api.DisplayUiService;
import vn.haohan.displayui.runtime.DisplayUiServiceImpl;
import vn.haohan.displayui.runtime.UiInteractionListener;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class HaoHanDisplayUIPlugin extends JavaPlugin {
    private DisplayUiServiceImpl service;

    @Override
    public void onEnable() {
        service = new DisplayUiServiceImpl(this);
        Bukkit.getServicesManager().register(
                DisplayUiService.class, service, this, ServicePriority.Normal);

        DisplayUiCommand command = new DisplayUiCommand(this, service);
        if (getCommand("hhdui") != null) getCommand("hhdui").setExecutor(command);
        Bukkit.getPluginManager().registerEvents(new UiInteractionListener(service), this);

        Bukkit.getScheduler().runTask(this, this::removeOrphanedDisplays);
        // Animation frames are advanced every tick; Display interpolation
        // smooths the metadata updates on the client.
        Bukkit.getScheduler().runTaskTimer(this, service::tick, 1L, 1L);
        getLogger().info("HaoHan Display UI engine is ready. API service: "
                + DisplayUiService.class.getName());
    }

    @Override
    public void onDisable() {
        if (service != null) service.shutdown();
        Bukkit.getServicesManager().unregisterAll(this);
    }

    private void removeOrphanedDisplays() {
        NamespacedKey sceneKey = new NamespacedKey(this, "scene_id");
        int removed = 0;
        for (var world : Bukkit.getWorlds()) {
            for (var entity : world.getEntities()) {
                if (entity.getPersistentDataContainer().has(sceneKey, PersistentDataType.STRING)) {
                    entity.remove();
                    removed++;
                }
            }
        }
        if (removed > 0) getLogger().info("Removed " + removed + " orphaned UI displays");
    }
}
