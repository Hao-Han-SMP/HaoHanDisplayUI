package dev.haohansmp.displayui.api;

/**
 * Manual optical X corrections for Minecraft text whose visible glyph edge
 * differs from its measured layout edge.
 */
public enum UiTextOpticalPreset {
    ITALIC(-1.0f),
    PLAIN(0.0f),
    GRADIENT(1.0f),
    BOLD(2.0f),
    BOLD_GRADIENT(3.0f);

    private final float xOffset;

    UiTextOpticalPreset(float xOffset) {
        this.xOffset = xOffset;
    }

    public float xOffset() {
        return xOffset;
    }
}
