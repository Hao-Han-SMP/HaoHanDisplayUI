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
package vn.haohan.displayui.runtime.scene.follow;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import vn.haohan.displayui.api.view.UiFollowMode;
import vn.haohan.displayui.api.view.UiFollowOptions;
import vn.haohan.displayui.utils.MathUtils;

import java.util.Objects;

/**
 * Owns follow configuration and calculates the next scene anchor.
 *
 * <p>Supports panel-based gaze-deadzone following: keeps the hologram stationary
 * when the player's crosshair is within the menu bounds, allowing effortless button clicking.
 * When the gaze turns outside bounds or the player moves physically, the UI smoothly glides
 * back into alignment in front of the player and docks.</p>
 */
public final class UiFollowController {
    public UiFollowController() {}
    private UiFollowMode mode = UiFollowMode.NONE;
    private Player target;
    private UiFollowOptions options = UiFollowOptions.defaults();
    private boolean isCatchingUp = false;

    private Location lastEyeLocation;

    public void configure(Player target, UiFollowOptions options) {
        this.target = Objects.requireNonNull(target, "target");
        this.options = Objects.requireNonNull(options, "options");
        this.mode = UiFollowMode.FOLLOW;
        this.isCatchingUp = false;
        this.lastEyeLocation = null;
    }

    public void stop() {
        mode = UiFollowMode.NONE;
        target = null;
        isCatchingUp = false;
        lastEyeLocation = null;
    }

    public void stopCatchUp() {
        isCatchingUp = false;
    }

    public boolean isCatchingUp() {
        return isCatchingUp;
    }

    public UiFollowMode mode() {
        return mode;
    }

    public Player target() {
        return target;
    }

    public UiFollowOptions options() { return options; }

    public int interpolationTicks() { return options.interpolationTicks(); }

    public Location next(Location current) {
        float minX = options.hasCustomBounds() ? options.minX() : -50.0f;
        float maxX = options.hasCustomBounds() ? options.maxX() : 50.0f;
        float minY = options.hasCustomBounds() ? options.minY() : -50.0f;
        float maxY = options.hasCustomBounds() ? options.maxY() : 50.0f;
        return next(current, 75.0f, minX, maxX, minY, maxY);
    }

    public Location next(Location current, float pixelsPerBlock,
                         float defaultMinX, float defaultMaxX,
                         float defaultMinY, float defaultMaxY) {
        return next(current, pixelsPerBlock, defaultMinX, defaultMaxX, defaultMinY, defaultMaxY, null);
    }

