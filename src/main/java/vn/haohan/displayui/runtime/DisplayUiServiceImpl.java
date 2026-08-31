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
import vn.haohan.displayui.api.interaction.UiScrollList;
import vn.haohan.displayui.api.icon.UiIconRegistry;
import vn.haohan.displayui.api.view.UiAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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
    private final Map<UUID, DragSession> dragging = new LinkedHashMap<>();

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
        return List.copyOf(scenes.values());
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

    public boolean handleLeftClick(Player player) {
        DragSession active = dragging.get(player.getUniqueId());
        if (active != null) {
            active.stop(player);
            dragging.remove(player.getUniqueId());
            player.sendActionBar(Component.empty());
            return true;
        }

        for (UiScene scene : scenes.values()) {
            if (!scene.isValid()) continue;
            var proj = scene.projectCursor(player);
            if (proj == null) continue;
            int modelIndex = scene.findModelNodeAt(proj.localX(), proj.localY());
            if (modelIndex >= 0) {
                dragging.put(player.getUniqueId(), new ModelDragSession(scene, modelIndex, proj.localX(), proj.localY()));
                player.sendActionBar(Component.text("✥ 3D Drag Mode · Move cursor to spin · Left-click to release", NamedTextColor.GOLD));
                return true;
            }
        }
        return false;
    }

    public boolean handleRightClick(Player player) {
        DragSession active = dragging.get(player.getUniqueId());
        if (active != null) {
            active.stop(player);
            dragging.remove(player.getUniqueId());
        }

        UiHit nearest = nearestHit(player);
        if (nearest == null) return false;
        nearest.scene().activate(nearest);
        if (nearest.control() instanceof vn.haohan.displayui.api.interaction.UiSlider slider) {
            dragging.put(player.getUniqueId(), new SliderDragSession(nearest.scene(), slider.id()));
        }
        return true;
    }

    public boolean handleScroll(Player player, int direction) {
        if (direction == 0) return false;
        UiHit nearest = scenes.values().stream()
                .map(scene -> scene.scrollHit(player))
                .filter(Objects::nonNull)
                .min(java.util.Comparator.comparingDouble(UiHit::distance))
                .orElse(null);
        if (nearest == null || !(nearest.control() instanceof UiScrollList scroll)) return false;
        return nearest.scene().scroll(nearest, scroll.offset() + direction * scroll.step());
    }

    public void stopDragging(Player player) {
        DragSession active = dragging.remove(player.getUniqueId());
        if (active != null) active.stop(player);
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
        if (ownerKey == null || !ownerKey.matches("[a-z0-9_.-]+:[a-z0-9/._:-]+")) {
            throw new IllegalArgumentException(
                    "ownerKey must be namespaced, for example haohanmetallurgy:forge_guide");
        }
    }

    private void tickHoverDescriptions() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (dragging.containsKey(player.getUniqueId())) continue;
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
            player.sendActionBar(interactionDescription(hit));
            hovered.put(player.getUniqueId(), current);
        }
        hovered.keySet().removeIf(id -> Bukkit.getPlayer(id) == null);
    }

    private void tickDragging() {
        dragging.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            Player player = Bukkit.getPlayer(playerId);
            DragSession session = entry.getValue();
            if (player == null || !session.isValid()) {
                if (session != null && player != null) session.stop(player);
                return true;
            }
            return !session.update(player);
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

    private interface DragSession {
        boolean isValid();
        boolean update(Player player);
        void stop(Player player);
    }

    private static final class SliderDragSession implements DragSession {
        private static final double POSITION_EPSILON = 0.0001;
        private static final float ANGLE_EPSILON = 0.01f;

        private final UiScene scene;
        private final String controlId;
        private double lastX, lastY, lastZ;
        private float lastYaw, lastPitch;
        private boolean hasView;

        private SliderDragSession(UiScene scene, String controlId) {
            this.scene = scene;
            this.controlId = controlId;
        }

        @Override
        public boolean isValid() {
            return scene.isValid();
        }

        @Override
        public boolean update(Player player) {
            if (!viewChanged(player)) return true;
            rememberView(player);
            return scene.dragSlider(player, controlId);
        }

        @Override
        public void stop(Player player) {
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
    }

    private static final class ModelDragSession implements DragSession {
        private final UiScene scene;
        private final int nodeIndex;
        private float lastLocalX;
        private float lastLocalY;

        private ModelDragSession(UiScene scene, int nodeIndex, float startX, float startY) {
            this.scene = scene;
            this.nodeIndex = nodeIndex;
            this.lastLocalX = startX;
            this.lastLocalY = startY;
        }

        @Override
        public boolean isValid() {
            return scene.isValid();
        }

        @Override
        public boolean update(Player player) {
            var proj = scene.projectCursor(player);
            if (proj == null) return true;
            float deltaX = proj.localX() - lastLocalX;
            float deltaY = proj.localY() - lastLocalY;
            lastLocalX = proj.localX();
            lastLocalY = proj.localY();
            return scene.dragModel(nodeIndex, deltaX, deltaY);
        }

        @Override
        public void stop(Player player) {
            scene.releaseModelDrag(nodeIndex);
        }
    }
}
