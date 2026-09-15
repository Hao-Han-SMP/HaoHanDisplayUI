/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package vn.haohan.displayui.runtime.scene.interaction;

import org.bukkit.Location;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiControl;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.runtime.scene.visibility.UiCameraBasis;

import java.util.Collection;

/** Calculates the world-space interaction AABB for a rotated UI page. */
public final class UiInteractionBounds {

    private UiInteractionBounds() {}

    public static Bounds calculate(UiDocument document, Collection<UiControl> controls,
                            Location origin, double pixels, UiCameraTransform camera) {
        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for (var button : document.buttons()) {
            minX = Math.min(minX, button.x() - button.hitSlop());
            maxX = Math.max(maxX, button.x() + button.width() + button.hitSlop());
            minY = Math.min(minY, button.y() - button.hitSlop());
            maxY = Math.max(maxY, button.y() + button.height() + button.hitSlop());
        }

        for (var control : controls) {
            minX = Math.min(minX, control.x() - control.hitSlop());
            maxX = Math.max(maxX, control.x() + control.width() + control.hitSlop());
            minY = Math.min(minY, control.y() - control.hitSlop());
            maxY = Math.max(maxY, control.y() + control.height() + control.hitSlop());
        }

        if (Float.isInfinite(minX) || Float.isInfinite(maxX) || Float.isInfinite(minY) || Float.isInfinite(maxY)) {
            return new Bounds(origin.clone(), 0.2, 0.2);
        }

        UiCameraBasis basis = UiCameraBasis.forFixedPlane(origin, camera);

        double worldMinX = Double.POSITIVE_INFINITY, worldMinY = Double.POSITIVE_INFINITY;
        double worldMinZ = Double.POSITIVE_INFINITY, worldMaxX = Double.NEGATIVE_INFINITY;
        double worldMaxY = Double.NEGATIVE_INFINITY, worldMaxZ = Double.NEGATIVE_INFINITY;
        for (float x : new float[] {minX, maxX}) {
            for (float y : new float[] {minY, maxY}) {
                var offset = basis.right().multiply(x / pixels)
                        .add(basis.up().multiply(-y / pixels));
                worldMinX = Math.min(worldMinX, offset.getX()); worldMaxX = Math.max(worldMaxX, offset.getX());
                worldMinY = Math.min(worldMinY, offset.getY()); worldMaxY = Math.max(worldMaxY, offset.getY());
                worldMinZ = Math.min(worldMinZ, offset.getZ()); worldMaxZ = Math.max(worldMaxZ, offset.getZ());
            }
        }
        // In Minecraft, an Interaction entity's origin is at its bottom feet.
        // Therefore, center.y must be worldMinY so that [worldMinY, worldMinY + height]
        // precisely covers [worldMinY, worldMaxY].
        Location center = origin.clone().add((worldMinX + worldMaxX) * 0.5,
                worldMinY, (worldMinZ + worldMaxZ) * 0.5);
        return new Bounds(center, Math.max(0.2, Math.max(worldMaxX - worldMinX, worldMaxZ - worldMinZ)),
                Math.max(0.2, worldMaxY - worldMinY));
    }

    public record Bounds(Location center, double width, double height) {}
}
