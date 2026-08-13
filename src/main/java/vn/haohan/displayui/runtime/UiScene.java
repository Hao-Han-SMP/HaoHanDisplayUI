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

import vn.haohan.displayui.HaoHanDisplayUIPlugin;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.UiOptions;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiButtonAction;
import vn.haohan.displayui.api.interaction.UiCheckbox;
import vn.haohan.displayui.api.interaction.UiClick;
import vn.haohan.displayui.api.interaction.UiClickHandler;
import vn.haohan.displayui.api.interaction.UiControl;
import vn.haohan.displayui.api.interaction.UiControlChange;
import vn.haohan.displayui.api.interaction.UiControlChangeHandler;
import vn.haohan.displayui.api.interaction.UiSlider;
import vn.haohan.displayui.api.interaction.event.UiButtonClickEvent;
import vn.haohan.displayui.api.interaction.event.UiControlChangeEvent;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.ItemNode;
import vn.haohan.displayui.api.node.TextNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.view.UiAudience;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

final class UiScene implements UiHandle {
    private final HaoHanDisplayUIPlugin plugin;
    private final UUID id;
    private final String ownerKey;
    private final UiOptions options;
    private final Consumer<UUID> onRemove;
    private final NamespacedKey sceneKey;
    private final NamespacedKey ownerDataKey;
    private final List<Display> entities = new ArrayList<>();
    private final List<Display> nodeEntities = new ArrayList<>();
    private final Set<UUID> forcedVisible = new HashSet<>();
    private final Set<UUID> forcedHidden = new HashSet<>();
    private final Set<UUID> visibleViewers = new HashSet<>();
    private final List<UiClickHandler> clickHandlers = new CopyOnWriteArrayList<>();
    private final List<UiControlChangeHandler> controlChangeHandlers = new CopyOnWriteArrayList<>();
    private final Map<String, UiControl> controlStates = new LinkedHashMap<>();

    private Location origin;
    private UiDocument document;
    private UiAudience audience;
    private UiCameraTransform cameraTransform;
    private boolean removed;
    private Interaction interactionEntity;
    private UiAnimation animation;
    private int animationAge;
    private List<UiAnimation> nodeAnimations = List.of();
    private int[] nodeAnimationAges = new int[0];

    UiScene(HaoHanDisplayUIPlugin plugin, UUID id, String ownerKey, Location origin,
            UiDocument document, UiOptions options, UiAudience audience,
            Consumer<UUID> onRemove) {
        this.plugin = plugin;
        this.id = id;
        this.ownerKey = ownerKey;
        this.origin = origin;
        this.document = document;
        this.options = options;
        this.cameraTransform = options.cameraTransform();
        this.audience = audience;
        this.onRemove = onRemove;
        document.controls().forEach(control -> controlStates.put(control.id(), control));
        this.sceneKey = new NamespacedKey(plugin, "scene_id");
        this.ownerDataKey = new NamespacedKey(plugin, "owner_key");
    }

    @Override public UUID id() { return id; }
    @Override public String ownerKey() { return ownerKey; }

    @Override
    public boolean isValid() {
        return !removed;
    }

    @Override
    public void update(UiDocument document) {
        ensureValid();
        UiDocument next = Objects.requireNonNull(document, "document");
        if (animation != null || !nodeAnimations.isEmpty()) stopAnimation();
        nodeAnimations = List.of();
        nodeAnimationAges = new int[0];
        UiDocument previous = this.document;
        updateControlStates(next);
        this.document = next;
        if (!incrementalUpdate(previous, next)) respawn();
        else syncViewers();
    }

    @Override
    public void move(Location origin) {
        ensureValid();
        Objects.requireNonNull(origin, "origin");
        if (origin.getWorld() == null) throw new IllegalArgumentException("origin must have a world");
        this.origin = origin.clone();
        respawn();
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
        respawn();
    }

