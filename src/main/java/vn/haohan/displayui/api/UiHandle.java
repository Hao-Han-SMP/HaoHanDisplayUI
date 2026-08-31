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
package vn.haohan.displayui.api;

import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.interaction.UiClickHandler;
import vn.haohan.displayui.api.interaction.UiControlChangeHandler;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.view.UiAudience;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface UiHandle {
    UUID id();
    String ownerKey();
    boolean isValid();
    void update(UiDocument document);
    void move(Location origin);
    void audience(UiAudience audience);
    void cameraTransform(UiCameraTransform transform);
    /** Starts or replaces the animation currently running on this scene. */
    void animate(UiAnimation animation);
    /** Animates each document node independently; list index matches node index. */
    void animateNodes(List<UiAnimation> animations);
    /** Stops the current animation and restores the scene's final state. */
    void stopAnimation();
    boolean isAnimating();
    default int nodeCount() { return 0; }
    Optional<vn.haohan.displayui.api.interaction.UiControl> control(String id);
    void onClick(UiClickHandler handler);
    void onControlChange(UiControlChangeHandler handler);
    void clearControlChangeHandlers();
    void clearClickHandlers();
    void show(Player player);
    void hide(Player player);
    void remove();
}
