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
package vn.haohan.displayui.api.bridge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.component.*;
import vn.haohan.displayui.api.component.event.ComponentChangeEvent;
import vn.haohan.displayui.api.component.event.ComponentClickEvent;
import vn.haohan.displayui.api.container.Container;
import vn.haohan.displayui.api.container.DropdownContainer;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiCheckbox;
import vn.haohan.displayui.api.interaction.UiSlider;
import vn.haohan.displayui.api.layer.Layer;
import vn.haohan.displayui.api.layer.LayerManager;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.UiBackgroundNode;
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.api.node.UiShapeNode;
import vn.haohan.displayui.api.text.UiTextAlignment;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-performance compiler and adapter that transforms the modern 3-tier
 * hierarchy ({@link LayerManager} / {@link Layer} / {@link Container} / {@link vn.haohan.displayui.api.component.Component})
 * into an immutable {@link UiDocument} with non-overlapping depths, zero Z-fighting,
 * and bidirectional interaction forwarding.
 */
public final class UiDocumentBridge {

    private static final UiAnimation STATIC_ANIMATION = UiAnimation.builder().durationTicks(1).build();

    private UiDocumentBridge() {
    }

    /**
     * Immutable compilation result holding both the compiled {@link UiDocument}
     * and the matching per-node {@link UiAnimation} sequence.
     */
    public record CompiledUi(UiDocument document, List<UiAnimation> nodeAnimations) {
        public boolean hasAnimations() {
            return nodeAnimations != null && !nodeAnimations.isEmpty()
                    && nodeAnimations.stream().anyMatch(a -> a != null && !a.isStatic());
        }
    }

    /**
     * Compiles an entire {@link LayerManager} into an optimized {@link UiDocument}.
     */
    public static UiDocument compile(LayerManager layerManager) {
        return compileWithAnimations(layerManager).document();
    }

    /**
     * Compiles an entire {@link LayerManager} into an optimized {@link CompiledUi} with node animations.
     */
    public static CompiledUi compileWithAnimations(LayerManager layerManager) {
        Objects.requireNonNull(layerManager, "layerManager cannot be null");
        UiDocument.Builder builder = UiDocument.builder();
        List<UiAnimation> nodeAnimations = new ArrayList<>();

        for (Layer layer : layerManager.layers()) {
            if (!layer.isVisible()) continue;
            compileLayer(layer, builder, nodeAnimations);
        }

        return new CompiledUi(builder.build(), Collections.unmodifiableList(nodeAnimations));
    }

    /**
     * Compiles a single {@link Layer} into a {@link UiDocument}.
     */
    public static UiDocument compile(Layer layer) {
        return compileWithAnimations(layer).document();
    }

    /**
     * Compiles a single {@link Layer} into a {@link CompiledUi} with node animations.
     */
    public static CompiledUi compileWithAnimations(Layer layer) {
        Objects.requireNonNull(layer, "layer cannot be null");
        UiDocument.Builder builder = UiDocument.builder();
        List<UiAnimation> nodeAnimations = new ArrayList<>();
        if (layer.isVisible()) {
            compileLayer(layer, builder, nodeAnimations);
        }
        return new CompiledUi(builder.build(), Collections.unmodifiableList(nodeAnimations));
    }

    /**
     * Compiles a single root {@link Container} into a {@link UiDocument} using a default layer depth.
     */
    public static UiDocument compile(Container rootContainer) {
        return compileWithAnimations(rootContainer).document();
    }

    /**
     * Compiles a single root {@link Container} into a {@link CompiledUi} with node animations.
     */
    public static CompiledUi compileWithAnimations(Container rootContainer) {
        Objects.requireNonNull(rootContainer, "rootContainer cannot be null");
        Layer virtualLayer = new Layer("virtual_root", 0);
        virtualLayer.addContainer(rootContainer);
        return compileWithAnimations(virtualLayer);
    }

