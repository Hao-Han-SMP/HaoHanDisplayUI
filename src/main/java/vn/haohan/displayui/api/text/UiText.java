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
package vn.haohan.displayui.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Fluent builder and utility methods for constructing Adventure {@link Component} text in Display UIs.
 * <p>
 * Provides helpers for multi-stop gradients, HEX color parsing, Minecraft glyph width estimation,
 * and component concatenation.
 */
public final class UiText {
    private UiText() {}

    /**
     * Creates a new fluent {@link Builder} to construct and format an Adventure {@link Component}.
     *
     * @return a new UiText builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Parses a HEX color string into a {@link TextColor}.
     *
     * @param value HEX color string (e.g., {@code "#FF5555"} or {@code "#FFFFFF"})
     * @return corresponding {@link TextColor}
     * @throws IllegalArgumentException if the HEX color string is invalid
     * @throws NullPointerException     if {@code value} is {@code null}
     */
    public static TextColor hex(String value) {
        TextColor color = TextColor.fromHexString(value);
        if (color == null) throw new IllegalArgumentException("Invalid hex color: " + value);
        return color;
    }

    /**
     * Estimates the physical rendered width of text in logical UI pixels.
     * <p>
     * Based on default Minecraft font glyph widths scaled by the font size ratio ({@code fontSize / 10.0f}).
     *
     * @param component Adventure text {@link Component} to measure
     * @param fontSize  font size in logical UI pixels (must be > 0)
     * @return estimated rendered width in UI pixels (minimum 1.0f)
     * @throws NullPointerException     if {@code component} is {@code null}
     * @throws IllegalArgumentException if {@code fontSize <= 0.0f}
     */
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
        // Minecraft's default font is approximately ten pixels tall. fontSize
        // is expressed in logical UI pixels, so glyph widths use the same
        // fontSize / 10 ratio as the original aligned-text layout.
        return Math.max(1.0f, pixels * fontSize / 10.0f);
    }

    /**
     * Convenience method to construct a two-color gradient text component.
     *
     * @param text        raw text string to display
     * @param from        gradient start color
     * @param to          gradient end color
     * @param decorations optional text decorations (BOLD, ITALIC, etc.)
     * @return styled {@link Component} with gradient colors applied
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public static Component gradient(String text, TextColor from, TextColor to,
                                     TextDecoration... decorations) {
        return builder().gradient(text, List.of(from, to), decorations).build();
    }

    /**
     * Fluent builder for assembling styled and decorated Adventure text components.
     */
    public static final class Builder {
        private Component result = Component.empty();

        /**
         * Appends an existing {@link Component} to the builder.
         *
         * @param component component to append
         * @return this builder instance
         * @throws NullPointerException if {@code component} is {@code null}
         */
        public Builder append(Component component) {
            result = result.append(Objects.requireNonNull(component, "component"));
            return this;
        }

        /**
         * Appends a raw plain text string without styling.
         *
         * @param text string to append
         * @return this builder instance
         * @throws NullPointerException if {@code text} is {@code null}
         */
        public Builder text(String text) {
            return append(Component.text(Objects.requireNonNull(text, "text")));
        }

        /**
         * Appends a colored text string with optional decorations.
         *
         * @param text        string to append
         * @param color       text color
         * @param decorations optional text decorations (BOLD, ITALIC, etc.)
         * @return this builder instance
         * @throws NullPointerException if {@code text} or {@code color} is {@code null}
         */
        public Builder text(String text, TextColor color, TextDecoration... decorations) {
            Component component = Component.text(Objects.requireNonNull(text, "text"),
                    Objects.requireNonNull(color, "color"));
            return append(decorate(component, decorations));
        }

        /**
         * Appends a text string formatted with an Adventure {@link Style}.
         *
         * @param text  string to append
         * @param style text style
         * @return this builder instance
         * @throws NullPointerException if {@code text} or {@code style} is {@code null}
         */
        public Builder styled(String text, Style style) {
            return append(Component.text(Objects.requireNonNull(text, "text"))
                    .style(Objects.requireNonNull(style, "style")));
        }

        /**
         * Appends a translatable text component with fallback and decorations.
         *
         * @param key         Minecraft translation key
         * @param fallback    fallback string if key is unmapped
         * @param color       text color
         * @param decorations optional text decorations
         * @return this builder instance
         * @throws NullPointerException if any required parameter is {@code null}
         */
        public Builder translatable(String key, String fallback, TextColor color,
                                    TextDecoration... decorations) {
            Component component = Component.translatable(Objects.requireNonNull(key, "key"))
                    .fallback(Objects.requireNonNull(fallback, "fallback"))
                    .color(Objects.requireNonNull(color, "color"));
            return append(decorate(component, decorations));
        }

        /**
         * Appends text styled with a two-color linear gradient.
         *
         * @param text        text string
         * @param from        gradient start color
         * @param to          gradient end color
         * @param decorations optional text decorations
         * @return this builder instance
         */
        public Builder gradient(String text, TextColor from, TextColor to,
                                TextDecoration... decorations) {
            return gradient(text, List.of(from, to), decorations);
        }

        /**
         * Appends text styled with a multi-stop gradient array and intensity factor.
         *
         * @param text        text string
         * @param colors      array of gradient stop colors
         * @param factor      blending intensity factor (0.0 to 1.0)
         * @param decorations optional text decorations
         * @return this builder instance
         */
        public Builder gradient(String text, TextColor[] colors, double factor,
                                TextDecoration... decorations) {
            Objects.requireNonNull(colors, "colors");
            return gradient(text, List.of(colors), factor, decorations);
        }

        /**
         * Appends text styled with a multi-stop gradient list.
         *
         * @param text        text string
         * @param stops       list of color stops (at least 2 colors)
         * @param decorations optional text decorations
         * @return this builder instance
         */
        public Builder gradient(String text, List<? extends TextColor> stops,
                                TextDecoration... decorations) {
            return gradient(text, stops, 1.0, decorations);
        }

        /**
         * Appends text styled with a multi-stop gradient list and intensity factor.
         *
         * @param text        text string
         * @param stops       list of color stops (at least 2 colors)
         * @param factor      blending intensity factor (0.0 to 1.0)
         * @param decorations optional text decorations
         * @return this builder instance
         * @throws IllegalArgumentException if fewer than 2 stops or factor outside [0, 1]
         */
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

        /**
         * Appends a space character to the text component.
         *
         * @return this builder instance
         */
        public Builder space() {
            return append(Component.space());
        }

        /**
         * Appends a newline character to the text component.
         *
         * @return this builder instance
         */
        public Builder newline() {
            return append(Component.newline());
        }

        /**
         * Builds the final immutable Adventure {@link Component}.
         *
         * @return assembled component
         */
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

