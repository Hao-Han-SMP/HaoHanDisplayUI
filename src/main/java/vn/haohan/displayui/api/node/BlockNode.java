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

import vn.haohan.displayui.api.layout.UiRect;
import org.bukkit.block.data.BlockData;

import java.util.Objects;

/** A block-model layer; useful for panels that do not rely on font glyphs. */
public record BlockNode(
        BlockData block,
        float x,
        float y,
        float depth,
        float width,
        float height,
        float thickness,
        boolean doubleSided
) implements UiNode {
    public BlockNode {
        Objects.requireNonNull(block, "block");
        if (width <= 0 || height <= 0 || thickness <= 0) {
            throw new IllegalArgumentException("block dimensions must be positive");
        }
        block = block.clone();
    }

    public BlockNode(BlockData block, float x, float y, float depth,
                     float width, float height, float thickness) {
        this(block, x, y, depth, width, height, thickness, false);
    }

    @Override
    public BlockData block() {
        return block.clone();
    }

    public BlockNode(BlockData block, UiRect bounds, float depth, float thickness) {
        this(block, Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), thickness, false);
    }

    public BlockNode(BlockData block, UiRect bounds, float depth, float thickness, boolean doubleSided) {
        this(block, Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), thickness, doubleSided);
    }

    @Override
    public BlockNode withDoubleSided(boolean doubleSided) {
        return new BlockNode(block, x, y, depth, width, height, thickness, doubleSided);
    }
}
