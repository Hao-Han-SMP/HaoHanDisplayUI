package dev.haohansmp.displayui.api;

import org.bukkit.entity.Player;

/** Data delivered to scene-local click callbacks. */
public record UiClick(
        UiHandle handle,
        UiButton button,
        Player player,
        float localX,
        float localY,
        double distance
) {}
