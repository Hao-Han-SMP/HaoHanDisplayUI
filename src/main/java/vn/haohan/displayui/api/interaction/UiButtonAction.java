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
package vn.haohan.displayui.api.interaction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.net.URI;
import java.util.Objects;

/**
 * Pre-configured action dispatched automatically when a player clicks an interactive button (unless cancelled).
 *
 * @param type  action type (open URL, run player command, run console command, suggest command)
 * @param value raw payload string (URL string or command line)
 * @param label display label component associated with the action
 */
public record UiButtonAction(Type type, String value, Component label) {
    /**
     * Supported action execution types.
     */
    public enum Type {
        /** No action is taken. */
        NONE,
        /** Opens a web URL in the player's web browser. */
        OPEN_URL,
        /** Executes a Minecraft command as the clicking player. */
        RUN_PLAYER_COMMAND,
        /** Executes a Minecraft command from the server console. */
        RUN_CONSOLE_COMMAND,
        /** Pre-fills a command into the player's chat prompt without sending it. */
        SUGGEST_COMMAND
    }

    public UiButtonAction {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(label, "label");
        if (type != Type.NONE && value.isBlank()) {
            throw new IllegalArgumentException("button action value cannot be blank");
        }
    }

    /**
     * Creates a no-op button action.
     *
     * @return empty {@link UiButtonAction} of type NONE
     */
    public static UiButtonAction none() {
        return new UiButtonAction(Type.NONE, "", Component.empty());
    }

    /**
     * Creates an action to open an HTTP or HTTPS link in the player's browser.
     *
     * @param url valid URL string (must start with {@code http://} or {@code https://})
     * @return open-url {@link UiButtonAction}
     * @throws IllegalArgumentException if URL is invalid or uses an unsupported protocol
     * @throws NullPointerException     if {@code url} is {@code null}
     */
    public static UiButtonAction openUrl(String url) {
        URI uri = URI.create(Objects.requireNonNull(url, "url"));
        if (!"http".equalsIgnoreCase(uri.getScheme())
                && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("button URL must use http or https");
        }
        return new UiButtonAction(Type.OPEN_URL, uri.toString(),
                Component.text("[Open link]", NamedTextColor.AQUA));
    }

    /**
     * Creates an action to execute a command as the clicking player.
     *
     * @param command command to execute (leading slash {@code /} is optional)
     * @return player-command {@link UiButtonAction}
     * @throws IllegalArgumentException if command is blank
     * @throws NullPointerException     if {@code command} is {@code null}
     */
    public static UiButtonAction playerCommand(String command) {
        return command(Type.RUN_PLAYER_COMMAND, command);
    }

    /**
     * Alias for {@link #playerCommand(String)}.
     *
     * @param command command to execute
     * @return player-command {@link UiButtonAction}
     */
    public static UiButtonAction executeCommand(String command) {
        return playerCommand(command);
    }

    /**
     * Creates an action to execute a command from the server console.
     *
     * @param command console command to execute
     * @return console-command {@link UiButtonAction}
     * @throws IllegalArgumentException if command is blank
     * @throws NullPointerException     if {@code command} is {@code null}
     */
    public static UiButtonAction consoleCommand(String command) {
        return command(Type.RUN_CONSOLE_COMMAND, command);
    }

    /**
     * Creates an action to suggest a command into the player's chat box.
     *
     * @param command command to suggest
     * @return suggest-command {@link UiButtonAction}
     * @throws IllegalArgumentException if command is blank
     * @throws NullPointerException     if {@code command} is {@code null}
     */
    public static UiButtonAction suggestCommand(String command) {
        String normalized = normalizeCommand(command);
        return new UiButtonAction(Type.SUGGEST_COMMAND, "/" + normalized,
                Component.text("[Use command]", NamedTextColor.GREEN));
    }

    /**
     * Creates a copy of this action with a different display label component.
     *
     * @param newLabel new Adventure {@link Component} label
     * @return a new {@link UiButtonAction} instance
     * @throws NullPointerException if {@code newLabel} is {@code null}
     */
    public UiButtonAction labeled(Component newLabel) {
        return new UiButtonAction(type, value,
                Objects.requireNonNull(newLabel, "newLabel"));
    }

    private static UiButtonAction command(Type type, String command) {
        String normalized = normalizeCommand(command);
        return new UiButtonAction(type, normalized, Component.empty());
    }

    private static String normalizeCommand(String command) {
        String normalized = Objects.requireNonNull(command, "command").trim();
        while (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.isBlank()) throw new IllegalArgumentException("command cannot be blank");
        return normalized;
    }
}
