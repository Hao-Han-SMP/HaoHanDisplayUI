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
package vn.haohan.displayui.api.animation;

/** Common easing curves for UI animations. */
public enum UiEasing {
    /** Constant speed. */
    LINEAR,
    /** Starts slowly and accelerates. */
    EASE_IN,
    /** Starts quickly and decelerates. */
    EASE_OUT,
    /** Accelerates and then decelerates. */
    EASE_IN_OUT,
    QUAD_IN,
    QUAD_OUT,
    QUAD_IN_OUT,
    CUBIC_IN,
    CUBIC_OUT,
    CUBIC_IN_OUT,
    /** Slightly overshoots the target for a soft pop. */
    BACK_OUT,
    /** A spring-like overshoot. */
    ELASTIC_OUT;

    /**
     * Maps a normalized linear progress value to this curve.
     * Values outside {@code [0, 1]} are clamped for safe use by callers.
     */
    public double apply(double progress) {
        double t = Math.max(0.0, Math.min(1.0, progress));
        return switch (this) {
            case LINEAR -> t;
            case EASE_IN, CUBIC_IN -> t * t * t;
            case EASE_OUT, CUBIC_OUT -> 1.0 - Math.pow(1.0 - t, 3.0);
            case EASE_IN_OUT, CUBIC_IN_OUT -> t < 0.5
                    ? 4.0 * t * t * t
                    : 1.0 - Math.pow(-2.0 * t + 2.0, 3.0) / 2.0;
            case QUAD_IN -> t * t;
            case QUAD_OUT -> 1.0 - (1.0 - t) * (1.0 - t);
            case QUAD_IN_OUT -> t < 0.5
                    ? 2.0 * t * t
                    : 1.0 - Math.pow(-2.0 * t + 2.0, 2.0) / 2.0;
            case BACK_OUT -> {
                double c1 = 1.70158;
                double c3 = c1 + 1.0;
                yield 1.0 + c3 * Math.pow(t - 1.0, 3.0)
                        + c1 * Math.pow(t - 1.0, 2.0);
            }
            case ELASTIC_OUT -> {
                if (t == 0.0 || t == 1.0) yield t;
                yield Math.pow(2.0, -10.0 * t)
                        * Math.sin((t * 10.0 - 0.75) * (2.0 * Math.PI / 3.0)) + 1.0;
            }
        };
    }
}