    private static final Map<String, Long> LAST_TOGGLE_TIMES = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Binds interaction listeners from a {@link UiHandle} to the components in the {@link LayerManager}.
     */
    public static void bindInteractions(UiHandle handle, LayerManager layerManager) {
        Objects.requireNonNull(handle, "handle cannot be null");
        Objects.requireNonNull(layerManager, "layerManager cannot be null");

        handle.clearClickHandlers();
        handle.clearControlChangeHandlers();

        // Click dispatching
        handle.onClick(event -> {
            String id = event.button().id();
            for (Layer layer : layerManager.layers()) {
                for (Container root : layer.containers()) {
                    // Check if clicked button is a DropdownContainer header toggle
                    if (handleDropdownToggle(root, id)) {
                        handle.update(layerManager);
                        return;
                    }

                    Optional<vn.haohan.displayui.api.component.Component> found = root.findComponent(id);
                    if (found.isPresent() && found.get() instanceof ButtonComponent btn) {
                        btn.triggerClick(new ComponentClickEvent(btn, event.player(), event.localX(), event.localY()));
                        return;
                    }
                }
            }
        });

        // Control change dispatching (Sliders & Checkboxes)
        handle.onControlChange(event -> {
            String id = event.control().id();
            for (Layer layer : layerManager.layers()) {
                for (Container root : layer.containers()) {
                    Optional<vn.haohan.displayui.api.component.Component> found = root.findComponent(id);
                    if (found.isPresent()) {
                        vn.haohan.displayui.api.component.Component comp = found.get();
                        if (comp instanceof SliderComponent slider) {
                            slider.triggerChange(new ComponentChangeEvent(slider, event.player(), event.oldValue(), event.value()));
                            return;
                        } else if (comp instanceof CheckboxComponent checkbox) {
                            checkbox.triggerToggle(new ComponentChangeEvent(checkbox, event.player(), event.oldValue(), event.value()));
                            return;
                        }
                    }
                }
            }
        });
    }

