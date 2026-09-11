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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import vn.haohan.displayui.HaoHanDisplayUIPlugin;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.UiOptions;
import vn.haohan.displayui.api.animation.Easings;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.loader.UiDocumentLoader;
import vn.haohan.displayui.api.view.UiAudience;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages auto-loading, caching, and opening of UI layout JSON documents
 * from the {@code plugins/HaoHanDisplayUI/layouts/} folder.
 */
public final class UiLayoutManager implements Listener {
    private final HaoHanDisplayUIPlugin plugin;
    private final File layoutsFolder;
    private final Map<String, UiDocument> loadedLayouts = new ConcurrentHashMap<>();
    private final Map<UUID, UiHandle> playerActiveUis = new ConcurrentHashMap<>();

    public UiLayoutManager(HaoHanDisplayUIPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.layoutsFolder = new File(plugin.getDataFolder(), "layouts");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Initializes the layouts directory and loads all JSON layout files.
     */
    public void reloadAll() {
        loadedLayouts.clear();
        if (!layoutsFolder.exists()) {
            if (layoutsFolder.mkdirs()) {
                createDefaultExampleFile();
            }
        }

        File[] files = layoutsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null || files.length == 0) {
            createDefaultExampleFile();
            files = layoutsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        }

        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                String layoutKey = name.substring(0, name.lastIndexOf('.')).toLowerCase();
                try {
                    UiDocumentLoader.DocumentLoadReport report = UiDocumentLoader.loadWithReport(file);
                    if (report.hasErrors()) {
                        plugin.getLogger().warning("Found " + report.errors().size() + " issue(s) in layout file '" + file.getName() + "':");
                        for (String err : report.errors()) {
                            plugin.getLogger().warning("  [!] " + err);
                        }
                    }
                    if (!report.document().nodes().isEmpty()) {
                        loadedLayouts.put(layoutKey, report.document());
                        plugin.getLogger().info("Successfully loaded UI layout '" + layoutKey + "' with "
                                + report.document().nodes().size() + " node(s), "
                                + report.document().buttons().size() + " button(s).");
                    } else {
                        plugin.getLogger().severe("Failed to load UI layout '" + file.getName() + "': No valid nodes could be parsed.");
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Unexpected exception loading UI layout '" + file.getName() + "': " + e.getMessage());
                }
            }
        }
        plugin.getLogger().info("LayoutManager: " + loadedLayouts.size() + " UI layout(s) ready in 'layouts/'.");
    }

    /**
     * Retrieves a loaded UI document by name (case-insensitive, without .json extension).
     */
    public Optional<UiDocument> getLayout(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(loadedLayouts.get(name.toLowerCase().trim()));
    }

    /**
     * Returns a sorted list of all loaded layout names.
     */
    public List<String> getLayoutNames() {
        List<String> list = new ArrayList<>(loadedLayouts.keySet());
        Collections.sort(list);
        return list;
    }

    /**
     * Opens a layout in front of a player.
     *
     * @param player the player to show the UI to
     * @param layoutName the name of the layout file (without .json)
     * @return true if opened successfully, false if layout not found
     */
    public boolean open(Player player, String layoutName) {
        Objects.requireNonNull(player, "player");
        Optional<UiDocument> docOpt = getLayout(layoutName);
        if (docOpt.isEmpty()) {
            return false;
        }

        close(player);

        UiDocument doc = docOpt.get();

        // Calculate spawn location: 2.5 blocks in front of player eye height, facing player
        Location eyeLoc = player.getEyeLocation();
        Location origin = eyeLoc.clone().add(eyeLoc.getDirection().multiply(2.5));
        origin.setYaw(player.getLocation().getYaw() + 180.0f);
        origin.setPitch(0.0f);

        UiOptions options = new UiOptions(80.0f, 12.0, false, 0.2f, "haohan_layout_" + layoutName, null);
        UiHandle handle = plugin.service().create(
                "haohandisplayui:layout_" + player.getUniqueId(),
                origin,
                doc,
                options,
                candidate -> candidate.getUniqueId().equals(player.getUniqueId())
        );

        handle.animate(UiAnimation.fadeIn(8, Easings.OutCubic));
        playerActiveUis.put(player.getUniqueId(), handle);
        return true;
    }

    /**
     * Closes any currently active layout UI for the specified player.
     */
    public boolean close(Player player) {
        if (player == null) return false;
        UiHandle existing = playerActiveUis.remove(player.getUniqueId());
        if (existing != null && existing.isValid()) {
            existing.remove();
            return true;
        }
        return false;
    }

    /**
     * Closes all active UIs for all players.
     */
    public void closeAll() {
        playerActiveUis.values().forEach(handle -> {
            if (handle != null && handle.isValid()) {
                handle.remove();
            }
        });
        playerActiveUis.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        close(event.getPlayer());
    }

    private void createDefaultExampleFile() {
        File sampleFile = new File(layoutsFolder, "example_menu.json");
        if (sampleFile.exists()) return;

        String sampleJson = """
{
  "name": "example_menu",
  "width": 256,
  "height": 192,
  "nodes": [
    {
      "type": "shape",
      "shapeType": "rounded_rect",
      "id": "bg_panel",
      "x": 28,
      "y": 26,
      "depth": 0.001,
      "width": 200,
      "height": 140,
      "color": "#121826",
      "alpha": 235,
      "cornerRadius": 10,
      "rotation": 0,
      "outline": true,
      "outlineColor": "#4f8ef7",
      "outlineAlpha": 255,
      "outlineThickness": 2.0,
      "outlineStyle": "solid",
      "doubleSided": false
    },
    {
      "type": "text",
      "id": "title_text",
      "text": "<yellow><bold>HaoHan Display UI</bold></yellow>",
      "boxX": 38,
      "boxY": 36,
      "depth": 0.002,
      "width": 180,
      "height": 20,
      "fontSize": 9.0,
      "contentWidth": 170,
      "alignment": "CENTER",
      "verticalAlignment": "MIDDLE",
      "shadow": true,
      "seeThrough": false,
      "doubleSided": false
    },
    {
      "type": "shape",
      "shapeType": "star5",
      "id": "star_icon",
      "x": 113,
      "y": 66,
      "depth": 0.002,
      "width": 30,
      "height": 30,
      "color": "#ffc83b",
      "alpha": 255,
      "rotation": 15,
      "outline": true,
      "outlineColor": "#ffffff",
      "outlineAlpha": 255,
      "outlineThickness": 1.5,
      "outlineStyle": "solid",
      "doubleSided": false
    },
    {
      "type": "item",
      "id": "sword_icon",
      "material": "DIAMOND_SWORD",
      "x": 68,
      "y": 116,
      "depth": 0.003,
      "scale": 1.0,
      "transform": "FIXED",
      "doubleSided": false
    },
    {
      "type": "shape",
      "shapeType": "rounded_rect",
      "id": "btn_help",
      "x": 98,
      "y": 126,
      "depth": 0.002,
      "width": 100,
      "height": 24,
      "color": "#2a4b8d",
      "alpha": 255,
      "cornerRadius": 6,
      "rotation": 0,
      "outline": true,
      "outlineColor": "#6ba3ff",
      "outlineAlpha": 255,
      "outlineThickness": 1.5,
      "outlineStyle": "solid",
      "doubleSided": false,
      "_button": {
        "id": "btn_help_act",
        "nodeId": "btn_help",
        "description": "Click to get help",
        "action": {
          "type": "PLAYER_COMMAND",
          "value": "help"
        }
      }
    },
    {
      "type": "text",
      "id": "btn_help_text",
      "text": "<white>Click Me (/help)</white>",
      "boxX": 98,
      "boxY": 126,
      "depth": 0.003,
      "width": 100,
      "height": 24,
      "fontSize": 7.0,
      "contentWidth": 94,
      "alignment": "CENTER",
      "verticalAlignment": "MIDDLE",
      "shadow": true,
      "seeThrough": false,
      "doubleSided": false
    }
  ]
}
""";
        try (FileWriter writer = new FileWriter(sampleFile, StandardCharsets.UTF_8)) {
            writer.write(sampleJson);
            plugin.getLogger().info("Created default sample UI layout: 'layouts/example_menu.json'");
        } catch (IOException e) {
            plugin.getLogger().warning("Could not create sample UI layout: " + e.getMessage());
        }
    }
}
