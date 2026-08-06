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
package dev.haohansmp.displayui.api.icon;

import dev.haohansmp.displayui.api.layout.UiRect;
import dev.haohansmp.displayui.api.node.UiIconNode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/** Shared registry for reusable vanilla or resource-pack-backed item icons. */
public interface UiIconRegistry {
    void register(Plugin owner, NamespacedKey key, Supplier<ItemStack> itemFactory);

    default void register(Plugin owner, NamespacedKey key, ItemStack template) {
        if (template == null) throw new NullPointerException("template");
        ItemStack snapshot = template.clone();
        register(owner, key, snapshot::clone);
    }

    boolean contains(NamespacedKey key);
    Set<NamespacedKey> keys();
    Optional<ItemStack> createItem(NamespacedKey key);

    default UiIconNode createNode(NamespacedKey key, UiRect bounds) {
        return createNode(key, bounds, 0.003f,
                ItemDisplay.ItemDisplayTransform.FIXED);
    }

    default UiIconNode createNode(NamespacedKey key, UiRect bounds, float depth,
                                  ItemDisplay.ItemDisplayTransform transform) {
        ItemStack item = createItem(key).orElseThrow(() ->
                new IllegalArgumentException("unknown UI icon: " + key));
        return new UiIconNode(item, bounds, depth, 16.0f, 16.0f, transform);
    }

    boolean unregister(Plugin owner, NamespacedKey key);
    int unregisterAll(Plugin owner);
}