    @Override
    public void animate(UiAnimation animation) {
        ensureValid();
        nodeAnimations = List.of();
        nodeAnimationAges = new int[0];
        this.animation = Objects.requireNonNull(animation, "animation");
        this.animationAge = -animation.delayTicks();
        applyAnimation(animationProgress());
    }

    @Override
    public void animateNodes(List<UiAnimation> animations) {
        ensureValid();
        Objects.requireNonNull(animations, "animations");
        if (animations.size() != document.nodes().size()) {
            throw new IllegalArgumentException("animations must contain one entry per document node");
        }
        nodeAnimations = List.copyOf(animations);
        nodeAnimationAges = new int[animations.size()];
        for (int i = 0; i < nodeAnimationAges.length; i++) {
            nodeAnimationAges[i] = -animations.get(i).delayTicks();
        }
        animation = null;
        applyNodeAnimations();
    }

    @Override
    public void stopAnimation() {
        if (removed || (animation == null && nodeAnimations.isEmpty())) return;
        if (animation != null) applyAnimation(1.0);
        if (!nodeAnimations.isEmpty()) {
            for (int i = 0; i < nodeAnimations.size(); i++) {
                applyAnimationToNode(i, nodeAnimations.get(i), 1.0);
            }
        }
        animation = null;
        nodeAnimations = List.of();
        nodeAnimationAges = new int[0];
    }

    @Override
    public boolean isAnimating() {
        return !removed && (animation != null || !nodeAnimations.isEmpty());
    }

    @Override
    public Optional<UiControl> control(String id) {
        ensureValid();
        return Optional.ofNullable(controlStates.get(Objects.requireNonNull(id, "id")));
    }

    @Override
    public void onClick(UiClickHandler handler) {
        ensureValid();
        clickHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    @Override
    public void clearClickHandlers() {
        clickHandlers.clear();
    }

    @Override
    public void onControlChange(UiControlChangeHandler handler) {
        ensureValid();
        controlChangeHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    @Override
    public void clearControlChangeHandlers() {
        controlChangeHandlers.clear();
    }

    @Override
    public void show(Player player) {
        ensureValid();
        forcedHidden.remove(player.getUniqueId());
        forcedVisible.add(player.getUniqueId());
        syncPlayer(player);
    }

    @Override
    public void hide(Player player) {
        ensureValid();
        forcedVisible.remove(player.getUniqueId());
        forcedHidden.add(player.getUniqueId());
        hideEntities(player);
    }

    @Override
    public void remove() {
        if (removed) return;
        removed = true;
        clearEntities();
        onRemove.accept(id);
    }

    void tick() {
        if (removed) return;
        boolean missingInteraction = (!document.buttons().isEmpty() || !controlStates.isEmpty())
                && (interactionEntity == null || !interactionEntity.isValid());
        if (entities.isEmpty() || entities.stream().anyMatch(entity -> !entity.isValid())
                || missingInteraction) {
            clearEntities();
            spawnIfLoaded();
        }
        tickAnimation();
        syncViewers();
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

        UiHit buttonHit = document.buttons().stream()
                .filter(button -> button.contains(projection.localX(), projection.localY()))
                .findFirst()
                .map(button -> new UiHit(this, button, null, player,
                        projection.localX(), projection.localY(), projection.distance()))
                .orElse(null);

        if (buttonHit != null) return buttonHit;
        return controlStates.values().stream()
                .filter(control -> control.contains(projection.localX(), projection.localY()))
                .findFirst()
                .map(control -> new UiHit(this, null, control, player,
                        projection.localX(), projection.localY(), projection.distance()))
                .orElse(null);
    }

    void activate(UiHit hit) {
        if (hit.control() != null) {
            activateControl(hit);
            return;
        }
        UiButtonClickEvent event = new UiButtonClickEvent(
                this, hit.button(), hit.player(), hit.localX(), hit.localY(), hit.distance());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        playClickSound(hit.player());
        executeAction(hit.button(), hit.player());

        UiClick click = new UiClick(this, hit.button(), hit.player(),
                hit.localX(), hit.localY(), hit.distance());
        for (UiClickHandler handler : clickHandlers) {
            try {
                handler.onClick(click);
            } catch (RuntimeException exception) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE,
                        "UI click handler failed for " + ownerKey + "/" + hit.button().id(), exception);
            }
        }
    }

