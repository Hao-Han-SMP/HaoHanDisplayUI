/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 */
package vn.haohan.displayui.api.animation;

import java.util.List;

/**
 * Named, reusable animation presets for common display UI effects.
 *
 * <p>Presets are deliberately built from the public {@link UiAnimation} API,
 * so consumers can use them without depending on runtime classes. A preset is
 * an entry animation; looping or sequencing can be composed by the consumer
 * with repeated calls to {@code UiHandle.animate}.</p>
 */
public final class UiEffects {
    private UiEffects() { }

    public static UiAnimation fadeIn() {
        return UiAnimation.fadeIn(12, Easings.OutCubic);
    }

    public static UiAnimation fadeIn(int durationTicks) {
        return UiAnimation.fadeIn(durationTicks, Easings.OutCubic);
    }

    public static UiAnimation slideInFromLeft() {
        return UiAnimation.slideIn(16, UiAnimation.Direction.LEFT, 28, Easings.OutCubic);
    }

    public static UiAnimation slideInFromRight() {
        return UiAnimation.slideIn(16, UiAnimation.Direction.RIGHT, 28, Easings.OutCubic);
    }

    public static UiAnimation slideInFromTop() {
        return UiAnimation.slideIn(16, UiAnimation.Direction.TOP, 22, Easings.OutCubic);
    }

    public static UiAnimation slideInFromBottom() {
        return UiAnimation.slideIn(16, UiAnimation.Direction.BOTTOM, 22, Easings.OutCubic);
    }

    public static UiAnimation popIn() {
        return UiAnimation.builder().durationTicks(18).easing(Easings.BackOut)
                .opacity(0.0f, 1.0f).scale(0.72f, 1.0f).build();
    }

    public static UiAnimation scaleIn() {
        return scaleIn(0.55f, Easings.OutCubic);
    }

    public static UiAnimation scaleIn(float fromScale, Easings easing) {
        return UiAnimation.builder().durationTicks(16).easing(easing)
                .opacity(0.0f, 1.0f).scale(fromScale, 1.0f).build();
    }

    public static UiAnimation scaleOut() {
        return UiAnimation.builder().durationTicks(14).easing(Easings.InCubic)
                .opacity(1.0f, 0.0f).scale(1.0f, 0.75f).build();
    }

    public static UiAnimation bounceIn() {
        return UiAnimation.builder().durationTicks(22).easing(Easings.BounceOut)
                .opacity(0.0f, 1.0f).scale(0.8f, 1.0f)
                .offset(UiAnimation.Direction.TOP, 26).build();
    }

    public static UiAnimation dropIn() {
        return UiAnimation.builder().durationTicks(20).easing(Easings.BounceOut)
                .opacity(0.0f, 1.0f).offset(UiAnimation.Direction.TOP, 30).build();
    }

    public static UiAnimation softRise() {
        return UiAnimation.builder().durationTicks(18).easing(Easings.OutQuad)
                .opacity(0.0f, 1.0f).offset(UiAnimation.Direction.BOTTOM, 14).build();
    }

    public static UiAnimation delayed(UiAnimation animation, int delayTicks) {
        return animation.delay(delayTicks);
    }

    /** Presets used by the built-in demo gallery, in stable display order. */
    public static List<UiAnimation> gallery() {
        return List.of(fadeIn(), slideInFromLeft(), slideInFromRight(),
                slideInFromTop(), slideInFromBottom(), popIn(),
                scaleIn(), bounceIn(),
                dropIn(), softRise());
    }
}
