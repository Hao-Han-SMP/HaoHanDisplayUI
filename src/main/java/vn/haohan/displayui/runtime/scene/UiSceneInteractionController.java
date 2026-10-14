/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.runtime.scene;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHit;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiControl;
import vn.haohan.displayui.api.interaction.UiScrollList;
import vn.haohan.displayui.api.node.EntityModelNode;
import vn.haohan.displayui.api.node.MobEntityNode;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.runtime.scene.visibility.UiCameraBasis;
import vn.haohan.displayui.utils.RaycastUtils;

import java.util.List;

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
        return hitAt(player, projection.localX(), projection.localY(), projection.distance());
    }

    UiHit hitAt(Player player, float localX, float localY, double distance) {
        if (!scene.canInteract(player)) return null;
        return findHit(scene.document(), scene.controls(), localX, localY, player, distance);
    }

    UiHit findHit(UiDocument document, List<UiControl> controls,
                  float px, float py, Player player, double distance) {
        if (document == null) return null;

        // 1. Priority 1: Interactive buttons and component inspect hitboxes (__inspect_comp_*)
        // Evaluated front-to-back (reverse of compilation order so foreground layers and children win)
        List<UiButton> buttons = document.buttons();
        for (int i = buttons.size() - 1; i >= 0; i--) {
            UiButton button = buttons.get(i);
            if (!button.id().startsWith("__inspect_cont_") && button.contains(px, py)) {
                return new UiHit(scene, button, null, player, px, py, distance);
            }
        }

        // 2. Priority 2: Interactive scene controls (Sliders, Checkboxes, etc.)
        // Evaluated front-to-back
        if (controls != null) {
            for (int i = controls.size() - 1; i >= 0; i--) {
                UiControl control = controls.get(i);
                if (control.contains(px, py)) {
                    return new UiHit(scene, null, control, player, px, py, distance);
                }
            }
        }

        // 3. Priority 3: Container inspect fallback backdrops (__inspect_cont_*)
        // Evaluated front-to-back (innermost child containers before parent containers, higher layers before lower layers)
        for (int i = buttons.size() - 1; i >= 0; i--) {
            UiButton button = buttons.get(i);
            if (button.id().startsWith("__inspect_cont_") && button.contains(px, py)) {
                return new UiHit(scene, button, null, player, px, py, distance);
            }
        }

        return null;
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
        return scrollHitAt(player, projection.localX(), projection.localY(), projection.distance());
    }

    UiHit scrollHitAt(Player player, float localX, float localY, double distance) {
        List<UiControl> controls = scene.controls();
        if (controls != null) {
            for (int i = controls.size() - 1; i >= 0; i--) {
                UiControl control = controls.get(i);
                if (control instanceof UiScrollList && control.contains(localX, localY)) {
                    return new UiHit(scene, null, control, player, localX, localY, distance);
                }
            }
        }
        return null;
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
