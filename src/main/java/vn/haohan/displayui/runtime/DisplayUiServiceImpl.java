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

import vn.haohan.displayui.HaoHanDisplayUIPlugin;
import vn.haohan.displayui.api.DisplayUiService;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.UiOptions;
import vn.haohan.displayui.api.interaction.UiControl;
import vn.haohan.displayui.api.icon.UiIconRegistry;
import vn.haohan.displayui.api.view.UiAudience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Location;

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
    private final UiIconRegistryImpl icons = new UiIconRegistryImpl();
    private final Map<UUID, UiScene> scenes = new LinkedHashMap<>();
    private final Map<UUID, HoverTarget> hovered = new LinkedHashMap<>();
    private final Map<UUID, DragTarget> dragging = new LinkedHashMap<>();

    public DisplayUiServiceImpl(HaoHanDisplayUIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public UiIconRegistry icons() {
        return icons;
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
        tickDragging();
        tickHoverDescriptions();
    }

    public boolean handleRightClick(Player player) {
        UiHit nearest = nearestHit(player);
        if (nearest == null) return false;
        nearest.scene().activate(nearest);
        if (nearest.control() instanceof vn.haohan.displayui.api.interaction.UiSlider slider) {
            dragging.put(player.getUniqueId(), new DragTarget(nearest.scene(), slider.id()));
        } else {
            dragging.remove(player.getUniqueId());
        }
        return true;
    }

    public void stopDragging(Player player) {
        dragging.remove(player.getUniqueId());
    }

    public void shutdown() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (hovered.containsKey(player.getUniqueId())) player.sendActionBar(Component.empty());
        });
        hovered.clear();
        dragging.clear();
        new ArrayList<>(scenes.values()).forEach(UiScene::remove);
        scenes.clear();
        icons.clear();
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
            if (hit == null || interactionDescription(hit).equals(Component.empty())) {
                if (previous != null) {
                    player.sendActionBar(Component.empty());
                    hovered.remove(player.getUniqueId());
                }
                continue;
            }

            HoverTarget current = new HoverTarget(hit.scene().id(), interactionId(hit));
            // Refresh every UI tick so the action bar remains visible while hovered.
            player.sendActionBar(interactionDescription(hit));
            hovered.put(player.getUniqueId(), current);
        }
        hovered.keySet().removeIf(id -> Bukkit.getPlayer(id) == null);
    }

    private void tickDragging() {
        dragging.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            Player player = Bukkit.getPlayer(playerId);
            DragTarget target = entry.getValue();
            if (player == null || !target.scene().isValid()) return true;
            // Keep the drag state alive, but skip raycast/control work while
            // the player's eye position and view direction are unchanged.
            if (!target.viewChanged(player)) return false;
            target.rememberView(player);
            return !target.scene().dragSlider(player, target.controlId());
        });
    }

    private UiHit nearestHit(Player player) {
        return scenes.values().stream()
                .map(scene -> scene.hit(player))
                .filter(Objects::nonNull)
                .min(java.util.Comparator.comparingDouble(UiHit::distance))
                .orElse(null);
    }

    private String interactionId(UiHit hit) {
        return hit.button() != null ? hit.button().id() : hit.control().id();
    }

    private Component interactionDescription(UiHit hit) {
        return hit.button() != null ? hit.button().description() : hit.control().description();
    }

    private record HoverTarget(UUID sceneId, String buttonId) {}
    private static final class DragTarget {
        private static final double POSITION_EPSILON = 0.0001;
        private static final float ANGLE_EPSILON = 0.01f;

        private final UiScene scene;
        private final String controlId;
        private double lastX;
        private double lastY;
        private double lastZ;
        private float lastYaw;
        private float lastPitch;
        private boolean hasView;

        private DragTarget(UiScene scene, String controlId) {
            this.scene = scene;
            this.controlId = controlId;
        }

        private boolean viewChanged(Player player) {
            Location eye = player.getEyeLocation();
            if (!hasView) return true;
            return Math.abs(eye.getX() - lastX) > POSITION_EPSILON
                    || Math.abs(eye.getY() - lastY) > POSITION_EPSILON
                    || Math.abs(eye.getZ() - lastZ) > POSITION_EPSILON
                    || angleChanged(eye.getYaw(), lastYaw)
                    || angleChanged(eye.getPitch(), lastPitch);
        }

        private void rememberView(Player player) {
            Location eye = player.getEyeLocation();
            lastX = eye.getX();
            lastY = eye.getY();
            lastZ = eye.getZ();
            lastYaw = eye.getYaw();
            lastPitch = eye.getPitch();
            hasView = true;
        }

        private static boolean angleChanged(float current, float previous) {
            float delta = Math.abs(current - previous) % 360.0f;
            delta = Math.min(delta, 360.0f - delta);
            return delta > ANGLE_EPSILON;
        }

        private UiScene scene() { return scene; }
        private String controlId() { return controlId; }
    }

    /** Avoid exposing the mutable backing collection through the service API. */
    private static final class ListView {
        static Collection<UiHandle> copyOf(Collection<? extends UiHandle> handles) {
            return List.copyOf(handles);
        }
    }
}
