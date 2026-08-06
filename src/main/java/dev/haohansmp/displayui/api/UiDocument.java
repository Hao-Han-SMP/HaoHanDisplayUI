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
package dev.haohansmp.displayui.api;

import dev.haohansmp.displayui.api.interaction.UiButton;
import dev.haohansmp.displayui.api.interaction.UiButtonAction;
import dev.haohansmp.displayui.api.node.AlignedTextNode;
import dev.haohansmp.displayui.api.node.UiIconNode;
import dev.haohansmp.displayui.api.node.UiNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Immutable scene description. Nodes render in ascending depth order. */
public record UiDocument(List<UiNode> nodes, List<UiButton> buttons) {
    public UiDocument {
        Objects.requireNonNull(nodes, "nodes");
        Objects.requireNonNull(buttons, "buttons");
        nodes = List.copyOf(nodes);
        buttons = List.copyOf(buttons);
    }

    public UiDocument(List<UiNode> nodes) {
        this(nodes, List.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<UiNode> nodes = new ArrayList<>();
        private final List<UiButton> buttons = new ArrayList<>();

        public Builder add(UiNode node) {
            nodes.add(Objects.requireNonNull(node, "node"));
            return this;
        }

        public Builder button(UiButton button) {
            buttons.add(Objects.requireNonNull(button, "button"));
            return this;
        }

        public Builder interactiveText(String id, AlignedTextNode text,
                                       net.kyori.adventure.text.Component description,
                                       UiButtonAction action) {
            add(text);
            button(UiButton.forText(id, text)
                    .describedBy(description)
                    .withAction(action));
            return this;
        }

        public Builder interactiveIcon(String id, UiIconNode icon,
                                       net.kyori.adventure.text.Component description,
                                       UiButtonAction action) {
            add(icon);
            button(UiButton.forIcon(id, icon)
                    .describedBy(description)
                    .withAction(action));
            return this;
        }

        public UiDocument build() {
            return new UiDocument(nodes, buttons);
        }
    }
}
