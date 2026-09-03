package vn.haohan.displayui.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathUtilsTest {
    @Test
    void clampsPrimitiveValues() {
        assertEquals(0, MathUtils.clamp(-1, 0, 10));
        assertEquals(10.0f, MathUtils.clamp(12.0f, 0.0f, 10.0f));
        assertEquals(0.5, MathUtils.clamp(0.5, 0.0, 1.0));
    }

    @Test
    void interpolatesAndRounds() {
        assertEquals(15.0f, MathUtils.lerp(10.0f, 20.0f, 0.5f));
        assertEquals(1.2, MathUtils.roundToStep(1.24, 0.1), 1e-9);
    }

    @Test
    void normalizesAndComparesAngles() {
        assertEquals(-170.0f, MathUtils.normalizeDegrees(190.0f));
        assertEquals(180.0f, MathUtils.normalizeDegrees(180.0f));
        assertEquals(20.0f, MathUtils.angleDifference(350.0f, 10.0f));
    }
}
