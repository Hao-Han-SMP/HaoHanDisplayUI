/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.runtime.scene;

import org.bukkit.entity.Player;

/** Coordinates periodic audience visibility synchronization for a scene. */
final class UiSceneVisibilityController {
    private final UiScene scene;

    UiSceneVisibilityController(UiScene scene) {
        this.scene = scene;
    }

    void tick() {
        for (Player online : scene.onlinePlayers()) {
            boolean shouldSee = scene.shouldShow(online);
            boolean isSeeing = scene.isViewerVisible(online);
            if (shouldSee && !isSeeing) scene.showEntities(online);
            else if (!shouldSee && isSeeing) scene.hideEntities(online);
            else if (shouldSee) {
                scene.syncItemBackfaces(online);
                scene.syncSideVisibility(online);
            }
        }
        scene.removeOfflineViewers();
    }
}
