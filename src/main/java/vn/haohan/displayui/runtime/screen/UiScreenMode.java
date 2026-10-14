/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.\n *
 * HaoHanDisplayUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HaoHanDisplayUI. If not, see <https://www.gnu.org/licenses/>.
 */
package vn.haohan.displayui.runtime.screen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import vn.haohan.displayui.api.UiHit;
import vn.haohan.displayui.runtime.scene.UiScene;
import vn.haohan.displayui.runtime.scene.visibility.UiCameraBasis;

import java.util.Objects;

/**
 * <ul>
 *   <li>Mounts player on an invisible, silent, non-moving dummy tripod (Pig).</li>
 *   <li>Sets camera view to tripod via ClientboundSetCameraPacket.</li>
 *   <li>Places the player in invisible mode.</li>
 *   <li>Draws/projects the UI at a configurable distance in front of eye level.</li>
 *   <li>Uses exact trigonometric tangent projection {@code project(angle, distance, edge)}
 *       to calculate virtual cursor position every tick.</li>
 *   <li>Synchronizes virtual cursor world location directly on the UI plane with Billboard.CENTER
 *       and setSeeThrough(true) to completely eliminate backface culling.</li>
 *   <li>Filters duplicate hover events by target ID to avoid repeat sound and actionbar flicker.</li>
 *   <li>Smooth cursor interpolation with {@code teleportDuration=1}.</li>
 *   <li>Shift / Sneak naturally dismounts the tripod, closing the GUI cleanly.</li>
 * </ul>
 */
public final class UiScreenMode {

    public static final double DEFAULT_DISTANCE = 2.5;
    public static final double DEFAULT_SENSITIVITY = 0.25;
    private static final float CURSOR_SCALE = 0.6f;
    private static final float TEXT_CENTER = 0.11f;
    private static final long CLICK_DELAY = 250L;
    private static final long SLIDER_HOLD_TIMEOUT_MS = 400L;
    private static final double CURSOR_DEPTH_OFFSET = 0.25;
    private static final String CURSOR_CHAR = "\u2A79";

    private final Player player;
    private final UiScene scene;
    private final Location originalSceneOrigin;

    private double distance = DEFAULT_DISTANCE;
    private double sensitivity = DEFAULT_SENSITIVITY;
    private Location start;
    private Location eye;
    private Vector screenCenter;
    private Pig tripod;
    private TextDisplay cursorEntity;

    private double edgeX = 2.5;
    private double edgeY = 2.5;
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    private float centerX;
    private float centerY;
    private float ppb;

    private float cursorX;
    private float cursorY;
    private float lastPlayerYaw;
    private float lastPlayerPitch;
    private boolean initializedAngles;
    private String activeSliderId;
    private long lastSliderHoldTime;
    private UiHit hovered;
    private long lastClick;
    private boolean active;

    public UiScreenMode(Player player, UiScene scene) {
        this(player, scene, DEFAULT_DISTANCE, DEFAULT_SENSITIVITY);
    }

    public UiScreenMode(Player player, UiScene scene, double distance) {
        this(player, scene, distance, DEFAULT_SENSITIVITY);
    }

    public UiScreenMode(Player player, UiScene scene, double distance, double sensitivity) {
        this.player = Objects.requireNonNull(player, "player");
        this.scene = Objects.requireNonNull(scene, "scene");
        this.originalSceneOrigin = scene.origin().clone();
        setDistance(distance);
        setSensitivity(sensitivity);
    }

