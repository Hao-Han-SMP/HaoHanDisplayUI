/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.runtime.scene;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHit;
import vn.haohan.displayui.api.interaction.UiScrollList;
import vn.haohan.displayui.api.node.EntityModelNode;
import vn.haohan.displayui.api.node.MobEntityNode;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.runtime.scene.visibility.UiCameraBasis;
import vn.haohan.displayui.utils.RaycastUtils;

/** Owns scene hit testing and the shared screen projection path. */
final class UiSceneInteractionController {
    private final UiScene scene;

    UiSceneInteractionController(UiScene scene) {
        this.scene = scene;
    }

    UiHit hit(Player player) {
        if (!scene.canInteract(player)) return null;
        RaycastUtils.Projection projection = projectCursor(player);
        if (projection == null) return null;

        UiDocument document = scene.document();
        UiHit buttonHit = document.buttons().stream()
                .filter(button -> button.contains(projection.localX(), projection.localY()))
                .findFirst()
                .map(button -> new UiHit(scene, button, null, player,
                        projection.localX(), projection.localY(), projection.distance()))
                .orElse(null);
        if (buttonHit != null) return buttonHit;

        return scene.controls().stream()
                .filter(control -> control.contains(projection.localX(), projection.localY()))
                .findFirst()
                .map(control -> new UiHit(scene, null, control, player,
                        projection.localX(), projection.localY(), projection.distance()))
                .orElse(null);
    }

    RaycastUtils.Projection projectCursor(Player player) {
        if (!scene.canInteract(player)) return null;
        RaycastUtils.Projection raw = projectRaw(player);
        if (raw == null) return null;
        return scene.isMirroredFor(player)
                ? new RaycastUtils.Projection(-raw.localX(), raw.localY(), raw.distance())
                : raw;
    }

    UiHit scrollHit(Player player) {
        RaycastUtils.Projection projection = projectCursor(player);
        if (projection == null) return null;
        return scene.controls().stream()
                .filter(control -> control instanceof UiScrollList
                        && control.contains(projection.localX(), projection.localY()))
                .findFirst()
                .map(control -> new UiHit(scene, null, control, player,
                        projection.localX(), projection.localY(), projection.distance()))
                .orElse(null);
    }

    int findModelNodeAt(float localX, float localY) {
        UiDocument document = scene.document();
        for (int i = 0; i < document.nodes().size(); i++) {
            UiNode node = document.nodes().get(i);
            if (node instanceof EntityModelNode model && model.contains(localX, localY)) return i;
            if (node instanceof MobEntityNode mob && mob.contains(localX, localY)) return i;
        }
        return -1;
    }

    private RaycastUtils.Projection projectRaw(Player player) {
        UiCameraBasis basis = scene.cameraBasis(player);
        return project(player, scene.renderOrigin().toVector(), basis,
                scene.pixelsPerBlock(), scene.maxDistance());
    }

    // Shared raycast projection used by scene controls and cursor interactions.
    public RaycastUtils.Projection project(Player player, Vector origin, UiCameraBasis basis,
                                                  float pixelsPerBlock, double maxDistance) {
        return RaycastUtils.project(
                player.getEyeLocation().toVector(),
                player.getEyeLocation().getDirection(),
                origin, basis.normal(), basis.right(), basis.up(),
                pixelsPerBlock, maxDistance);
    }
}
