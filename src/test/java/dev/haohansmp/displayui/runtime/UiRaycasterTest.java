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
package dev.haohansmp.displayui.runtime;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UiRaycasterTest {
    @Test
    void projectsCameraRayIntoLogicalPixelCoordinates() {
        var hit = UiRaycaster.project(
                new Vector(0, 0, 4),
                new Vector(1, -0.5, -4),
                new Vector(0, 0, 0),
                new Vector(0, 0, 1),
                40.0f, 8.0);

        assertEquals(40.0f, hit.localX(), 0.001f);
        assertEquals(20.0f, hit.localY(), 0.001f);
        assertEquals(Math.sqrt(17.25), hit.distance(), 0.001);
    }

    @Test
    void rejectsParallelBehindAndOutOfRangeRays() {
        Vector eye = new Vector(0, 0, 4);
        Vector origin = new Vector(0, 0, 0);
        Vector normal = new Vector(0, 0, 1);
        assertNull(UiRaycaster.project(eye, new Vector(1, 0, 0), origin, normal, 40, 8));
        assertNull(UiRaycaster.project(eye, new Vector(0, 0, 1), origin, normal, 40, 8));
        assertNull(UiRaycaster.project(eye, new Vector(0, 0, -1), origin, normal, 40, 3));
    }

    @Test
    void supportsAnExplicitTiltedPlaneBasis() {
        double inverseRootTwo = 1.0 / Math.sqrt(2.0);
        var hit = UiRaycaster.project(
                new Vector(0, 2, 4), new Vector(0, -2, -4),
                new Vector(0, 0, 0),
                new Vector(0, inverseRootTwo, inverseRootTwo),
                new Vector(1, 0, 0),
                new Vector(0, inverseRootTwo, -inverseRootTwo),
                40, 8);
        assertEquals(0.0f, hit.localX(), 0.001f);
        assertEquals(0.0f, hit.localY(), 0.001f);
    }
}
