package dev.haohansmp.displayui.api;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

@FunctionalInterface
public interface UiAudience {
    boolean canView(Player player);

    static UiAudience all() {
        return player -> true;
    }

    static UiAudience only(Set<UUID> viewers) {
        Set<UUID> immutable = Set.copyOf(viewers);
        return player -> immutable.contains(player.getUniqueId());
    }
}
