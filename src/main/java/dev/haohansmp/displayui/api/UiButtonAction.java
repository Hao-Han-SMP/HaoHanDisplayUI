package dev.haohansmp.displayui.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.net.URI;
import java.util.Objects;

/** Trusted action executed by the engine after an uncancelled button click. */
public record UiButtonAction(Type type, String value, Component label) {
    public enum Type {
        NONE,
        OPEN_URL,
        RUN_PLAYER_COMMAND,
        RUN_CONSOLE_COMMAND,
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

    public static UiButtonAction none() {
        return new UiButtonAction(Type.NONE, "", Component.empty());
    }

    public static UiButtonAction openUrl(String url) {
        URI uri = URI.create(Objects.requireNonNull(url, "url"));
        if (!"http".equalsIgnoreCase(uri.getScheme())
                && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("button URL must use http or https");
        }
        return new UiButtonAction(Type.OPEN_URL, uri.toString(),
                Component.text("[Open link]", NamedTextColor.AQUA));
    }

    public static UiButtonAction playerCommand(String command) {
        return command(Type.RUN_PLAYER_COMMAND, command);
    }

    /** Executes immediately as the clicking player and respects their permissions. */
    public static UiButtonAction executeCommand(String command) {
        return playerCommand(command);
    }

    public static UiButtonAction consoleCommand(String command) {
        return command(Type.RUN_CONSOLE_COMMAND, command);
    }

    public static UiButtonAction suggestCommand(String command) {
        String normalized = normalizeCommand(command);
        return new UiButtonAction(Type.SUGGEST_COMMAND, "/" + normalized,
                Component.text("[Use command]", NamedTextColor.GREEN));
    }

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
