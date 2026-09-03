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
package vn.haohan.displayui.runtime;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import vn.haohan.displayui.HaoHanDisplayUIPlugin;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.UiOptions;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiCheckbox;
import vn.haohan.displayui.api.interaction.UiClick;
import vn.haohan.displayui.api.interaction.UiClickHandler;
import vn.haohan.displayui.api.interaction.UiControl;
import vn.haohan.displayui.api.interaction.UiControlChange;
import vn.haohan.displayui.api.interaction.UiControlChangeHandler;
import vn.haohan.displayui.api.interaction.UiScrollList;
import vn.haohan.displayui.api.interaction.UiScrollAnimation;
import vn.haohan.displayui.api.interaction.UiSlider;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.node.UiModelRotation;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.EntityModelNode;
import vn.haohan.displayui.api.node.ItemNode;
import vn.haohan.displayui.api.node.LineNode;
import vn.haohan.displayui.api.node.MobEntityNode;
import vn.haohan.displayui.api.node.ParallelogramNode;
import vn.haohan.displayui.api.node.PolylineNode;
import vn.haohan.displayui.api.node.TextNode;
import vn.haohan.displayui.api.node.TriangleNode;
import vn.haohan.displayui.api.node.UiBackgroundNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.api.shape.DisplayShapeMath;
import vn.haohan.displayui.api.shape.TRSResult;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.view.UiAudience;
import vn.haohan.displayui.api.view.UiFollowMode;
import vn.haohan.displayui.utils.ColorUtils;
import vn.haohan.displayui.utils.GeometryUtils;
import vn.haohan.displayui.utils.MathUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class UiScene implements UiHandle {
    /** Target length for multi-tick transition animations. */
    private static final int INTERPOLATION_TICKS = 4;
    /**
     * Client-side interpolation window for per-tick animation targets.
     *
     * Animation frames are produced by the server scheduler at 20 Hz. A
     * one-tick window exposes every server-frame boundary as a visible step,
     * especially on high-refresh clients. Two ticks gives the client enough
     * samples to blend between frames while keeping the animation responsive.
     */
    private static final int ANIMATION_INTERPOLATION_TICKS = 2;
    /** Normalizes TextDisplay's native placeholder bounds to UI pixel bounds. */
    private static final float BACKGROUND_NATIVE_WIDTH_SCALE = 4.25f;
    private static final float BACKGROUND_NATIVE_HEIGHT_SCALE = 4.10f;
    /** Keeps the translucent background behind text, icons, and row blocks. */
    private static final float BACKGROUND_DEPTH_OFFSET = -0.02f;
    private final HaoHanDisplayUIPlugin plugin;
    private final UUID id;
    private final String ownerKey;
    private final UiOptions options;
    private final Consumer<UUID> onRemove;
    private final NamespacedKey sceneKey;
    private final NamespacedKey ownerDataKey;
    private final List<Display> entities = new ArrayList<>();
    private final List<List<Display>> nodeEntities = new ArrayList<>();
    private final Set<UUID> forcedVisible = new HashSet<>();
    private final Set<UUID> forcedHidden = new HashSet<>();
    private final Set<UUID> visibleViewers = new HashSet<>();
    private final List<UiClickHandler> clickHandlers = new CopyOnWriteArrayList<>();
    private final List<UiControlChangeHandler> controlChangeHandlers = new CopyOnWriteArrayList<>();
    private final UiControlStateStore controlStates = new UiControlStateStore();
    private final Map<String, Integer> renderedScrollOffsets = new LinkedHashMap<>();
    private final UiAnimationClock animationClock = new UiAnimationClock();
    private final UiFollowController follow = new UiFollowController();
    private UiScrollAnimation scrollAnimation = UiScrollAnimation.none();

    private Location origin;
    private UiDocument document;
    private UiAudience audience;
    private UiCameraTransform cameraTransform;
    private boolean removed;
    private boolean doubleSided;
    private boolean mirrorSide;
    private Interaction interactionEntity;
    private UiAnimation animation;
    private double animationAgeTicks;
    private List<UiAnimation> nodeAnimations = List.of();
    private double[] nodeAnimationAgesTicks = new double[0];


    UiScene(HaoHanDisplayUIPlugin plugin, UUID id, String ownerKey, Location origin,
            UiDocument document, UiOptions options, UiAudience audience,
            Consumer<UUID> onRemove) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.id = Objects.requireNonNull(id, "id");
        this.ownerKey = Objects.requireNonNull(ownerKey, "ownerKey");
        this.origin = Objects.requireNonNull(origin, "origin").clone();
        this.document = Objects.requireNonNull(document, "document");
        this.options = Objects.requireNonNull(options, "options");
        this.cameraTransform = options.cameraTransform();
        this.doubleSided = options.doubleSided();
        this.mirrorSide = options.mirrorSide();
        this.audience = Objects.requireNonNull(audience, "audience");
        this.onRemove = Objects.requireNonNull(onRemove, "onRemove");
        this.sceneKey = new NamespacedKey(plugin, "scene_id");
        this.ownerDataKey = new NamespacedKey(plugin, "scene_owner");
        updateControlStates(this.document);
        respawn();
    }

    @Override
    public UUID id() { return id; }

    @Override
    public String ownerKey() { return ownerKey; }

    public Location origin() { return origin.clone(); }

    public UiDocument document() { return document; }

    @Override
    public int nodeCount() { return document.nodes().size(); }

    @Override
    public Optional<UiControl> control(String id) {
        return Optional.ofNullable(controlStates.get(id));
    }

    @Override
    public boolean isValid() { return !removed; }

    @Override
    public void update(UiDocument document) {
        ensureValid();
        UiDocument next = Objects.requireNonNull(document, "document");
        UiDocument previous = this.document;
        Map<String, UiControl> oldControls = controlStates.snapshot();
        updateControlStates(next);
        this.document = next;
        if (!incrementalUpdate(previous, next)) {
            respawn();
        } else {
            // Document refreshes are allowed while an animation is running
            // (for example, a page may update a rainbow text every tick).
            // incrementalUpdate writes the document's base transforms, so
            // restore the current animation frame before the next tick. This
            // prevents the client from alternating between static and
            // animated targets.
            if (isAnimating()) applyCurrentTransforms(ANIMATION_INTERPOLATION_TICKS);
            syncViewers();
        }

        for (UiControl newControl : controlStates.values()) {
            if (newControl instanceof UiScrollList newScrollList) {
                Integer prevOffset = renderedScrollOffsets.get(newScrollList.id());
                if (prevOffset != null && prevOffset != newScrollList.offset()) {
                    int direction = Integer.compare(newScrollList.offset(), prevOffset);
                    animateScrollViewport(newScrollList, direction);
                } else {
                    UiControl oldControl = oldControls.get(newScrollList.id());
                    if (oldControl instanceof UiScrollList oldScrollList) {
                        int direction = Integer.compare(newScrollList.offset(), oldScrollList.offset());
                        if (direction != 0) {
                            animateScrollViewport(newScrollList, direction);
                        }
                    }
                }
                renderedScrollOffsets.put(newScrollList.id(), newScrollList.offset());
            }
        }
    }

    private boolean isNodeInViewport(UiNode node, UiScrollList list) {
        float listTop = list.y() - 1.0f;
        float listBottom = list.y() + list.height() + 1.0f;
        float listLeft = list.x() - 1.0f;
        float listRight = list.x() + list.width() + 1.0f;

        if (node instanceof AlignedTextNode text) {
            return text.boxY() >= listTop && text.boxY() + text.boxHeight() <= listBottom
                    && text.boxX() >= listLeft && text.boxX() + text.boxWidth() <= listRight;
        } else if (node instanceof UiBackgroundNode) {
            return false;
        } else if (node instanceof ItemNode item) {
            return item.y() >= listTop && item.y() <= listBottom
                    && item.x() >= listLeft && item.x() <= listRight;
        } else if (node instanceof UiIconNode icon) {
            return icon.boxY() >= listTop && icon.bottom() <= listBottom
                    && icon.boxX() >= listLeft && icon.right() <= listRight;
        } else if (node instanceof BlockNode block) {
            return block.y() >= listTop && block.y() + block.height() <= listBottom
                    && block.x() >= listLeft && block.x() + block.width() <= listRight;
        } else if (node instanceof LineNode line) {
            return line.y1() >= listTop && line.y2() <= listBottom;
        }
        return false;
    }

    @Override
    public void move(Location origin) {
        move(origin, 0);
    }

    private void move(Location origin, int interpolationTicks) {
        ensureValid();
        Objects.requireNonNull(origin, "origin");
        if (origin.getWorld() == null) throw new IllegalArgumentException("origin must have a world");
        this.origin = origin.clone();
        for (Display display : entities) {
            if (display.isValid()) {
                display.setInterpolationDelay(0);
                display.setInterpolationDuration(interpolationTicks);
                display.teleport(this.origin);
                display.setRotation(origin.getYaw(), origin.getPitch());
            }
        }
        updateInteractionHitbox();
        // Follow movement changes the entity anchor only. While an animation is
        // active, its frame is the sole owner of the display transformation.
        if (!isAnimating()) resetTransforms(0);
    }

    @Override
    public void audience(UiAudience audience) {
        ensureValid();
        this.audience = Objects.requireNonNull(audience, "audience");
        syncViewers();
    }

    @Override
    public void cameraTransform(UiCameraTransform transform) {
        ensureValid();
        this.cameraTransform = Objects.requireNonNull(transform, "transform");
        applyCameraTransform();
        respawn();
    }

    @Override
    public void mirrorSide(boolean enabled) {
        ensureValid();
        if (mirrorSide == enabled) return;
        mirrorSide = enabled;
        // A side switch changes the direction of the back-face transforms. Do
        // not interpolate from the old side: when this happens during an
        // animation, the client can repeatedly chase two opposite targets.
        applyCurrentTransformsImmediately();
        syncViewers();
    }

    @Override
    public void doubleSided(boolean enabled) {
        ensureValid();
        if (doubleSided == enabled) return;
        doubleSided = enabled;
        respawn();
    }

    public UiCameraTransform cameraTransform() {
        return cameraTransform;
    }

    @Override
    public void follow(Player target, UiFollowMode mode) {
        follow(target, mode, 3.0, 0.0f);
    }

    @Override
    public void follow(Player target, UiFollowMode mode, double distance) {
        follow(target, mode, distance, 0.0f);
    }

    @Override
    public void follow(Player target, UiFollowMode mode, double distance, float pitchOffset) {
        ensureValid();
        follow.configure(target, mode, distance, pitchOffset);
    }

    @Override
    public void stopFollow() {
        follow.stop();
    }

    @Override
    public UiFollowMode followMode() {
        return follow.mode();
    }

    @Override
    public Player followTarget() {
        return follow.target();
    }

    @Override
    public void show(Player player) {
        ensureValid();
        UUID playerId = Objects.requireNonNull(player, "player").getUniqueId();
        forcedHidden.remove(playerId);
        forcedVisible.add(playerId);
        syncViewers();
    }

    @Override
    public void hide(Player player) {
        ensureValid();
        UUID playerId = Objects.requireNonNull(player, "player").getUniqueId();
        forcedVisible.remove(playerId);
        forcedHidden.add(playerId);
        syncViewers();
    }

    @Override
    public void animate(UiAnimation animation) {
        ensureValid();
        this.animation = Objects.requireNonNull(animation, "animation");
        this.nodeAnimations = List.of();
        this.nodeAnimationAgesTicks = new double[0];
        this.animationAgeTicks = 0.0;
        animationClock.reset();
        configureAnimationInterpolation();
        applyAnimation(0.0);
    }

    @Override
    public void animateNodes(List<UiAnimation> animations) {
        ensureValid();
        Objects.requireNonNull(animations, "animations");
        int nodeCount = document.nodes().size();
        List<UiAnimation> list;
        if (animations.size() == nodeCount) {
            list = List.copyOf(animations);
        } else if (animations.isEmpty()) {
            list = List.of();
        } else {
            List<UiAnimation> adapted = new ArrayList<>(nodeCount);
            for (int i = 0; i < nodeCount; i++) {
                adapted.add(i < animations.size() ? animations.get(i) : animations.get(animations.size() - 1));
            }
            list = List.copyOf(adapted);
        }
        nodeAnimations = list;
        nodeAnimationAgesTicks = new double[list.size()];
        animation = null;
        animationClock.reset();
        configureAnimationInterpolation();
        applyNodeAnimationFrames();
    }

    @Override
    public void stopAnimation() {
        if (removed || (animation == null && nodeAnimations.isEmpty())) return;
        if (animation != null) applyAnimation(1.0);
        if (!nodeAnimations.isEmpty()) {
            for (int i = 0; i < nodeAnimations.size(); i++) {
                if (!isStaticAnimation(nodeAnimations.get(i))) {
                    applyAnimationToNode(i, nodeAnimations.get(i), 1.0);
                }
            }
        }
        animation = null;
        nodeAnimations = List.of();
        nodeAnimationAgesTicks = new double[0];
        animationAgeTicks = 0.0;
        animationClock.clear();
    }

    @Override
    public boolean isAnimating() {
        return !removed && (animation != null || !nodeAnimations.isEmpty());
    }

    @Override
    public void onClick(UiClickHandler handler) {
        ensureValid();
        clickHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    @Override
    public void onControlChange(UiControlChangeHandler handler) {
        ensureValid();
        controlChangeHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    @Override
    public void clearClickHandlers() {
        clickHandlers.clear();
    }

    @Override
    public void clearControlChangeHandlers() {
        controlChangeHandlers.clear();
    }

    @Override
    public void remove() {
        if (removed) return;
        removed = true;
        clearEntities();
        clickHandlers.clear();
        controlChangeHandlers.clear();
        controlStates.clear();
        renderedScrollOffsets.clear();
        stopFollow();
        onRemove.accept(id);
    }

    @Override
    public void scrollAnimation(UiScrollAnimation animation) {
        ensureValid();
        scrollAnimation = Objects.requireNonNull(animation, "animation");
    }

    void tick() {
        if (removed) return;
        tickFollow();
        tickAudience();
        tickAnimation();
    }

    private void tickFollow() {
        Location next = follow.next(origin);
        if (next != null) move(next, follow.interpolationTicks());
    }

    private void tickAudience() {
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            boolean shouldSee = shouldShow(online);
            boolean isSeeing = visibleViewers.contains(online.getUniqueId());
            if (shouldSee && !isSeeing) showEntities(online);
            else if (!shouldSee && isSeeing) hideEntities(online);
            else if (shouldSee && isSeeing) {
                syncItemBackfaces(online);
                syncSideVisibility(online);
            }
        }
        visibleViewers.removeIf(playerId -> plugin.getServer().getPlayer(playerId) == null);
    }

    private void tickAnimation() {
        if (animation != null) {
            advanceAnimationClock();
            double effectiveAge = animationAgeTicks - animation.delayTicks();
            if (effectiveAge < 0.0) return;
            if (effectiveAge >= animation.durationTicks()) {
                applyAnimationFrame(1.0f);
                animation = null;
                animationAgeTicks = 0.0;
            } else {
                float progress = (float) (effectiveAge / animation.durationTicks());
                applyAnimationFrame((float) animation.easing().apply(progress));
            }
        } else if (!nodeAnimations.isEmpty()) {
            double deltaTicks = advanceAnimationClock();
            boolean anyRunning = false;
            for (int i = 0; i < nodeAnimations.size(); i++) {
                UiAnimation a = nodeAnimations.get(i);
                if (a.durationTicks() <= 0) continue;
                nodeAnimationAgesTicks[i] += deltaTicks;
                double effectiveAge = nodeAnimationAgesTicks[i] - a.delayTicks();
                if (effectiveAge < a.durationTicks()) anyRunning = true;
            }
            applyNodeAnimationFrames();
            if (!anyRunning) {
                nodeAnimations = List.of();
                nodeAnimationAgesTicks = new double[0];
            }
        }
    }

    /** Advances animation time from a monotonic clock, expressed in 20 Hz ticks. */
    private double advanceAnimationClock() {
        double deltaTicks = animationClock.advance();
        if (animation != null) animationAgeTicks += deltaTicks;
        return deltaTicks;
    }

    private void updateInteractionHitbox() {
        if (interactionEntity == null || !interactionEntity.isValid()) return;
        if (document.buttons().isEmpty() && controlStates.isEmpty()) return;

        List<UiControl> controls = List.copyOf(controlStates.values());
        float minX = document.buttons().stream().map(button -> button.x() - button.hitSlop())
                .min(Float::compare).orElse(Float.POSITIVE_INFINITY);
        minX = Math.min(minX, controls.stream().map(control -> control.x() - control.hitSlop())
                .min(Float::compare).orElse(0.0f));
        float maxX = document.buttons().stream()
                .map(button -> button.x() + button.width() + button.hitSlop())
                .max(Float::compare).orElse(Float.NEGATIVE_INFINITY);
        maxX = Math.max(maxX, controls.stream()
                .map(control -> control.x() + control.width() + control.hitSlop())
                .max(Float::compare).orElse(0.0f));
        float minY = document.buttons().stream()
                .map(button -> button.y() - button.hitSlop())
                .min(Float::compare).orElse(Float.POSITIVE_INFINITY);
        minY = Math.min(minY, controls.stream()
                .map(control -> control.y() - control.hitSlop())
                .min(Float::compare).orElse(0.0f));
        float maxY = document.buttons().stream()
                .map(button -> button.y() + button.height() + button.hitSlop())
                .max(Float::compare).orElse(Float.NEGATIVE_INFINITY);
        maxY = Math.max(maxY, controls.stream()
                .map(control -> control.y() + control.height() + control.hitSlop())
                .max(Float::compare).orElse(0.0f));
        float pixels = options.pixelsPerBlock();

        Vector normal = origin.getDirection().setY(0.0);
        if (normal.lengthSquared() < 0.0001) normal.setZ(1.0);
        normal.normalize();
        Vector right = new Vector(normal.getZ(), 0.0, -normal.getX());
        double centerX = (minX + maxX) * 0.5 / pixels;
        Location hitboxLocation = origin.clone()
                .add(right.multiply(centerX))
                .add(0.0, -maxY / pixels, 0.0);
        final float hitboxWidth = maxX - minX;
        final float hitboxHeight = maxY - minY;

        if (interactionEntity != null && interactionEntity.isValid()) {
            interactionEntity.teleport(hitboxLocation);
            interactionEntity.setInteractionWidth(Math.max(0.2f, hitboxWidth / pixels));
            interactionEntity.setInteractionHeight(Math.max(0.2f, hitboxHeight / pixels));
            return;
        }

        interactionEntity = origin.getWorld().spawn(hitboxLocation, Interaction.class, interaction -> {
            interaction.setInteractionWidth(Math.max(0.2f, hitboxWidth / pixels));
            interaction.setInteractionHeight(Math.max(0.2f, hitboxHeight / pixels));
            interaction.setResponsive(true);
            interaction.setPersistent(false);
            interaction.setInvulnerable(true);
            interaction.addScoreboardTag(options.scoreboardTag());
            interaction.addScoreboardTag("hhdui_interaction");
            interaction.getPersistentDataContainer().set(
                    sceneKey, PersistentDataType.STRING, id.toString());
            interaction.getPersistentDataContainer().set(
                    ownerDataKey, PersistentDataType.STRING, ownerKey);
        });
    }

    UiHit hit(Player player) {
        if (removed || (document.buttons().isEmpty() && controlStates.isEmpty())
                || !shouldShow(player)) return null;

        PlaneBasis basis = planeBasis(player);
        UiRaycaster.Projection projection = UiRaycaster.project(
                player.getEyeLocation().toVector(),
                player.getEyeLocation().getDirection(),
                origin.toVector(), basis.normal(), basis.right(), basis.up(),
                options.pixelsPerBlock(), options.maxDistance());
        if (projection == null) return null;

        boolean mirror = mirrorSide && isTwoSided() && !isFrontFacing(player);
        float localX = mirror ? -projection.localX() : projection.localX();
        float localY = projection.localY();

        UiHit buttonHit = document.buttons().stream()
                .filter(button -> button.contains(localX, localY))
                .findFirst()
                .map(button -> new UiHit(this, button, null, player,
                                         localX, localY, projection.distance()))
                .orElse(null);

        if (buttonHit != null) return buttonHit;

        return controlStates.values().stream()
                .filter(control -> control.contains(localX, localY))
                .findFirst()
                .map(control -> new UiHit(this, null, control, player,
                                          localX, localY, projection.distance()))
                .orElse(null);
    }

    UiRaycaster.Projection projectCursor(Player player) {
        if (removed || !shouldShow(player)) return null;
        PlaneBasis basis = planeBasis(player);
        UiRaycaster.Projection raw = UiRaycaster.project(
                player.getEyeLocation().toVector(),
                player.getEyeLocation().getDirection(),
                origin.toVector(), basis.normal(), basis.right(), basis.up(),
                options.pixelsPerBlock(), options.maxDistance());
        if (raw == null) return null;
        boolean mirror = mirrorSide && isTwoSided() && !isFrontFacing(player);
        return mirror
               ? new UiRaycaster.Projection(-raw.localX(), raw.localY(), raw.distance())
               : raw;
    }

    int findModelNodeAt(float localX, float localY) {
        if (document == null) return -1;
        for (int i = 0; i < document.nodes().size(); i++) {
            UiNode node = document.nodes().get(i);
            if (node instanceof EntityModelNode model && model.contains(localX, localY)) {
                return i;
            } else if (node instanceof MobEntityNode mob && mob.contains(localX, localY)) {
                return i;
            }
        }
        return -1;
    }

    boolean dragModel(int nodeIndex, float deltaX, float deltaY) {
        if (nodeIndex < 0 || nodeIndex >= nodeEntities.size() || document == null || nodeIndex >= document.nodes().size()) {
            return false;
        }
        UiNode node = document.nodes().get(nodeIndex);
        EntityModelNode model = null;
        UiModelRotation rotation = null;
        if (node instanceof EntityModelNode em) {
            model = em;
            rotation = em.rotation();
        } else if (node instanceof MobEntityNode mob) {
            rotation = mob.rotation();
            model = EntityModelNode.forMob(
                            mob.entityType().name().toLowerCase(),
                            mob.x(), mob.y(), mob.width(), mob.height(), mob.scale())
                    .withYaw(mob.yaw())
                    .withPitch(mob.pitch())
                    .withDoubleSided(mob.doubleSided());
        }
        if (model == null) return false;

        float sensitivity = rotation != null ? rotation.sensitivity() : 1.0f;
        float newYaw = model.yaw() + deltaX * sensitivity;
        float newPitch = model.pitch() - deltaY * sensitivity;
        if (rotation != null) {
            newYaw = rotation.clampYaw(newYaw);
            newPitch = rotation.clampPitch(newPitch);
        }

        EntityModelNode updated = model.withRotation(newYaw, newPitch, model.roll());
        List<Transformation> transforms = computeModelTransforms(updated, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = nodeEntities.get(nodeIndex);
        if (list != null) {
            for (int j = 0; j < list.size() && j < transforms.size(); j++) {
                Display display = list.get(j);
                if (display != null && display.isValid()) {
                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(1);
                    display.setTransformation(transforms.get(j));
                }
            }
        }
        return true;
    }

    void releaseModelDrag(int nodeIndex) {
        if (nodeIndex < 0 || nodeIndex >= nodeEntities.size() || document == null || nodeIndex >= document.nodes().size()) {
            return;
        }
        UiNode node = document.nodes().get(nodeIndex);
        List<Transformation> transforms = getNodeTransformations(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = nodeEntities.get(nodeIndex);
        if (list != null) {
            for (int j = 0; j < list.size() && j < transforms.size(); j++) {
                Display display = list.get(j);
                if (display != null && display.isValid()) {
                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(INTERPOLATION_TICKS);
                    display.setTransformation(transforms.get(j));
                }
            }
        }
    }

    boolean activate(UiHit hit) {
        return triggerClick(hit);
    }

    UiHit scrollHit(Player player) {
        if (removed || controlStates.isEmpty() || !shouldShow(player)) return null;
        PlaneBasis basis = planeBasis(player);
        UiRaycaster.Projection projection = UiRaycaster.project(
                player.getEyeLocation().toVector(),
                player.getEyeLocation().getDirection(),
                origin.toVector(), basis.normal(), basis.right(), basis.up(),
                options.pixelsPerBlock(), options.maxDistance());
        if (projection == null) return null;

        boolean mirror = mirrorSide && isTwoSided() && !isFrontFacing(player);
        float localX = mirror ? -projection.localX() : projection.localX();
        float localY = projection.localY();

        return controlStates.values().stream()
                .filter(c -> c instanceof UiScrollList && c.contains(localX, localY))
                .findFirst()
                .map(control -> new UiHit(this, null, control, player,
                                          localX, localY, projection.distance()))
                .orElse(null);
    }

    boolean scroll(UiHit hit, int nextOffset) {
        if (hit == null || !(hit.control() instanceof UiScrollList scrollList)) return false;
        int clamped = Math.clamp(nextOffset, 0, scrollList.maxOffset());
        return changeControl(hit, clamped, true);
    }

    /**
     * Continuous flow scroll animation:
     * - Uses exact physical row distance (18.0f) so items slide continuously from their previous screen position.
     * - Animates only the row cards (background, icon, checkbox, text) inside the viewport.
     * - The entering row smoothly expands from its center (scale 0.25 -> 1.0) and fades in (opacity 0.0 -> 1.0).
     * - All existing rows glide synchronously with cubic-out momentum for a natural scroll feel.
     */
    private void animateScrollViewport(UiScrollList list, int direction) {
        List<UiAnimation> animations = scrollAnimation.create(document, list, direction);
        if (!animations.isEmpty()) animateNodes(animations);
    }

    boolean dragSlider(Player player, String controlId) {
        return updateSlider(player, controlId);
    }

    boolean updateHover(Player player) {
        if (removed || !shouldShow(player) || isAnimating() || nodeEntities.isEmpty()) return false;
        UiRaycaster.Projection projection = projectCursor(player);

        boolean anyUpdated = false;
        for (int i = 0; i < nodeEntities.size() && i < document.nodes().size(); i++) {
            UiNode node = document.nodes().get(i);
            UiModelRotation rotation = null;
            EntityModelNode model = null;
            if (node instanceof EntityModelNode em) {
                model = em;
                rotation = em.rotation();
            } else if (node instanceof MobEntityNode mob) {
                rotation = mob.rotation();
                model = EntityModelNode.forMob(
                                mob.entityType().name().toLowerCase(),
                                mob.x(), mob.y(), mob.width(), mob.height(), mob.scale())
                        .withYaw(mob.yaw())
                        .withPitch(mob.pitch())
                        .withDoubleSided(mob.doubleSided());
            }

            if (model != null && rotation != null && rotation.mode() == UiModelRotation.Mode.CURSOR_TRACKING) {
                EntityModelNode targetModel;
                if (projection != null && model.contains(projection.localX(), projection.localY())) {
                    float centerX = model.x() + model.width() * 0.5f;
                    float centerY = model.y() + model.height() * 0.5f;
                    float deltaX = (projection.localX() - centerX) / (model.width() * 0.5f);
                    float deltaY = (projection.localY() - centerY) / (model.height() * 0.5f);

                    float maxAngle = 35.0f;
                    float targetYaw = rotation.clampYaw(model.yaw() + deltaX * maxAngle);
                    float targetPitch = rotation.clampPitch(model.pitch() - deltaY * maxAngle);
                    targetModel = model.withRotation(targetYaw, targetPitch, model.roll());
                } else {
                    targetModel = model;
                }

                List<Transformation> transforms = computeModelTransforms(targetModel, 1.0f, 0.0f, 0.0f, 0.0f);
                List<Display> list = nodeEntities.get(i);
                if (list != null) {
                    for (int j = 0; j < list.size() && j < transforms.size(); j++) {
                        Display display = list.get(j);
                        if (display != null && display.isValid()) {
                            display.setInterpolationDelay(0);
                            display.setInterpolationDuration(ANIMATION_INTERPOLATION_TICKS);
                            display.setTransformation(transforms.get(j));
                        }
                    }
                }
                anyUpdated = true;
            }
        }
        return anyUpdated;
    }

    private boolean updateSlider(Player player, String controlId) {
        UiControl control = controlStates.get(controlId);
        if (!(control instanceof UiSlider slider)) return false;

        UiRaycaster.Projection projection = projectCursor(player);
        if (projection == null) return false;

        double nextValue = slider.valueAt(projection.localX());
        UiHit hit = new UiHit(this, null, slider, player, projection.localX(), projection.localY(), projection.distance());
        return changeControl(hit, nextValue, false);
    }

    private boolean triggerClick(UiHit hit) {
        if (hit == null) return false;
        boolean accepted = false;
        if (hit.button() != null) {
            UiClick click = new UiClick(this, hit.button(), hit.player(), hit.localX(), hit.localY(), hit.distance());
            for (UiClickHandler handler : clickHandlers) {
                handler.onClick(click);
                accepted = true;
            }
            if (executeButtonAction(hit.button(), hit.player())) accepted = true;
        } else if (hit.control() != null) {
            accepted = switch (hit.control()) {
                case UiButton ignored -> false;
                case UiCheckbox checkbox -> changeControl(hit, checkbox.checked() ? 0.0 : 1.0, true);
                case UiSlider slider -> changeControl(hit, slider.valueAt(hit.localX()), true);
                case UiScrollList scrollList -> {
                    int nextOffset = Math.min(scrollList.maxOffset(), scrollList.offset() + scrollList.step());
                    yield changeControl(hit, nextOffset, true);
                }
            };
        }
        if (accepted && options.clickSound() != null) {
            hit.player().playSound(origin, options.clickSound(),
                                   options.clickSoundVolume(), options.clickSoundPitch());
        }
        return accepted;
    }

    private boolean executeButtonAction(UiButton button, Player player) {
        var action = button.action();
        switch (action.type()) {
            case NONE -> { return false; }
            case OPEN_URL -> player.sendMessage(action.label()
                    .clickEvent(ClickEvent.openUrl(action.value())));
            case RUN_PLAYER_COMMAND -> player.performCommand(action.value());
            case RUN_CONSOLE_COMMAND -> plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(), action.value());
            case SUGGEST_COMMAND -> player.sendMessage(action.label()
                    .clickEvent(ClickEvent.suggestCommand(action.value())));
        }
        return true;
    }

    private boolean changeControl(UiHit hit, double nextValue, boolean updateScene) {
        UiControl control = hit.control();
        if (control == null) return false;
        UiControlStateStore.Change stateChange = controlStates.change(control, nextValue);
        if (stateChange == null) return false;

        UiControlChange change = new UiControlChange(this, stateChange.control(), hit.player(),
                stateChange.oldValue(), stateChange.newValue(), hit.localX(), hit.localY(), hit.distance());
        for (UiControlChangeHandler handler : controlChangeHandlers) {
            handler.onChange(change);
        }
        if (updateScene) {
            update(this.document);
        }
        return true;
    }

    private void updateControlStates(UiDocument next) {
        controlStates.synchronize(next);
    }

    private List<Display> spawnNode(UiNode node) {
        if (node instanceof UiBackgroundNode background) return spawnBackground(background);
        if (node instanceof TextNode text) return spawnText(text);
        if (node instanceof AlignedTextNode text) return spawnAlignedText(text);
        if (node instanceof ItemNode item) return spawnItem(item);
        if (node instanceof UiIconNode icon) return spawnIcon(icon);
        if (node instanceof BlockNode block) return spawnBlock(block);
        if (node instanceof EntityModelNode model) return spawnEntityModel(model);
        if (node instanceof MobEntityNode mob) return spawnMobEntity(mob);
        if (node instanceof LineNode line) return spawnLine(line);
        if (node instanceof ParallelogramNode parallelogram) return spawnParallelogram(parallelogram);
        if (node instanceof TriangleNode triangle) return spawnTriangle(triangle);
        if (node instanceof PolylineNode polyline) return spawnPolyline(polyline);
        throw new IllegalArgumentException("Unsupported UI node: " + node.getClass().getName());
    }

    private List<Display> spawnBackground(UiBackgroundNode node) {
        List<Transformation> transforms = computeBackgroundTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = new ArrayList<>(transforms.size());
        for (Transformation tf : transforms) {
            list.add(origin.getWorld().spawn(origin, TextDisplay.class, display -> {
                configure(display, node);
                display.text(Component.text(" "));
                display.setTextOpacity((byte) 0);
                display.setAlignment(TextDisplay.TextAlignment.LEFT);
                display.setBackgroundColor(node.background());
                display.setTransformation(tf);
            }));
        }
        return list;
    }

    private List<Display> spawnText(TextNode node) {
        List<Transformation> transforms = computeTextTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = new ArrayList<>(transforms.size());
        for (Transformation tf : transforms) {
            list.add(origin.getWorld().spawn(origin, TextDisplay.class, display -> {
                configure(display, node);
                display.text(node.text());
                display.setShadowed(node.shadow());
                display.setSeeThrough(node.seeThrough());
                display.setAlignment(node.alignment());
                display.setLineWidth(node.lineWidth());
                display.setTransformation(tf);
            }));
        }
        return list;
    }

    private List<Display> spawnAlignedText(AlignedTextNode node) {
        List<Transformation> transforms = computeAlignedTextTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = new ArrayList<>(transforms.size());
        for (Transformation tf : transforms) {
            list.add(origin.getWorld().spawn(origin, TextDisplay.class, display -> {
                configure(display, node);
                display.text(node.text());
                display.setAlignment(TextDisplay.TextAlignment.CENTER);
                display.setLineWidth(Math.max(1,
                                              Math.round(node.width() * 20.0f / node.fontSize())));
                display.setShadowed(node.shadow());
                display.setSeeThrough(node.seeThrough());
                display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                display.setTextOpacity((byte) 255);
                display.setTransformation(tf);
            }));
        }
        return list;
    }

    private List<Display> spawnItem(ItemNode node) {
        List<Transformation> transforms = computeItemTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = new ArrayList<>(transforms.size());
        for (Transformation tf : transforms) {
            list.add(origin.getWorld().spawn(origin, ItemDisplay.class, display -> {
                configure(display, node);
                display.setItemStack(node.item());
                display.setItemDisplayTransform(node.transform());
                display.setTransformation(tf);
            }));
        }
        return list;
    }

    private List<Display> spawnIcon(UiIconNode node) {
        List<Transformation> transforms = computeIconTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = new ArrayList<>(transforms.size());
        for (Transformation tf : transforms) {
            list.add(origin.getWorld().spawn(origin, ItemDisplay.class, display -> {
                configure(display, node);
                display.setItemStack(node.item());
                display.setItemDisplayTransform(node.transform());
                display.setTransformation(tf);
            }));
        }
        return list;
    }

    private List<Display> spawnEntityModel(EntityModelNode node) {
        List<Transformation> transforms = computeModelTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = new ArrayList<>(transforms.size());
        for (Transformation tf : transforms) {
            list.add(origin.getWorld().spawn(origin, ItemDisplay.class, display -> {
                configure(display, node);
                display.setItemStack(node.item());
                display.setItemDisplayTransform(node.transform());
                display.setTransformation(tf);
            }));
        }
        return list;
    }

    private List<Display> spawnMobEntity(MobEntityNode node) {
        EntityModelNode model = EntityModelNode.forMob(
                        node.entityType().name().toLowerCase(),
                        node.x(), node.y(), node.width(), node.height(), node.scale())
                .withYaw(node.yaw())
                .withPitch(node.pitch())
                .withDoubleSided(node.doubleSided());
        return spawnEntityModel(model);
    }

    private List<Display> spawnBlock(BlockNode node) {
        List<Transformation> transforms = computeBlockTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = new ArrayList<>(transforms.size());
        for (Transformation tf : transforms) {
            list.add(origin.getWorld().spawn(origin, BlockDisplay.class, display -> {
                configure(display, node);
                display.setBlock(node.block());
                display.setTransformation(tf);
            }));
        }
        return list;
    }

    private List<Display> spawnLine(LineNode node) {
        List<Transformation> transforms = computeLineTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = new ArrayList<>(transforms.size());
        for (Transformation tf : transforms) {
            list.add(origin.getWorld().spawn(origin, TextDisplay.class, display -> {
                configure(display, node);
                display.text(Component.text(" "));
                display.setBackgroundColor(node.color());
                display.setTransformation(tf);
            }));
        }
        return list;
    }

    private List<Display> spawnParallelogram(ParallelogramNode node) {
        List<Transformation> transforms = computeParallelogramTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = new ArrayList<>(transforms.size());
        for (Transformation tf : transforms) {
            list.add(origin.getWorld().spawn(origin, TextDisplay.class, display -> {
                configure(display, node);
                display.text(Component.text(" "));
                display.setBackgroundColor(node.color());
                display.setTransformation(tf);
            }));
        }
        return list;
    }

    private List<Display> spawnTriangle(TriangleNode node) {
        List<Transformation> transforms = computeTriangleTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = new ArrayList<>(transforms.size());
        for (Transformation tf : transforms) {
            list.add(origin.getWorld().spawn(origin, TextDisplay.class, display -> {
                configure(display, node);
                display.text(Component.text(" "));
                display.setBackgroundColor(node.color());
                display.setTransformation(tf);
            }));
        }
        return list;
    }

    private List<Display> spawnPolyline(PolylineNode node) {
        List<Transformation> transforms = computePolylineTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> list = new ArrayList<>(transforms.size());
        for (Transformation tf : transforms) {
            list.add(origin.getWorld().spawn(origin, TextDisplay.class, display -> {
                configure(display, node);
                display.text(Component.text(" "));
                display.setBackgroundColor(node.color());
                display.setTransformation(tf);
            }));
        }
        return list;
    }

    private void spawnInteraction() {
        if (document.buttons().isEmpty() && controlStates.isEmpty()) return;
        List<UiControl> controls = List.copyOf(controlStates.values());
        float minX = document.buttons().stream().map(button -> button.x() - button.hitSlop())
                .min(Float::compare).orElse(Float.POSITIVE_INFINITY);
        minX = Math.min(minX, controls.stream().map(control -> control.x() - control.hitSlop())
                .min(Float::compare).orElse(0.0f));
        float maxX = document.buttons().stream()
                .map(button -> button.x() + button.width() + button.hitSlop())
                .max(Float::compare).orElse(Float.NEGATIVE_INFINITY);
        maxX = Math.max(maxX, controls.stream()
                .map(control -> control.x() + control.width() + control.hitSlop())
                .max(Float::compare).orElse(0.0f));
        float minY = document.buttons().stream()
                .map(button -> button.y() - button.hitSlop())
                .min(Float::compare).orElse(Float.POSITIVE_INFINITY);
        minY = Math.min(minY, controls.stream()
                .map(control -> control.y() - control.hitSlop())
                .min(Float::compare).orElse(0.0f));
        float maxY = document.buttons().stream()
                .map(button -> button.y() + button.height() + button.hitSlop())
                .max(Float::compare).orElse(Float.NEGATIVE_INFINITY);
        maxY = Math.max(maxY, controls.stream()
                .map(control -> control.y() + control.height() + control.hitSlop())
                .max(Float::compare).orElse(0.0f));
        float pixels = options.pixelsPerBlock();

        Vector normal = origin.getDirection().setY(0.0);
        if (normal.lengthSquared() < 0.0001) normal.setZ(1.0);
        normal.normalize();
        Vector right = new Vector(normal.getZ(), 0.0, -normal.getX());
        double centerX = (minX + maxX) * 0.5 / pixels;
        Location hitboxLocation = origin.clone()
                .add(right.multiply(centerX))
                .add(0.0, -maxY / pixels, 0.0);
        final float hitboxWidth = maxX - minX;
        final float hitboxHeight = maxY - minY;

        interactionEntity = origin.getWorld().spawn(hitboxLocation, Interaction.class, interaction -> {
            interaction.setInteractionWidth(Math.max(0.2f, hitboxWidth / pixels));
            interaction.setInteractionHeight(Math.max(0.2f, hitboxHeight / pixels));
            interaction.setResponsive(true);
            interaction.setPersistent(false);
            interaction.setInvulnerable(true);
            interaction.addScoreboardTag(options.scoreboardTag());
            interaction.addScoreboardTag("hhdui_interaction");
            interaction.getPersistentDataContainer().set(
                    sceneKey, PersistentDataType.STRING, id.toString());
            interaction.getPersistentDataContainer().set(
                    ownerDataKey, PersistentDataType.STRING, ownerKey);
        });
    }

    private void configure(Display display, UiNode node) {
        display.setPersistent(false);
        display.setGravity(false);
        display.setInvulnerable(true);
        display.setVisibleByDefault(false);
        display.setBillboard(cameraTransform.billboard());
        display.setRotation(origin.getYaw(), origin.getPitch());
        display.setViewRange(options.viewRange());
        display.setShadowRadius(0.0f);
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(INTERPOLATION_TICKS);
    }

    private Transformation transform(UiNode node, float sx, float sy, float sz,
                                     float offsetX, float offsetY, float offsetZ) {
        Quaternionf rotation = localRotation();
        float pixels = options.pixelsPerBlock();
        Vector3f translation = new Vector3f((node.x() + offsetX) / pixels,
                                            -(node.y() + offsetY) / pixels, node.depth() + offsetZ);
        rotation.transform(translation);
        return new Transformation(
                translation, rotation, new Vector3f(sx, sy, sz), new Quaternionf());
    }

    private Transformation transformBack(UiNode node, float sx, float sy, float sz,
                                         float offsetX, float offsetY, float offsetZ) {
        Quaternionf baseRotation = localRotation();
        Quaternionf backRotation = new Quaternionf(baseRotation).rotateY((float) Math.PI);
        float pixels = options.pixelsPerBlock();
        // TextDisplay rotates around its translation point. Aligned text is
        // laid out from the left edge, so the back copy must start at the
        // opposite edge before its 180-degree rotation. Without this offset,
        // back-side labels drift away from their button backgrounds.
        float targetX;
        // Keep the same logical anchor as the front copy. TextDisplay's own
        // alignment handles the text box; shifting to boxX + width here moves
        // centered/left-aligned labels away from their mirrored controls.
        targetX = mirrorSide ? -(node.x() + offsetX) : (node.x() + offsetX);
        float extraBackOffset = (node instanceof ItemNode || node instanceof UiIconNode) ? 0.026f : 0.022f;
        float backDepth = -(node.depth() + extraBackOffset + offsetZ);
        Vector3f translation = new Vector3f(targetX / pixels,
                                            -(node.y() + offsetY) / pixels, backDepth);
        baseRotation.transform(translation);
        return new Transformation(
                translation, backRotation, new Vector3f(sx, sy, sz), new Quaternionf());
    }

    private Transformation modelTransform(EntityModelNode node, float scale,
                                          float offsetX, float offsetY, float offsetZ) {
        Quaternionf baseRotation = localRotation();
        Quaternionf modelRot = new Quaternionf()
                .rotateY((float) Math.toRadians(node.yaw()))
                .rotateX((float) Math.toRadians(node.pitch()))
                .rotateZ((float) Math.toRadians(node.roll()));
        Quaternionf totalRotation = new Quaternionf(baseRotation).mul(modelRot);
        float pixels = options.pixelsPerBlock();
        Vector3f translation = new Vector3f((node.x() + offsetX) / pixels,
                                            -(node.y() + offsetY) / pixels, node.depth() + offsetZ);
        baseRotation.transform(translation);
        return new Transformation(
                translation, totalRotation,
                new Vector3f(node.scaleX() * scale, node.scaleY() * scale, node.scaleZ() * scale),
                new Quaternionf());
    }

    private Transformation blockTransform(BlockNode node, float scale,
                                          float offsetX, float offsetY, float offsetZ) {
        float pixels = options.pixelsPerBlock();
        Quaternionf rotation = localRotation();
        // Scale symmetrically from the center of the block
        float centerShiftX = (node.width() * 0.5f) * (1.0f - scale);
        float centerShiftY = (node.height() * 0.5f) * (1.0f - scale);
        Vector3f translation = new Vector3f(
                (node.x() + offsetX + centerShiftX) / pixels,
                -(node.y() + node.height() + offsetY - centerShiftY) / pixels,
                    node.depth() + offsetZ - node.thickness() / pixels);
        rotation.transform(translation);
        return new Transformation(
                translation, rotation,
                new Vector3f(node.width() / pixels * scale,
                             node.height() / pixels * scale,
                             node.thickness() / pixels * scale),
                new Quaternionf());
    }

    private Transformation modelTransformBack(EntityModelNode node, float scale,
                                              float offsetX, float offsetY, float offsetZ) {
        Quaternionf baseRotation = localRotation();
        Quaternionf modelRot = new Quaternionf()
                .rotateY((float) Math.toRadians(node.yaw() + 180.0f))
                .rotateX((float) Math.toRadians(-node.pitch()))
                .rotateZ((float) Math.toRadians(-node.roll()));
        Quaternionf totalRotation = new Quaternionf(baseRotation).mul(modelRot);
        float pixels = options.pixelsPerBlock();
        float targetX = mirrorSide ? -(node.x() + offsetX) : (node.x() + offsetX);
        float backDepth = -(node.depth() + 0.028f + offsetZ);
        Vector3f translation = new Vector3f(targetX / pixels,
                                            -(node.y() + offsetY) / pixels, backDepth);
        baseRotation.transform(translation);
        return new Transformation(
                translation, totalRotation,
                new Vector3f(node.scaleX() * scale, node.scaleY() * scale, node.scaleZ() * scale),
                new Quaternionf());
    }

    private List<Transformation> computeBlockTransforms(BlockNode node, float scale,
                                                        float offsetX, float offsetY, float offsetZ) {
        float pixels = options.pixelsPerBlock();
        Quaternionf rotation = localRotation();
        float centerShiftX = (node.width() * 0.5f) * (1.0f - scale);
        float centerShiftY = (node.height() * 0.5f) * (1.0f - scale);
        float thickness = isDoubleSided(node) ? Math.max(node.thickness(), 2.0f) : node.thickness();
        Vector3f translation = new Vector3f(
                (node.x() + offsetX + centerShiftX) / pixels,
                -(node.y() + node.height() + offsetY - centerShiftY) / pixels,
                node.depth() + offsetZ - thickness / pixels);
        rotation.transform(translation);

        boolean doubleSided = isDoubleSided(node);
        boolean asymmetric = Math.abs(node.x() + (node.x() + node.width())) > 1.0f;
        List<Transformation> list = new ArrayList<>(doubleSided && asymmetric ? 2 : 1);
        list.add(new Transformation(
                translation, rotation,
                new Vector3f(node.width() / pixels * scale,
                             node.height() / pixels * scale,
                             thickness / pixels * scale),
                new Quaternionf()));

        if (doubleSided && asymmetric) {
            float mirroredX = mirrorSide ? -(node.x() + node.width()) : node.x();
            float backOffsetX = mirrorSide ? -offsetX : offsetX;
            Vector3f backTranslation = new Vector3f(
                    (mirroredX + backOffsetX + centerShiftX) / pixels,
                    -(node.y() + node.height() + offsetY - centerShiftY) / pixels,
                    -(node.depth() + 0.022f + offsetZ));
            rotation.transform(backTranslation);
            list.add(new Transformation(
                    backTranslation, rotation,
                    new Vector3f(node.width() / pixels * scale,
                                 node.height() / pixels * scale,
                                 thickness / pixels * scale),
                    new Quaternionf()));
        }
        return list;
    }

    private void applyAnimation(double progress) {
        if (animation == null || nodeEntities.isEmpty()) return;
        for (int i = 0; i < nodeEntities.size(); i++) {
            applyAnimationToNode(i, animation, progress);
        }
    }

    private Transformation toTransformation(TRSResult trs) {
        Quaternionf baseRot = localRotation();
        Vector3f worldTranslation = new Vector3f(trs.translation());
        baseRot.transform(worldTranslation);
        Quaternionf finalLeftRot = new Quaternionf(baseRot).mul(trs.leftRotation());
        return new Transformation(worldTranslation, finalLeftRot, trs.scale(), trs.rightRotation());
    }

    private List<Transformation> computeBackgroundTransforms(UiBackgroundNode node, float scale,
                                                             float offsetX, float offsetY, float offsetZ) {
        float pixels = options.pixelsPerBlock();
        float frontDepth = node.depth() + BACKGROUND_DEPTH_OFFSET + offsetZ;
        Vector3f p1 = new Vector3f((node.x() + offsetX) / pixels, -(node.y() + node.height() + offsetY) / pixels, frontDepth);
        Vector3f p2 = new Vector3f((node.x() + node.width() + offsetX) / pixels, -(node.y() + node.height() + offsetY) / pixels, frontDepth);
        Vector3f p3 = new Vector3f((node.x() + offsetX) / pixels, -(node.y() + offsetY) / pixels, frontDepth);
        if (scale != 1.0f) {
            Vector3f center = new Vector3f((node.x() + node.width() * 0.5f + offsetX) / pixels,
                                           -(node.y() + node.height() * 0.5f + offsetY) / pixels, frontDepth);
            p1.set(new Vector3f(center).add(new Vector3f(p1).sub(center).mul(scale)));
            p2.set(new Vector3f(center).add(new Vector3f(p2).sub(center).mul(scale)));
            p3.set(new Vector3f(center).add(new Vector3f(p3).sub(center).mul(scale)));
        }
        boolean doubleSided = isDoubleSided(node);
        List<Transformation> list = new ArrayList<>(doubleSided ? 2 : 1);
        list.add(toTransformation(DisplayShapeMath.computeParallelogramTRS(p1, p2, p3)));
        if (doubleSided) {
            float backDepth = -(node.depth() + BACKGROUND_DEPTH_OFFSET) + offsetZ;
            // The background is a depth-only backing surface. Keep its back
            // face aligned with the front face when the content is mirrored;
            // mirroring this second surface makes the panel visibly flip.
            float leftX = node.x() + node.width() + offsetX;
            float rightX = node.x() + offsetX;
            Vector3f p1b = new Vector3f(leftX / pixels,
                    -(node.y() + node.height() + offsetY) / pixels, backDepth);
            Vector3f p2b = new Vector3f(rightX / pixels,
                    -(node.y() + node.height() + offsetY) / pixels, backDepth);
            Vector3f p3b = new Vector3f(leftX / pixels,
                    -(node.y() + offsetY) / pixels, backDepth);
            if (scale != 1.0f) {
                Vector3f center = new Vector3f((leftX + rightX) * 0.5f / pixels,
                        -(node.y() + node.height() * 0.5f + offsetY) / pixels, backDepth);
                p1b.set(new Vector3f(center).add(new Vector3f(p1b).sub(center).mul(scale)));
                p2b.set(new Vector3f(center).add(new Vector3f(p2b).sub(center).mul(scale)));
                p3b.set(new Vector3f(center).add(new Vector3f(p3b).sub(center).mul(scale)));
            }
            list.add(toTransformation(DisplayShapeMath.computeParallelogramTRS(p1b, p2b, p3b)));
        }
        return list;
    }

    private List<Transformation> computeTextTransforms(TextNode node, float scale,
                                                       float offsetX, float offsetY, float offsetZ) {
        float displayScale = node.scale() * scale;
        boolean doubleSided = isDoubleSided(node);
        List<Transformation> list = new ArrayList<>(doubleSided ? 2 : 1);
        list.add(transform(node, displayScale, displayScale, displayScale, offsetX, offsetY, offsetZ));
        if (doubleSided) {
            list.add(transformBack(node, displayScale, displayScale, displayScale, offsetX, offsetY, offsetZ));
        }
        return list;
    }

    private List<Transformation> computeAlignedTextTransforms(AlignedTextNode node, float scale,
                                                              float offsetX, float offsetY, float offsetZ) {
        float displayScale = node.fontSize() / 20.0f * scale;
        boolean doubleSided = isDoubleSided(node);
        List<Transformation> list = new ArrayList<>(doubleSided ? 2 : 1);
        list.add(transform(node, displayScale, displayScale, displayScale, offsetX, offsetY, offsetZ));
        if (doubleSided) {
            list.add(transformBack(node, displayScale, displayScale, displayScale, offsetX, offsetY, offsetZ));
        }
        return list;
    }

    private List<Transformation> computeItemTransforms(ItemNode node, float scale,
                                                       float offsetX, float offsetY, float offsetZ) {
        float displayScale = node.scale() * scale;
        boolean doubleSided = isDoubleSided(node);
        List<Transformation> list = new ArrayList<>(doubleSided ? 2 : 1);
        list.add(transform(node, displayScale, displayScale, displayScale, offsetX, offsetY, offsetZ));
        if (doubleSided) {
            list.add(transformBack(node, displayScale, displayScale, displayScale, offsetX, offsetY, offsetZ));
        }
        return list;
    }

    private List<Transformation> computeIconTransforms(UiIconNode node, float scale,
                                                       float offsetX, float offsetY, float offsetZ) {
        float pixels = options.pixelsPerBlock();
        float sx = node.width() / pixels * scale;
        float sy = node.height() / pixels * scale;
        float sz = Math.min(node.width(), node.height()) / pixels * scale;
        boolean doubleSided = isDoubleSided(node);
        List<Transformation> list = new ArrayList<>(doubleSided ? 2 : 1);
        list.add(transform(node, sx, sy, sz, offsetX, offsetY, offsetZ));
        if (doubleSided) {
            list.add(transformBack(node, sx, sy, sz, offsetX, offsetY, offsetZ));
        }
        return list;
    }

    private List<Transformation> computeModelTransforms(EntityModelNode node, float scale,
                                                        float offsetX, float offsetY, float offsetZ) {
        boolean doubleSided = isDoubleSided(node);
        List<Transformation> list = new ArrayList<>(doubleSided ? 2 : 1);
        list.add(modelTransform(node, scale, offsetX, offsetY, offsetZ));
        if (doubleSided) {
            list.add(modelTransformBack(node, scale, offsetX, offsetY, offsetZ));
        }
        return list;
    }

    private List<Transformation> computeLineTransforms(LineNode node, float scale,
                                                       float offsetX, float offsetY, float offsetZ) {
        float pixels = options.pixelsPerBlock();
        float x1 = node.x1();
        float y1 = node.y1();
        float x2 = node.x2();
        float y2 = node.y2();
        float depth = node.depth();
        float rollRad = (float) Math.toRadians(node.roll());
        boolean doubleSided = isDoubleSided(node);

        Vector3f p1 = new Vector3f((x1 + offsetX) / pixels, -(y1 + offsetY) / pixels, depth + offsetZ);
        Vector3f p2 = new Vector3f((x2 + offsetX) / pixels, -(y2 + offsetY) / pixels, depth + offsetZ);
        if (p1.distanceSquared(p2) < 1e-6f) {
            p2.add(0.001f, 0.0f, 0.0f);
        }
        float thickness = Math.max(0.0001f, (node.thickness() / pixels) * scale);
        if (scale != 1.0f) {
            Vector3f center = new Vector3f(p1).add(p2).mul(0.5f);
            p1.set(new Vector3f(center).add(new Vector3f(p1).sub(center).mul(scale)));
            p2.set(new Vector3f(center).add(new Vector3f(p2).sub(center).mul(scale)));
        }
        List<Transformation> list = new ArrayList<>(doubleSided ? 2 : 1);
        TRSResult trs = DisplayShapeMath.computeLineTRS(p1, p2, thickness, rollRad);
        list.add(toTransformation(trs));
        if (doubleSided) {
            float backDepth = -(depth + 0.022f + offsetZ);
            float backOffsetX = mirrorSide ? -offsetX : offsetX;
            float bx1 = mirrorSide ? -x1 : x1;
            float bx2 = mirrorSide ? -x2 : x2;
            Vector3f p1b = new Vector3f((bx1 + backOffsetX) / pixels, -(y1 + offsetY) / pixels, backDepth);
            Vector3f p2b = new Vector3f((bx2 + backOffsetX) / pixels, -(y2 + offsetY) / pixels, backDepth);
            if (scale != 1.0f) {
                GeometryUtils.scaleAroundMidpoint(p1b, p2b, scale);
            }
            // Keep the back segment as a separate, reversed face.  Reversing the
            // endpoints makes the line's local winding deterministic at every angle.
            Vector3f backStart = p1b;
            Vector3f backEnd = p2b;
            if (GeometryUtils.lineFacingNormal(p1, p2)
                    .dot(GeometryUtils.lineFacingNormal(p1b, p2b)) >= 0.0f) {
                backStart = p2b;
                backEnd = p1b;
            }
            TRSResult backTrs = DisplayShapeMath.computeLineTRS(backStart, backEnd, thickness, -rollRad);
            list.add(toTransformation(backTrs));
        }
        return list;
    }

    private List<Transformation> computeParallelogramTransforms(ParallelogramNode node, float scale,
                                                                float offsetX, float offsetY, float offsetZ) {
        float pixels = options.pixelsPerBlock();
        float depth = node.depth();
        boolean doubleSided = isDoubleSided(node);

        Vector3f p1 = new Vector3f((node.x1() + offsetX) / pixels, -(node.y1() + offsetY) / pixels, depth + offsetZ);
        Vector3f p2 = new Vector3f((node.x2() + offsetX) / pixels, -(node.y2() + offsetY) / pixels, depth + offsetZ);
        Vector3f p3 = new Vector3f((node.x3() + offsetX) / pixels, -(node.y3() + offsetY) / pixels, depth + offsetZ);

        Vector3f[] front = GeometryUtils.normalizeParallelogramWinding(p1, p2, p3);
        p1 = front[0];
        p2 = front[1];
        p3 = front[2];

        if (scale != 1.0f) {
            Vector3f center = new Vector3f(p2).add(p3).mul(0.5f);
            p1.set(new Vector3f(center).add(new Vector3f(p1).sub(center).mul(scale)));
            p2.set(new Vector3f(center).add(new Vector3f(p2).sub(center).mul(scale)));
            p3.set(new Vector3f(center).add(new Vector3f(p3).sub(center).mul(scale)));
        }

        List<Transformation> list = new ArrayList<>(doubleSided ? 2 : 1);
        TRSResult trs = DisplayShapeMath.computeParallelogramTRS(p1, p2, p3);
        list.add(toTransformation(trs));
        if (doubleSided) {
            float backDepth = -(depth + 0.022f + offsetZ);
            float backOffsetX = mirrorSide ? -offsetX : offsetX;
            float bx1 = mirrorSide ? -node.x1() : node.x1();
            float bx2 = mirrorSide ? -node.x2() : node.x2();
            float bx3 = mirrorSide ? -node.x3() : node.x3();
            Vector3f p1b = new Vector3f((bx1 + backOffsetX) / pixels, -(node.y1() + offsetY) / pixels, backDepth);
            Vector3f p2b = new Vector3f((bx2 + backOffsetX) / pixels, -(node.y2() + offsetY) / pixels, backDepth);
            Vector3f p3b = new Vector3f((bx3 + backOffsetX) / pixels, -(node.y3() + offsetY) / pixels, backDepth);
            GeometryUtils.scaleAroundParallelogramCenter(p1b, p2b, p3b, scale);
            // Preserve the original width edge while reversing the surface winding.
            Vector3f[] backSource = GeometryUtils.normalizeParallelogramWinding(p1b, p2b, p3b);
            Vector3f[] back = GeometryUtils.reverseParallelogramWinding(
                    backSource[0], backSource[1], backSource[2]);
            TRSResult backTrs = DisplayShapeMath.computeParallelogramTRS(back[0], back[1], back[2]);
            list.add(toTransformation(backTrs));
        }
        return list;
    }

    private List<Transformation> computeTriangleTransforms(TriangleNode node, float scale,
                                                           float offsetX, float offsetY, float offsetZ) {
        float pixels = options.pixelsPerBlock();
        float depth = node.depth();
        boolean doubleSided = isDoubleSided(node);

        Vector3f p1 = new Vector3f((node.x1() + offsetX) / pixels, -(node.y1() + offsetY) / pixels, depth + offsetZ);
        Vector3f p2 = new Vector3f((node.x2() + offsetX) / pixels, -(node.y2() + offsetY) / pixels, depth + offsetZ);
        Vector3f p3 = new Vector3f((node.x3() + offsetX) / pixels, -(node.y3() + offsetY) / pixels, depth + offsetZ);

        Vector3f[] front = GeometryUtils.normalizeTriangleWinding(p1, p2, p3);
        p1 = front[0];
        Vector3f frontP2 = front[1];
        Vector3f frontP3 = front[2];

        if (scale != 1.0f) {
            Vector3f center = new Vector3f(p1).add(frontP2).add(frontP3).div(3.0f);
            p1.set(new Vector3f(center).add(new Vector3f(p1).sub(center).mul(scale)));
            frontP2.set(new Vector3f(center).add(new Vector3f(frontP2).sub(center).mul(scale)));
            frontP3.set(new Vector3f(center).add(new Vector3f(frontP3).sub(center).mul(scale)));
        }

        List<TRSResult> trsResults = DisplayShapeMath.computeTriangleTRS(p1, frontP2, frontP3);
        List<Transformation> list = new ArrayList<>(doubleSided ? 6 : 3);
        for (TRSResult trs : trsResults) {
            list.add(toTransformation(trs));
        }
        if (doubleSided) {
            float backDepth = -(depth + 0.022f + offsetZ);
            float backOffsetX = mirrorSide ? -offsetX : offsetX;
            float bx1 = mirrorSide ? -node.x1() : node.x1();
            float bx2 = mirrorSide ? -node.x2() : node.x2();
            float bx3 = mirrorSide ? -node.x3() : node.x3();
            Vector3f p1b = new Vector3f((bx1 + backOffsetX) / pixels, -(node.y1() + offsetY) / pixels, backDepth);
            Vector3f p2b = new Vector3f((bx2 + backOffsetX) / pixels, -(node.y2() + offsetY) / pixels, backDepth);
            Vector3f p3b = new Vector3f((bx3 + backOffsetX) / pixels, -(node.y3() + offsetY) / pixels, backDepth);
            GeometryUtils.scaleAroundCentroid(p1b, p2b, p3b, scale);
            Vector3f[] backSource = GeometryUtils.normalizeTriangleWinding(p1b, p2b, p3b);
            Vector3f[] back = GeometryUtils.reverseTriangleWinding(
                    backSource[0], backSource[1], backSource[2]);
            List<TRSResult> backTrsResults = DisplayShapeMath.computeTriangleTRS(back[0], back[1], back[2]);
            for (TRSResult trs : backTrsResults) {
                list.add(toTransformation(trs));
            }
        }
        return list;
    }

    private List<Transformation> computePolylineTransforms(PolylineNode node, float scale,
                                                           float offsetX, float offsetY, float offsetZ) {
        List<PolylineNode.Point> pts = node.points();
        if (pts.size() < 2) {
            return List.of(transform(node, scale, scale, scale, offsetX, offsetY, offsetZ));
        }
        float pixels = options.pixelsPerBlock();
        float depth = node.depth();
        float thickness = Math.max(0.0001f, (node.thickness() / pixels) * scale);
        boolean doubleSided = isDoubleSided(node);

        int segmentCount = pts.size() - 1 + (node.closed() && pts.size() > 2 ? 1 : 0);
        List<Transformation> list = new ArrayList<>(doubleSided ? segmentCount * 2 : segmentCount);

        for (int i = 0; i < segmentCount; i++) {
            PolylineNode.Point ptA = pts.get(i % pts.size());
            PolylineNode.Point ptB = pts.get((i + 1) % pts.size());

            Vector3f p1 = new Vector3f((ptA.x() + offsetX) / pixels, -(ptA.y() + offsetY) / pixels, depth + offsetZ);
            Vector3f p2 = new Vector3f((ptB.x() + offsetX) / pixels, -(ptB.y() + offsetY) / pixels, depth + offsetZ);
            if (p1.distanceSquared(p2) < 1e-6f) {
                p2.add(0.001f, 0.0f, 0.0f);
            }
            if (scale != 1.0f) {
                Vector3f center = new Vector3f(p1).add(p2).mul(0.5f);
                p1.set(new Vector3f(center).add(new Vector3f(p1).sub(center).mul(scale)));
                p2.set(new Vector3f(center).add(new Vector3f(p2).sub(center).mul(scale)));
            }
            TRSResult trs = DisplayShapeMath.computeLineTRS(p1, p2, thickness, 0.0f);
            list.add(toTransformation(trs));
            if (doubleSided) {
                float backDepth = -(depth + 0.022f + offsetZ);
                float backOffsetX = mirrorSide ? -offsetX : offsetX;
                float bx1 = mirrorSide ? -ptA.x() : ptA.x();
                float bx2 = mirrorSide ? -ptB.x() : ptB.x();
                Vector3f p1b = new Vector3f((bx1 + backOffsetX) / pixels, -(ptA.y() + offsetY) / pixels, backDepth);
                Vector3f p2b = new Vector3f((bx2 + backOffsetX) / pixels, -(ptB.y() + offsetY) / pixels, backDepth);
                if (scale != 1.0f) {
                    GeometryUtils.scaleAroundMidpoint(p1b, p2b, scale);
                }
                // Choose the endpoint order from the actual line normals.
                // Mirroring and near-vertical lines do not always invert the
                // same basis axis, so a mirrorSide boolean alone is unreliable.
                Vector3f backStart = p1b;
                Vector3f backEnd = p2b;
                if (GeometryUtils.lineFacingNormal(p1, p2)
                        .dot(GeometryUtils.lineFacingNormal(p1b, p2b)) >= 0.0f) {
                    backStart = p2b;
                    backEnd = p1b;
                }
                TRSResult backTrs = DisplayShapeMath.computeLineTRS(backStart, backEnd, thickness, 0.0f);
                list.add(toTransformation(backTrs));
            }
        }
        return list;
    }

    private List<Transformation> getNodeTransformations(UiNode node, float scale,
                                                        float offsetX, float offsetY, float offsetZ) {
        if (node instanceof BlockNode block) {
            return computeBlockTransforms(block, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof AlignedTextNode text) {
            return computeAlignedTextTransforms(text, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof TextNode text) {
            return computeTextTransforms(text, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof ItemNode item) {
            return computeItemTransforms(item, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof UiIconNode icon) {
            return computeIconTransforms(icon, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof EntityModelNode model) {
            return computeModelTransforms(model, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof MobEntityNode mob) {
            EntityModelNode model = EntityModelNode.forMob(
                            mob.entityType().name().toLowerCase(),
                            mob.x(), mob.y(), mob.width(), mob.height(), mob.scale())
                    .withYaw(mob.yaw())
                    .withPitch(mob.pitch())
                    .withDoubleSided(mob.doubleSided());
            return computeModelTransforms(model, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof UiBackgroundNode background) {
            return computeBackgroundTransforms(background, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof LineNode line) {
            return computeLineTransforms(line, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof ParallelogramNode parallelogram) {
            return computeParallelogramTransforms(parallelogram, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof TriangleNode triangle) {
            return computeTriangleTransforms(triangle, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof PolylineNode polyline) {
            return computePolylineTransforms(polyline, scale, offsetX, offsetY, offsetZ);
        }
        return List.of();
    }

    private void applyAnimationToNode(int index, UiAnimation current, double progress) {
        if (index >= nodeEntities.size() || index >= document.nodes().size()) return;
        double eased = current.easing().apply(progress);
        float scale = interpolate(current.fromScale(), current.toScale(), eased);
        float opacity = Math.clamp(interpolate(current.fromOpacity(), current.toOpacity(), eased), 0.0f, 1.0f);
        float offsetX = current.offsetX() * (1.0f - (float) eased);
        float offsetY = current.offsetY() * (1.0f - (float) eased);
        float offsetZ = current.offsetZ() * (1.0f - (float) eased);
        UiNode node = document.nodes().get(index);
        List<Transformation> transforms = getNodeTransformations(node, scale, offsetX, offsetY, offsetZ);
        List<Display> displays = nodeEntities.get(index);
        for (int j = 0; j < displays.size() && j < transforms.size(); j++) {
            Display display = displays.get(j);
            if (display == null || !display.isValid()) continue;
            display.setInterpolationDelay(0);
            display.setInterpolationDuration(ANIMATION_INTERPOLATION_TICKS);
            display.setTransformation(transforms.get(j));
            if (display instanceof TextDisplay textDisplay) {
                if (node instanceof UiBackgroundNode background) {
                    textDisplay.setTextOpacity((byte) 0);
                    textDisplay.setBackgroundColor(ColorUtils.withOpacity(background.background(), opacity));
                } else {
                    textDisplay.setTextOpacity((byte) Math.round(opacity * 255.0f));
                }
            }
        }
    }

    private void applyAnimationFrame(float progress) {
        applyAnimationFrame(progress, ANIMATION_INTERPOLATION_TICKS);
    }

    private void applyAnimationFrame(float progress, int interpolationTicks) {
        if (animation == null) return;
        float scale = MathUtils.lerp(animation.fromScale(), animation.toScale(), progress);
        float invProgress = 1.0f - progress;
        float offsetX = animation.offsetX() * invProgress;
        float offsetY = animation.offsetY() * invProgress;
        float offsetZ = animation.offsetZ() * invProgress;
        float opacity = MathUtils.lerp(animation.fromOpacity(), animation.toOpacity(), progress);
        byte opacityByte = (byte) Math.round(opacity * 255.0f);

        for (int i = 0; i < nodeEntities.size() && i < document.nodes().size(); i++) {
            UiNode node = document.nodes().get(i);
            List<Transformation> transforms = getNodeTransformations(node, scale, offsetX, offsetY, offsetZ);
            List<Display> displays = nodeEntities.get(i);
            if (displays == null) continue;

            for (int j = 0; j < displays.size() && j < transforms.size(); j++) {
                Display display = displays.get(j);
                if (display == null || !display.isValid()) continue;
                display.setInterpolationDelay(0);
                display.setInterpolationDuration(interpolationTicks);
                display.setTransformation(transforms.get(j));
                if (display instanceof TextDisplay textDisplay) {
                    if (node instanceof UiBackgroundNode background) {
                        textDisplay.setTextOpacity((byte) 0);
                        textDisplay.setBackgroundColor(ColorUtils.withOpacity(background.background(), opacity));
                    } else {
                        textDisplay.setTextOpacity(opacityByte);
                    }
                }
            }
        }
    }

    private void applyNodeAnimationFrames() {
        applyNodeAnimationFrames(ANIMATION_INTERPOLATION_TICKS);
    }

    private void applyNodeAnimationFrames(int interpolationTicks) {
        for (int i = 0; i < nodeAnimations.size() && i < document.nodes().size() && i < nodeEntities.size(); i++) {
            UiAnimation a = nodeAnimations.get(i);
            UiNode node = document.nodes().get(i);
            List<Display> displays = nodeEntities.get(i);
            if (displays == null) continue;

            float scale = 1.0f;
            float offsetX = 0.0f;
            float offsetY = 0.0f;
            float offsetZ = 0.0f;
            byte opacityByte = (byte) 255;

            if (a.durationTicks() > 0) {
                double effectiveAge = nodeAnimationAgesTicks[i] - a.delayTicks();
                float progress;
                if (effectiveAge < 0) {
                    progress = 0.0f;
                } else if (effectiveAge >= a.durationTicks()) {
                    progress = 1.0f;
                } else {
                    progress = (float) a.easing().apply(effectiveAge / a.durationTicks());
                }

                scale = MathUtils.lerp(a.fromScale(), a.toScale(), progress);
                float invProgress = 1.0f - progress;
                offsetX = a.offsetX() * invProgress;
                offsetY = a.offsetY() * invProgress;
                offsetZ = a.offsetZ() * invProgress;
                float opacity = MathUtils.lerp(a.fromOpacity(), a.toOpacity(), progress);
                opacityByte = (byte) Math.round(opacity * 255.0f);
            }

            List<Transformation> transforms = getNodeTransformations(node, scale, offsetX, offsetY, offsetZ);
            for (int j = 0; j < displays.size() && j < transforms.size(); j++) {
                Display display = displays.get(j);
                if (display == null || !display.isValid()) continue;
                display.setInterpolationDelay(0);
                display.setInterpolationDuration(interpolationTicks);
                display.setTransformation(transforms.get(j));
                if (display instanceof TextDisplay textDisplay) {
                    textDisplay.setTextOpacity(opacityByte);
                }
            }
        }
    }

    private void resetTransforms() {
        resetTransforms(ANIMATION_INTERPOLATION_TICKS);
    }

    private void resetTransforms(int interpolationTicks) {
        for (int i = 0; i < nodeEntities.size() && i < document.nodes().size(); i++) {
            UiNode node = document.nodes().get(i);
            List<Transformation> transforms = getNodeTransformations(node, 1.0f, 0.0f, 0.0f, 0.0f);
            List<Display> displays = nodeEntities.get(i);
            if (displays == null) continue;

            for (int j = 0; j < displays.size() && j < transforms.size(); j++) {
                Display display = displays.get(j);
                if (display == null || !display.isValid()) continue;
                display.setInterpolationDelay(0);
                display.setInterpolationDuration(interpolationTicks);
                display.setTransformation(transforms.get(j));
                if (display instanceof TextDisplay textDisplay) {
                    textDisplay.setTextOpacity((byte) 255);
                }
            }
        }
    }

    /** Re-evaluates the active frame after a side change without stale client interpolation. */
    private void applyCurrentTransformsImmediately() {
        applyCurrentTransforms(0);
    }

    private void applyCurrentTransforms(int interpolationTicks) {
        if (animation != null) {
            double effectiveAge = animationAgeTicks - animation.delayTicks();
            float progress;
            if (effectiveAge <= 0.0 || animation.durationTicks() <= 0) {
                progress = 0.0f;
            } else if (effectiveAge >= animation.durationTicks()) {
                progress = 1.0f;
            } else {
                progress = (float) animation.easing().apply(effectiveAge / animation.durationTicks());
            }
            applyAnimationFrame(progress, interpolationTicks);
        } else if (!nodeAnimations.isEmpty()) {
            applyNodeAnimationFrames(interpolationTicks);
        } else {
            resetTransforms(interpolationTicks);
        }
    }

    private void syncViewers() {
        World world = origin.getWorld();
        if (world == null || entities.isEmpty()) return;
        visibleViewers.removeIf(playerId -> {
            Player player = Bukkit.getPlayer(playerId);
            return player == null || player.getWorld() != world;
        });
        for (Player player : world.getPlayers()) syncPlayer(player);
    }

    private void syncPlayer(Player player) {
        boolean visible = shouldShow(player);
        boolean alreadyVisible = visibleViewers.contains(player.getUniqueId());
        if (visible != alreadyVisible) {
            for (Display display : entities) {
                if (visible) player.showEntity(plugin, display);
                else player.hideEntity(plugin, display);
            }
            if (visible) visibleViewers.add(player.getUniqueId());
            else visibleViewers.remove(player.getUniqueId());
        }
        if (visible && options.cullItemBackfaces()) syncItemBackfaces(player);
        if (visible) syncSideVisibility(player);
    }

    private void showEntities(Player player) {
        entities.forEach(entity -> player.showEntity(plugin, entity));
        visibleViewers.add(player.getUniqueId());
        syncItemBackfaces(player);
        syncSideVisibility(player);
    }

    private void syncSideVisibility(Player player) {
        boolean front = isFrontFacing(player);
        for (int i = 0; i < nodeEntities.size() && i < document.nodes().size(); i++) {
            List<Display> displays = nodeEntities.get(i);
            UiNode node = document.nodes().get(i);
            if (displays == null || displays.isEmpty()) continue;
            boolean twoSided = isDoubleSided(node);
            if (displays.size() == 1) {
                if (front || twoSided) player.showEntity(plugin, displays.get(0));
                else player.hideEntity(plugin, displays.get(0));
                continue;
            }
            for (int j = 0; j < displays.size(); j++) {
                boolean back = isBackDisplay(node, displays.size(), j);
                boolean show = twoSided && (front != back);
                if (show) player.showEntity(plugin, displays.get(j));
                else player.hideEntity(plugin, displays.get(j));
            }
        }
    }

    private boolean isBackDisplay(UiNode node, int size, int index) {
        if (node instanceof PolylineNode) return (index & 1) == 1;
        if (node instanceof TriangleNode) return index >= size / 2;
        return index > 0;
    }

    private void configureAnimationInterpolation() {
        for (int i = 0; i < nodeEntities.size(); i++) {
            // A scene animation applies to every node; node animations have
            // one animation descriptor per node. Both paths need the same
            // interpolation setup before their first frame is sent.
            UiAnimation configured = animation != null
                    ? animation
                    : (i < nodeAnimations.size() ? nodeAnimations.get(i) : null);
            if (configured == null || isStaticAnimation(configured)) continue;
            List<Display> displays = nodeEntities.get(i);
            if (displays == null) continue;
            for (Display display : displays) {
                if (display == null || !display.isValid()) continue;
                display.setInterpolationDelay(0);
                display.setInterpolationDuration(ANIMATION_INTERPOLATION_TICKS);
            }
        }
    }

    private boolean isStaticAnimation(UiAnimation animation) {
        return animation.offsetX() == 0.0f && animation.offsetY() == 0.0f
                && animation.offsetZ() == 0.0f
                && animation.fromScale() == 1.0f && animation.toScale() == 1.0f
                && animation.fromOpacity() == 1.0f && animation.toOpacity() == 1.0f;
    }

    private Transformation backgroundTransform(UiBackgroundNode node, float scale) {
        return backgroundTransform(node, scale, 0.0f, 0.0f, 0.0f);
    }

    private Transformation backgroundTransform(UiBackgroundNode node, float scale,
                                               float offsetX, float offsetY,
                                               float offsetZ) {
        float pixels = options.pixelsPerBlock();
        return transform(node,
                         node.width() / pixels * BACKGROUND_NATIVE_WIDTH_SCALE * scale,
                         node.height() / pixels * BACKGROUND_NATIVE_HEIGHT_SCALE * scale,
                         0.0f, offsetX + node.width() * 0.5f,
                         offsetY + node.height() * 1.0f,
                         offsetZ + BACKGROUND_DEPTH_OFFSET);
    }

    private float interpolate(float from, float to, double progress) {
        return (float) MathUtils.lerp(from, to, progress);
    }

    private TextDisplay.TextAlignment screenAlignment(TextDisplay.TextAlignment alignment) {
        return switch (alignment) {
            case LEFT -> TextDisplay.TextAlignment.RIGHT;
            case RIGHT -> TextDisplay.TextAlignment.LEFT;
            case CENTER -> TextDisplay.TextAlignment.CENTER;
        };
    }

    private TextDisplay.TextAlignment screenAlignment(
            UiTextAlignment alignment) {
        return switch (alignment) {
            case LEFT -> TextDisplay.TextAlignment.RIGHT;
            case RIGHT -> TextDisplay.TextAlignment.LEFT;
            case CENTER -> TextDisplay.TextAlignment.CENTER;
        };
    }

    /** A mirrored side needs the same back-face display as double-sided mode. */
    private boolean isTwoSided() {
        return doubleSided
                || document.nodes().stream().anyMatch(UiNode::doubleSided);
    }

    private boolean isDoubleSided(UiNode node) {
        return node.doubleSided() || doubleSided;
    }

    private void syncItemBackfaces(Player player) {
        if (isTwoSided() || cameraTransform.billboard() != Display.Billboard.FIXED) return;
        boolean frontFacing = isFrontFacing(player);
        for (int i = 0; i < nodeEntities.size() && i < document.nodes().size(); i++) {
            UiNode node = document.nodes().get(i);
            if (node.doubleSided()) continue;
            if (!(node instanceof ItemNode) && !(node instanceof UiIconNode) && !(node instanceof EntityModelNode) && !(node instanceof MobEntityNode)) continue;
            List<Display> list = nodeEntities.get(i);
            if (list == null) continue;
            for (Display display : list) {
                if (display == null || !display.isValid()) continue;
                if (frontFacing) player.showEntity(plugin, display);
                else player.hideEntity(plugin, display);
            }
        }
    }

    private boolean isFrontFacing(Player player) {
        Vector normal = origin.getDirection();
        if (normal.lengthSquared() < 0.0001) normal.setZ(1.0);
        normal.normalize();
        Vector toPlayer = player.getEyeLocation().toVector().subtract(origin.toVector());
        if (toPlayer.lengthSquared() < 0.0001) return true;
        return normal.dot(toPlayer.normalize()) > 0.0;
    }

    private boolean shouldShow(Player player) {
        UUID playerId = player.getUniqueId();
        if (forcedHidden.contains(playerId)) return false;
        if (!forcedVisible.contains(playerId) && !audience.canView(player)) return false;
        if (player.getWorld() != origin.getWorld()) return false;
        if (player.getEyeLocation().distanceSquared(origin) > options.maxDistance() * options.maxDistance()) {
            return false;
        }
        if (!options.requireFront() || isTwoSided()) return true;
        var normal = origin.getDirection().setY(0.0);
        var toPlayer = player.getEyeLocation().toVector().subtract(origin.toVector()).setY(0.0);
        return normal.lengthSquared() < 0.0001 || toPlayer.lengthSquared() < 0.0001
                || normal.normalize().dot(toPlayer.normalize()) > 0.05;
    }

    private void hideEntities(Player player) {
        entities.forEach(entity -> player.hideEntity(plugin, entity));
        visibleViewers.remove(player.getUniqueId());
    }

    private void clearEntities() {
        entities.stream().filter(Entity::isValid).forEach(Entity::remove);
        entities.clear();
        nodeEntities.clear();
        visibleViewers.clear();
        renderedScrollOffsets.clear();
        if (interactionEntity != null && interactionEntity.isValid()) interactionEntity.remove();
        interactionEntity = null;
    }

    private void ensureValid() {
        if (removed) throw new IllegalStateException("UI scene has been removed");
    }

    private void updateNode(List<Display> currentDisplays, UiNode node) {
        if (currentDisplays == null || currentDisplays.isEmpty()) return;
        for (Display display : currentDisplays) {
            if (display != null && display.isValid()) {
                display.setInterpolationDelay(0);
                display.setInterpolationDuration(INTERPOLATION_TICKS);
            }
        }
        if (node instanceof UiBackgroundNode background) {
            List<Transformation> transforms = computeBackgroundTransforms(background, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof TextDisplay textDisplay) {
                    textDisplay.setBackgroundColor(background.background());
                    textDisplay.setTransformation(transforms.get(j));
                }
            }
        } else if (node instanceof TextNode text) {
            List<Transformation> transforms = computeTextTransforms(text, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof TextDisplay textDisplay) {
                    textDisplay.text(text.text());
                    textDisplay.setShadowed(text.shadow());
                    textDisplay.setSeeThrough(text.seeThrough());
                    textDisplay.setAlignment(text.alignment());
                    textDisplay.setLineWidth(text.lineWidth());
                    textDisplay.setTransformation(transforms.get(j));
                }
            }
        } else if (node instanceof AlignedTextNode text) {
            List<Transformation> transforms = computeAlignedTextTransforms(text, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof TextDisplay textDisplay) {
                    textDisplay.text(text.text());
                    textDisplay.setShadowed(text.shadow());
                    textDisplay.setSeeThrough(text.seeThrough());
                    textDisplay.setLineWidth(Math.max(1,
                                                      Math.round(text.width() * 20.0f / text.fontSize())));
                    textDisplay.setTransformation(transforms.get(j));
                }
            }
        } else if (node instanceof ItemNode item) {
            List<Transformation> transforms = computeItemTransforms(item, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof ItemDisplay itemDisplay) {
                    itemDisplay.setItemStack(item.item());
                    itemDisplay.setItemDisplayTransform(item.transform());
                    itemDisplay.setTransformation(transforms.get(j));
                }
            }
        } else if (node instanceof UiIconNode icon) {
            List<Transformation> transforms = computeIconTransforms(icon, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof ItemDisplay itemDisplay) {
                    itemDisplay.setItemStack(icon.item());
                    itemDisplay.setItemDisplayTransform(icon.transform());
                    itemDisplay.setTransformation(transforms.get(j));
                }
            }
        } else if (node instanceof BlockNode block) {
            List<Transformation> transforms = computeBlockTransforms(block, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof BlockDisplay blockDisplay) {
                    blockDisplay.setBlock(block.block());
                    blockDisplay.setTransformation(transforms.get(j));
                }
            }
        } else if (node instanceof EntityModelNode model) {
            List<Transformation> transforms = computeModelTransforms(model, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof ItemDisplay itemDisplay) {
                    itemDisplay.setItemStack(model.item());
                    itemDisplay.setItemDisplayTransform(model.transform());
                    itemDisplay.setTransformation(transforms.get(j));
                }
            }
        } else if (node instanceof MobEntityNode mob) {
            EntityModelNode model = EntityModelNode.forMob(
                            mob.entityType().name().toLowerCase(),
                            mob.x(), mob.y(), mob.width(), mob.height(), mob.scale())
                    .withYaw(mob.yaw())
                    .withPitch(mob.pitch())
                    .withDoubleSided(mob.doubleSided());
            List<Transformation> transforms = computeModelTransforms(model, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof ItemDisplay itemDisplay) {
                    itemDisplay.setItemStack(model.item());
                    itemDisplay.setItemDisplayTransform(model.transform());
                    itemDisplay.setTransformation(transforms.get(j));
                }
            }
        } else if (node instanceof LineNode line) {
            List<Transformation> transforms = computeLineTransforms(line, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof TextDisplay textDisplay) {
                    textDisplay.setBackgroundColor(line.color());
                    textDisplay.setTransformation(transforms.get(j));
                }
            }
        } else if (node instanceof ParallelogramNode parallelogram) {
            List<Transformation> transforms = computeParallelogramTransforms(parallelogram, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof TextDisplay textDisplay) {
                    textDisplay.setBackgroundColor(parallelogram.color());
                    textDisplay.setTransformation(transforms.get(j));
                }
            }
        } else if (node instanceof TriangleNode triangle) {
            List<Transformation> transforms = computeTriangleTransforms(triangle, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof TextDisplay textDisplay) {
                    textDisplay.setBackgroundColor(triangle.color());
                    textDisplay.setTransformation(transforms.get(j));
                }
            }
        } else if (node instanceof PolylineNode polyline) {
            List<Transformation> transforms = computePolylineTransforms(polyline, 1.0f, 0.0f, 0.0f, 0.0f);
            for (int j = 0; j < currentDisplays.size() && j < transforms.size(); j++) {
                Display display = currentDisplays.get(j);
                if (display instanceof TextDisplay textDisplay) {
                    textDisplay.setBackgroundColor(polyline.color());
                    textDisplay.setTransformation(transforms.get(j));
                }
            }
        }
    }

    private boolean incrementalUpdate(UiDocument previous, UiDocument next) {
        if (previous == null || previous.nodes().size() != next.nodes().size()) return false;
        for (int i = 0; i < next.nodes().size(); i++) {
            UiNode prev = previous.nodes().get(i);
            UiNode curr = next.nodes().get(i);
            if (!prev.getClass().equals(curr.getClass())) return false;
            if (prev.doubleSided() != curr.doubleSided()) return false;
            if (prev instanceof PolylineNode p1 && curr instanceof PolylineNode p2) {
                if (p1.points().size() != p2.points().size()) return false;
            }
        }
        for (int i = 0; i < next.nodes().size(); i++) {
            updateNode(nodeEntities.get(i), next.nodes().get(i));
        }
        updateInteractionHitbox();
        return true;
    }

    private void respawn() {
        clearEntities();
        if (document != null) {
            for (UiNode node : document.nodes()) {
                List<Display> spawned = spawnNode(node);
                nodeEntities.add(spawned);
                entities.addAll(spawned);
            }
            for (UiControl control : document.controls()) {
                if (control instanceof UiScrollList scrollList) {
                    renderedScrollOffsets.put(scrollList.id(), scrollList.offset());
                }
            }
        }
        spawnInteraction();
        syncViewers();
    }

    private void applyCameraTransform() {
        for (Display display : entities) {
            if (display.isValid()) {
                display.setBillboard(cameraTransform.billboard());
                display.setInterpolationDelay(0);
                display.setInterpolationDuration(INTERPOLATION_TICKS);
            }
        }
        resetTransforms();
    }

    private Quaternionf localRotation() {
        Quaternionf rotation = new Quaternionf();
        if (cameraTransform.angleZ() != 0.0f) {
            rotation.rotateZ((float) Math.toRadians(cameraTransform.angleZ()));
        }
        if (cameraTransform.angleY() != 0.0f) {
            rotation.rotateY((float) Math.toRadians(cameraTransform.angleY()));
        }
        if (cameraTransform.angleX() != 0.0f) {
            rotation.rotateX((float) Math.toRadians(cameraTransform.angleX()));
        }
        return rotation;
    }

    private PlaneBasis planeBasis(Player player) {
        Vector baseNormal = origin.getDirection();
        if (baseNormal.lengthSquared() < 0.0001) baseNormal.setZ(1);
        baseNormal.normalize();

        Vector cameraNormal = player.getEyeLocation().getDirection().multiply(-1);
        if (cameraNormal.lengthSquared() < 0.0001) cameraNormal = baseNormal.clone();
        cameraNormal.normalize();

        Vector normal = baseNormal;
        if (!cameraTransform.lockX() && !cameraTransform.lockY()) normal = cameraNormal;
        else if (!cameraTransform.lockX()) {
            normal = new Vector(cameraNormal.getX(), baseNormal.getY(), cameraNormal.getZ()).normalize();
        } else if (!cameraTransform.lockY()) {
            normal = new Vector(baseNormal.getX(), cameraNormal.getY(), baseNormal.getZ()).normalize();
        }

        Vector right = new Vector(normal.getZ(), 0, -normal.getX());
        if (right.lengthSquared() < 0.0001) right = new Vector(1, 0, 0);
        right.normalize();
        Vector up = normal.clone().crossProduct(right).normalize();

        return new PlaneBasis(normal, right, up);
    }

    private record PlaneBasis(Vector normal, Vector right, Vector up) {}
}
