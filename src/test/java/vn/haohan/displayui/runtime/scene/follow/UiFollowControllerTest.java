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

    @Test
    void testGazeInsideBoundsStaysStationary() {
        // Configure with explicit bounds [-100, 100, -50, 50]
        UiFollowOptions options = UiFollowOptions.builder()
                .distance(2.0)
                .bounds(-100f, 100f, -50f, 50f)
                .deadzoneMargin(10f)
                .gazeDeadzone(true)
                .build();
        controller.configure(player, options);

        // Player looking along +Z
        Location eyeLocation = new Location(world, 0, 1.6, 0, 0, 0);
        when(player.getEyeLocation()).thenReturn(eyeLocation);

        // UI is at distance 2.0 along +Z facing back toward player
        Location current = new Location(world, 0, 1.6, 2.0, 180, 0);

        // Player is looking directly at center (0, 0), well within bounds
        Location next = controller.next(current, 75f, -100f, 100f, -50f, 50f);
        assertNull(next, "Should stay stationary when crosshair is inside panel bounds");
        assertFalse(controller.isCatchingUp());
    }

    @Test
    void testGazeOutsideBoundsCatchesUp() {
        UiFollowOptions options = UiFollowOptions.builder()
                .distance(2.0)
                .bounds(-50f, 50f, -50f, 50f)
                .deadzoneMargin(5f)
                .gazeDeadzone(true)
                .build();
        controller.configure(player, options);

        // Player looks 45 degrees to the right (yaw 45)
        Location eyeLocation = new Location(world, 0, 1.6, 0, 45, 0);
        when(player.getEyeLocation()).thenReturn(eyeLocation);

        // UI is still at (0, 1.6, 2.0)
        Location current = new Location(world, 0, 1.6, 2.0, 180, 0);

        Location next = controller.next(current, 75f, -50f, 50f, -50f, 50f);
        assertNotNull(next, "Should catch up when gaze is outside panel bounds");
        assertTrue(controller.isCatchingUp());
    }

    @Test
    void testPitchInversionFacesPlayer() {
        // Player looks up 30 degrees (Minecraft pitch = -30)
        Location eyeUp = new Location(world, 0, 1.6, 0, 0, -30);
        Location targetUp = UiFollowController.calculateIdealTarget(eyeUp, 2.0);
        assertEquals(30.0f, targetUp.getPitch(), 0.01f, "UI pitch should be +30 to face downward toward player's eyes");
        assertEquals(180.0f, targetUp.getYaw(), 0.01f);

        // Player looks down 45 degrees (Minecraft pitch = +45)
        Location eyeDown = new Location(world, 0, 1.6, 0, 0, 45);
        Location targetDown = UiFollowController.calculateIdealTarget(eyeDown, 2.0);
        assertEquals(-45.0f, targetDown.getPitch(), 0.01f, "UI pitch should be -45 to face upward toward player's eyes");
    }

    @Test
    void testDockingThreshold() {
        UiFollowOptions options = UiFollowOptions.builder()
                .distance(2.0)
                .gazeDeadzone(true)
                .build();
        controller.configure(player, options);

        Location eyeLocation = new Location(world, 0, 1.6, 0, 0, 0);
        when(player.getEyeLocation()).thenReturn(eyeLocation);

        Location ideal = UiFollowController.calculateIdealTarget(eyeLocation, 2.0);
        // Place current very close to ideal (distSq < 0.02, yaw < 3, pitch < 3) but gaze outside
        Location current = ideal.clone().add(0.05, 0, 0.05);

        // Default bounds 500..600 ensures gaze at (0, 0) is outside panel bounds
        Location next = controller.next(current, 75f, 500f, 600f, 500f, 600f);
        // It docks to ideal target and isCatchingUp becomes false
        assertNotNull(next);
        assertEquals(ideal.getX(), next.getX(), 0.001);
        assertEquals(ideal.getY(), next.getY(), 0.001);
        assertEquals(ideal.getZ(), next.getZ(), 0.001);
        assertFalse(controller.isCatchingUp(), "isCatchingUp should be false after docking");
    }

    @Test
    void testRaycastProjection() {
        org.bukkit.util.Vector eyePos = new org.bukkit.util.Vector(0, 1.6, 0);
        org.bukkit.util.Vector rayDir = new org.bukkit.util.Vector(0, 0, 1);
        org.bukkit.util.Vector planeOrigin = new org.bukkit.util.Vector(0, 1.6, 2.0);
        org.bukkit.util.Vector planeDir = new org.bukkit.util.Vector(0, 0, -1);

        UiFollowController.RaycastResult res = UiFollowController.projectCrosshair(
                eyePos, rayDir, planeOrigin, planeDir, 75.0f, 5.0);

        assertTrue(res.hits());
        assertEquals(2.0, res.hitDistance(), 0.001);
        assertEquals(0.0f, res.localX(), 0.001f);
        assertEquals(0.0f, res.localY(), 0.001f);
    }

    @Test
    void testDocumentBoundsDetectionWithAllNodeTypes() {
        vn.haohan.displayui.api.node.UiGradientBackgroundNode gradientBg =
                new vn.haohan.displayui.api.node.UiGradientBackgroundNode(
                        -96f, -64f, 0f, 192f, 128f,
                        vn.haohan.displayui.api.gradient.UiGradient.horizontal(
                                org.bukkit.Color.RED, org.bukkit.Color.BLUE),
                        8, 8, false);

        vn.haohan.displayui.api.node.UiShapeNode shape =
                new vn.haohan.displayui.api.node.UiShapeNode(
                        "rounded_rect", 10f, 20f, 100f, 50f, 0.001f,
                        org.bukkit.Color.WHITE, false, org.bukkit.Color.BLACK,
                        1.0f, "solid", 5f, 0f, false);

        vn.haohan.displayui.api.interaction.UiButton button =
                new vn.haohan.displayui.api.interaction.UiButton("btn1", -80f, 50f, 30f, 15f);

        vn.haohan.displayui.api.UiDocument doc = vn.haohan.displayui.api.UiDocument.builder()
                .gradientBackground(gradientBg)
                .add(shape)
                .button(button)
                .build();

        float[] bounds = vn.haohan.displayui.runtime.scene.UiScene.calculateDocumentLocalBounds(doc, java.util.List.of());

        // Min X should be -96 (from gradientBg) or -84 with hitSlop
        assertTrue(bounds[0] <= -96f, "minX should be <= -96");
        // Max X should be at least 110 (10 + 100 from shape)
        assertTrue(bounds[1] >= 110f, "maxX should be >= 110");
        // Min Y should be -64 (from gradientBg)
        assertEquals(-64f, bounds[2], 0.001f, "minY should be -64");
        // Max Y should be at least 70 (20 + 50 from shape or 64 from gradientBg)
        assertTrue(bounds[3] >= 70f, "maxY should be >= 70");
    }

    @Test
    void testGazeFollowUsingSceneCursorProjection() {
        UiFollowOptions options = UiFollowOptions.builder()
                .distance(2.5)
                .gazeDeadzone(true)
                .deadzoneMargin(8f)
                .build();
        controller.configure(player, options);

        // Player is turning head at yaw 20, pitch -10
        Location eyeLocation = new Location(world, 10, 1.6, 10, 20, -10);
        when(player.getEyeLocation()).thenReturn(eyeLocation);

        Location current = new Location(world, 10, 1.6, 12.5, 200, 10);

        // Cursor projection hits the UI at (15px, -20px) which is well within [-96, 96] x [-64, 64]
        vn.haohan.displayui.utils.RaycastUtils.Projection cursorInside =
                new vn.haohan.displayui.utils.RaycastUtils.Projection(15.0f, -20.0f, 2.5);

        Location nextInside = controller.next(current, 75f, -96f, 96f, -64f, 64f, cursorInside);
        assertNull(nextInside, "UI must NOT move when cursor is inside document bounds, even when player rotates");
        assertFalse(controller.isCatchingUp());

        // Now cursor moves outside bounds: (150px, 0px)
        vn.haohan.displayui.utils.RaycastUtils.Projection cursorOutside =
                new vn.haohan.displayui.utils.RaycastUtils.Projection(150.0f, 0.0f, 2.5);

        Location nextOutside = controller.next(current, 75f, -96f, 96f, -64f, 64f, cursorOutside);
        assertNotNull(nextOutside, "UI must catch up when cursor is outside document bounds");
        assertTrue(controller.isCatchingUp());
    }
}
