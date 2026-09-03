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
import vn.haohan.displayui.api.interaction.UiScrollAnimation;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.view.UiAudience;
import vn.haohan.displayui.api.view.UiFollowMode;
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
    /** Enables or disables two-sided rendering for every node in this scene. */
    void doubleSided(boolean enabled);
    /** Sets whether the rendered back side uses mirrored horizontal coordinates. */
    void mirrorSide(boolean enabled);
    /** Configures two-sided rendering and back-side mirroring in one call. */
    default void sides(boolean doubleSided, boolean mirrorSide) {
        doubleSided(doubleSided);
        mirrorSide(mirrorSide);
    }
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

    /** Sets the animation factory used when a scroll list offset changes. */
    void scrollAnimation(UiScrollAnimation animation);

    /** Disables scroll transitions; list updates remain fully functional. */
    default void clearScrollAnimation() {
        scrollAnimation(UiScrollAnimation.none());
    }

    /** Configures player camera follow mode with default distance and smoothing. */
    void follow(Player player, UiFollowMode mode);

    /** Configures player camera follow mode with custom distance. */
    void follow(Player player, UiFollowMode mode, double distance);

    /** Configures player camera follow mode with custom distance and smooth damping factor (0.0 to 1.0). */
    void follow(Player player, UiFollowMode mode, double distance, float damping);

    /** Disables player follow mode and leaves the UI at its current world position. */
    void stopFollow();

    /** Returns the active follow mode. */
    UiFollowMode followMode();

    /** Returns the target player being followed, or null if follow is disabled. */
    Player followTarget();
}
