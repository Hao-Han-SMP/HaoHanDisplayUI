/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HaoHanDisplayUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HaoHanDisplayUI. If not, see <https://www.gnu.org/licenses/>.
 */
package vn.haohan.displayui.api;

import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiCheckbox;
import vn.haohan.displayui.api.interaction.UiControl;
import vn.haohan.displayui.api.interaction.UiSlider;
import vn.haohan.displayui.api.interaction.UiScrollList;
import vn.haohan.displayui.api.interaction.UiButtonAction;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.EntityModelNode;
import vn.haohan.displayui.api.node.MobEntityNode;
import org.bukkit.Color;
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.api.gradient.UiGradientPosition;
import vn.haohan.displayui.api.node.UiBackgroundNode;
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.node.UiNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Immutable scene document describing a complete Display UI hierarchy.
 * <p>
 * {@code UiDocument} encapsulates the collection of visible display nodes (text, items, blocks, entities, shapes),
 * interactive buttons ({@link UiButton}), and advanced controls ({@link UiControl}).
 * Nodes are rendered in increasing depth order along the Z-axis.
 *
 * @param nodes    list of visual display nodes rendered in the UI
 * @param buttons  list of clickable interactive button regions
 * @param controls list of interactive stateful controls (sliders, checkboxes, scroll lists)
 */
