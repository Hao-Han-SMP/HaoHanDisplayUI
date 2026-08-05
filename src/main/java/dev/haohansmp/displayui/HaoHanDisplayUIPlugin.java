package dev.haohansmp.displayui;

import dev.haohansmp.displayui.api.DisplayUiService;
import dev.haohansmp.displayui.runtime.DisplayUiServiceImpl;
import dev.haohansmp.displayui.runtime.UiInteractionListener;
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
        Bukkit.getScheduler().runTaskTimer(this, service::tick, 1L, 5L);
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
