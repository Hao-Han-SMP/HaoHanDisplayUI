/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.\n */
package vn.haohan.displayui.api.component;

import net.kyori.adventure.text.Component;
import vn.haohan.displayui.api.layout.UiAnchorPoint;

import java.util.Objects;

/**
 * Interactive scroll list component defining a scrollable viewport
 * that captures hotbar / mouse-wheel scroll events.
 */
public class ScrollListComponent extends AbstractComponent {

    private int maxOffset = 0;
    private int offset = 0;
    private int step = 1;
    private Component description = Component.empty();

    public ScrollListComponent(String id) {
        super(id);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public int maxOffset() {
        return maxOffset;
    }

    public int offset() {
        return offset;
    }

    public int step() {
        return step;
    }

    public Component description() {
        return description;
    }

    public static final class Builder {
        private final String id;
        private UiAnchorPoint anchor = UiAnchorPoint.TOP_LEFT;
        private UiAnchorPoint origin = UiAnchorPoint.TOP_LEFT;
        private float offsetX = 0.0f;
        private float offsetY = 0.0f;
        private float width = 100.0f;
        private float height = 100.0f;
        private int maxOffset = 0;
        private int offset = 0;
        private int step = 1;
        private Component description = Component.empty();

        public Builder(String id) {
            this.id = Objects.requireNonNull(id, "id cannot be null");
        }

        public Builder anchor(UiAnchorPoint anchor) {
            this.anchor = Objects.requireNonNull(anchor, "anchor cannot be null");
            return this;
        }

        public Builder origin(UiAnchorPoint origin) {
            this.origin = Objects.requireNonNull(origin, "origin cannot be null");
            return this;
        }

        public Builder offset(float x, float y) {
            this.offsetX = x;
            this.offsetY = y;
            return this;
        }

        public Builder size(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder range(int maxOffset, int currentOffset) {
            this.maxOffset = Math.max(0, maxOffset);
            this.offset = Math.clamp(currentOffset, 0, this.maxOffset);
            return this;
        }

        public Builder step(int step) {
            this.step = Math.max(1, step);
            return this;
        }

        public Builder description(Component description) {
            this.description = description != null ? description : Component.empty();
            return this;
        }

        public ScrollListComponent build() {
            ScrollListComponent comp = new ScrollListComponent(id);
            comp.anchor = anchor;
            comp.origin = origin;
            comp.x = offsetX;
            comp.y = offsetY;
            comp.width = width;
            comp.height = height;
            comp.maxOffset = maxOffset;
            comp.offset = offset;
            comp.step = step;
            comp.description = description;
            return comp;
        }
    }
}
