package dev.haohansmp.displayui.api;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.TextDisplay;

import java.util.Objects;

public record TextNode(
        Component text,
        float x,
        float y,
        float depth,
        int lineWidth,
        float scale,
        TextDisplay.TextAlignment alignment,
        boolean shadow,
        boolean seeThrough
) implements UiNode {
    public TextNode {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(alignment, "alignment");
        if (lineWidth < 1) throw new IllegalArgumentException("lineWidth must be positive");
        if (scale <= 0.0f) throw new IllegalArgumentException("scale must be positive");
    }

    public static TextNode left(Component text, float x, float y, int lineWidth) {
        return new TextNode(text, x, y, 0.002f, lineWidth, 0.5f,
                TextDisplay.TextAlignment.LEFT, true, false);
    }
}
