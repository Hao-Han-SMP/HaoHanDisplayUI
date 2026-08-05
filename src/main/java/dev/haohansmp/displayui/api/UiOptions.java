package dev.haohansmp.displayui.api;

/** Runtime behavior shared by every node in a display group. */
public record UiOptions(
        float pixelsPerBlock,
        double maxDistance,
        boolean requireFront,
        float viewRange,
        String scoreboardTag,
        UiCameraTransform cameraTransform
) {
    public UiOptions {
        if (pixelsPerBlock <= 0.0f) throw new IllegalArgumentException("pixelsPerBlock must be positive");
        if (maxDistance <= 0.0) throw new IllegalArgumentException("maxDistance must be positive");
        if (viewRange <= 0.0f) throw new IllegalArgumentException("viewRange must be positive");
        if (scoreboardTag == null || scoreboardTag.isBlank()) scoreboardTag = "haohan_display_ui";
        if (cameraTransform == null) cameraTransform = UiCameraTransform.fixed();
    }

    public UiOptions(float pixelsPerBlock, double maxDistance, boolean requireFront,
                     float viewRange, String scoreboardTag) {
        this(pixelsPerBlock, maxDistance, requireFront, viewRange, scoreboardTag,
                UiCameraTransform.fixed());
    }

    public UiOptions withCameraTransform(UiCameraTransform transform) {
        return new UiOptions(pixelsPerBlock, maxDistance, requireFront, viewRange,
                scoreboardTag, transform);
    }

    public static UiOptions defaults() {
        return new UiOptions(40.0f, 8.0, true, 0.15f, "haohan_display_ui",
                UiCameraTransform.fixed());
    }
}
