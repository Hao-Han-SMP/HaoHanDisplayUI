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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Convenience builder for multi-color, gradient and styled Adventure text. */
public final class UiText {
    private UiText() {}

    public static Builder builder() {
        return new Builder();
    }

    public static TextColor hex(String value) {
        TextColor color = TextColor.fromHexString(value);
        if (color == null) throw new IllegalArgumentException("Invalid hex color: " + value);
        return color;
    }

    public static float estimateWidth(Component component, float fontSize) {
        Objects.requireNonNull(component, "component");
        if (fontSize <= 0.0f) throw new IllegalArgumentException("fontSize must be positive");

        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        float pixels = 0.0f;
        for (int codePoint : plain.codePoints().toArray()) {
            pixels += switch (codePoint) {
                case ' ' -> 4.0f;
                case '!', '.', ',', ':', ';', '|', '\'', 'i', 'l' -> 2.0f;
                case 'I', '[', ']', '(', ')', 't', 'f', 'k' -> 4.0f;
                case '<', '>', '*', '"' -> 5.0f;
                case '@', '~' -> 7.0f;
                default -> 6.0f;
            };
        }
        return Math.max(1.0f, pixels * fontSize / 10.0f);
    }

    public static Component gradient(String text, TextColor from, TextColor to,
                                     TextDecoration... decorations) {
        return builder().gradient(text, List.of(from, to), decorations).build();
    }

    public static final class Builder {
        private Component result = Component.empty();

        public Builder append(Component component) {
            result = result.append(Objects.requireNonNull(component, "component"));
            return this;
        }

        public Builder text(String text) {
            return append(Component.text(Objects.requireNonNull(text, "text")));
        }

        public Builder text(String text, TextColor color, TextDecoration... decorations) {
            Component component = Component.text(Objects.requireNonNull(text, "text"),
                    Objects.requireNonNull(color, "color"));
            return append(decorate(component, decorations));
        }

        public Builder styled(String text, Style style) {
            return append(Component.text(Objects.requireNonNull(text, "text"))
                    .style(Objects.requireNonNull(style, "style")));
        }

        public Builder translatable(String key, String fallback, TextColor color,
                                    TextDecoration... decorations) {
            Component component = Component.translatable(Objects.requireNonNull(key, "key"))
                    .fallback(Objects.requireNonNull(fallback, "fallback"))
                    .color(Objects.requireNonNull(color, "color"));
            return append(decorate(component, decorations));
        }

        public Builder gradient(String text, TextColor from, TextColor to,
                                TextDecoration... decorations) {
            return gradient(text, List.of(from, to), decorations);
        }

        public Builder gradient(String text, TextColor[] colors, double factor,
                                TextDecoration... decorations) {
            Objects.requireNonNull(colors, "colors");
            return gradient(text, List.of(colors), factor, decorations);
        }

        public Builder gradient(String text, List<? extends TextColor> stops,
                                TextDecoration... decorations) {
            return gradient(text, stops, 1.0, decorations);
        }

        public Builder gradient(String text, List<? extends TextColor> stops,
                                double factor, TextDecoration... decorations) {
            Objects.requireNonNull(text, "text");
            Objects.requireNonNull(stops, "stops");
            if (stops.size() < 2) throw new IllegalArgumentException("gradient needs at least 2 colors");
            if (factor < 0.0 || factor > 1.0 || !Double.isFinite(factor)) {
                throw new IllegalArgumentException("gradient factor must be between 0 and 1");
            }
            if (stops.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("gradient colors cannot be null");
            }

            int[] codePoints = text.codePoints().toArray();
            for (int index = 0; index < codePoints.length; index++) {
                double progress = codePoints.length <= 1 ? 0.0
                        : (double) index / (codePoints.length - 1);
                double scaled = progress * (stops.size() - 1);
                int segment = Math.min(stops.size() - 2, (int) Math.floor(scaled));
                double local = scaled - segment;
                TextColor fullGradient = interpolate(
                        stops.get(segment), stops.get(segment + 1), local);
                TextColor color = interpolate(stops.getFirst(), fullGradient, factor);
                Component character = Component.text(
                        new String(Character.toChars(codePoints[index])), color);
                result = result.append(decorate(character, decorations));
            }
            return this;
        }

        public Builder space() {
            return append(Component.space());
        }

        public Builder newline() {
            return append(Component.newline());
        }

        public Component build() {
            return result;
        }

        private Component decorate(Component component, TextDecoration[] decorations) {
            Arrays.stream(decorations).forEach(decoration ->
                    Objects.requireNonNull(decoration, "decoration"));
            return component.decorate(decorations);
        }

        private TextColor interpolate(TextColor from, TextColor to, double progress) {
            int red = channel(from.red(), to.red(), progress);
            int green = channel(from.green(), to.green(), progress);
            int blue = channel(from.blue(), to.blue(), progress);
            return TextColor.color(red, green, blue);
        }

        private int channel(int from, int to, double progress) {
            return (int) Math.round(from + (to - from) * progress);
        }
    }
}
