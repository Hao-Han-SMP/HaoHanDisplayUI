/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 */
package vn.haohan.displayui.api.animation;

import java.util.List;

/**
 * Collection of popular, production-ready preset animations for Display UI scenes.
 * <p>
 * Built on top of the public {@link UiAnimation} API, offering out-of-the-box micro-interactions
 * for menu transitions, page turns, popup modals, and button state feedback.
 */
public final class UiEffects {
    private UiEffects() { }

    /**
     * 12-tick fade-in animation using OutCubic easing.
     *
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation fadeIn() {
        return UiAnimation.fadeIn(12, Easings.OutCubic);
    }

    /**
     * Fade-in animation with custom duration.
     *
     * @param durationTicks duration in server ticks
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation fadeIn(int durationTicks) {
        return UiAnimation.fadeIn(durationTicks, Easings.OutCubic);
    }

    /**
     * 16-tick slide-in from the left over 28 pixels using OutCubic easing.
     *
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation slideInFromLeft() {
        return UiAnimation.slideIn(16, UiAnimation.Direction.LEFT, 28, Easings.OutCubic);
    }

    /**
     * 16-tick slide-in from the right over 28 pixels using OutCubic easing.
     *
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation slideInFromRight() {
        return UiAnimation.slideIn(16, UiAnimation.Direction.RIGHT, 28, Easings.OutCubic);
    }

    /**
     * 16-tick slide-in from the top over 22 pixels using OutCubic easing.
     *
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation slideInFromTop() {
        return UiAnimation.slideIn(16, UiAnimation.Direction.TOP, 22, Easings.OutCubic);
    }

    /**
     * 16-tick slide-in from the bottom over 22 pixels using OutCubic easing.
     *
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation slideInFromBottom() {
        return UiAnimation.slideIn(16, UiAnimation.Direction.BOTTOM, 22, Easings.OutCubic);
    }

    /**
     * 18-tick popping spring entrance using BackOut easing.
     *
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation popIn() {
        return UiAnimation.builder().durationTicks(18).easing(Easings.BackOut)
                .opacity(0.0f, 1.0f).scale(0.72f, 1.0f).build();
    }

    /**
     * Smooth scale-in entrance from 55% scale using OutCubic easing.
     *
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation scaleIn() {
        return scaleIn(0.55f, Easings.OutCubic);
    }

    /**
     * Scale-in entrance with customizable initial scale and easing.
     *
     * @param fromScale starting scale multiplier
     * @param easing    easing curve
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation scaleIn(float fromScale, Easings easing) {
        return UiAnimation.builder().durationTicks(16).easing(easing)
                .opacity(0.0f, 1.0f).scale(fromScale, 1.0f).build();
    }

    /**
     * 14-tick scale-out exit animation.
     *
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation scaleOut() {
        return UiAnimation.builder().durationTicks(14).easing(Easings.InCubic)
                .opacity(1.0f, 0.0f).scale(1.0f, 0.75f).build();
    }

    /**
     * 22-tick bounce-in entrance using BounceOut easing.
     *
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation bounceIn() {
        return UiAnimation.builder().durationTicks(22).easing(Easings.BounceOut)
                .opacity(0.0f, 1.0f).scale(0.8f, 1.0f)
                .offset(UiAnimation.Direction.TOP, 26).build();
    }

    /**
     * 20-tick gravity drop-in effect with floor bounce.
     *
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation dropIn() {
        return UiAnimation.builder().durationTicks(20).easing(Easings.BounceOut)
                .opacity(0.0f, 1.0f).offset(UiAnimation.Direction.TOP, 30).build();
    }

    /**
     * 18-tick soft upward rise using OutQuad easing.
     *
     * @return a new {@link UiAnimation} instance
     */
    public static UiAnimation softRise() {
        return UiAnimation.builder().durationTicks(18).easing(Easings.OutQuad)
                .opacity(0.0f, 1.0f).offset(UiAnimation.Direction.BOTTOM, 14).build();
    }

    /**
     * Attaches an execution delay to an existing animation preset.
     *
     * @param animation  base animation
     * @param delayTicks delay duration in server ticks
     * @return new {@link UiAnimation} copy with attached delay
     */
    public static UiAnimation delayed(UiAnimation animation, int delayTicks) {
        return animation.delay(delayTicks);
    }

    /**
     * Returns a collection of all standard animation presets suitable for demo galleries.
     *
     * @return list of preset {@link UiAnimation} instances
     */
    public static List<UiAnimation> gallery() {
        return List.of(fadeIn(), slideInFromLeft(), slideInFromRight(),
                slideInFromTop(), slideInFromBottom(), popIn(),
                scaleIn(), bounceIn(),
                dropIn(), softRise());
    }
}
