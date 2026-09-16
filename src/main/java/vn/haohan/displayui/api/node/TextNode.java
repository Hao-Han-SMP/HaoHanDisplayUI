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
package vn.haohan.displayui.api.node;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.TextDisplay;

import java.util.Objects;

/**
 * Raw Adventure text display node bound directly to a Minecraft TextDisplay entity.
 *
 * @param text        Adventure text component {@link Component}
 * @param x           horizontal coordinate in UI pixels
 * @param y           vertical coordinate in UI pixels
 * @param depth       Z-depth layer offset
 * @param lineWidth   line wrapping width in UI pixels (>= 1)
 * @param scale       uniform visual scale factor (scale > 0)
 * @param alignment   Bukkit TextDisplay text alignment (LEFT, RIGHT, CENTER)
 * @param shadow      whether text shadow rendering is enabled
 * @param seeThrough  whether text renders through obstructing blocks
 * @param doubleSided whether text is rendered on both front and back faces
 */
public record TextNode(
        Component text,
        float x,
        float y,
        float depth,
        int lineWidth,
        float scale,
        TextDisplay.TextAlignment alignment,
        boolean shadow,
        boolean seeThrough,
        boolean doubleSided
) implements UiNode {
    public TextNode {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(alignment, "alignment");
        if (lineWidth < 1) throw new IllegalArgumentException("lineWidth must be positive");
        if (scale <= 0.0f) throw new IllegalArgumentException("scale must be positive");
    }

    public TextNode(Component text, float x, float y, float depth, int lineWidth, float scale,
                    TextDisplay.TextAlignment alignment, boolean shadow, boolean seeThrough) {
        this(text, x, y, depth, lineWidth, scale, alignment, shadow, seeThrough, false);
    }

    /**
     * Convenience factory creating a left-aligned text node with standard defaults.
     *
     * @param text      text component
     * @param x         horizontal origin in UI pixels
     * @param y         vertical origin in UI pixels
     * @param lineWidth maximum line wrap width
     * @return a new left-aligned {@link TextNode}
     */
    public static TextNode left(Component text, float x, float y, int lineWidth) {
        return new TextNode(text, x, y, 0.002f, lineWidth, 0.5f,
                TextDisplay.TextAlignment.LEFT, true, false, false);
    }

    public TextNode withDoubleSided(boolean doubleSided) {
        return new TextNode(text, x, y, depth, lineWidth, scale, alignment, shadow, seeThrough, doubleSided);
    }

    public TextNode doubleSided(boolean doubleSided) {
        return withDoubleSided(doubleSided);
    }
}

