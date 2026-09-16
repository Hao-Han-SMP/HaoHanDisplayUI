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
package vn.haohan.displayui.api.icon;

import vn.haohan.displayui.api.layout.UiRect;
import vn.haohan.displayui.api.node.UiIconNode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Central registry for managing and sharing reusable item icons across plugins,
 * supporting both vanilla items and custom items / CustomModelData from resource packs.
 */
public interface UiIconRegistry {
    /**
     * Registers a new icon with a factory supplier providing fresh {@link ItemStack} instances.
     *
     * @param owner the plugin that owns this icon registration
     * @param key unique {@link NamespacedKey} identifying the icon
     * @param itemFactory supplier function creating a new ItemStack whenever requested
     */
    void register(Plugin owner, NamespacedKey key, Supplier<ItemStack> itemFactory);

    /**
     * Registers a new icon using a template {@link ItemStack}.
     *
     * @param owner the plugin that owns this icon registration
     * @param key unique {@link NamespacedKey} identifying the icon
     * @param template template item stack (will be safely cloned)
     */
    default void register(Plugin owner, NamespacedKey key, ItemStack template) {
        if (template == null) throw new NullPointerException("template");
        ItemStack snapshot = template.clone();
        register(owner, key, snapshot::clone);
    }

    /**
     * Checks whether an icon with the given key is registered.
     *
     * @param key identifier key to check
     * @return {@code true} if the icon exists in the registry
     */
    boolean contains(NamespacedKey key);

    /**
     * Retrieves an unmodifiable set of all currently registered icon keys.
     *
     * @return set of registered {@link NamespacedKey} instances
     */
    Set<NamespacedKey> keys();

    /**
     * Creates a new {@link ItemStack} associated with the given icon key.
     *
     * @param key identifier key of the icon
     * @return an {@link Optional} containing the freshly instantiated ItemStack, or empty if key was not found
     */
    Optional<ItemStack> createItem(NamespacedKey key);

    /**
     * Creates a {@link UiIconNode} sized to the specified bounding box.
     *
     * @param key identifier key of the icon
     * @param bounds target position and size bounding box
     * @return a new {@link UiIconNode} instance
     */
    default UiIconNode createNode(NamespacedKey key, UiRect bounds) {
        return createNode(key, bounds, 0.003f,
                ItemDisplay.ItemDisplayTransform.FIXED);
    }

    /**
     * Creates a {@link UiIconNode} with custom depth and display transform mode.
     *
     * @param key identifier key of the icon
     * @param bounds target position and size bounding box
     * @param depth display depth Z-offset
     * @param transform item display transform mode {@link ItemDisplay.ItemDisplayTransform}
     * @return a new {@link UiIconNode} instance
     */
    default UiIconNode createNode(NamespacedKey key, UiRect bounds, float depth,
                                  ItemDisplay.ItemDisplayTransform transform) {
        ItemStack item = createItem(key).orElseThrow(() ->
                new IllegalArgumentException("unknown UI icon: " + key));
        return new UiIconNode(item, bounds, depth, 16.0f, 16.0f, transform);
    }

    /**
     * Unregisters a specific icon owned by the calling plugin.
     *
     * @param owner plugin owning the icon
     * @param key identifier key to unregister
     * @return {@code true} if the icon was successfully removed
     */
    boolean unregister(Plugin owner, NamespacedKey key);

    /**
     * Unregisters all icons owned by the specified plugin (typically invoked during onDisable).
     *
     * @param owner plugin owning the icons
     * @return the number of unregistered icons
     */
    int unregisterAll(Plugin owner);
}
