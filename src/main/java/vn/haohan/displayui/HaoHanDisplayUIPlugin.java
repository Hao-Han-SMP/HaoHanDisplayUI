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

import java.util.logging.Level;

public final class HaoHanDisplayUIPlugin extends JavaPlugin {
    private DisplayUiServiceImpl service;

    @Override
    public void onEnable() {
        service = new DisplayUiServiceImpl(this);
        Bukkit.getServicesManager().register(
                DisplayUiService.class, service, this, ServicePriority.Normal);

        DisplayUiCommand command = new DisplayUiCommand(this, service);
        if (getCommand("hhdui") != null) {
            getCommand("hhdui").setExecutor(command);
            getCommand("hhdui").setTabCompleter(command);
        }
        Bukkit.getPluginManager().registerEvents(new UiInteractionListener(service), this);

        if (isFolia()) {
            getLogger().info("Folia detected — using GlobalRegionScheduler.");
            Bukkit.getGlobalRegionScheduler().run(this, task -> removeOrphanedDisplays());
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> service.tick(), 1L, 1L);
        } else {
            Bukkit.getScheduler().runTask(this, this::removeOrphanedDisplays);
            Bukkit.getScheduler().runTaskTimer(this, service::tick, 1L, 1L);
        }
        getLogger().info("HaoHan Display UI engine is ready. API service: "
                + DisplayUiService.class.getName());
    }

    @Override
    public void onDisable() {
        if (service != null) service.shutdown();
        Bukkit.getServicesManager().unregisterAll(this);
    }

    public DisplayUiServiceImpl service() {
        return service;
    }

    /**
     * Detects whether the server is running Folia by checking for the
     * {@code io.papermc.paper.threadedregions.RegionizedServer} class,
     * which is exclusive to Folia builds.
     */
    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void removeOrphanedDisplays() {
        NamespacedKey sceneKey = new NamespacedKey(this, "scene_id");
        int removed = 0;
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntitiesByClass(Display.class)) {
                if (entity.getPersistentDataContainer().has(sceneKey, PersistentDataType.STRING)) {
                    entity.remove();
                    removed++;
                }
            }
        }
        if (removed > 0) {
            getLogger().info("Cleaned up " + removed + " orphaned Display UI entities from previous server run.");
        }
    }
}
