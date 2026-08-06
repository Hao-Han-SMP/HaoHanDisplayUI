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
package dev.haohansmp.displayui.api.node;

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
