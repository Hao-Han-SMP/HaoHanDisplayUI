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

import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public record ItemNode(
        ItemStack item,
        float x,
        float y,
        float depth,
        float scale,
        ItemDisplay.ItemDisplayTransform transform
) implements UiNode {
    public ItemNode {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(transform, "transform");
        if (item.getType().isAir()) throw new IllegalArgumentException("item cannot be air");
        if (scale <= 0.0f) throw new IllegalArgumentException("scale must be positive");
        item = item.clone();
    }

    @Override
    public ItemStack item() {
        return item.clone();
    }

    public static ItemNode fixed(ItemStack item, float x, float y, float scale) {
        return new ItemNode(item, x, y, 0.003f, scale, ItemDisplay.ItemDisplayTransform.FIXED);
    }
}
