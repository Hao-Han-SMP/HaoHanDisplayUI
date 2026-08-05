package dev.haohansmp.displayui.api;

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