    /** Enters screen mode: mounts player onto the tripod and initializes the screen cursor. */
    public void enter() {
        if (active) return;
        active = true;

        // Clean up any stale cursor or tripod entities near player before spawning new ones
        for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 20, 20, 20)) {
            if (e.getScoreboardTags().contains("hhdui_screen_cursor")
                    || e.getScoreboardTags().contains("hhdui_screen_tripod")) {
                e.remove();
            }
        }

        float[] bounds = scene.calculateDocumentLocalBounds();
        this.minX = bounds[0];
        this.maxX = bounds[1];
        this.minY = bounds[2];
        this.maxY = bounds[3];
        this.centerX = (minX + maxX) / 2.0f;
        this.centerY = (minY + maxY) / 2.0f;
        this.ppb = scene.pixelsPerBlock();

        this.cursorX = this.centerX;
        this.cursorY = this.centerY;
        this.lastPlayerYaw = this.player.getYaw();
        this.lastPlayerPitch = this.player.getPitch();
        this.initializedAngles = true;
        this.activeSliderId = null;

        double docW = Math.max(0.5, (maxX - minX) / ppb);
        double docH = Math.max(0.5, (maxY - minY) / ppb);
        this.edgeX = (docW / 2.0);
        this.edgeY = (docH / 2.0);

        this.lastClick = System.currentTimeMillis();
        mount();

        // Position the UI scene at distance in front of eye, centered and facing the player
        Vector forward = eye.getDirection().setY(0).normalize();
        if (forward.lengthSquared() < 0.0001) forward = new Vector(0, 0, 1);

        float uiYaw = (eye.getYaw() + 180.0f) % 360.0f;
        Location orient = eye.clone();
        orient.setYaw(uiYaw);
        orient.setPitch(0.0f);

        UiCameraBasis basis = UiCameraBasis.forFixedPlane(orient, scene.cameraTransform());

        this.screenCenter = eye.toVector().add(forward.clone().multiply(distance));
        Vector originPos = screenCenter.clone()
                .subtract(basis.right().multiply(centerX / ppb))
                .add(basis.up().multiply(centerY / ppb));

        Location sceneOrigin = originPos.toLocation(player.getWorld());
        sceneOrigin.setYaw(uiYaw);
        sceneOrigin.setPitch(0.0f);
        scene.move(sceneOrigin, 0);

        // Spawn smooth cursor text display, guaranteed visible above all layers without clipping
        Vector toEye = eye.toVector().subtract(screenCenter).normalize();
        Vector initialCursorPos = sceneOrigin.toVector()
                .add(basis.right().multiply(centerX / ppb))
                .add(basis.up().multiply(-centerY / ppb))
                .add(toEye.multiply(CURSOR_DEPTH_OFFSET));

        Location cursorStart = initialCursorPos.toLocation(player.getWorld());
        cursorEntity = eye.getWorld().spawn(cursorStart, TextDisplay.class, display -> {
            display.setPersistent(false);
            display.text(Component.text(CURSOR_CHAR, NamedTextColor.WHITE));
            display.setShadowed(true);
            display.setBillboard(Display.Billboard.CENTER);
            display.setSeeThrough(false);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setTeleportDuration(1);
            display.setInterpolationDuration(1);
            display.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            display.setDefaultBackground(false);
            display.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new Quaternionf().rotateZ((float) Math.toRadians(-45.0)),
                    new Vector3f(CURSOR_SCALE * 0.45f, CURSOR_SCALE * 0.45f, CURSOR_SCALE * 0.45f),
                    new Quaternionf()
            ));
            display.addScoreboardTag("hhdui_screen_cursor");
        });

        playSound(player, Sound.UI_TOAST_IN, 1.4f);
        tick();
    }

    /** Mounts the player onto an invisible tripod pig and sets camera. */
    private void mount() {
        this.start = this.player.getLocation().clone();
        this.eye = this.player.getEyeLocation().clone();
        this.eye.setPitch(0F);

        this.player.leaveVehicle();
        this.player.setRotation(this.eye.getYaw(), 0F);
        this.player.setInvisible(true);

        this.tripod = this.eye.getWorld().spawn(this.eye, Pig.class, pig -> {
            pig.setInvisible(true);
            pig.setAI(false);
            pig.setGravity(false);
            pig.setSilent(true);
            pig.setInvulnerable(true);
            pig.setPersistent(false);
            pig.addScoreboardTag("hhdui_screen_tripod");
        });

        Location tripodLoc = this.eye.clone().subtract(0, this.tripod.getEyeHeight(), 0);
        tripodLoc.setYaw(this.eye.getYaw());
        tripodLoc.setPitch(0F);
        this.tripod.teleport(tripodLoc);
        this.tripod.setRotation(this.eye.getYaw(), 0F);
        this.tripod.addPassenger(this.player);
        setCamera(this.player, this.tripod);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("HaoHanDisplayUI");
        if (plugin != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> this.player.sendActionBar(Component.empty()), 2L);
        }
    }

    /** Ticks the screen mode: projects player yaw & pitch to cursor and performs hit detection. */
    public void tick() {
        if (!active || !player.isOnline()) return;

        if (tripod == null || !tripod.isValid() || !tripod.getPassengers().contains(player)) {
            exit();
            return;
        }

        if (!initializedAngles) {
            this.lastPlayerYaw = this.player.getYaw();
            this.lastPlayerPitch = this.player.getPitch();
            this.initializedAngles = true;
        }

        final float currentYaw = this.player.getYaw();
        final float currentPitch = this.player.getPitch();

        final float deltaYaw = angleDelta(currentYaw, this.lastPlayerYaw);
        final float deltaPitch = currentPitch - this.lastPlayerPitch;

        this.lastPlayerYaw = currentYaw;
        this.lastPlayerPitch = currentPitch;

        // Linear mouse response: eliminates non-linear tangent acceleration curve
        final float speed = (float) (ppb * distance * 0.045 * sensitivity);
        final float moveX = deltaYaw * speed;
        final float moveY = deltaPitch * speed;

        // Instantly clamped to document bounds: counter-movement responds immediately without dead zone
        this.cursorX = Math.max(minX, Math.min(maxX, this.cursorX + moveX));
        this.cursorY = Math.max(minY, Math.min(maxY, this.cursorY + moveY));

        // Sync cursor world location accurately on the UI plane (1:1 with cursorX and cursorY)
        if (cursorEntity != null && cursorEntity.isValid()) {
            UiCameraBasis basis = UiCameraBasis.forFixedPlane(scene.origin(), scene.cameraTransform());
            Vector center = (screenCenter != null ? screenCenter : eye.toVector().add(eye.getDirection().setY(0).normalize().multiply(distance)));
            Vector toEye = eye.toVector().subtract(center).normalize();
            Vector cursorWorldPos = scene.origin().toVector()
                    .add(basis.right().multiply(cursorX / ppb))
                    .add(basis.up().multiply(-cursorY / ppb))
                    .add(toEye.multiply(CURSOR_DEPTH_OFFSET));
            Location cursorLoc = cursorWorldPos.toLocation(player.getWorld());
            this.cursorEntity.teleport(cursorLoc);
        }

        // Handle active slider dragging session (Hold to Drag)
        if (this.activeSliderId != null) {
            java.util.Optional<vn.haohan.displayui.api.interaction.UiControl> ctrl = scene.control(activeSliderId);
            if (ctrl.isEmpty() || !(ctrl.get() instanceof vn.haohan.displayui.api.interaction.UiSlider)) {
                releaseSlider();
            } else {
                final long now = System.currentTimeMillis();
                // If user stopped holding/moving for longer than timeout, auto-release slider
                if (now - this.lastSliderHoldTime > SLIDER_HOLD_TIMEOUT_MS) {
                    releaseSlider();
                } else {
                scene.dragSlider(player, activeSliderId, cursorX);
                if (cursorEntity != null && cursorEntity.isValid()) {
                    cursorEntity.text(Component.text(CURSOR_CHAR, NamedTextColor.GOLD));
                }
                player.sendActionBar(Component.text("✦ Giữ chuột để kéo Slider · Thả chuột để thả", NamedTextColor.YELLOW));
                return;
                }
            }
        }

        final UiHit button = scene.hitAt(player, cursorX, cursorY);

        // Prevent redundant sound trigger and actionbar flicker if hovering the same target
        if (isSameTarget(button, this.hovered)) {
            this.hovered = button;
            return;
        }

        if (this.hovered != null) {
            // Unhover previous
            if (cursorEntity != null && cursorEntity.isValid()) {
                cursorEntity.text(Component.text(CURSOR_CHAR, NamedTextColor.WHITE));
            }
            player.sendActionBar(Component.empty());
        }

        if (button != null) {
            if (cursorEntity != null && cursorEntity.isValid()) {
                cursorEntity.text(Component.text(CURSOR_CHAR, NamedTextColor.AQUA));
            }
            playSound(this.player, Sound.BLOCK_NOTE_BLOCK_HAT, 1.6f);
            Component desc = button.button() != null ? button.button().description() : button.control().description();
            if (desc != null && !desc.equals(Component.empty())) {
                player.sendActionBar(desc);
            }
        }

        this.hovered = button;
    }

    /** Checks if two UiHit instances point to the same interactive button or control. */
    private static boolean isSameTarget(UiHit a, UiHit b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.button() != null && b.button() != null) {
            return Objects.equals(a.button().id(), b.button().id());
        }
        if (a.control() != null && b.control() != null) {
            return Objects.equals(a.control().id(), b.control().id());
        }
        return false;
    }

    /** Clicks the button or control under the virtual cursor. */
    public boolean handleClick() {
        return handleClick(false);
    }

    public boolean handleLeftClick() {
        return handleClick(false);
    }

    public boolean handleRightClick() {
        return handleClick(true);
    }

    public boolean handleClick(boolean isRightClick) {
        if (!active) return false;

        // If currently dragging a slider
        if (this.activeSliderId != null) {
            if (isRightClick && (this.hovered == null || (this.hovered.control() != null && this.hovered.control().id().equals(activeSliderId)))) {
                // Continuous right-click hold packets from client refresh hold time
                this.lastSliderHoldTime = System.currentTimeMillis();
                return true;
            }
            // Explicit left click or clicking outside releases the slider immediately
            releaseSlider();
            if (this.hovered == null) return true;
        }

        final long now = System.currentTimeMillis();
        if (now - this.lastClick < CLICK_DELAY) return false;
        this.lastClick = now;

        if (this.hovered == null) return false;

        // If clicked on a slider, begin slider dragging hold session
        if (this.hovered.control() instanceof vn.haohan.displayui.api.interaction.UiSlider slider) {
            this.activeSliderId = slider.id();
            this.lastSliderHoldTime = System.currentTimeMillis();
            scene.dragSlider(player, activeSliderId, cursorX);
            if (cursorEntity != null && cursorEntity.isValid()) {
                cursorEntity.text(Component.text(CURSOR_CHAR, NamedTextColor.GOLD));
            }
            playSound(this.player, Sound.UI_BUTTON_CLICK, 1.2f);
            return true;
        }

        // For buttons and other controls, activate via scene without duplicate sound
        return scene.activate(this.hovered);
    }

    private void releaseSlider() {
        if (this.activeSliderId == null) return;
        this.activeSliderId = null;
        if (cursorEntity != null && cursorEntity.isValid()) {
            cursorEntity.text(Component.text(CURSOR_CHAR, this.hovered != null ? NamedTextColor.AQUA : NamedTextColor.WHITE));
        }
        player.sendActionBar(Component.empty());
        playSound(this.player, Sound.UI_BUTTON_CLICK, 0.9f);
    }

    /** Scrolls the scroll list under the virtual cursor. */
    public boolean handleScroll(int direction) {
        if (!active || direction == 0) return false;
        UiHit hit = scene.scrollHitAt(player, cursorX, cursorY);
        if (hit == null || !(hit.control() instanceof vn.haohan.displayui.api.interaction.UiScrollList scroll)) return false;
        return scene.scroll(hit, scroll.offset() + direction * scroll.step());
    }

    /** Exits screen mode: cleans up entities, restores player camera & location. */
    public void exit() {
        if (!active) return;
        active = false;
        this.activeSliderId = null;
        this.initializedAngles = false;

        if (cursorEntity != null && cursorEntity.isValid()) {
            cursorEntity.remove();
        }
        cursorEntity = null;

        if (player.isOnline()) {
            this.player.leaveVehicle();
            setCamera(this.player, this.player);
            if (tripod != null && tripod.isValid()) {
                this.tripod.remove();
            }
            tripod = null;
            this.player.setInvisible(false);
            playSound(this.player, Sound.UI_TOAST_OUT, 1.0f);
            if (start != null) {
                this.player.teleport(this.start);
            }
            this.player.sendActionBar(Component.empty());
        } else {
            if (tripod != null && tripod.isValid()) {
                this.tripod.remove();
            }
            tripod = null;
        }

        if (scene.isValid() && originalSceneOrigin != null) {
            scene.move(originalSceneOrigin, 0);
        }
    }

    /**
     * Turns a spot on the panel (x, y) into a 3D world position relative to eye location.
     * Matches {@code Util.toWorld} from tutorial video.
     */
    public static Location toWorld(Location eye, double x, double y, double distance) {
        final Vector forward = eye.getDirection();
        final Vector right = forward.clone().rotateAroundY(-Math.PI / 2);
        return eye.clone().add(forward.multiply(distance)).add(right.multiply(x)).add(0, y, 0);
    }

    /**
     * Calculates angular delta normalized between -180 and 180 degrees.
     */
    public static float angleDelta(float current, float previous) {
        return Location.normalizeYaw(current - previous);
    }

    /**
     * Projects angle deviation onto a 2D surface distance using tangent formula,
     * clamping to edge. Matches {@code Util.project} from tutorial video.
     */
    public static double project(float angle, double distance, double edge) {
        final float turned = Location.normalizeYaw(angle);

        if (Math.abs(turned) > 80) {
            return turned > 0 ? edge : -edge;
        }

        return Math.max(-edge, Math.min(edge, Math.tan(Math.toRadians(turned)) * distance));
    }

    /** Plays a sound to the player at volume 0.6f. Matches {@code Util.playSound}. */
    public static void playSound(Player player, Sound sound, float pitch) {
        player.playSound(player.getLocation(), sound, 0.6f, pitch);
    }

    /**
     * Sends ClientboundSetCameraPacket to the player to attach/detach camera viewpoint.
     * Matches {@code Util.setCamera} from tutorial video.
     */
    public static void setCamera(Player player, Entity target) {
        try {
            Object playerHandle = player.getClass().getMethod("getHandle").invoke(player);
            Object targetHandle = target.getClass().getMethod("getHandle").invoke(target);

            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetCameraPacket");
            java.lang.reflect.Constructor<?> packetConstructor = null;
            for (var constructor : packetClass.getConstructors()) {
                if (constructor.getParameterCount() == 1) {
                    packetConstructor = constructor;
                    break;
                }
            }
            if (packetConstructor == null) return;
            Object packet = packetConstructor.newInstance(targetHandle);

            java.lang.reflect.Field connectionField = playerHandle.getClass().getField("connection");
            Object connection = connectionField.get(playerHandle);

            for (var method : connection.getClass().getMethods()) {
                if (method.getName().equals("send") && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isAssignableFrom(packetClass)) {
                    method.invoke(connection, packet);
                    break;
                }
            }
        } catch (Throwable ignored) {
            // Graceful fallback if NMS security restricts reflection
        }
    }

    public double distance() { return distance; }

    public void setDistance(double distance) {
        this.distance = Math.max(0.8, Math.min(10.0, distance));
    }

    public double sensitivity() { return sensitivity; }

    public void setSensitivity(double sensitivity) {
        this.sensitivity = Math.max(0.05, Math.min(2.0, sensitivity));
    }

    public float cursorX() { return cursorX; }
    public float cursorY() { return cursorY; }
    public boolean isActive() { return active; }
    public Player player() { return player; }
    public UiScene scene() { return scene; }
    public Pig tripodEntity() { return tripod; }
    public Location lockedPosition() { return tripod != null ? tripod.getLocation() : null; }
}
