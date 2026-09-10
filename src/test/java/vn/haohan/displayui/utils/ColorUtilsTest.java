package vn.haohan.displayui.utils;

import org.bukkit.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorUtilsTest {
    @Test
    void appliesClampedOpacityToExistingAlpha() {
        Color color = Color.fromARGB(200, 10, 20, 30);
        Color result = ColorUtils.withOpacity(color, 0.5f);

        assertEquals(100, result.getAlpha());
        assertEquals(10, result.getRed());
        assertEquals(20, result.getGreen());
        assertEquals(30, result.getBlue());
    }

    @Test
    void interpolatesArgbChannelsProperly() {
        Color from = Color.fromARGB(0, 100, 50, 200);
        Color to = Color.fromARGB(200, 200, 150, 100);

        Color mid = ColorUtils.interpolate(from, to, 0.5f);
        assertEquals(100, mid.getAlpha());
        assertEquals(150, mid.getRed());
        assertEquals(100, mid.getGreen());
        assertEquals(150, mid.getBlue());

        Color start = ColorUtils.interpolate(from, to, 0.0f);
        assertEquals(from, start);

        Color end = ColorUtils.interpolate(from, to, 1.0f);
        assertEquals(to, end);

        // Clamping tests
        assertEquals(from, ColorUtils.interpolate(from, to, -0.5f));
        assertEquals(to, ColorUtils.interpolate(from, to, 1.5f));
    }
}
