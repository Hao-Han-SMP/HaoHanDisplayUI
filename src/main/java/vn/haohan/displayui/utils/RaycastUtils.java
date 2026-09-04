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
package vn.haohan.displayui.utils;

import org.bukkit.util.Vector;

public final class RaycastUtils {
    private RaycastUtils() {}

    public static Projection project(Vector eye, Vector rayDirection, Vector planeOrigin,
                              Vector planeNormal, float pixelsPerBlock,
                              double maxDistance) {
        Vector normal = planeNormal.clone().setY(0.0);
        if (normal.lengthSquared() < 0.0001) return null;
        normal.normalize();
        Vector right = new Vector(normal.getZ(), 0.0, -normal.getX());
        return project(eye, rayDirection, planeOrigin, normal, right,
                new Vector(0, 1, 0), pixelsPerBlock, maxDistance);
    }

    public static Projection project(Vector eye, Vector rayDirection, Vector planeOrigin,
                              Vector planeNormal, Vector planeRight, Vector planeUp,
                              float pixelsPerBlock, double maxDistance) {
        Vector normal = planeNormal.clone();
        Vector right = planeRight.clone();
        Vector up = planeUp.clone();
        if (normal.lengthSquared() < 0.0001 || right.lengthSquared() < 0.0001
                || up.lengthSquared() < 0.0001) return null;
        normal.normalize();
        right.normalize();
        up.normalize();
        Vector ray = rayDirection.clone().normalize();
        double denominator = normal.dot(ray);
        if (Math.abs(denominator) < 1.0e-6) return null;

        double distance = normal.dot(planeOrigin.clone().subtract(eye)) / denominator;
        if (distance <= 0.0 || distance > maxDistance) return null;

        Vector offset = eye.clone().add(ray.multiply(distance)).subtract(planeOrigin);
        float localX = (float) (offset.dot(right) * pixelsPerBlock);
        float localY = (float) (-offset.dot(up) * pixelsPerBlock);
        return new Projection(localX, localY, distance);
    }

    public record Projection(float localX, float localY, double distance) {}
}
