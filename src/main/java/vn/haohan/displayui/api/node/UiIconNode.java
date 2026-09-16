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
 * An item icon node rendered inside the UI canvas with distinct source UV coordinates
 * and target viewport dimensions.
 *
 * @param item        the {@link ItemStack} to render as an icon
 * @param boxX        the top-left X coordinate of the container bounding box (pixels)
 * @param boxY        the top-left Y coordinate of the container bounding box (pixels)
 * @param depth       Z-depth layer offset
 * @param width       viewport display width on the UI canvas (pixels)
 * @param height      viewport display height on the UI canvas (pixels)
 * @param uWidth      source UV texture width
 * @param vHeight     source UV texture height
 * @param transform   Minecraft {@link ItemDisplay.ItemDisplayTransform} transformation mode
 * @param doubleSided whether back faces are rendered
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

    /**
     * Constructs an icon with default depth (0.003f), FIXED transform, and single-sided rendering.
     */
    public UiIconNode(ItemStack item, float x, float y, float width, float height,
                      float uWidth, float vHeight) {
        this(item, x, y, 0.003f, width, height, uWidth, vHeight,
                ItemDisplay.ItemDisplayTransform.FIXED, false);
    }

    /**
     * Constructs an icon fitted to bounding rectangle {@link UiRect}.
     */
    public UiIconNode(ItemStack item, UiRect bounds, float uWidth, float vHeight) {
        this(item, bounds, 0.003f, uWidth, vHeight,
                ItemDisplay.ItemDisplayTransform.FIXED);
    }

    /**
     * Constructs an icon fitted to bounding rectangle {@link UiRect} with custom depth and transform.
     */
    public UiIconNode(ItemStack item, UiRect bounds, float depth,
                      float uWidth, float vHeight,
                      ItemDisplay.ItemDisplayTransform transform) {
        this(item, Objects.requireNonNull(bounds, "bounds").x(), bounds.y(), depth,
                bounds.width(), bounds.height(), uWidth, vHeight, transform, false);
    }

    /**
     * Constructs an icon with full parameters and single-sided rendering (doubleSided = false).
     */
    public UiIconNode(ItemStack item, float boxX, float boxY, float depth,
                      float width, float height, float uWidth, float vHeight,
                      ItemDisplay.ItemDisplayTransform transform) {
        this(item, boxX, boxY, depth, width, height, uWidth, vHeight, transform, false);
    }

    /** Returns a defensive copy of the ItemStack. */
    @Override public ItemStack item() { return item.clone(); }

    /** Returns center X coordinate of the icon. */
    @Override public float x() { return boxX + width * 0.5f; }
    /** Returns center Y coordinate of the icon. */
    @Override public float y() { return boxY + height * 0.5f; }

    /** Returns the right boundary coordinate (boxX + width). */
    public float right() { return boxX + width; }
    /** Returns the bottom boundary coordinate (boxY + height). */
    public float bottom() { return boxY + height; }
    /** Returns the horizontal scaling factor (width / uWidth). */
    public float scaleU() { return width / uWidth; }
    /** Returns the vertical scaling factor (height / vHeight). */
    public float scaleV() { return height / vHeight; }

    /**
     * Implementation from {@link UiNode#withDoubleSided(boolean)}.
     */
    public UiIconNode withDoubleSided(boolean doubleSided) {
        return new UiIconNode(item, boxX, boxY, depth, width, height, uWidth, vHeight, transform, doubleSided);
    }

    /**
     * Fluent alias for {@link #withDoubleSided(boolean)}.
     */
    public UiIconNode doubleSided(boolean doubleSided) {
        return withDoubleSided(doubleSided);
    }

    /**
     * Returns a copy with updated display width and height dimensions.
     */
    public UiIconNode withDimensions(float width, float height) {
        return new UiIconNode(item, boxX, boxY, depth, width, height, uWidth, vHeight, transform, doubleSided);
    }

    /**
     * Returns a copy with updated source UV width and height.
     */
    public UiIconNode withSource(float uWidth, float vHeight) {
        return new UiIconNode(item, boxX, boxY, depth, width, height, uWidth, vHeight, transform, doubleSided);
    }

    /**
     * Returns a copy with an updated Minecraft ItemDisplay transform mode.
     */
    public UiIconNode withTransform(ItemDisplay.ItemDisplayTransform transform) {
        return new UiIconNode(item, boxX, boxY, depth, width, height, uWidth, vHeight, transform, doubleSided);
    }
}
