/* Copyright (C) 2026 HaoHanSMP */
package vn.haohan.displayui.runtime.scene;

import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.utils.TimerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Owns scene animation state and advances animation time. */
final class UiSceneAnimationController {
    private final UiScene scene;
    private final TimerUtils clock = new TimerUtils();
    private UiAnimation animation;
    private double animationAgeTicks;
    private List<UiAnimation> nodeAnimations = List.of();
    private double[] nodeAnimationAgesTicks = new double[0];

    UiSceneAnimationController(UiScene scene) {
        this.scene = scene;
    }

    UiAnimation animation() { return animation; }
    List<UiAnimation> nodeAnimations() { return nodeAnimations; }
    double animationAgeTicks() { return animationAgeTicks; }
    double[] nodeAnimationAgesTicks() { return nodeAnimationAgesTicks; }

    void start(UiAnimation next) {
        animation = Objects.requireNonNull(next, "animation");
        nodeAnimations = List.of();
        nodeAnimationAgesTicks = new double[0];
        animationAgeTicks = 0.0;
        clock.reset();
        scene.applyAnimationFrame(0.0f, 0);
        scene.configureAnimationInterpolation();
    }

    void startNodes(List<UiAnimation> animations, int nodeCount) {
        Objects.requireNonNull(animations, "animations");
        List<UiAnimation> adapted;
        if (animations.size() == nodeCount) {
            adapted = List.copyOf(animations);
        } else if (animations.isEmpty()) {
            adapted = List.of();
        } else {
            List<UiAnimation> list = new ArrayList<>(nodeCount);
            for (int i = 0; i < nodeCount; i++) {
                list.add(i < animations.size() ? animations.get(i) : animations.get(animations.size() - 1));
            }
            adapted = List.copyOf(list);
        }
        nodeAnimations = adapted;
        nodeAnimationAgesTicks = new double[adapted.size()];
        animation = null;
        animationAgeTicks = 0.0;
        clock.reset();
        scene.applyNodeAnimationFrames(0);
        scene.configureAnimationInterpolation();
    }

    void stop() {
        if (scene.isRemoved() || (animation == null && nodeAnimations.isEmpty())) return;
        if (animation != null) scene.applyAnimation(1.0);
        if (!nodeAnimations.isEmpty()) {
            for (int i = 0; i < nodeAnimations.size(); i++) {
                if (!nodeAnimations.get(i).isStatic()) {
                    scene.applyAnimationToNode(i, nodeAnimations.get(i), 1.0);
                }
            }
        }
        animation = null;
        nodeAnimations = List.of();
        nodeAnimationAgesTicks = new double[0];
        animationAgeTicks = 0.0;
        clock.clear();
    }

    boolean isAnimating() {
        return !scene.isRemoved() && (animation != null || !nodeAnimations.isEmpty());
    }

    void tick() {
        if (animation != null) {
            double deltaTicks = clock.advance();
            animationAgeTicks += deltaTicks;
            double effectiveAge = animationAgeTicks - animation.delayTicks();
            if (effectiveAge < 0.0) return;
            if (effectiveAge >= animation.durationTicks()) {
                scene.applyAnimationFrame(1.0f);
                animation = null;
                animationAgeTicks = 0.0;
            } else {
                float progress = (float) (effectiveAge / animation.durationTicks());
                scene.applyAnimationFrame((float) animation.easing().apply(progress));
            }
            return;
        }

        if (nodeAnimations.isEmpty()) return;
        double deltaTicks = clock.advance();
        boolean anyRunning = false;
        for (int i = 0; i < nodeAnimations.size(); i++) {
            UiAnimation current = nodeAnimations.get(i);
            if (current.durationTicks() <= 0) continue;
            nodeAnimationAgesTicks[i] += deltaTicks;
            double effectiveAge = nodeAnimationAgesTicks[i] - current.delayTicks();
            if (effectiveAge < current.durationTicks()) anyRunning = true;
        }
        scene.applyNodeAnimationFrames();
        if (!anyRunning) {
            nodeAnimations = List.of();
            nodeAnimationAgesTicks = new double[0];
        }
    }
}
