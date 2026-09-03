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
import org.bukkit.util.Vector;
import vn.haohan.displayui.api.view.UiFollowMode;
import vn.haohan.displayui.utils.MathUtils;

import java.util.Objects;

/** Owns follow configuration and calculates the next scene anchor. */
final class UiFollowController {
    static final int HARD_INTERPOLATION_TICKS = 2;
    static final int SMOOTH_INTERPOLATION_TICKS = 3;

    private UiFollowMode mode = UiFollowMode.NONE;
    private Player target;
    private double distance = 3.0;
    private float pitchOffset;
    private double positionDamping = 0.18;
    private double rotationDamping = 0.20;

    void configure(Player target, UiFollowMode mode, double distance, float pitchOffset) {
        this.target = Objects.requireNonNull(target, "target");
        this.mode = Objects.requireNonNull(mode, "mode");
        if (distance <= 0.0) throw new IllegalArgumentException("distance must be positive");
        this.distance = distance;
        this.pitchOffset = pitchOffset;
    }

    void stop() {
        mode = UiFollowMode.NONE;
        target = null;
    }

    UiFollowMode mode() {
        return mode;
    }

    Player target() {
        return target;
    }

    int interpolationTicks() {
        return mode == UiFollowMode.HARD
                ? HARD_INTERPOLATION_TICKS : SMOOTH_INTERPOLATION_TICKS;
    }

    Location next(Location current) {
        if (mode == UiFollowMode.NONE || target == null || !target.isOnline()) return null;
        if (target.getWorld() != current.getWorld()) return null;

        Location eye = target.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        Location targetLocation = eye.clone().add(direction.multiply(distance));
        targetLocation.setPitch(eye.getPitch() + pitchOffset);
        targetLocation.setYaw(eye.getYaw() + 180.0f);
        if (mode == UiFollowMode.HARD) return targetLocation;

        Location next = current.clone();
        next.setX(MathUtils.lerp(current.getX(), targetLocation.getX(), positionDamping));
        next.setY(MathUtils.lerp(current.getY(), targetLocation.getY(), positionDamping));
        next.setZ(MathUtils.lerp(current.getZ(), targetLocation.getZ(), positionDamping));
        float yawDiff = MathUtils.signedAngleDifference(targetLocation.getYaw(), current.getYaw());
        next.setYaw(current.getYaw() + yawDiff * (float) rotationDamping);
        float pitchDiff = targetLocation.getPitch() - current.getPitch();
        next.setPitch(current.getPitch() + pitchDiff * (float) rotationDamping);
        return next;
    }
}
