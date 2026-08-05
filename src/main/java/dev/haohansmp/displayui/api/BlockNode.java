package dev.haohansmp.displayui.api;

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
        float thickness
) implements UiNode {
    public BlockNode {
        Objects.requireNonNull(block, "block");
        if (width <= 0 || height <= 0 || thickness <= 0) {
            throw new IllegalArgumentException("block dimensions must be positive");
        }
        block = block.clone();
    }

    @Override
    public BlockData block() {
        return block.clone();
    }
}
