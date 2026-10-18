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
package vn.haohan.displayui.api.container;

import org.bukkit.Color;
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.api.layout.UiAnchorPoint;

import java.util.Objects;

/**
 * A specialized scrollable container that clips its children to a viewport
 * and provides smooth pixel-based scrolling with an optional auto-rendered scrollbar.
 */
public class ScrollContainer extends Container {

    private float scrollOffset = 0.0f;
    private float maxScrollOffset = 0.0f;
    private float scrollStep = 10.0f;

    private boolean showScrollbar = true;
    private float scrollbarWidth = 3.0f;
    private float scrollbarPadding = 1.0f;
    private Color scrollbarTrackColor = Color.fromARGB(120, 20, 20, 25);
    private Color scrollbarThumbColor = Color.fromARGB(220, 100, 140, 200);

    public ScrollContainer(String id) {
        super(id);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public float scrollOffset() {
        return scrollOffset;
    }

    public ScrollContainer scrollOffset(float offset) {
        this.scrollOffset = Math.clamp(offset, 0.0f, Math.max(0.0f, maxScrollOffset));
        return this;
    }

    public float maxScrollOffset() {
        return maxScrollOffset;
    }

    public ScrollContainer maxScrollOffset(float maxOffset) {
        this.maxScrollOffset = Math.max(0.0f, maxOffset);
        this.scrollOffset = Math.clamp(this.scrollOffset, 0.0f, this.maxScrollOffset);
        return this;
    }

    public float scrollStep() {
        return scrollStep;
    }

    public ScrollContainer scrollStep(float step) {
        this.scrollStep = Math.max(0.1f, step);
        return this;
    }

    public boolean showScrollbar() {
        return showScrollbar;
    }

    public ScrollContainer showScrollbar(boolean show) {
        this.showScrollbar = show;
        return this;
    }

    public float scrollbarWidth() {
        return scrollbarWidth;
    }

    public ScrollContainer scrollbarWidth(float width) {
        this.scrollbarWidth = Math.max(0.5f, width);
        return this;
    }

    public float scrollbarPadding() {
        return scrollbarPadding;
    }

    public ScrollContainer scrollbarPadding(float padding) {
        this.scrollbarPadding = Math.max(0.0f, padding);
        return this;
    }

    public Color scrollbarTrackColor() {
        return scrollbarTrackColor;
    }

    public ScrollContainer scrollbarTrackColor(Color color) {
        this.scrollbarTrackColor = color;
        return this;
    }

    public Color scrollbarThumbColor() {
        return scrollbarThumbColor;
    }

    public ScrollContainer scrollbarThumbColor(Color color) {
        this.scrollbarThumbColor = color;
        return this;
    }

    public static class Builder extends Container.Builder {
        private float scrollOffset = 0.0f;
        private float maxScrollOffset = 0.0f;
        private float scrollStep = 10.0f;
        private boolean showScrollbar = true;
        private float scrollbarWidth = 3.0f;
        private float scrollbarPadding = 1.0f;
        private Color scrollbarTrackColor = Color.fromARGB(120, 20, 20, 25);
        private Color scrollbarThumbColor = Color.fromARGB(220, 100, 140, 200);

        public Builder(String id) {
            super(id);
        }

        @Override
        public Builder anchor(UiAnchorPoint anchor) { super.anchor(anchor); return this; }
        @Override
        public Builder origin(UiAnchorPoint origin) { super.origin(origin); return this; }
        @Override
        public Builder offset(float x, float y) { super.offset(x, y); return this; }
        @Override
        public Builder size(float width, float height) { super.size(width, height); return this; }
        @Override
        public Builder scale(float scale) { super.scale(scale); return this; }
        @Override
        public Builder opacity(float opacity) { super.opacity(opacity); return this; }
        @Override
        public Builder borderRound(float radius) { super.borderRound(radius); return this; }
        @Override
        public Builder backgroundColor(Color color) { super.backgroundColor(color); return this; }
        @Override
        public Builder gradient(UiGradient gradient) { super.gradient(gradient); return this; }
        @Override
        public Builder visible(boolean visible) { super.visible(visible); return this; }

        public Builder scrollOffset(float offset) {
            this.scrollOffset = offset;
            return this;
        }

        public Builder maxScrollOffset(float maxOffset) {
            this.maxScrollOffset = Math.max(0.0f, maxOffset);
            return this;
        }

        public Builder scrollStep(float step) {
            this.scrollStep = Math.max(0.1f, step);
            return this;
        }

        public Builder showScrollbar(boolean show) {
            this.showScrollbar = show;
            return this;
        }

        public Builder scrollbarWidth(float width) {
            this.scrollbarWidth = Math.max(0.5f, width);
            return this;
        }

        public Builder scrollbarPadding(float padding) {
            this.scrollbarPadding = Math.max(0.0f, padding);
            return this;
        }

        public Builder scrollbarTrackColor(Color color) {
            this.scrollbarTrackColor = color;
            return this;
        }

        public Builder scrollbarThumbColor(Color color) {
            this.scrollbarThumbColor = color;
            return this;
        }

        @Override
        public ScrollContainer build() {
            ScrollContainer container = new ScrollContainer(id);
            container.anchor(anchor);
            container.origin(origin);
            container.offset(x, y);
            container.size(width, height);
            container.scale(scale);
            container.opacity(opacity);
            container.borderRound(borderRound);
            container.backgroundColor(backgroundColor);
            container.gradient(gradient);
            container.visible(visible);

            container.maxScrollOffset(maxScrollOffset);
            container.scrollOffset(scrollOffset);
            container.scrollStep(scrollStep);
            container.showScrollbar(showScrollbar);
            container.scrollbarWidth(scrollbarWidth);
            container.scrollbarPadding(scrollbarPadding);
            if (scrollbarTrackColor != null) container.scrollbarTrackColor(scrollbarTrackColor);
            if (scrollbarThumbColor != null) container.scrollbarThumbColor(scrollbarThumbColor);

            return container;
        }
    }
}
