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
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.component.Component;
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.api.layer.Layer;
import vn.haohan.displayui.api.layout.UiAnchorPoint;
import vn.haohan.displayui.api.layout.UiRect;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Hierarchical UI Container that manages child {@link Component}s and nested sub-{@link Container}s.
 *
 * <p>Key characteristics:
 * <ul>
 *   <li><b>9-Way Anchor &amp; Origin:</b> Positioned relative to parent layer or parent container.</li>
 *   <li><b>Recursive Hierarchy:</b> Containers can be nested within other containers arbitrarily deep.</li>
 *   <li><b>Proportional Scale Cascade:</b> Scaling a parent container scales all nested containers
 *       and inner components proportionally by {@code parentScale * childScale}.</li>
 *   <li><b>Scoped Animation Cascade:</b> Calling {@link #animate(UiAnimation)} animates this container
 *       and all elements within its subtree, strictly isolating siblings and parent layers from movement.</li>
 *   <li><b>Real-Time Updates:</b> Can be modified and updated in real-time.</li>
 * </ul>
 */
public class Container {

    private final String id;
    private UiAnchorPoint anchor = UiAnchorPoint.TOP_LEFT;
    private UiAnchorPoint origin = UiAnchorPoint.TOP_LEFT;
    private float x = 0.0f;
    private float y = 0.0f;
    private float width = 100.0f;
    private float height = 100.0f;
    private float scale = 1.0f;
    private float opacity = 1.0f;
    private float borderRound = 0.0f;
    private Color backgroundColor;
    private UiGradient gradient;
    private boolean visible = true;

    private Container parentContainer;
    private Layer layer;
    private UiAnimation activeAnimation;

    private final Map<String, Component> components = new LinkedHashMap<>();
    private final Map<String, Container> childContainers = new LinkedHashMap<>();
    private final List<Consumer<Container>> updateListeners = new ArrayList<>();

    public Container(String id) {
        this.id = Objects.requireNonNull(id, "Container id cannot be null");
        if (id.isBlank()) throw new IllegalArgumentException("Container id cannot be empty");
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String id() {
        return id;
    }

    public UiAnchorPoint anchor() {
        return anchor;
    }

    public Container anchor(UiAnchorPoint anchor) {
        this.anchor = Objects.requireNonNull(anchor, "anchor cannot be null");
        return this;
    }

    public UiAnchorPoint origin() {
        return origin;
    }

    public Container origin(UiAnchorPoint origin) {
        this.origin = Objects.requireNonNull(origin, "origin cannot be null");
        return this;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public Container offset(float x, float y) {
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            throw new IllegalArgumentException("Offsets must be finite");
        }
        this.x = x;
        this.y = y;
        return this;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public Container size(float width, float height) {
        if (!Float.isFinite(width) || !Float.isFinite(height) || width < 0.0f || height < 0.0f) {
            throw new IllegalArgumentException("Container dimensions must be non-negative and finite");
        }
        this.width = width;
        this.height = height;
        return this;
    }

    public float scale() {
        return scale;
    }

    public Container scale(float scale) {
        if (!Float.isFinite(scale) || scale < 0.0f) {
            throw new IllegalArgumentException("Container scale must be non-negative and finite");
        }
        this.scale = scale;
        return this;
    }

    public float borderRound() {
        return borderRound;
    }

    public Container borderRound(float radius) {
        if (!Float.isFinite(radius) || radius < 0.0f) {
            throw new IllegalArgumentException("Container borderRound must be non-negative and finite");
        }
        this.borderRound = radius;
        return this;
    }

    public Color backgroundColor() {
        return backgroundColor;
    }

    public Container backgroundColor(Color color) {
        this.backgroundColor = color;
        return this;
    }

    public UiGradient gradient() {
        return gradient;
    }

    public Container gradient(UiGradient gradient) {
        this.gradient = gradient;
        return this;
    }

    public float opacity() {
        return opacity;
    }

    public Container opacity(float opacity) {
        if (!Float.isFinite(opacity)) throw new IllegalArgumentException("opacity must be finite");
        this.opacity = Math.clamp(opacity, 0.0f, 1.0f);
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public Container visible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public Optional<Container> parentContainer() {
        return Optional.ofNullable(parentContainer);
    }

    public void setParentContainer(Container parent) {
        this.parentContainer = parent;
        if (parent != null) {
            this.layer = parent.layer;
        }
    }

    public Optional<Layer> layer() {
        return Optional.ofNullable(layer);
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
        for (Container child : childContainers.values()) {
            child.setLayer(layer);
        }
    }

    // --- Component Management ---

    public Container addComponent(Component component) {
        Objects.requireNonNull(component, "component cannot be null");
        component.setParent(this);
        components.put(component.id(), component);
        return this;
    }

    public Optional<Component> removeComponent(String componentId) {
        Component removed = components.remove(componentId);
        if (removed != null) {
            removed.setParent(null);
        }
        return Optional.ofNullable(removed);
    }

    public Optional<Component> getComponent(String componentId) {
        return Optional.ofNullable(components.get(componentId));
    }

    /**
     * Recursively searches for a component by ID within this container and all nested child containers.
     */
    public Optional<Component> findComponent(String componentId) {
        Component direct = components.get(componentId);
        if (direct != null) return Optional.of(direct);
        for (Container sub : childContainers.values()) {
            Optional<Component> found = sub.findComponent(componentId);
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    public Collection<Component> components() {
        return Collections.unmodifiableCollection(components.values());
    }

    public void clearComponents() {
        for (Component c : components.values()) {
            c.setParent(null);
        }
        components.clear();
    }

    // --- Child Container Management ---

    public Container addContainer(Container child) {
        Objects.requireNonNull(child, "child container cannot be null");
        if (child == this) throw new IllegalArgumentException("Cannot add container as child of itself");
        child.setParentContainer(this);
        childContainers.put(child.id(), child);
        return this;
    }

    public Optional<Container> removeContainer(String containerId) {
        Container removed = childContainers.remove(containerId);
        if (removed != null) {
            removed.setParentContainer(null);
            removed.setLayer(null);
        }
        return Optional.ofNullable(removed);
    }

    public Optional<Container> getContainer(String containerId) {
        return Optional.ofNullable(childContainers.get(containerId));
    }

    /**
     * Recursively searches for a nested container by ID.
     */
    public Optional<Container> findContainer(String containerId) {
        Container direct = childContainers.get(containerId);
        if (direct != null) return Optional.of(direct);
        for (Container sub : childContainers.values()) {
            Optional<Container> found = sub.findContainer(containerId);
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    public Collection<Container> childContainers() {
        return Collections.unmodifiableCollection(childContainers.values());
    }

    public void clearContainers() {
        for (Container c : childContainers.values()) {
            c.setParentContainer(null);
            c.setLayer(null);
        }
        childContainers.clear();
    }

    // --- Hierarchy & Coordinate Resolution ---

    /**
     * Effective scale combining all parent containers and this container's scale.
     */
    public float effectiveScale() {
        float parentScale = parentContainer != null ? parentContainer.effectiveScale() : 1.0f;
        return parentScale * this.scale;
    }

    /**
     * Effective opacity combining all parent containers and this container's opacity.
     */
    public float effectiveOpacity() {
        float parentOpacity = parentContainer != null ? parentContainer.effectiveOpacity() : 1.0f;
        return parentOpacity * this.opacity;
    }

    /**
     * Computes the local position [left, top] within the immediate parent's space.
     */
    public float[] computePosition(float parentWidth, float parentHeight) {
        return anchor.resolve(parentWidth, parentHeight, x, y, width, height, origin);
    }

    /**
     * Computes the global coordinates [left, top] within the root layer space.
     */
    public float[] computeGlobalPosition() {
        if (parentContainer == null) {
            // Root container in layer
            if (layer != null && layer.hasExplicitBounds()) {
                float anchorX = layer.x() + layer.width() * anchor.xFactor();
                float anchorY = layer.y() + layer.height() * anchor.yFactor();
                return new float[]{
                        anchorX + x - width * origin.xFactor(),
                        anchorY + y - height * origin.yFactor()
                };
            }
            // By default, center of world/layer billboard is at (0, 0)
            return new float[]{
                    x - width * origin.xFactor(),
                    y - height * origin.yFactor()
            };
        }
        float[] parentGlobal = parentContainer.computeGlobalPosition();
        float scrollShiftY = (parentContainer instanceof ScrollContainer scroll)
                ? -scroll.scrollOffset() * parentContainer.scale()
                : 0.0f;
        float[] local = computePosition(parentContainer.width(), parentContainer.height());
        return new float[]{
                parentGlobal[0] + local[0] * parentContainer.scale(),
                parentGlobal[1] + scrollShiftY + local[1] * parentContainer.scale()
        };
    }

    /**
     * Computes the global bounding rectangle in root layer space.
     */
    public UiRect computeGlobalBounds() {
        float[] pos = computeGlobalPosition();
        float effScale = effectiveScale();
        return new UiRect(pos[0], pos[1], width * effScale, height * effScale);
    }

    // --- Animation System ---

    /**
     * Starts an animation on this container and cascades it to all child containers and components.
     * Sibling containers and parent containers remain unaffected.
     */
    public void animate(UiAnimation animation) {
        this.activeAnimation = Objects.requireNonNull(animation, "animation cannot be null");
    }

    public void stopAnimation() {
        this.activeAnimation = null;
    }

    public boolean isAnimating() {
        return activeAnimation != null;
    }

    public Optional<UiAnimation> activeAnimation() {
        return Optional.ofNullable(activeAnimation);
    }

    /**
     * Returns true if either this container or any of its ancestors is currently animating.
     */
    public boolean isCascadeAnimating() {
        if (activeAnimation != null) return true;
        return parentContainer != null && parentContainer.isCascadeAnimating();
    }

    // --- Real-Time Update Notifications ---

    public void onUpdate(Consumer<Container> listener) {
        updateListeners.add(Objects.requireNonNull(listener, "listener cannot be null"));
    }

    /**
     * Triggers a real-time update on this container.
     */
    public void update() {
        for (Consumer<Container> listener : updateListeners) {
            listener.accept(this);
        }
    }

    // --- Fluent Builder ---

    public static class Builder {
        protected final String id;
        protected UiAnchorPoint anchor = UiAnchorPoint.CENTER;
        protected UiAnchorPoint origin = UiAnchorPoint.CENTER;
        protected float x = 0.0f;
        protected float y = 0.0f;
        protected float width = 100.0f;
        protected float height = 100.0f;
        protected float scale = 1.0f;
        protected float opacity = 1.0f;
        protected float borderRound = 0.0f;
        protected Color backgroundColor;
        protected UiGradient gradient;
        protected boolean visible = true;

        public Builder(String id) {
            this.id = id;
        }

        public Builder anchor(UiAnchorPoint anchor) {
            this.anchor = anchor;
            return this;
        }

        public Builder origin(UiAnchorPoint origin) {
            this.origin = origin;
            return this;
        }

        public Builder offset(float x, float y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder opacity(float opacity) {
            this.opacity = opacity;
            return this;
        }

        public Builder borderRound(float borderRound) {
            this.borderRound = borderRound;
            return this;
        }

        public Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder gradient(UiGradient gradient) {
            this.gradient = gradient;
            return this;
        }

        public Builder visible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public Container build() {
            Container container = new Container(id);
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
            return container;
        }
    }
}
