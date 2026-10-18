/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package vn.haohan.displayui.api.component;

import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import vn.haohan.displayui.api.layout.UiAnchorPoint;

import java.util.Objects;

/**
 * Component representing an in-world item icon or material display inside a {@link vn.haohan.displayui.api.container.Container}.
 * Fully integrated into the 9-direction anchor/origin layout and layer depth system.
 */
public class IconComponent extends AbstractComponent {

    private ItemStack itemStack;
    private Material material = Material.AIR;
    private ItemDisplay.ItemDisplayTransform transform = ItemDisplay.ItemDisplayTransform.FIXED;
    private boolean doubleSided = false;

    public IconComponent(String id) {
        super(id);
        this.width = 16.0f;
        this.height = 16.0f;
    }

    public IconComponent(String id, ItemStack itemStack) {
        this(id);
        this.itemStack = itemStack;
        if (itemStack != null) {
            this.material = itemStack.getType();
        }
    }

    public IconComponent(String id, Material material) {
        this(id);
        this.material = material != null ? material : Material.AIR;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public ItemStack itemStack() {
        return itemStack;
    }

    public IconComponent itemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        if (itemStack != null) {
            this.material = itemStack.getType();
        }
        return this;
    }

    public Material material() {
        if (material != null) return material;
        if (itemStack != null) return itemStack.getType();
        return Material.AIR;
    }

    public IconComponent material(Material material) {
        this.material = material != null ? material : Material.AIR;
        return this;
    }

    public ItemStack resolveItemStack() {
        if (itemStack != null) return itemStack;
        if (material != null && material != Material.AIR) {
            try {
                return new ItemStack(material);
            } catch (Throwable ignored) {
                return null;
            }
        }
        return null;
    }

    public ItemDisplay.ItemDisplayTransform transform() {
        return transform;
    }

    public IconComponent transform(ItemDisplay.ItemDisplayTransform transform) {
        this.transform = Objects.requireNonNullElse(transform, ItemDisplay.ItemDisplayTransform.FIXED);
        return this;
    }

    public boolean doubleSided() {
        return doubleSided;
    }

    public IconComponent doubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
        return this;
    }

    public static final class Builder {
        private final String id;
        private ItemStack itemStack;
        private Material material = Material.AIR;
        private UiAnchorPoint anchor = UiAnchorPoint.CENTER;
        private UiAnchorPoint origin = UiAnchorPoint.CENTER;
        private float x = 0.0f;
        private float y = 0.0f;
        private float width = 16.0f;
        private float height = 16.0f;
        private float scale = 1.0f;
        private ItemDisplay.ItemDisplayTransform transform = ItemDisplay.ItemDisplayTransform.FIXED;
        private boolean doubleSided = false;

        public Builder(String id) {
            this.id = id;
        }

        public Builder item(ItemStack itemStack) {
            this.itemStack = itemStack;
            if (itemStack != null) {
                this.material = itemStack.getType();
            }
            return this;
        }

        public Builder material(Material material) {
            this.material = material != null ? material : Material.AIR;
            return this;
        }

        public Builder anchor(UiAnchorPoint anchor) {
            this.anchor = anchor;
            return this;
        }

        public Builder origin(UiAnchorPoint origin) {
            this.origin = origin;
            return this;
        }

        public Builder offset(float x, float y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder transform(ItemDisplay.ItemDisplayTransform transform) {
            this.transform = transform;
            return this;
        }

        public Builder doubleSided(boolean doubleSided) {
            this.doubleSided = doubleSided;
            return this;
        }

        public IconComponent build() {
            IconComponent comp = new IconComponent(id);
            if (itemStack != null) {
                comp.itemStack(itemStack);
            } else {
                comp.material(material);
            }
            comp.anchor(anchor);
            comp.origin(origin);
            comp.offset(x, y);
            comp.size(width, height);
            comp.scale(scale);
            comp.transform(transform);
            comp.doubleSided(doubleSided);
            return comp;
        }
    }
}