    private static boolean handleDropdownToggle(Container container, String buttonId) {
        if (container instanceof DropdownContainer dropdown) {
            if (dropdown.toggleButtonId().equals(buttonId)) {
                long now = System.currentTimeMillis();
                Long last = LAST_TOGGLE_TIMES.get(buttonId);
                if (last != null && now - last < 250) {
                    return true;
                }
                LAST_TOGGLE_TIMES.put(buttonId, now);
                dropdown.toggle();
                return true;
            }
        }
        for (Container child : container.childContainers()) {
            if (handleDropdownToggle(child, buttonId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Binds interaction listeners from a {@link UiHandle} to a single root {@link Container}.
     */
    public static void bindInteractions(UiHandle handle, Container container) {
        Layer virtualLayer = new Layer("virtual_root", 0);
        virtualLayer.addContainer(container);
        LayerManager manager = new LayerManager();
        manager.createLayer(virtualLayer.id(), virtualLayer.zIndex());
        manager.getLayer(virtualLayer.id()).ifPresent(l -> l.addContainer(container));
        bindInteractions(handle, manager);
    }

    // --- Internal Compilation Pipeline ---

    private static void compileLayer(Layer layer, UiDocument.Builder builder, List<UiAnimation> nodeAnimations) {
        AtomicInteger depthCounter = new AtomicInteger(0);
        UiAnimation layerAnim = layer.activeAnimation().orElse(null);

        // Layer background, if configured
        if (layer.gradient() != null) {
            float depth = layer.allocateDepth(depthCounter.getAndIncrement());
            addNode(builder, nodeAnimations, new UiGradientBackgroundNode(
                    layer.x(), layer.y(), depth, layer.width(), layer.height(), layer.gradient()
            ).withGrid(12, 8).withDoubleSided(layer.doubleSided()), layerAnim);
        } else if (layer.backgroundColor() != null) {
            float depth = layer.allocateDepth(depthCounter.getAndIncrement());
            addNode(builder, nodeAnimations, new UiBackgroundNode(
                    layer.x(), layer.y(), depth, layer.width(), layer.height(), layer.backgroundColor()
            ), layerAnim);
        }

        // Compile root containers
        for (Container container : layer.containers()) {
            if (!container.isVisible()) continue;
            compileContainer(layer, container, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                    layerAnim, depthCounter, builder, nodeAnimations);
        }
    }

    private static void compileContainer(Layer layer, Container container,
                                         float parentGlobalX, float parentGlobalY,
                                         float parentWidth, float parentHeight,
                                         float parentScale, float parentOpacity,
                                         UiAnimation parentAnimation,
                                         AtomicInteger depthCounter,
                                         UiDocument.Builder builder,
                                         List<UiAnimation> nodeAnimations) {

        float effScale = parentScale * container.scale();
        float effOpacity = parentOpacity * container.opacity();
        UiAnimation currentAnim = container.activeAnimation().orElse(parentAnimation);

        // Compute container's global position
        float globalX;
        float globalY;
        if (container.parentContainer() == null) {
            float[] rootPos = container.computeGlobalPosition();
            globalX = rootPos[0];
            globalY = rootPos[1];
        } else {
            float[] localPos = container.computePosition(parentWidth, parentHeight);
            globalX = parentGlobalX + localPos[0] * parentScale;
            globalY = parentGlobalY + localPos[1] * parentScale;
        }

        float width = container.width() * effScale;
        float height = container.height() * effScale;

        // Specialized Dropdown Container Header Compilation
        if (container instanceof DropdownContainer dropdown) {
            dropdown.refreshLayout();
            compileDropdownHeader(layer, dropdown, globalX, globalY, effScale, effOpacity,
                    currentAnim, depthCounter, builder, nodeAnimations);
        } else {
            // 1. Standard Container Background Node
            if (container.gradient() != null) {
                float depth = layer.allocateDepth(depthCounter.getAndIncrement());
                addNode(builder, nodeAnimations, new UiGradientBackgroundNode(globalX, globalY, depth, width, height, container.gradient()), currentAnim);
            } else if (container.borderRound() > 0.0f && container.backgroundColor() != null) {
                float depth = layer.allocateDepth(depthCounter.getAndIncrement());
                addNode(builder, nodeAnimations, UiShapeNode.builder("rounded_rect", globalX, globalY, width, height)
                        .color(container.backgroundColor())
                        .cornerRadius(container.borderRound() * effScale)
                        .depth(depth)
                        .build(), currentAnim);
            } else if (container.backgroundColor() != null) {
                float depth = layer.allocateDepth(depthCounter.getAndIncrement());
                addNode(builder, nodeAnimations, new UiBackgroundNode(globalX, globalY, depth, width, height, container.backgroundColor()), currentAnim);
            }
        }

        // 2. Container Components
        for (vn.haohan.displayui.api.component.Component comp : container.components()) {
            if (!comp.isVisible()) continue;
            compileComponent(layer, container, comp, globalX, globalY, effScale, effOpacity,
                    currentAnim, depthCounter, builder, nodeAnimations);
        }

        // 3. Child Containers (Recursive nesting)
        for (Container child : container.childContainers()) {
            if (!child.isVisible()) continue;
            compileContainer(layer, child, globalX, globalY, container.width(), container.height(),
                    effScale, effOpacity, currentAnim, depthCounter, builder, nodeAnimations);
        }
    }

    private static void compileDropdownHeader(Layer layer, DropdownContainer dropdown,
                                              float globalX, float globalY,
                                              float effScale, float effOpacity,
                                              UiAnimation parentAnimation,
                                              AtomicInteger depthCounter,
                                              UiDocument.Builder builder,
                                              List<UiAnimation> nodeAnimations) {
        float headerW = dropdown.width() * effScale;
        float headerH = dropdown.headerHeight() * effScale;
        UiAnimation headerAnim = parentAnimation; // header moves with parent, not dropdown child animation

        // 1. Header Shape Background
        float bgDepth = layer.allocateDepth(depthCounter.getAndIncrement());
        Color bg = dropdown.isExpanded() ? dropdown.headerExpandedBackgroundColor() : dropdown.headerBackgroundColor();
        addNode(builder, nodeAnimations, UiShapeNode.builder("rounded_rect", globalX, globalY, headerW, headerH)
                .color(bg)
                .cornerRadius(dropdown.headerBorderRound() * effScale)
                .depth(bgDepth)
                .build(), headerAnim);

        // 2. Header Title Text
        float titleDepth = layer.allocateDepth(depthCounter.getAndIncrement());
        float textW = Math.max(1.0f, headerW - 24.0f * effScale);
        addNode(builder, nodeAnimations, new AlignedTextNode(
                dropdown.headerTitle(), globalX + 6.0f * effScale, globalY, textW, headerH, UiTextAlignment.LEFT
        ).fontSize(dropdown.headerTitleFontSize() * effScale)
         .shadowed(true)
         .atDepth(titleDepth), headerAnim);

        // 3. Indicator Arrow (▶ or ▼)
        float arrowDepth = layer.allocateDepth(depthCounter.getAndIncrement());
        String indicator = dropdown.isExpanded() ? dropdown.indicatorExpanded() : dropdown.indicatorCollapsed();
        addNode(builder, nodeAnimations, new AlignedTextNode(
                Component.text(indicator, dropdown.indicatorColor()),
                globalX + headerW - 18.0f * effScale, globalY, 14.0f * effScale, headerH, UiTextAlignment.CENTER
        ).fontSize(dropdown.headerTitleFontSize() * effScale)
         .shadowed(true)
         .atDepth(arrowDepth), headerAnim);

        // 4. Header Click Button Hit Zone
        builder.button(new UiButton(dropdown.toggleButtonId(), globalX, globalY, headerW, headerH));
    }

    private static void compileComponent(Layer layer, Container container,
                                         vn.haohan.displayui.api.component.Component comp,
                                         float containerGlobalX, float containerGlobalY,
                                         float containerScale, float containerOpacity,
                                         UiAnimation containerAnimation,
                                         AtomicInteger depthCounter,
                                         UiDocument.Builder builder,
                                         List<UiAnimation> nodeAnimations) {

        float compEffScale = containerScale * comp.scale();
        float[] localPos = comp.computePosition(container.width(), container.height());
        float globalX = containerGlobalX + localPos[0] * containerScale;
        float globalY = containerGlobalY + localPos[1] * containerScale;
        float width = comp.width() * compEffScale;
        float height = comp.height() * compEffScale;

        UiAnimation compAnim = comp.activeAnimation().orElse(containerAnimation);

        switch (comp) {
            case TextComponent textComp -> {
                if (textComp.backgroundColor() != null) {
                    float bgDepth = layer.allocateDepth(depthCounter.getAndIncrement());
                    addNode(builder, nodeAnimations, new UiBackgroundNode(globalX, globalY, bgDepth, width, height, textComp.backgroundColor()), compAnim);
                }
                float textDepth = layer.allocateDepth(depthCounter.getAndIncrement());
                AlignedTextNode node = new AlignedTextNode(
                        textComp.text(), globalX, globalY, width, height, textComp.alignment()
                ).fontSize(textComp.fontSize() * compEffScale)
                 .shadowed(textComp.shadow())
                 .seeThrough(textComp.seeThrough())
                 .atDepth(textDepth);

                if (textComp.opticalPreset() != null) {
                    node = node.opticalPreset(textComp.opticalPreset());
                }
                addNode(builder, nodeAnimations, node, compAnim);
            }

            case ButtonComponent btn -> {
                float bgDepth = layer.allocateDepth(depthCounter.getAndIncrement());
                Color btnBg = btn.backgroundColor() != null ? btn.backgroundColor() : Color.fromRGB(35, 45, 65);

                UiShapeNode.Builder shapeBuilder = UiShapeNode.builder("rounded_rect", globalX, globalY, width, height)
                        .color(btnBg)
                        .cornerRadius(btn.borderRound() * compEffScale)
                        .depth(bgDepth);

                if (btn.outline()) {
                    shapeBuilder.outline(true)
                            .outlineColor(btn.outlineColor())
                            .outlineThickness(btn.outlineThickness() * compEffScale);
                }
                addNode(builder, nodeAnimations, shapeBuilder.build(), compAnim);

                if (!btn.label().equals(Component.empty())) {
                    float labelDepth = layer.allocateDepth(depthCounter.getAndIncrement());
                    addNode(builder, nodeAnimations, new AlignedTextNode(btn.label(), globalX, globalY, width, height, UiTextAlignment.CENTER)
                            .fontSize(5.0f * compEffScale)
                            .atDepth(labelDepth)
                            .shadowed(true), compAnim);
                }

                builder.button(new UiButton(btn.id(), globalX, globalY, width, height)
                        .hitSlop(btn.hitSlop() * compEffScale)
                        .withAction(btn.action()));
            }

            case SliderComponent slider -> {
                float trackDepth = layer.allocateDepth(depthCounter.getAndIncrement());
                float fillDepth = layer.allocateDepth(depthCounter.getAndIncrement());
                float thumbDepth = layer.allocateDepth(depthCounter.getAndIncrement());

                float trackH = Math.max(2.0f, height * 0.35f);
                float trackY = globalY + (height - trackH) * 0.5f;
                addNode(builder, nodeAnimations, UiShapeNode.builder("rounded_rect", globalX, trackY, width, trackH)
                        .color(slider.trackColor())
                        .cornerRadius(trackH * 0.5f)
                        .depth(trackDepth)
                        .build(), compAnim);

                float progress = (float) slider.normalizedProgress();
                float fillW = width * progress;
                if (fillW > 0.0f) {
                    addNode(builder, nodeAnimations, UiShapeNode.builder("rounded_rect", globalX, trackY, fillW, trackH)
                            .color(slider.fillColor())
                            .cornerRadius(trackH * 0.5f)
                            .depth(fillDepth)
                            .build(), compAnim);
                }

                float thumbW = Math.max(6.0f, height * 0.8f);
                float thumbH = height;
                float thumbX = globalX + (width - thumbW) * progress;
                addNode(builder, nodeAnimations, UiShapeNode.builder("rounded_rect", thumbX, globalY, thumbW, thumbH)
                        .color(slider.thumbColor())
                        .cornerRadius(slider.borderRound() * compEffScale)
                        .depth(thumbDepth)
                        .build(), compAnim);

                builder.slider(new UiSlider(slider.id(), globalX, globalY, width, height,
                        slider.minimum(), slider.maximum(), slider.value(), slider.step(),
                        Component.empty()));
            }

            case CheckboxComponent checkbox -> {
                float boxDepth = layer.allocateDepth(depthCounter.getAndIncrement());
                addNode(builder, nodeAnimations, UiShapeNode.builder("rounded_rect", globalX, globalY, width, height)
                        .color(checkbox.currentBoxColor())
                        .cornerRadius(checkbox.borderRound() * compEffScale)
                        .depth(boxDepth)
                        .build(), compAnim);

                if (checkbox.checked()) {
                    float checkDepth = layer.allocateDepth(depthCounter.getAndIncrement());
                    addNode(builder, nodeAnimations, new AlignedTextNode(Component.text("✓", NamedTextColor.WHITE),
                            globalX, globalY, width, height, UiTextAlignment.CENTER)
                            .fontSize(4.5f * compEffScale)
                            .atDepth(checkDepth), compAnim);
                }

                builder.checkbox(new UiCheckbox(checkbox.id(), globalX, globalY, width, height,
                        checkbox.checked()));
            }

            case ShapeComponent shape -> {
                float depth = layer.allocateDepth(depthCounter.getAndIncrement());
                UiShapeNode.Builder shapeBuilder = UiShapeNode.builder(shape.shapeType(), globalX, globalY, width, height)
                        .color(shape.color())
                        .cornerRadius(shape.borderRound() * compEffScale)
                        .rotation(shape.rotation())
                        .depth(depth);

                if (shape.outline()) {
                    shapeBuilder.outline(true)
                            .outlineColor(shape.outlineColor())
                            .outlineThickness(shape.outlineThickness() * compEffScale)
                            .outlineStyle(shape.outlineStyle());
                }
                addNode(builder, nodeAnimations, shapeBuilder.build(), compAnim);
            }

            case IconComponent icon -> {
                ItemStack stack = icon.resolveItemStack();
                if (stack != null && !stack.getType().isAir()) {
                    float depth = layer.allocateDepth(depthCounter.getAndIncrement());
                    addNode(builder, nodeAnimations, new UiIconNode(
                            stack, globalX, globalY, depth, width, height,
                            16.0f, 16.0f, icon.transform(), icon.doubleSided()
                    ), compAnim);
                }
            }

            case CustomNodeComponent customComp -> {
                float depth = layer.allocateDepth(depthCounter.getAndIncrement());
                UiNode node = customComp.instantiate(globalX, globalY, width, height, depth, compEffScale);
                if (node != null) {
                    addNode(builder, nodeAnimations, node, compAnim);
                }
            }

            default -> {
                if (comp.backgroundColor() != null) {
                    float depth = layer.allocateDepth(depthCounter.getAndIncrement());
                    addNode(builder, nodeAnimations, new UiBackgroundNode(globalX, globalY, depth, width, height, comp.backgroundColor()), compAnim);
                }
            }
        }
    }

    private static void addNode(UiDocument.Builder builder, List<UiAnimation> nodeAnimations,
                                UiNode node, UiAnimation animation) {
        builder.add(node);
        if (nodeAnimations != null) {
            nodeAnimations.add(animation != null ? animation : STATIC_ANIMATION);
        }
    }
}
