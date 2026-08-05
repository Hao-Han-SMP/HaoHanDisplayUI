package dev.haohansmp.displayui.runtime;

import dev.haohansmp.displayui.api.UiButton;
import org.bukkit.entity.Player;

record UiHit(
        UiScene scene,
        UiButton button,
        Player player,
        float localX,
        float localY,
        double distance
) {}