public record UiDocument(List<UiNode> nodes, List<UiButton> buttons,
                         List<UiControl> controls) {
    public UiDocument {
        Objects.requireNonNull(nodes, "nodes");
        Objects.requireNonNull(buttons, "buttons");
        Objects.requireNonNull(controls, "controls");
        nodes = List.copyOf(nodes);
        buttons = List.copyOf(buttons);
        controls = List.copyOf(controls);
        validateUniqueIds(buttons, controls);
    }

    /**
     * Creates a static UI document containing only display nodes without interactive controls.
     *
     * @param nodes list of visual display nodes
     */
    public UiDocument(List<UiNode> nodes) {
        this(nodes, List.of(), List.of());
    }

    /**
     * Creates a UI document containing display nodes and basic clickable buttons.
     *
     * @param nodes   list of visual display nodes
     * @param buttons list of clickable buttons
     */
    public UiDocument(List<UiNode> nodes, List<UiButton> buttons) {
        this(nodes, buttons, List.of());
    }

    /**
     * Creates a new fluent {@link Builder} to construct a {@link UiDocument}.
     *
     * @return a new document builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for assembling {@link UiDocument} instances with nodes and controls.
     */
    public static final class Builder {
        private final List<UiNode> nodes = new ArrayList<>();
        private final List<UiButton> buttons = new ArrayList<>();
        private final List<UiControl> controls = new ArrayList<>();

        /**
         * Appends a display node to the UI document.
         *
         * @param node visual node to add
         * @return this builder instance
         */
        public Builder add(UiNode node) {
            nodes.add(Objects.requireNonNull(node, "node"));
            return this;
        }

        /**
         * Appends a collection of display nodes to the UI document.
         *
         * @param nodes collection of visual nodes to add
         * @return this builder instance
         */
        public Builder addAll(Collection<? extends UiNode> nodes) {
            Objects.requireNonNull(nodes, "nodes").forEach(this::add);
            return this;
        }

        /**
         * Appends an interactive button to the UI document.
         *
         * @param button button to add
         * @return this builder instance
         */
        public Builder button(UiButton button) {
            buttons.add(Objects.requireNonNull(button, "button"));
            return this;
        }

        /**
         * Appends an interactive control (slider, checkbox, scroll list) to the UI document.
         *
         * @param control control to add
         * @return this builder instance
         */
        public Builder control(UiControl control) {
            controls.add(Objects.requireNonNull(control, "control"));
            return this;
        }

        /**
         * Appends a slider control ({@link UiSlider}) to the UI document.
         *
         * @param slider slider control to add
         * @return this builder instance
         */
        public Builder slider(UiSlider slider) {
            return control(slider);
        }

        /**
         * Appends a checkbox control ({@link UiCheckbox}) to the UI document.
         *
         * @param checkbox checkbox control to add
         * @return this builder instance
         */
        public Builder checkbox(UiCheckbox checkbox) {
            return control(checkbox);
        }

        /**
         * Appends a scrollable list control ({@link UiScrollList}) to the UI document.
         *
         * @param scrollList scroll list control to add
         * @return this builder instance
         */
        public Builder scrollList(UiScrollList scrollList) {
            return control(scrollList);
        }

        /**
         * Appends a 3D entity display model node ({@link EntityModelNode}) to the document.
         *
         * @param model entity model node to add
         * @return this builder instance
         */
        public Builder entityModel(EntityModelNode model) {
            return add(model);
        }

        /**
         * Appends a living mob entity display node ({@link MobEntityNode}) to the document.
         *
         * @param mob mob entity node to add
         * @return this builder instance
         */
        public Builder mob(MobEntityNode mob) {
            return add(mob);
        }

        /**
         * Appends a solid-colored background rectangle node ({@link UiBackgroundNode}) to the document.
         *
         * @param background background node to add
         * @return this builder instance
         */
        public Builder background(UiBackgroundNode background) {
            return add(background);
        }

        /**
         * Convenience method to construct and append a solid-colored background rectangle.
         *
         * @param x          horizontal origin in UI pixels
         * @param y          vertical origin in UI pixels
         * @param depth      Z-depth offset
         * @param width      width in UI pixels
         * @param height     height in UI pixels
         * @param background background color
         * @return this builder instance
         */
        public Builder background(float x, float y, float depth, float width, float height, Color background) {
            return add(new UiBackgroundNode(x, y, depth, width, height, background));
        }

        /**
         * Appends a gradient background rectangle node ({@link UiGradientBackgroundNode}) to the document.
         *
         * @param node gradient background node to add
         * @return this builder instance
         */
        public Builder gradientBackground(UiGradientBackgroundNode node) {
            return add(node);
        }

        /**
         * Convenience method to construct and append a two-point gradient background rectangle.
         *
         * @param x          horizontal origin in UI pixels
         * @param y          vertical origin in UI pixels
         * @param depth      Z-depth offset
         * @param width      width in UI pixels
         * @param height     height in UI pixels
         * @param startPos   gradient start anchor position
         * @param startColor gradient start color
         * @param endPos     gradient end anchor position
         * @param endColor   gradient end color
         * @return this builder instance
         */
        public Builder gradientBackground(float x, float y, float depth, float width, float height,
                                          UiGradientPosition startPos, Color startColor,
                                          UiGradientPosition endPos, Color endColor) {
            return add(new UiGradientBackgroundNode(x, y, depth, width, height, startPos, startColor, endPos, endColor));
        }

        /**
         * Convenience method to construct and append a gradient background with custom multi-point {@link UiGradient}.
         *
         * @param x        horizontal origin in UI pixels
         * @param y        vertical origin in UI pixels
         * @param depth    Z-depth offset
         * @param width    width in UI pixels
         * @param height   height in UI pixels
         * @param gradient multi-stop gradient definition
         * @return this builder instance
         */
        public Builder gradientBackground(float x, float y, float depth, float width, float height, UiGradient gradient) {
            return add(new UiGradientBackgroundNode(x, y, depth, width, height, gradient));
        }

        /**
         * Adds a text node and automatically registers a clickable interaction region bound to it.
         *
         * @param id          unique button identifier
         * @param text        text display node
         * @param description tooltip or button description component
         * @param action      click callback action
         * @return this builder instance
         */
        public Builder interactiveText(String id, AlignedTextNode text,
                                       net.kyori.adventure.text.Component description,
                                       UiButtonAction action) {
            add(text);
            button(UiButton.forText(id, text)
                    .describedBy(description)
                    .withAction(action));
            return this;
        }

        /**
         * Adds a display node and automatically binds a clickable hit zone matching its bounds.
         *
         * @param id          unique button identifier
         * @param node        visual node to add and bind
         * @param description tooltip or button description component
         * @param action      click callback action
         * @return this builder instance
         */
        public Builder interactive(String id, UiNode node,
                                   net.kyori.adventure.text.Component description,
                                   UiButtonAction action) {
            add(node);
            button(UiButton.forNode(id, node)
                    .describedBy(description)
                    .withAction(action));
            return this;
        }

        /**
         * Adds an icon node and automatically binds a clickable interaction region to it.
         *
         * @param id          unique button identifier
         * @param icon        icon display node
         * @param description tooltip or button description component
         * @param action      click callback action
         * @return this builder instance
         */
        public Builder interactiveIcon(String id, UiIconNode icon,
                                       net.kyori.adventure.text.Component description,
                                       UiButtonAction action) {
            add(icon);
            button(UiButton.forIcon(id, icon)
                    .describedBy(description)
                    .withAction(action));
            return this;
        }

        /**
         * Adds an entity model node and automatically binds a clickable interaction region to it.
         *
         * @param id          unique button identifier
         * @param model       entity model display node
         * @param description tooltip or button description component
         * @param action      click callback action
         * @return this builder instance
         */
        public Builder interactiveModel(String id, EntityModelNode model,
                                        net.kyori.adventure.text.Component description,
                                       UiButtonAction action) {
            add(model);
            button(UiButton.forModel(id, model)
                    .describedBy(description)
                    .withAction(action));
            return this;
        }

        /**
         * Adds a mob entity node and automatically binds a clickable interaction region to it.
         *
         * @param id          unique button identifier
         * @param mob         mob entity display node
         * @param description tooltip or button description component
         * @param action      click callback action
         * @return this builder instance
         */
        public Builder interactiveMob(String id, MobEntityNode mob,
                                      net.kyori.adventure.text.Component description,
                                      UiButtonAction action) {
            add(mob);
            button(UiButton.forMob(id, mob)
                    .describedBy(description)
                    .withAction(action));
            return this;
        }

        /**
         * Constructs an immutable {@link UiDocument} instance from the configured builder state.
         *
         * @return immutable document ready for rendering
         */
        public UiDocument build() {
            return new UiDocument(nodes, buttons, controls);
        }
    }

    private static void validateUniqueIds(List<UiButton> buttons, List<UiControl> controls) {
        java.util.Set<String> ids = new java.util.HashSet<>();
        buttons.forEach(button -> {
            if (!ids.add(button.id())) throw new IllegalArgumentException(
                    "duplicate interaction id: " + button.id());
        });
        controls.forEach(control -> {
            if (!ids.add(control.id())) throw new IllegalArgumentException(
                    "duplicate interaction id: " + control.id());
        });
    }
}
