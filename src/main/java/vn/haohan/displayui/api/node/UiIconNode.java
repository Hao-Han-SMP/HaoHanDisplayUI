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
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * An item icon node rendered inside a UI panel with explicit source and display
 * dimensions.
 */
public record UiIconNode(
        ItemStack item,
        float boxX,
        float boxY,
        float depth,
        float width,
        float height,
        float uWidth,
        float vHeight,
        ItemDisplay.ItemDisplayTransform transform,
        boolean doubleSided
) implements UiNode {
    public UiIconNode {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(transform, "transform");
        if (item.getType().isAir()) throw new IllegalArgumentException("icon item cannot be air");
        if (width <= 0 || height <= 0 || uWidth <= 0 || vHeight <= 0) {
            throw new IllegalArgumentException("icon dimensions must be positive");
        }
        item = item.clone();
    }

    public UiIconNode(ItemStack item, float x, float y, float width, float height,
                      float uWidth, float vHeight) {
        this(item, x, y, 0.003f, width, height, uWidth, vHeight,
                ItemDisplay.ItemDisplayTransform.FIXED, false);
    }

    public UiIconNode(ItemStack item, UiRect bounds, float uWidth, float vHeight) {
        this(item, bounds, 0.003f, uWidth, vHeight,
                ItemDisplay.ItemDisplayTransform.FIXED);
    }

    public UiIconNode(ItemStack item, UiRect bounds, float depth,
                      float uWidth, float vHeight,
                      ItemDisplay.ItemDisplayTransform transform) {
        this(item, Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), uWidth, vHeight, transform, false);
    }

    public UiIconNode(ItemStack item, float boxX, float boxY, float depth,
                      float width, float height, float uWidth, float vHeight,
                      ItemDisplay.ItemDisplayTransform transform) {
        this(item, boxX, boxY, depth, width, height, uWidth, vHeight, transform, false);
    }

    @Override public ItemStack item() { return item.clone(); }
    @Override public float x() { return boxX + width * 0.5f; }
    @Override public float y() { return boxY + height * 0.5f; }

    public float right() { return boxX + width; }
    public float bottom() { return boxY + height; }
    public float scaleU() { return width / uWidth; }
    public float scaleV() { return height / vHeight; }

    public UiIconNode withDoubleSided(boolean doubleSided) {
        return new UiIconNode(item, boxX, boxY, depth, width, height, uWidth, vHeight, transform, doubleSided);
    }

    public UiIconNode doubleSided(boolean doubleSided) {
        return withDoubleSided(doubleSided);
    }

    public UiIconNode withDimensions(float width, float height) {
        return new UiIconNode(item, boxX, boxY, depth, width, height, uWidth, vHeight, transform, doubleSided);
    }

    public UiIconNode withSource(float uWidth, float vHeight) {
        return new UiIconNode(item, boxX, boxY, depth, width, height, uWidth, vHeight, transform, doubleSided);
    }

    public UiIconNode withTransform(ItemDisplay.ItemDisplayTransform transform) {
        return new UiIconNode(item, boxX, boxY, depth, width, height, uWidth, vHeight, transform, doubleSided);
    }
}
