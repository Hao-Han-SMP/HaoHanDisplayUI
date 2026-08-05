package dev.haohansmp.displayui.api;

import org.bukkit.Location;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface DisplayUiService {
    UiHandle create(String ownerKey, Location origin, UiDocument document);
    UiHandle create(String ownerKey, Location origin, UiDocument document,
                    UiOptions options, UiAudience audience);
    Optional<UiHandle> find(UUID id);
    Collection<UiHandle> active();
    int removeOwnedBy(String ownerKey);
}
