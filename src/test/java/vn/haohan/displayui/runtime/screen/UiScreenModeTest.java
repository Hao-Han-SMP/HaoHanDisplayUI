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
package vn.haohan.displayui.runtime.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiScreenModeTest {

    @Test
    void angleDeltaCalculatesSmallDirectDifferences() {
        assertEquals(5.0f, UiScreenMode.angleDelta(10.0f, 5.0f), 0.001f);
        assertEquals(-5.0f, UiScreenMode.angleDelta(5.0f, 10.0f), 0.001f);
        assertEquals(0.0f, UiScreenMode.angleDelta(45.0f, 45.0f), 0.001f);
    }

    @Test
    void angleDeltaHandlesWraparoundAcross180Boundary() {
        // Turning slightly right across 180 -> -175 (difference is +5 degrees)
        assertEquals(5.0f, UiScreenMode.angleDelta(-175.0f, 180.0f), 0.001f);
        // Turning slightly left across -180 -> 175 (difference is -5 degrees)
        assertEquals(-5.0f, UiScreenMode.angleDelta(175.0f, -180.0f), 0.001f);
    }

    @Test
    void boundsClampingRestrictsCursorToDocumentArea() {
        float[] bounds = new float[] {-100.0f, 100.0f, -50.0f, 50.0f};
        float testX = 150.0f;
        float testY = -80.0f;

        float clampedX = Math.max(bounds[0], Math.min(bounds[1], testX));
        float clampedY = Math.max(bounds[2], Math.min(bounds[3], testY));

        assertEquals(100.0f, clampedX);
        assertEquals(-50.0f, clampedY);
        assertTrue(clampedX >= bounds[0] && clampedX <= bounds[1]);
        assertTrue(clampedY >= bounds[2] && clampedY <= bounds[3]);
    }

    @Test
    void raycastProjectionMapsLookRayToPlaneCoordinates() {
        org.bukkit.util.Vector eye = new org.bukkit.util.Vector(0, 2, 3);
        org.bukkit.util.Vector dir = new org.bukkit.util.Vector(0, 0, -1); // looking forward along -Z
        org.bukkit.util.Vector planeOrigin = new org.bukkit.util.Vector(0, 2, 0); // plane at Z=0
        org.bukkit.util.Vector normal = new org.bukkit.util.Vector(0, 0, 1);
        org.bukkit.util.Vector right = new org.bukkit.util.Vector(1, 0, 0);
        org.bukkit.util.Vector up = new org.bukkit.util.Vector(0, 1, 0);
        float ppb = 75.0f;

        // Looking directly forward hits origin -> (0, 0)
        var projCenter = vn.haohan.displayui.utils.RaycastUtils.project(
                eye, dir, planeOrigin, normal, right, up, ppb, 50.0);
        org.junit.jupiter.api.Assertions.assertNotNull(projCenter);
        assertEquals(0.0f, projCenter.localX(), 0.001f);
        assertEquals(0.0f, projCenter.localY(), 0.001f);

        // Looking slightly to the right (X=1, Y=0, Z=-3) -> hits at X=1 -> localX = 75.0
        org.bukkit.util.Vector dirRight = new org.bukkit.util.Vector(1, 0, -3).normalize();
        var projRight = vn.haohan.displayui.utils.RaycastUtils.project(
                eye, dirRight, planeOrigin, normal, right, up, ppb, 50.0);
        org.junit.jupiter.api.Assertions.assertNotNull(projRight);
        assertEquals(75.0f, projRight.localX(), 0.01f);
        assertEquals(0.0f, projRight.localY(), 0.01f);

        // Looking slightly down (X=0, Y=-1, Z=-3) -> hits at Y=1 (lower) -> offset.up = -1 -> localY = +75.0 (down in screen coords)
        org.bukkit.util.Vector dirDown = new org.bukkit.util.Vector(0, -1, -3).normalize();
        var projDown = vn.haohan.displayui.utils.RaycastUtils.project(
                eye, dirDown, planeOrigin, normal, right, up, ppb, 50.0);
        org.junit.jupiter.api.Assertions.assertNotNull(projDown);
        assertEquals(0.0f, projDown.localX(), 0.01f);
        assertEquals(75.0f, projDown.localY(), 0.01f);

        // Looking away (along +Z) -> returns null
        org.bukkit.util.Vector dirAway = new org.bukkit.util.Vector(0, 0, 1).normalize();
        var projAway = vn.haohan.displayui.utils.RaycastUtils.project(
                eye, dirAway, planeOrigin, normal, right, up, ppb, 50.0);
        org.junit.jupiter.api.Assertions.assertNull(projAway);
    }
}
