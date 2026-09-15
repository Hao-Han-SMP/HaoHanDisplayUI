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
import vn.haohan.displayui.api.view.UiFollowOptions;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

/**
 * Handle controlling an active Display UI instance rendered in the Minecraft world.
 * <p>
 * {@code UiHandle} provides the complete runtime lifecycle and manipulation API for a spawned UI:
 * updating document structure, moving world position, applying animations, configuring camera billboards,
 * following players, registering interaction handlers (clicks, control changes), and removing the UI.
 */
public interface UiHandle {

    /**
     * Returns the unique identifier of this UI instance.
     *
     * @return unique {@link UUID} of the UI instance
     */
    UUID id();

    /**
     * Returns the plugin or module namespace key that owns this UI instance.
     *
     * @return owning plugin key string
     */
    String ownerKey();

    /**
     * Checks if this UI instance is still valid and actively rendered in the world.
     *
     * @return {@code true} if active and alive; {@code false} if destroyed or world unloaded
     */
    boolean isValid();

    /**
     * Updates the UI layout and content to a new document.
     * <p>
     * The underlying engine computes an optimized visual diff between the current and new document,
     * updating, spawning, or despawning display entities seamlessly without visual flickering.
     *
     * @param document new UI document specification
     * @throws NullPointerException if {@code document} is {@code null}
     */
    void update(UiDocument document);

    /**
     * Teleports or smoothly moves the UI root origin to a new location in the world.
     *
     * @param origin new world origin coordinates
     * @throws NullPointerException if {@code origin} is {@code null}
     */
    void move(Location origin);

    /**
     * Updates the target audience allowed to view this UI instance.
     *
     * @param audience audience specification defining visible players
     * @throws NullPointerException if {@code audience} is {@code null}
     */
    void audience(UiAudience audience);

    /**
     * Sets the camera orientation and billboard transformation mode for this UI.
     *
     * @param transform billboard and camera transform configuration
     * @throws NullPointerException if {@code transform} is {@code null}
     */
    void cameraTransform(UiCameraTransform transform);

    /**
     * Enables or disables double-sided rendering across all nodes in this UI.
     *
     * @param enabled {@code true} to render display entities for both front and back faces; {@code false} for front only
     */
    void doubleSided(boolean enabled);

    /**
     * Configures whether the reverse/back side of the UI has its horizontal layout mirrored.
     *
     * @param enabled {@code true} to mirror coordinates on the back side; {@code false} to preserve orientation
     */
    void mirrorSide(boolean enabled);

    /**
     * Convenience method to configure both double-sided rendering and back-side mirroring in one call.
     *
     * @param doubleSided {@code true} to enable two-sided rendering
     * @param mirrorSide  {@code true} to horizontally mirror the back side
     */
    default void sides(boolean doubleSided, boolean mirrorSide) {
        doubleSided(doubleSided);
        mirrorSide(mirrorSide);
    }

    /**
     * Starts or overrides a global scene animation applied to this UI.
     *
     * @param animation animation definition to execute
     * @throws NullPointerException if {@code animation} is {@code null}
     */
    void animate(UiAnimation animation);

    /**
     * Applies distinct node animations mapped by element index within the document.
     *
     * @param animations list of animations corresponding to document nodes by index
     * @throws NullPointerException if {@code animations} is {@code null}
     */
    void animateNodes(List<UiAnimation> animations);

    /**
     * Stops any currently playing animations and immediately snaps entities to their final resting state.
     */
    void stopAnimation();

    /**
     * Checks if this UI is currently executing an active animation.
     *
     * @return {@code true} if animating; {@code false} otherwise
     */
    boolean isAnimating();

    /**
     * Returns the total number of rendered display nodes in this UI instance.
     *
     * @return number of active nodes
     */
    default int nodeCount() { return 0; }

    /**
     * Finds an interactive control (button, slider, checkbox, scroll list) by its unique identifier.
     *
     * @param id control ID to search for
     * @return an {@link Optional} containing the {@link vn.haohan.displayui.api.interaction.UiControl} if found, or empty if absent
     */
    Optional<vn.haohan.displayui.api.interaction.UiControl> control(String id);

    /**
     * Registers a listener to receive player interaction click events on this UI.
     *
     * @param handler click handler callback
     * @throws NullPointerException if {@code handler} is {@code null}
     * @see #clearClickHandlers()
     */
    void onClick(UiClickHandler handler);

    /**
     * Registers a listener to receive control state changes (slider drags, checkbox toggles, etc.).
     *
     * @param handler change handler callback
     * @throws NullPointerException if {@code handler} is {@code null}
     * @see #clearControlChangeHandlers()
     */
    void onControlChange(UiControlChangeHandler handler);

    /**
     * Removes all registered control change listeners from this UI.
     */
    void clearControlChangeHandlers();

    /**
     * Removes all registered click interaction listeners from this UI.
     */
    void clearClickHandlers();

    /**
     * Adds a player to the visible audience of this UI instance.
     *
     * @param player player to reveal the UI to
     * @throws NullPointerException if {@code player} is {@code null}
     */
    void show(Player player);

    /**
     * Removes a player from the visible audience of this UI instance.
     *
     * @param player player to hide the UI from
     * @throws NullPointerException if {@code player} is {@code null}
     */
    void hide(Player player);

    /**
     * Despawns and permanently destroys this UI instance, cleaning up all Minecraft display entities.
     */
    void remove();

    /**
     * Configures the animation easing and duration used when scrollable lists change scroll offsets.
     *
     * @param animation scroll animation configuration
     * @throws NullPointerException if {@code animation} is {@code null}
     * @see #clearScrollAnimation()
     */
    void scrollAnimation(UiScrollAnimation animation);

    /**
     * Disables smooth scroll animations, causing scroll lists to jump instantaneously to target offsets.
     */
    default void clearScrollAnimation() {
        scrollAnimation(UiScrollAnimation.none());
    }

    /**
     * Activates player-following mode using default following parameters.
     *
     * @param player target player to follow
     * @throws NullPointerException if {@code player} is {@code null}
     * @see #follow(Player, UiFollowOptions)
     */
    void follow(Player player);

    /**
     * Activates player-following mode with custom distance, damping, and interpolation options.
     *
     * @param player  target player to follow
     * @param options customized following settings
     * @throws NullPointerException if {@code player} or {@code options} is {@code null}
     * @see #stopFollow()
     */
    void follow(Player player, UiFollowOptions options);

    /**
     * Convenience method to activate player-following mode with explicit parameter values.
     *
     * @param player              target player to follow
     * @param distance            distance maintained from player eye location in blocks
     * @param damping             spring/damping coefficient between 0.0f and 1.0f
     * @param interpolationTicks  interpolation duration in server ticks for smooth repositioning
     */
    default void follow(Player player, double distance, float damping, int interpolationTicks) {
        follow(player, UiFollowOptions.of(distance, damping, interpolationTicks));
    }

    /**
     * Returns current follow configuration options, or default options if follow mode is disabled.
     *
     * @return active {@link UiFollowOptions}
     */
    UiFollowOptions followOptions();

    /**
     * Stops following any player and locks the UI at its current world location.
     */
    void stopFollow();

    /**
     * Returns the active following mode behavior (DISABLED, POSITION, LOOK_AT, FULL).
     *
     * @return current {@link UiFollowMode}
     */
    UiFollowMode followMode();

    /**
     * Returns the player currently targeted for following, if any.
     *
     * @return targeted {@link Player}, or {@code null} if following is disabled
     */
    Player followTarget();
}

