/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.utils;

import org.bukkit.Color;

/** Color transformations shared by display rendering code. */
public final class ColorUtils {
    private ColorUtils() { throw new AssertionError("utility class"); }

    public static Color withOpacity(Color color, float opacity) {
        int alpha = Math.round(color.getAlpha() * MathUtils.clamp(opacity, 0.0f, 1.0f));
        return Color.fromARGB(alpha, color.getRed(), color.getGreen(), color.getBlue());
    }
}
