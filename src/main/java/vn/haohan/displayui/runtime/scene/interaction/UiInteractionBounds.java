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
        float minX = document.buttons().stream().map(button -> button.x() - button.hitSlop())
                .min(Float::compare).orElse(Float.POSITIVE_INFINITY);
        minX = Math.min(minX, controls.stream().map(control -> control.x() - control.hitSlop())
                .min(Float::compare).orElse(0.0f));
        float maxX = document.buttons().stream().map(button -> button.x() + button.width() + button.hitSlop())
                .max(Float::compare).orElse(Float.NEGATIVE_INFINITY);
        maxX = Math.max(maxX, controls.stream().map(control -> control.x() + control.width() + control.hitSlop())
                .max(Float::compare).orElse(0.0f));
        float minY = document.buttons().stream().map(button -> button.y() - button.hitSlop())
                .min(Float::compare).orElse(Float.POSITIVE_INFINITY);
        minY = Math.min(minY, controls.stream().map(control -> control.y() - control.hitSlop())
                .min(Float::compare).orElse(0.0f));
        float maxY = document.buttons().stream().map(button -> button.y() + button.height() + button.hitSlop())
                .max(Float::compare).orElse(Float.NEGATIVE_INFINITY);
        maxY = Math.max(maxY, controls.stream().map(control -> control.y() + control.height() + control.hitSlop())
                .max(Float::compare).orElse(0.0f));

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
        Location center = origin.clone().add((worldMinX + worldMaxX) * 0.5,
                (worldMinY + worldMaxY) * 0.5, (worldMinZ + worldMaxZ) * 0.5);
        return new Bounds(center, Math.max(0.2, Math.max(worldMaxX - worldMinX, worldMaxZ - worldMinZ)),
                Math.max(0.2, worldMaxY - worldMinY));
    }

    public record Bounds(Location center, double width, double height) {}
}
