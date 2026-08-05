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
