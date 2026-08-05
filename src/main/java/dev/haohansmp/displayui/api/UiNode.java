package dev.haohansmp.displayui.api;

/** One renderable node in a UI document. Coordinates are logical pixels. */
public sealed interface UiNode permits TextNode, AlignedTextNode, ItemNode, UiIconNode, BlockNode {
    float x();
    float y();
    float depth();
}
