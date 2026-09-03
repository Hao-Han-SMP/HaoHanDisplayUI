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
package vn.haohan.displayui.api.interaction;

import vn.haohan.displayui.api.layout.UiRect;
import vn.haohan.displayui.utils.MathUtils;
import net.kyori.adventure.text.Component;

import java.util.Objects;

/** Horizontal continuous or stepped value control. */
public record UiSlider(
        String id,
        float x,
        float y,
        float width,
        float height,
        double minimum,
        double maximum,
        double value,
        double step,
        Component description,
        float hitSlop
) implements UiControl {
    public UiSlider {
        validateId(id);
        Objects.requireNonNull(description, "description");
        validateBounds(width, height);
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum >= maximum) {
            throw new IllegalArgumentException("slider minimum must be finite and lower than maximum");
        }
        if (!Double.isFinite(value)) throw new IllegalArgumentException("slider value must be finite");
        if (!Double.isFinite(step) || step < 0.0) {
            throw new IllegalArgumentException("slider step must be finite and non-negative");
        }
        if (!Float.isFinite(hitSlop) || hitSlop < 0.0f) {
            throw new IllegalArgumentException("slider hitSlop must be finite and non-negative");
        }
        value = snap(value, minimum, maximum, step);
    }

    public UiSlider(String id, float x, float y, float width, float height,
                    double minimum, double maximum, double value) {
        this(id, x, y, width, height, minimum, maximum, value, 0.0,
                Component.empty(), 0.0f);
    }

    public UiSlider(String id, float x, float y, float width, float height,
                    double minimum, double maximum, double value, double step,
                    Component description) {
        this(id, x, y, width, height, minimum, maximum, value, step,
                description, 0.0f);
    }

    public UiSlider withValue(double nextValue) {
        return new UiSlider(id, x, y, width, height, minimum, maximum,
                nextValue, step, description, hitSlop);
    }

    public UiSlider describedBy(Component nextDescription) {
        return new UiSlider(id, x, y, width, height, minimum, maximum,
                value, step, Objects.requireNonNull(nextDescription, "description"), hitSlop);
    }

    public UiSlider hitSlop(float pixels) {
        return new UiSlider(id, x, y, width, height, minimum, maximum,
                value, step, description, pixels);
    }

    /** Converts a local X coordinate to the nearest valid slider value. */
    public double valueAt(float localX) {
        double progress = MathUtils.clamp((localX - x) / (double) width, 0.0, 1.0);
        return snap(minimum + (maximum - minimum) * progress,
                minimum, maximum, step);
    }

    public double progress() {
        return (value - minimum) / (maximum - minimum);
    }

    /** The logical bounds developers can use to draw a track. */
    public UiRect trackRect() {
        return new UiRect(x, y, width, height);
    }

    /** A logical fill rectangle, sized from the current value. */
    public UiRect fillRect(float minimumWidth) {
        if (!Float.isFinite(minimumWidth) || minimumWidth < 0.0f) {
            throw new IllegalArgumentException("minimumWidth must be finite and non-negative");
        }
        return new UiRect(x, y, Math.max(minimumWidth, (float) (width * progress())), height);
    }

    /** Centers a thumb rectangle at the current value. */
    public UiRect thumbRect(float thumbWidth, float thumbHeight) {
        if (!Float.isFinite(thumbWidth) || !Float.isFinite(thumbHeight)
                || thumbWidth <= 0.0f || thumbHeight <= 0.0f) {
            throw new IllegalArgumentException("thumb dimensions must be positive and finite");
        }
        return UiRect.centered((float) (x + width * progress()),
                y + height * 0.5f, thumbWidth, thumbHeight);
    }

    private static double snap(double raw, double minimum, double maximum, double step) {
        double bounded = MathUtils.clamp(raw, minimum, maximum);
        if (step <= 0.0) return bounded;
        double snapped = minimum + MathUtils.roundToStep(bounded - minimum, step);
        return MathUtils.clamp(snapped, minimum, maximum);
    }

    private static void validateId(String id) {
        Objects.requireNonNull(id, "id");
        if (!id.matches("[a-z0-9_.-]+")) {
            throw new IllegalArgumentException("control id must contain only [a-z0-9_.-]");
        }
    }

    private static void validateBounds(float width, float height) {
        if (!Float.isFinite(width) || !Float.isFinite(height)
                || width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("control dimensions must be positive and finite");
        }
    }
}
