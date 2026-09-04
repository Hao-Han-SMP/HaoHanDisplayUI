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

/** Owns follow configuration and calculates the next scene anchor. */
public final class UiFollowController {
    public UiFollowController() {}
    private UiFollowMode mode = UiFollowMode.NONE;
    private Player target;
    private UiFollowOptions options = UiFollowOptions.defaults();

    public void configure(Player target, UiFollowOptions options) {
        this.target = Objects.requireNonNull(target, "target");
        this.options = Objects.requireNonNull(options, "options");
        this.mode = UiFollowMode.FOLLOW;
    }

    public void stop() {
        mode = UiFollowMode.NONE;
        target = null;
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
        if (mode == UiFollowMode.NONE || target == null || !target.isOnline()) return null;
        if (target.getWorld() != current.getWorld()) return null;

        Location eye = target.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        Location targetLocation = eye.clone().add(direction.multiply(options.distance()));
        targetLocation.setPitch(eye.getPitch());
        targetLocation.setYaw(eye.getYaw() + 180.0f);
        Location next = current.clone();
        next.setX(MathUtils.lerp(current.getX(), targetLocation.getX(), options.positionDamping()));
        next.setY(MathUtils.lerp(current.getY(), targetLocation.getY(), options.positionDamping()));
        next.setZ(MathUtils.lerp(current.getZ(), targetLocation.getZ(), options.positionDamping()));
        float yawDiff = MathUtils.signedAngleDifference(targetLocation.getYaw(), current.getYaw());
        next.setYaw(current.getYaw() + yawDiff * options.rotationDamping());
        float pitchDiff = targetLocation.getPitch() - current.getPitch();
        next.setPitch(current.getPitch() + pitchDiff * options.rotationDamping());
        return next;
    }
}
