/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 */
package vn.haohan.displayui.runtime.scene.follow;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.view.UiFollowMode;
import vn.haohan.displayui.api.view.UiFollowOptions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UiFollowControllerTest {

    private UiFollowController controller;
    private Player player;
    private World world;

    @BeforeEach
    void setUp() {
        controller = new UiFollowController();
        player = mock(Player.class);
        world = mock(World.class);

        when(player.isOnline()).thenReturn(true);
        when(player.getWorld()).thenReturn(world);
    }

    @Test
    void testInitialState() {
        assertEquals(UiFollowMode.NONE, controller.mode());
        assertNull(controller.target());
        assertNull(controller.next(new Location(world, 0, 0, 0)));
    }

    @Test
    void testConfigureAndStop() {
        UiFollowOptions options = UiFollowOptions.of(3.0, 0.5f, 4);
        controller.configure(player, options);

        assertEquals(UiFollowMode.FOLLOW, controller.mode());
        assertEquals(player, controller.target());
        assertEquals(4, controller.interpolationTicks());

        controller.stop();
        assertEquals(UiFollowMode.NONE, controller.mode());
        assertNull(controller.target());
    }

    @Test
    void testNextReturnsNullWhenOfflineOrDifferentWorld() {
        controller.configure(player, UiFollowOptions.defaults());

        World otherWorld = mock(World.class);
        assertNull(controller.next(new Location(otherWorld, 0, 0, 0)));

        when(player.isOnline()).thenReturn(false);
        assertNull(controller.next(new Location(world, 0, 0, 0)));
    }

    @Test
    void testNextFollowsTarget() {
        controller.configure(player, UiFollowOptions.of(2.0, 0.5f, 4));

        Location eyeLocation = new Location(world, 0, 1.6, 0, 0, 0);
        when(player.getEyeLocation()).thenReturn(eyeLocation);

        Location current = new Location(world, 0, 1.6, 5, 180, 0);
        Location next = controller.next(current);

        assertNotNull(next);
        // Distance is 2 in front (yaw 0, pitch 0 -> direction (0, 0, 1) or (-sin yaw, 0, cos yaw) -> z: 2 or -z)
        // Lerp should move current towards targetLocation
        assertNotEquals(current.getZ(), next.getZ());
    }

    @Test
    void testNextSettlesAndReturnsNullWhenClose() {
        controller.configure(player, UiFollowOptions.of(2.0, 0.5f, 4));

        Location eyeLocation = new Location(world, 0, 1.6, 0, 0, 0);
        when(player.getEyeLocation()).thenReturn(eyeLocation);

        Location targetLocation = eyeLocation.clone().add(eyeLocation.getDirection().normalize().multiply(2.0));
        targetLocation.setPitch(0);
        targetLocation.setYaw(180.0f);

        // When current is already settled exactly at target
        Location next = controller.next(targetLocation);
        assertNull(next, "Should return null when already settled within deadzone threshold");
    }

    @Test
    void testSnapWhenFarAway() {
        controller.configure(player, UiFollowOptions.of(2.0, 0.5f, 4));

        Location eyeLocation = new Location(world, 100, 1.6, 100, 0, 0);
        when(player.getEyeLocation()).thenReturn(eyeLocation);

        Location current = new Location(world, 0, 1.6, 0, 180, 0);
        Location next = controller.next(current);

        assertNotNull(next);
        // Distance is > 16 blocks, should snap immediately to targetLocation
        assertEquals(100, next.getX(), 0.01);
        assertEquals(1.6, next.getY(), 0.01);
    }
}
