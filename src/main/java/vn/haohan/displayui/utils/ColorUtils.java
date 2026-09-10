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

    public static Color interpolate(Color from, Color to, float progress) {
        float t = MathUtils.clamp(progress, 0.0f, 1.0f);
        int alpha = Math.round(from.getAlpha() + (to.getAlpha() - from.getAlpha()) * t);
        int red = Math.round(from.getRed() + (to.getRed() - from.getRed()) * t);
        int green = Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * t);
        int blue = Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * t);
        return Color.fromARGB(
                MathUtils.clamp(alpha, 0, 255),
                MathUtils.clamp(red, 0, 255),
                MathUtils.clamp(green, 0, 255),
                MathUtils.clamp(blue, 0, 255)
        );
    }
}
