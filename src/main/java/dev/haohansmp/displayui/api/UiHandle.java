package dev.haohansmp.displayui.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface UiHandle {
    UUID id();
    String ownerKey();
    boolean isValid();
    void update(UiDocument document);
    void move(Location origin);
    void audience(UiAudience audience);
    void cameraTransform(UiCameraTransform transform);
    void onClick(UiClickHandler handler);
    void clearClickHandlers();
    void show(Player player);
    void hide(Player player);
    void remove();
}
