package dev.haohansmp.displayui.api;

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
