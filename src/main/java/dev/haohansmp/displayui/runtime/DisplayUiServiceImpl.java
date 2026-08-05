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
package dev.haohansmp.displayui.runtime;

import dev.haohansmp.displayui.HaoHanDisplayUIPlugin;
import dev.haohansmp.displayui.api.DisplayUiService;
import dev.haohansmp.displayui.api.UiAudience;
import dev.haohansmp.displayui.api.UiDocument;
import dev.haohansmp.displayui.api.UiHandle;
import dev.haohansmp.displayui.api.UiOptions;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class DisplayUiServiceImpl implements DisplayUiService {
    private final HaoHanDisplayUIPlugin plugin;
    private final Map<UUID, UiScene> scenes = new LinkedHashMap<>();
    private final Map<UUID, HoverTarget> hovered = new LinkedHashMap<>();

    public DisplayUiServiceImpl(HaoHanDisplayUIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public UiHandle create(String ownerKey, Location origin, UiDocument document) {
        return create(ownerKey, origin, document, UiOptions.defaults(), UiAudience.all());
    }

    @Override
    public UiHandle create(String ownerKey, Location origin, UiDocument document,
                           UiOptions options, UiAudience audience) {
        validateOwner(ownerKey);
        Objects.requireNonNull(origin, "origin");
        if (origin.getWorld() == null) throw new IllegalArgumentException("origin must have a world");
        UiScene scene = new UiScene(plugin, UUID.randomUUID(), ownerKey,
                origin.clone(), Objects.requireNonNull(document, "document"),
                Objects.requireNonNull(options, "options"),
                Objects.requireNonNull(audience, "audience"), this::forget);
        scenes.put(scene.id(), scene);
        scene.tick();
        return scene;
    }

    @Override
    public Optional<UiHandle> find(UUID id) {
        return Optional.ofNullable(scenes.get(id));
    }

    @Override
    public Collection<UiHandle> active() {
        return ListView.copyOf(scenes.values());
    }

    @Override
    public int removeOwnedBy(String ownerKey) {
        validateOwner(ownerKey);
        var matches = scenes.values().stream()
                .filter(scene -> scene.ownerKey().equals(ownerKey))
                .toList();
        matches.forEach(UiScene::remove);
        return matches.size();
    }

    public void tick() {
        new ArrayList<>(scenes.values()).forEach(UiScene::tick);
        tickHoverDescriptions();
    }

    public boolean handleRightClick(Player player) {
        UiHit nearest = nearestHit(player);
        if (nearest == null) return false;
        nearest.scene().activate(nearest);
        return true;
    }

    public void shutdown() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (hovered.containsKey(player.getUniqueId())) player.sendActionBar(Component.empty());
        });
        hovered.clear();
        new ArrayList<>(scenes.values()).forEach(UiScene::remove);
        scenes.clear();
    }

    private void forget(UUID id) {
        scenes.remove(id);
    }

    private void validateOwner(String ownerKey) {
        if (ownerKey == null || !ownerKey.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
            throw new IllegalArgumentException(
                    "ownerKey must be namespaced, for example haohanmetallurgy:forge_guide");
        }
    }

    private void tickHoverDescriptions() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UiHit hit = nearestHit(player);
            HoverTarget previous = hovered.get(player.getUniqueId());
            if (hit == null || hit.button().description().equals(Component.empty())) {
                if (previous != null) {
                    player.sendActionBar(Component.empty());
                    hovered.remove(player.getUniqueId());
                }
                continue;
            }

            HoverTarget current = new HoverTarget(hit.scene().id(), hit.button().id());
            // Refresh every UI tick so the action bar remains visible while hovered.
            player.sendActionBar(hit.button().description());
            hovered.put(player.getUniqueId(), current);
        }
        hovered.keySet().removeIf(id -> Bukkit.getPlayer(id) == null);
    }

    private UiHit nearestHit(Player player) {
        return scenes.values().stream()
                .map(scene -> scene.hit(player))
                .filter(Objects::nonNull)
                .min(java.util.Comparator.comparingDouble(UiHit::distance))
                .orElse(null);
    }

    private record HoverTarget(UUID sceneId, String buttonId) {}

    /** Avoid exposing the mutable backing collection through the service API. */
    private static final class ListView {
        static Collection<UiHandle> copyOf(Collection<? extends UiHandle> handles) {
            return List.copyOf(handles);
        }
    }
}