    public Location next(Location current, float pixelsPerBlock,
                         float defaultMinX, float defaultMaxX,
                         float defaultMinY, float defaultMaxY,
                         vn.haohan.displayui.utils.RaycastUtils.Projection cursor) {
        if (mode == UiFollowMode.NONE || target == null || !target.isOnline()) return null;
        if (target.getWorld() != current.getWorld()) return null;

        Location eye = target.getEyeLocation();

        // Check if player physically moved their position in world space (> 2cm)
        boolean playerMoved = false;
        if (lastEyeLocation != null && lastEyeLocation.getWorld() == eye.getWorld()) {
            double movedDistSq = eye.toVector().distanceSquared(lastEyeLocation.toVector());
            playerMoved = (movedDistSq > 0.0004);
        }
        lastEyeLocation = eye.clone();

        double distToOrigin = eye.distance(current);
        double minDist = options.distance() * options.minDistanceRatio();
        double maxDist = options.distance() * options.maxDistanceRatio();
        boolean outOfDistance = (distToOrigin < minDist || distToOrigin > maxDist);

        boolean crosshairInside = false;
        if (options.gazeDeadzone()) {
            float minX = options.hasCustomBounds() ? options.minX() : defaultMinX;
            float maxX = options.hasCustomBounds() ? options.maxX() : defaultMaxX;
            float minY = options.hasCustomBounds() ? options.minY() : defaultMinY;
            float maxY = options.hasCustomBounds() ? options.maxY() : defaultMaxY;
            float margin = options.deadzoneMargin();

            if (cursor != null) {
                crosshairInside = (cursor.localX() >= (minX - margin) && cursor.localX() <= (maxX + margin))
                        && (cursor.localY() >= (minY - margin) && cursor.localY() <= (maxY + margin));
            } else {
                RaycastResult res = projectCrosshair(eye.toVector(), eye.getDirection(),
                        current.toVector(), current.getDirection(),
                        pixelsPerBlock, options.distance() * 2.5);
                crosshairInside = res.hits()
                        && (res.localX() >= (minX - margin) && res.localX() <= (maxX + margin))
                        && (res.localY() >= (minY - margin) && res.localY() <= (maxY + margin));
            }

            // Gaze deadzone: holds UI stationary when player is stationary, docked, and gaze is within panel
            if (!playerMoved && !isCatchingUp && crosshairInside && !outOfDistance) {
                return null;
            }
        }

        Location idealTarget = calculateIdealTarget(eye, options.distance());

        double dx = idealTarget.getX() - current.getX();
        double dy = idealTarget.getY() - current.getY();
        double dz = idealTarget.getZ() - current.getZ();
        double distSq = dx * dx + dy * dy + dz * dz;

        // If player moved extremely far (e.g. teleported > 16 blocks), snap immediately
        if (distSq > 256.0) {
            isCatchingUp = false;
            return idealTarget;
        }

        float yawDiff = MathUtils.signedAngleDifference(idealTarget.getYaw(), current.getYaw());
        float pitchDiff = idealTarget.getPitch() - current.getPitch();

        // Settling threshold when close enough and player not moving
        if (!playerMoved && distSq < 0.0001 && Math.abs(yawDiff) < 0.05f && Math.abs(pitchDiff) < 0.05f) {
            isCatchingUp = false;
            return null;
        }

        // Dock if close enough to ideal target when stationary
        if (!playerMoved && distSq < 0.02 && Math.abs(yawDiff) < 3.0f && Math.abs(pitchDiff) < 3.0f) {
            isCatchingUp = false;
            return idealTarget;
        }

        isCatchingUp = true;

        double speedPos = options.positionDamping();
        double speedRot = options.rotationDamping();

        // Accelerate if distance or angle is large (snappy catchup)
        if (distSq > 4.0 || Math.abs(yawDiff) > 60.0f) {
            speedPos = Math.max(speedPos, 0.65);
            speedRot = Math.max(speedRot, 0.65);
        }

        if (speedPos >= 0.999f && speedRot >= 0.999f) {
            isCatchingUp = false;
            return idealTarget;
        }

        Location next = current.clone();
        next.setX(current.getX() + dx * speedPos);
        next.setY(current.getY() + dy * speedPos);
        next.setZ(current.getZ() + dz * speedPos);
        next.setYaw(current.getYaw() + yawDiff * (float) speedRot);
        next.setPitch(current.getPitch() + pitchDiff * (float) speedRot);

        // Snap if within settling threshold after lerping to avoid micro-drift
        if (idealTarget.distanceSquared(next) < 0.0001
                && Math.abs(MathUtils.signedAngleDifference(idealTarget.getYaw(), next.getYaw())) < 0.05f
                && Math.abs(idealTarget.getPitch() - next.getPitch()) < 0.05f) {
            isCatchingUp = false;
            return idealTarget;
        }

        return next;
    }

    public static Location calculateIdealTarget(Location eye, double distance) {
        return calculateTargetAtDirection(eye, eye.getDirection(), distance);
    }

    public static Location calculateTargetAtDirection(Location eye, Vector dir, double distance) {
        Vector normalized = dir.clone().normalize();
        Location target = eye.clone().add(normalized.clone().multiply(distance));
        Location dirLoc = eye.clone();
        dirLoc.setDirection(normalized);
        target.setYaw(dirLoc.getYaw() + 180.0f);
        target.setPitch(-dirLoc.getPitch());
        return target;
    }

    public record RaycastResult(boolean hits, float localX, float localY, double hitDistance) {
        public static final RaycastResult MISS = new RaycastResult(false, 0, 0, -1);
    }

    public static RaycastResult projectCrosshair(Vector eyePos, Vector rayDir, Vector planeOrigin,
                                                 Vector planeDir, float pixelsPerBlock, double maxDistance) {
        Vector normal = planeDir.clone().normalize();
        if (normal.lengthSquared() < 0.0001) normal = new Vector(0, 0, 1);
        Vector right = new Vector(normal.getZ(), 0.0, -normal.getX());
        if (right.lengthSquared() < 0.0001) right = new Vector(1, 0, 0);
        right.normalize();
        Vector up = normal.clone().crossProduct(right).normalize();

        vn.haohan.displayui.utils.RaycastUtils.Projection proj = vn.haohan.displayui.utils.RaycastUtils.project(
                eyePos, rayDir, planeOrigin, normal, right, up, pixelsPerBlock, maxDistance);
        if (proj == null) {
            return RaycastResult.MISS;
        }
        return new RaycastResult(true, proj.localX(), proj.localY(), proj.distance());
    }
}
