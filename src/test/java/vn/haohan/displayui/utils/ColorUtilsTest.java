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
}
