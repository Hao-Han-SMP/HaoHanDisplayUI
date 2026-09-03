package vn.haohan.displayui.utils;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeometryUtilsTest {
    @Test
    void scalesPointsAroundTheirExpectedCenters() {
        Vector3f first = new Vector3f(0, 0, 0);
        Vector3f second = new Vector3f(4, 0, 0);
        GeometryUtils.scaleAroundMidpoint(first, second, 0.5f);
        assertEquals(1.0f, first.x, 1e-6f);
        assertEquals(3.0f, second.x, 1e-6f);
    }

    @Test
    void normalizesTriangleWindingWithoutMutatingInputs() {
        Vector3f first = new Vector3f(0, 0, 0);
        Vector3f second = new Vector3f(0, 1, 0);
        Vector3f third = new Vector3f(1, 0, 0);

        Vector3f[] normalized = GeometryUtils.normalizeTriangleWinding(first, second, third);

        assertEquals(1.0f, new Vector3f(normalized[1]).sub(normalized[0])
                .cross(new Vector3f(normalized[2]).sub(normalized[0])).z, 1e-6f);
        assertEquals(0.0f, first.x, 1e-6f);
        assertEquals(1.0f, second.y, 1e-6f);
    }
}
