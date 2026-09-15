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

/**
 * Interactive slider control supporting continuous or stepped numeric adjustment.
 * <p>
 * Enables players to adjust a double value within a bounded range [{@code minimum}, {@code maximum}].
 *
 * @param id          unique control identifier
 * @param x           top-left X coordinate in UI pixels
 * @param y           top-left Y coordinate in UI pixels
 * @param width       slider width in UI pixels
 * @param height      slider height in UI pixels
 * @param minimum     lower numeric bound
 * @param maximum     upper numeric bound
 * @param value       current value
 * @param step        stepping increment (0.0 for continuous smooth sliding)
 * @param description tooltip or label component
 * @param hitSlop     expanded raycast hit margin in UI pixels
 */
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

    /**
     * Constructs a basic continuous slider.
     *
     * @param id      unique control identifier
     * @param x       top-left X in UI pixels
     * @param y       top-left Y in UI pixels
     * @param width   width in UI pixels
     * @param height  height in UI pixels
     * @param minimum lower numeric bound
     * @param maximum upper numeric bound
     * @param value   initial value
     */
    public UiSlider(String id, float x, float y, float width, float height,
                    double minimum, double maximum, double value) {
        this(id, x, y, width, height, minimum, maximum, value, 0.0,
                Component.empty(), 0.0f);
    }

    /**
     * Constructs a slider with step snapping and description component.
     *
     * @param id          unique control identifier
     * @param x           top-left X in UI pixels
     * @param y           top-left Y in UI pixels
     * @param width       width in UI pixels
     * @param height      height in UI pixels
     * @param minimum     lower numeric bound
     * @param maximum     upper numeric bound
     * @param value       initial value
     * @param step        stepping increment
     * @param description tooltip or label component
     */
    public UiSlider(String id, float x, float y, float width, float height,
                    double minimum, double maximum, double value, double step,
                    Component description) {
        this(id, x, y, width, height, minimum, maximum, value, step,
                description, 0.0f);
    }

    /**
     * Creates a copy of this slider with a new value (snapped to step and clamped).
     *
     * @param nextValue new numeric value
     * @return a new {@link UiSlider} instance
     */
    public UiSlider withValue(double nextValue) {
        return new UiSlider(id, x, y, width, height, minimum, maximum,
                nextValue, step, description, hitSlop);
    }

    /**
     * Creates a copy of this slider with an updated description component.
     *
     * @param nextDescription new description component
     * @return a new {@link UiSlider} instance
     */
    public UiSlider describedBy(Component nextDescription) {
        return new UiSlider(id, x, y, width, height, minimum, maximum,
                value, step, Objects.requireNonNull(nextDescription, "description"), hitSlop);
    }

    /**
     * Creates a copy of this slider with modified raycast hit slop margin.
     *
     * @param pixels hit margin expansion in UI pixels
     * @return a new {@link UiSlider} instance
     */
    public UiSlider hitSlop(float pixels) {
        return new UiSlider(id, x, y, width, height, minimum, maximum,
                value, step, description, pixels);
    }

    /**
     * Computes the numeric value corresponding to a local X click coordinate.
     *
     * @param localX local canvas X coordinate in UI pixels
     * @return snapped and clamped slider value
     */
    public double valueAt(float localX) {
        double progress = MathUtils.clamp((localX - x) / (double) width, 0.0, 1.0);
        return snap(minimum + (maximum - minimum) * progress,
                minimum, maximum, step);
    }

    /**
     * Returns the normalized progress ratio of the current value between 0.0 and 1.0.
     *
     * @return normalized progress ratio
     */
    public double progress() {
        return (value - minimum) / (maximum - minimum);
    }

    /**
     * Returns the bounding layout rectangle {@link UiRect} of the slider track.
     *
     * @return track {@link UiRect}
     */
    public UiRect trackRect() {
        return new UiRect(x, y, width, height);
    }

    /**
     * Returns the bounding layout rectangle {@link UiRect} representing the filled portion of the track.
     *
     * @param minimumWidth minimum rendered fill width in UI pixels
     * @return fill {@link UiRect}
     * @throws IllegalArgumentException if {@code minimumWidth < 0}
     */
    public UiRect fillRect(float minimumWidth) {
        if (!Float.isFinite(minimumWidth) || minimumWidth < 0.0f) {
            throw new IllegalArgumentException("minimumWidth must be finite and non-negative");
        }
        return new UiRect(x, y, Math.max(minimumWidth, (float) (width * progress())), height);
    }

    /**
     * Returns the centered bounding layout rectangle {@link UiRect} of the draggable slider thumb.
     *
     * @param thumbWidth  thumb width in UI pixels
     * @param thumbHeight thumb height in UI pixels
     * @return thumb {@link UiRect}
     * @throws IllegalArgumentException if dimensions are non-positive or non-finite
     */
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