    /** Updates a slider while its owning player keeps the drag gesture active. */
    boolean dragSlider(Player player, String controlId) {
        if (removed) return false;
        UiControl state = controlStates.get(controlId);
        if (!(state instanceof UiSlider slider) || !shouldShow(player)) return false;

        PlaneBasis basis = planeBasis(player);
        UiRaycaster.Projection projection = UiRaycaster.project(
                player.getEyeLocation().toVector(),
                player.getEyeLocation().getDirection(),
                origin.toVector(), basis.normal(), basis.right(), basis.up(),
                options.pixelsPerBlock(), options.maxDistance());
        if (projection == null) return false;

        UiHit dragHit = new UiHit(this, null, slider, player,
                projection.localX(), projection.localY(), projection.distance());
        return changeControl(dragHit, slider.valueAt(projection.localX()), false);
    }

    private void activateControl(UiHit hit) {
        UiControl control = hit.control();
        double nextValue = control instanceof UiSlider slider
                ? slider.valueAt(hit.localX())
                : ((UiCheckbox) control).checked() ? 0.0 : 1.0;
        changeControl(hit, nextValue, true);
    }

    private boolean changeControl(UiHit hit, double nextValue, boolean playSound) {
        UiControl control = hit.control();
        double oldValue = controlValue(control);
        if (Double.compare(oldValue, nextValue) == 0) return false;

        UiControlChangeEvent event = new UiControlChangeEvent(this, control, hit.player(),
                oldValue, nextValue, hit.localX(), hit.localY(), hit.distance());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        UiControl nextControl = control instanceof UiSlider slider
                ? slider.withValue(nextValue)
                : ((UiCheckbox) control).checked(nextValue >= 0.5);
        controlStates.put(control.id(), nextControl);
        if (playSound) playClickSound(hit.player());
        UiControlChange change = new UiControlChange(this, nextControl, hit.player(),
                oldValue, controlValue(nextControl), hit.localX(), hit.localY(), hit.distance());
        for (UiControlChangeHandler handler : controlChangeHandlers) {
            try {
                handler.onChange(change);
            } catch (RuntimeException exception) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE,
                        "UI control handler failed for " + ownerKey + "/" + control.id(), exception);
            }
        }
        return true;
    }

    private double controlValue(UiControl control) {
        if (control instanceof UiSlider slider) return slider.value();
        if (control instanceof UiCheckbox checkbox) return checkbox.checked() ? 1.0 : 0.0;
        throw new IllegalArgumentException("Unsupported UI control: " + control.getClass().getName());
    }

    private void playClickSound(Player player) {
        if (options.clickSound() == null || options.clickSoundVolume() <= 0.0f) return;
        player.playSound(player.getLocation(), options.clickSound(),
                options.clickSoundVolume(), options.clickSoundPitch());
    }

    private void respawn() {
        clearEntities();
        spawnIfLoaded();
        syncViewers();
    }

    private void spawnIfLoaded() {
        World world = origin.getWorld();
        if (world == null || !world.isChunkLoaded(origin.getBlockX() >> 4, origin.getBlockZ() >> 4)) return;
        nodeEntities.clear();
        for (int i = 0; i < document.nodes().size(); i++) nodeEntities.add(null);
        java.util.stream.IntStream.range(0, document.nodes().size())
                .boxed()
                .sorted(java.util.Comparator.comparingDouble(
                        index -> document.nodes().get(index).depth()))
                .forEach(index -> {
                    Display display = spawnNode(document.nodes().get(index));
                    entities.add(display);
                    nodeEntities.set(index, display);
                });
        spawnInteraction();
        applyAnimation(animationProgress());
        applyNodeAnimations();
    }

    /** Updates only nodes whose type/content changed; stable entities survive page updates. */
    private boolean incrementalUpdate(UiDocument previous, UiDocument next) {
        World world = origin.getWorld();
        if (world == null || !world.isChunkLoaded(origin.getBlockX() >> 4, origin.getBlockZ() >> 4)) {
            return false;
        }
        int common = Math.min(previous.nodes().size(), next.nodes().size());
        boolean entitySetChanged = previous.nodes().size() != next.nodes().size();
        for (int i = 0; i < common; i++) {
            UiNode oldNode = previous.nodes().get(i);
            UiNode newNode = next.nodes().get(i);
            Display current = i < nodeEntities.size() ? nodeEntities.get(i) : null;
            if (current == null || !current.isValid()) return false;
            if (oldNode.getClass() != newNode.getClass()) {
                replaceNode(i, newNode);
                entitySetChanged = true;
            } else if (!oldNode.equals(newNode)) {
                updateNode(current, newNode);
            }
        }
        for (int i = common; i < next.nodes().size(); i++) {
            Display display = spawnNode(next.nodes().get(i));
            nodeEntities.add(display);
            entities.add(display);
        }
        while (nodeEntities.size() > next.nodes().size()) {
            int last = nodeEntities.size() - 1;
            Display display = nodeEntities.remove(last);
            if (display != null && display.isValid()) display.remove();
            entities.remove(display);
        }

        if (!sameInteractionLayout(previous, next)) {
            if (interactionEntity != null && interactionEntity.isValid()) interactionEntity.remove();
            interactionEntity = null;
            spawnInteraction();
        }
        // Newly spawned/replaced entities need a fresh showEntity packet even
        // when the player was already visible for the previous document.
        if (entitySetChanged) visibleViewers.clear();
        return true;
    }

    private void replaceNode(int index, UiNode node) {
        Display previous = nodeEntities.get(index);
        if (previous != null && previous.isValid()) previous.remove();
        entities.remove(previous);
        Display replacement = spawnNode(node);
        nodeEntities.set(index, replacement);
        entities.add(replacement);
    }

    private void updateControlStates(UiDocument next) {
        Map<String, UiControl> previous = new LinkedHashMap<>(controlStates);
        controlStates.clear();
        next.controls().forEach(control -> {
            UiControl old = previous.get(control.id());
            if (old instanceof UiSlider oldSlider && control instanceof UiSlider slider
                    && sameControlLayout(oldSlider, slider)) {
                controlStates.put(control.id(), slider.withValue(oldSlider.value()));
            } else if (old instanceof UiCheckbox oldCheckbox && control instanceof UiCheckbox checkbox
                    && sameControlLayout(oldCheckbox, checkbox)) {
                controlStates.put(control.id(), checkbox.checked(oldCheckbox.checked()));
            } else {
                controlStates.put(control.id(), control);
            }
        });
    }

    private Display spawnNode(UiNode node) {
        if (node instanceof TextNode text) return spawnText(text);
        if (node instanceof AlignedTextNode text) return spawnAlignedText(text);
        if (node instanceof ItemNode item) return spawnItem(item);
        if (node instanceof UiIconNode icon) return spawnIcon(icon);
        if (node instanceof BlockNode block) return spawnBlock(block);
        throw new IllegalArgumentException("Unsupported UI node: " + node.getClass().getName());
    }

    private TextDisplay spawnText(TextNode node) {
        return origin.getWorld().spawn(origin, TextDisplay.class, display -> {
            configure(display, node);
            display.text(node.text());
            display.setAlignment(screenAlignment(node.alignment()));
            display.setLineWidth(node.lineWidth());
            display.setShadowed(node.shadow());
            display.setSeeThrough(node.seeThrough());
            display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            display.setTextOpacity((byte) 255);
            display.setTransformation(transform(node, node.scale(), node.scale(), node.scale()));
        });
    }

    private TextDisplay spawnAlignedText(AlignedTextNode node) {
        return origin.getWorld().spawn(origin, TextDisplay.class, display -> {
            configure(display, node);
            display.text(node.text());
            // node.x() is already the measured visual center. Native LEFT/RIGHT
            // would add another half-width shift, so always anchor at CENTER.
            display.setAlignment(TextDisplay.TextAlignment.CENTER);
            display.setLineWidth(Math.max(1,
                    Math.round(node.width() * 20.0f / node.fontSize())));
            display.setShadowed(node.shadow());
            display.setSeeThrough(node.seeThrough());
            display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            display.setTextOpacity((byte) 255);
            float displayScale = node.fontSize() / 20.0f;
            display.setTransformation(transform(
                    node, displayScale, displayScale, displayScale));
        });
    }

    private ItemDisplay spawnItem(ItemNode node) {
        return origin.getWorld().spawn(origin, ItemDisplay.class, display -> {
            configure(display, node);
            display.setItemStack(node.item());
            display.setItemDisplayTransform(node.transform());
            display.setTransformation(transform(node, node.scale(), node.scale(), node.scale()));
        });
    }

    private ItemDisplay spawnIcon(UiIconNode node) {
        return origin.getWorld().spawn(origin, ItemDisplay.class, display -> {
            configure(display, node);
            display.setItemStack(node.item());
            display.setItemDisplayTransform(node.transform());
            float pixels = options.pixelsPerBlock();
            display.setTransformation(transform(node,
                    node.width() / pixels,
                    node.height() / pixels,
                    Math.min(node.width(), node.height()) / pixels));
        });
    }

    private BlockDisplay spawnBlock(BlockNode node) {
        return origin.getWorld().spawn(origin, BlockDisplay.class, display -> {
            configure(display, node);
            display.setBlock(node.block());
            display.setTransformation(blockTransform(node, 1.0f, 0.0f, 0.0f, 0.0f));
        });
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
        float minY = document.buttons().stream().map(button -> button.y() - button.hitSlop())
                .min(Float::compare).orElse(Float.POSITIVE_INFINITY);
        minY = Math.min(minY, controls.stream().map(control -> control.y() - control.hitSlop())
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
        display.setInterpolationDuration(2);
        display.addScoreboardTag(options.scoreboardTag());
        display.addScoreboardTag("hhdui_scene");
        display.getPersistentDataContainer().set(sceneKey, PersistentDataType.STRING, id.toString());
        display.getPersistentDataContainer().set(ownerDataKey, PersistentDataType.STRING, ownerKey);
    }

    private Transformation transform(UiNode node, float sx, float sy, float sz) {
        return transform(node, sx, sy, sz, 0.0f, 0.0f, 0.0f);
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

    private Transformation blockTransform(BlockNode node, float scale,
                                           float offsetX, float offsetY, float offsetZ) {
        float pixels = options.pixelsPerBlock();
        Quaternionf rotation = localRotation();
        Vector3f translation = new Vector3f(
                (node.x() + offsetX) / pixels,
                -(node.y() + node.height() + offsetY) / pixels,
                node.depth() + offsetZ - node.thickness() / pixels);
        rotation.transform(translation);
        return new Transformation(
                translation, rotation,
                new Vector3f(node.width() / pixels * scale,
                        node.height() / pixels * scale,
                        node.thickness() / pixels * scale),
                new Quaternionf());
    }

    private void tickAnimation() {
        if (animation != null) {
            animationAge++;
            applyAnimation(animationProgress());
            if (animationAge >= animation.durationTicks()) {
                applyAnimation(1.0);
                animation = null;
            }
        }
        if (!nodeAnimations.isEmpty()) {
            boolean running = false;
            for (int i = 0; i < nodeAnimations.size(); i++) {
                UiAnimation current = nodeAnimations.get(i);
                nodeAnimationAges[i]++;
                applyAnimationToNode(i, current, nodeAnimationProgress(i));
                if (nodeAnimationAges[i] < current.durationTicks()) running = true;
                else applyAnimationToNode(i, current, 1.0);
            }
            if (!running) {
                nodeAnimations = List.of();
                nodeAnimationAges = new int[0];
            }
        }
    }

    private double animationProgress() {
        if (animation == null || animationAge <= 0) return 0.0;
        return Math.min(1.0, animationAge / (double) animation.durationTicks());
    }

    private void applyAnimation(double progress) {
        if (animation == null || nodeEntities.isEmpty()) return;
        for (int i = 0; i < nodeEntities.size(); i++) {
            applyAnimationToNode(i, animation, progress);
        }
    }

    private void applyNodeAnimations() {
        if (nodeAnimations.isEmpty()) return;
        for (int i = 0; i < nodeAnimations.size(); i++) {
            applyAnimationToNode(i, nodeAnimations.get(i), nodeAnimationProgress(i));
        }
    }

    private double nodeAnimationProgress(int index) {
        UiAnimation current = nodeAnimations.get(index);
        int age = nodeAnimationAges[index];
        if (age <= 0) return 0.0;
        return Math.min(1.0, age / (double) current.durationTicks());
    }

    private void applyAnimationToNode(int index, UiAnimation current, double progress) {
        if (index >= nodeEntities.size()) return;
        Display display = nodeEntities.get(index);
        if (display == null || !display.isValid()) return;
        double eased = current.easing().apply(progress);
        float scale = interpolate(current.fromScale(), current.toScale(), eased);
        float opacity = Math.max(0.0f, Math.min(1.0f,
                interpolate(current.fromOpacity(), current.toOpacity(), eased)));
        float offsetX = current.offsetX() * (1.0f - (float) eased);
        float offsetY = current.offsetY() * (1.0f - (float) eased);
        float offsetZ = current.offsetZ() * (1.0f - (float) eased);
        UiNode node = document.nodes().get(index);
        if (node instanceof BlockNode block) {
            display.setTransformation(blockTransform(block, scale, offsetX, offsetY, offsetZ));
        } else if (node instanceof AlignedTextNode text) {
            float displayScale = text.fontSize() / 20.0f * scale;
            display.setTransformation(transform(text, displayScale, displayScale,
                    displayScale, offsetX, offsetY, offsetZ));
        } else if (node instanceof TextNode text) {
            float displayScale = text.scale() * scale;
            display.setTransformation(transform(text, displayScale, displayScale,
                    displayScale, offsetX, offsetY, offsetZ));
        } else if (node instanceof ItemNode item) {
            float displayScale = item.scale() * scale;
            display.setTransformation(transform(item, displayScale, displayScale,
                    displayScale, offsetX, offsetY, offsetZ));
        } else if (node instanceof UiIconNode icon) {
            float pixels = options.pixelsPerBlock();
            display.setTransformation(transform(icon, icon.width() / pixels * scale,
                    icon.height() / pixels * scale,
                    Math.min(icon.width(), icon.height()) / pixels * scale,
                    offsetX, offsetY, offsetZ));
        }
        if (display instanceof TextDisplay textDisplay) {
            textDisplay.setTextOpacity((byte) Math.round(opacity * 255.0f));
        }
    }

    private float interpolate(float from, float to, double progress) {
        return (float) (from + (to - from) * progress);
    }

    private TextDisplay.TextAlignment screenAlignment(TextDisplay.TextAlignment alignment) {
        // A fixed display facing its audience has a mirrored local X axis. Swap
        // the native side anchors so API LEFT/RIGHT match the player's screen.
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

    private void syncViewers() {
        World world = origin.getWorld();
        if (world == null || entities.isEmpty()) return;
        // A player who disconnected or changed world must be shown again when
        // they later re-enter this scene's world.
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
    }

    private void syncItemBackfaces(Player player) {
        if (cameraTransform.billboard() != Display.Billboard.FIXED) return;
        boolean frontFacing = isFrontFacing(player);
        for (int i = 0; i < nodeEntities.size(); i++) {
            UiNode node = document.nodes().get(i);
            if (!(node instanceof ItemNode) && !(node instanceof UiIconNode)) continue;
            Display display = nodeEntities.get(i);
            if (display == null || !display.isValid()) continue;
            if (frontFacing) player.showEntity(plugin, display);
            else player.hideEntity(plugin, display);
        }
    }

    private boolean isFrontFacing(Player player) {
        Vector normal = planeBasis(player).normal();
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
        if (!options.requireFront()) return true;
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
        if (interactionEntity != null && interactionEntity.isValid()) interactionEntity.remove();
        interactionEntity = null;
    }

    private void ensureValid() {
        if (removed) throw new IllegalStateException("UI scene has been removed");
    }

    private void updateNode(Display display, UiNode node) {
        if (node instanceof TextNode text) {
                TextDisplay textDisplay = (TextDisplay) display;
                textDisplay.text(text.text());
                textDisplay.setAlignment(screenAlignment(text.alignment()));
                textDisplay.setLineWidth(text.lineWidth());
                textDisplay.setShadowed(text.shadow());
                textDisplay.setSeeThrough(text.seeThrough());
                textDisplay.setTransformation(transform(text, text.scale(), text.scale(), text.scale()));
            } else if (node instanceof AlignedTextNode text) {
                TextDisplay textDisplay = (TextDisplay) display;
                textDisplay.text(text.text());
                textDisplay.setLineWidth(Math.max(1,
                        Math.round(text.width() * 20.0f / text.fontSize())));
                textDisplay.setShadowed(text.shadow());
                textDisplay.setSeeThrough(text.seeThrough());
                float scale = text.fontSize() / 20.0f;
                textDisplay.setTransformation(transform(text, scale, scale, scale));
            } else if (node instanceof ItemNode item) {
                ItemDisplay itemDisplay = (ItemDisplay) display;
                itemDisplay.setItemStack(item.item());
                itemDisplay.setItemDisplayTransform(item.transform());
                itemDisplay.setTransformation(transform(item, item.scale(), item.scale(), item.scale()));
            } else if (node instanceof UiIconNode icon) {
                ItemDisplay itemDisplay = (ItemDisplay) display;
                itemDisplay.setItemStack(icon.item());
                itemDisplay.setItemDisplayTransform(icon.transform());
                float pixels = options.pixelsPerBlock();
                itemDisplay.setTransformation(transform(icon, icon.width() / pixels,
                        icon.height() / pixels, Math.min(icon.width(), icon.height()) / pixels));
            } else if (node instanceof BlockNode block) {
                BlockDisplay blockDisplay = (BlockDisplay) display;
                blockDisplay.setBlock(block.block());
                blockDisplay.setTransformation(blockTransform(block, 1.0f, 0.0f, 0.0f, 0.0f));
        }
    }

    private boolean sameInteractionLayout(UiDocument current, UiDocument next) {
        if (current.buttons().size() != next.buttons().size()
                || current.controls().size() != next.controls().size()) return false;
        for (int i = 0; i < current.buttons().size(); i++) {
            UiButton a = current.buttons().get(i);
            UiButton b = next.buttons().get(i);
            if (!a.id().equals(b.id()) || a.x() != b.x() || a.y() != b.y()
                    || a.width() != b.width() || a.height() != b.height()
                    || a.hitSlop() != b.hitSlop()) return false;
        }
        return sameControlLayouts(current.controls(), next.controls());
    }

    private boolean sameControlLayout(UiControl current, UiControl next) {
        return current.id().equals(next.id()) && current.getClass() == next.getClass()
                && current.x() == next.x() && current.y() == next.y()
                && current.width() == next.width() && current.height() == next.height()
                && current.hitSlop() == next.hitSlop()
                && (!(current instanceof UiSlider left) || !(next instanceof UiSlider right)
                || (left.minimum() == right.minimum()
                && left.maximum() == right.maximum() && left.step() == right.step()));
    }

    private boolean sameControlLayouts(List<UiControl> current, List<UiControl> next) {
        if (current.size() != next.size()) return false;
        for (int i = 0; i < current.size(); i++) {
            if (!sameControlLayout(current.get(i), next.get(i))) return false;
        }
        return true;
    }

    private Quaternionf localRotation() {
        float radians = (float) (Math.PI / 180.0);
        return new Quaternionf().rotateXYZ(
                cameraTransform.angleX() * radians,
                cameraTransform.angleY() * radians,
                cameraTransform.angleZ() * radians);
    }

    private PlaneBasis planeBasis(Player player) {
        Vector baseNormal = origin.getDirection();
        if (baseNormal.lengthSquared() < 0.0001) baseNormal.setZ(1);
        baseNormal.normalize();

        // Client billboards use the camera quaternion, not the vector from the
        // display origin to the eye. This distinction is largest near panel edges.
        Vector cameraNormal = player.getEyeLocation().getDirection().multiply(-1);
        if (cameraNormal.lengthSquared() < 0.0001) cameraNormal = baseNormal.clone();
        cameraNormal.normalize();

        Vector normal;
        if (cameraTransform.lockX() && cameraTransform.lockY()) {
            normal = baseNormal;
        } else if (cameraTransform.lockX()) {
            normal = cameraNormal.clone().setY(0);
            if (normal.lengthSquared() < 0.0001) normal = baseNormal.clone().setY(0);
            normal.normalize();
        } else if (cameraTransform.lockY()) {
            Vector horizontal = baseNormal.clone().setY(0);
            if (horizontal.lengthSquared() < 0.0001) horizontal.setZ(1);
            horizontal.normalize();
            double vertical = Math.max(-0.9999, Math.min(0.9999, cameraNormal.getY()));
            normal = horizontal.multiply(Math.sqrt(1.0 - vertical * vertical))
                    .setY(vertical).normalize();
        } else {
            normal = cameraNormal;
        }

        Vector right = new Vector(normal.getZ(), 0, -normal.getX());
        if (right.lengthSquared() < 0.0001) right.setX(1);
        right.normalize();
        Vector up = normal.clone().crossProduct(right).normalize();

        double radians = Math.PI / 180.0;
        if (cameraTransform.angleX() != 0) {
            Vector axis = right.clone();
            up.rotateAroundAxis(axis, cameraTransform.angleX() * radians);
            normal.rotateAroundAxis(axis, cameraTransform.angleX() * radians);
        }
        if (cameraTransform.angleY() != 0) {
            Vector axis = up.clone();
            right.rotateAroundAxis(axis, cameraTransform.angleY() * radians);
            normal.rotateAroundAxis(axis, cameraTransform.angleY() * radians);
        }
        if (cameraTransform.angleZ() != 0) {
            Vector axis = normal.clone();
            right.rotateAroundAxis(axis, cameraTransform.angleZ() * radians);
            up.rotateAroundAxis(axis, cameraTransform.angleZ() * radians);
        }
        return new PlaneBasis(normal.normalize(), right.normalize(), up.normalize());
    }

    private void executeAction(UiButton button, Player player) {
        UiButtonAction action = button.action();
        String value = action.value().replace("{player}", player.getName());
        switch (action.type()) {
            case NONE -> { }
            case RUN_PLAYER_COMMAND -> Bukkit.dispatchCommand(player, value);
            case RUN_CONSOLE_COMMAND -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value);
            case OPEN_URL -> player.sendMessage(
                    Component.text("Link: ", NamedTextColor.GRAY)
                            .append(action.label().clickEvent(ClickEvent.openUrl(value))));
            case SUGGEST_COMMAND -> player.sendMessage(
                    action.label().clickEvent(ClickEvent.suggestCommand(value)));
        }
    }

    private record PlaneBasis(Vector normal, Vector right, Vector up) {}
}
