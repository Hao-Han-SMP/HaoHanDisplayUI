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
package dev.haohansmp.displayui.runtime;

import dev.haohansmp.displayui.api.icon.UiIconRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

final class UiIconRegistryImpl implements UiIconRegistry {
    private final Map<NamespacedKey, Entry> entries = new LinkedHashMap<>();

    @Override
    public void register(Plugin owner, NamespacedKey key, Supplier<ItemStack> itemFactory) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(itemFactory, "itemFactory");
        validItem(itemFactory, key);
        Entry existing = entries.get(key);
        if (existing != null && existing.owner() != owner) {
            throw new IllegalStateException("UI icon already registered by "
                    + existing.owner().getName() + ": " + key);
        }
        entries.put(key, new Entry(owner, itemFactory));
    }

    @Override
    public boolean contains(NamespacedKey key) {
        return entries.containsKey(Objects.requireNonNull(key, "key"));
    }

    @Override
    public Set<NamespacedKey> keys() {
        return Set.copyOf(entries.keySet());
    }

    @Override
    public Optional<ItemStack> createItem(NamespacedKey key) {
        Entry entry = entries.get(Objects.requireNonNull(key, "key"));
        if (entry == null) return Optional.empty();
        return Optional.of(validItem(entry.itemFactory(), key));
    }

    @Override
    public boolean unregister(Plugin owner, NamespacedKey key) {
        Objects.requireNonNull(owner, "owner");
        Entry entry = entries.get(Objects.requireNonNull(key, "key"));
        if (entry == null || entry.owner() != owner) return false;
        entries.remove(key);
        return true;
    }

    @Override
    public int unregisterAll(Plugin owner) {
        Objects.requireNonNull(owner, "owner");
        int before = entries.size();
        entries.entrySet().removeIf(entry -> entry.getValue().owner() == owner);
        return before - entries.size();
    }

    void clear() {
        entries.clear();
    }

    private ItemStack validItem(Supplier<ItemStack> factory, NamespacedKey key) {
        ItemStack item = factory.get();
        if (item == null || item.getType().isAir()) {
            throw new IllegalArgumentException("UI icon factory returned null/air: " + key);
        }
        return item.clone();
    }

    private record Entry(Plugin owner, Supplier<ItemStack> itemFactory) {}
}
