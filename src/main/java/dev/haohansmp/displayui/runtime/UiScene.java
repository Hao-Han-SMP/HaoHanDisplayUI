package dev.haohansmp.displayui.runtime;

import dev.haohansmp.displayui.HaoHanDisplayUIPlugin;
import dev.haohansmp.displayui.api.BlockNode;
import dev.haohansmp.displayui.api.AlignedTextNode;
import dev.haohansmp.displayui.api.ItemNode;
import dev.haohansmp.displayui.api.UiIconNode;
import dev.haohansmp.displayui.api.TextNode;
import dev.haohansmp.displayui.api.UiAudience;
import dev.haohansmp.displayui.api.UiButton;
import dev.haohansmp.displayui.api.UiButtonAction;
import dev.haohansmp.displayui.api.UiCameraTransform;
import dev.haohansmp.displayui.api.UiClick;
import dev.haohansmp.displayui.api.UiClickHandler;
import dev.haohansmp.displayui.api.UiDocument;
import dev.haohansmp.displayui.api.UiHandle;
import dev.haohansmp.displayui.api.UiNode;
import dev.haohansmp.displayui.api.UiOptions;
import dev.haohansmp.displayui.api.event.UiButtonClickEvent;
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
    /** Display for each document node, kept in document order for in-place updates. */
    private final List<Display> nodeEntities = new ArrayList<>();
    private final Set<UUID> forcedVisible = new HashSet<>();
    private final Set<UUID> forcedHidden = new HashSet<>();
    /** Players to whom the current entity generation has already been shown. */
    private final Set<UUID> visibleViewers = new HashSet<>();
    private final List<UiClickHandler> clickHandlers = new CopyOnWriteArrayList<>();

    private Location origin;
    private UiDocument document;
    private UiAudience audience;
    private UiCameraTransform cameraTransform;
    private boolean removed;
    private Interaction interactionEntity;

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
        if (canUpdateTextInPlace(this.document, next)) {
            applyTextUpdate(this.document, next);
            this.document = next;
            return;
        }
        this.document = next;
        respawn();
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
    public void onClick(UiClickHandler handler) {
        ensureValid();
        clickHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    @Override
    public void clearClickHandlers() {
        clickHandlers.clear();
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
        boolean missingInteraction = !document.buttons().isEmpty()
                && (interactionEntity == null || !interactionEntity.isValid());
        if (entities.isEmpty() || entities.stream().anyMatch(entity -> !entity.isValid())
                || missingInteraction) {
            clearEntities();
            spawnIfLoaded();
        }
        syncViewers();
    }

    UiHit hit(Player player) {
        if (removed || document.buttons().isEmpty() || !shouldShow(player)) return null;

        PlaneBasis basis = planeBasis(player);
        UiRaycaster.Projection projection = UiRaycaster.project(
                player.getEyeLocation().toVector(),
                player.getEyeLocation().getDirection(),
                origin.toVector(), basis.normal(), basis.right(), basis.up(),
                options.pixelsPerBlock(), options.maxDistance());
        if (projection == null) return null;

        return document.buttons().stream()
                .filter(button -> button.contains(projection.localX(), projection.localY()))
                .findFirst()
                .map(button -> new UiHit(this, button, player,
                        projection.localX(), projection.localY(), projection.distance()))
                .orElse(null);
    }

    void activate(UiHit hit) {
        UiButtonClickEvent event = new UiButtonClickEvent(
                this, hit.button(), hit.player(), hit.localX(), hit.localY(), hit.distance());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

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
            float pixels = options.pixelsPerBlock();
            Quaternionf rotation = localRotation();
            Vector3f translation = new Vector3f(node.x() / pixels,
                    -(node.y() + node.height()) / pixels,
                    node.depth() - node.thickness() / pixels);
            rotation.transform(translation);
            display.setTransformation(new Transformation(
                    translation,
                    rotation,
                    new Vector3f(node.width() / pixels,
                            node.height() / pixels, node.thickness() / pixels),
                    new Quaternionf()));
        });
    }

    private void spawnInteraction() {
        if (document.buttons().isEmpty()) return;
        float minX = document.buttons().stream().map(button -> button.x() - button.hitSlop())
                .min(Float::compare).orElse(0.0f);
        float maxX = document.buttons().stream()
                .map(button -> button.x() + button.width() + button.hitSlop())
                .max(Float::compare).orElse(0.0f);
        float minY = document.buttons().stream().map(button -> button.y() - button.hitSlop())
                .min(Float::compare).orElse(0.0f);
        float maxY = document.buttons().stream()
                .map(button -> button.y() + button.height() + button.hitSlop())
                .max(Float::compare).orElse(0.0f);
        float pixels = options.pixelsPerBlock();

        Vector normal = origin.getDirection().setY(0.0);
        if (normal.lengthSquared() < 0.0001) normal.setZ(1.0);
        normal.normalize();
        Vector right = new Vector(normal.getZ(), 0.0, -normal.getX());
        double centerX = (minX + maxX) * 0.5 / pixels;
        Location hitboxLocation = origin.clone()
                .add(right.multiply(centerX))
                .add(0.0, -maxY / pixels, 0.0);

        interactionEntity = origin.getWorld().spawn(hitboxLocation, Interaction.class, interaction -> {
            interaction.setInteractionWidth(Math.max(0.2f, (maxX - minX) / pixels));
            interaction.setInteractionHeight(Math.max(0.2f, (maxY - minY) / pixels));
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
        Quaternionf rotation = localRotation();
        Vector3f translation = new Vector3f(node.x() / options.pixelsPerBlock(),
                -node.y() / options.pixelsPerBlock(), node.depth());
        rotation.transform(translation);
        return new Transformation(
                translation, rotation, new Vector3f(sx, sy, sz), new Quaternionf());
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
            dev.haohansmp.displayui.api.UiTextAlignment alignment) {
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
        if (visible == alreadyVisible) return;
        for (Display display : entities) {
            if (visible) player.showEntity(plugin, display);
            else player.hideEntity(plugin, display);
        }
        if (visible) visibleViewers.add(player.getUniqueId());
        else visibleViewers.remove(player.getUniqueId());
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

    /**
     * Text animation should not rebuild the whole scene. If node geometry,
     * styles and buttons are unchanged, only Adventure components are patched.
     */
    private boolean canUpdateTextInPlace(UiDocument current, UiDocument next) {
        if (!current.buttons().equals(next.buttons())
                || current.nodes().size() != next.nodes().size()
                || nodeEntities.size() != current.nodes().size()) {
            return false;
        }
        for (int i = 0; i < current.nodes().size(); i++) {
            Display display = nodeEntities.get(i);
            if (display == null || !display.isValid()
                    || !sameLayout(current.nodes().get(i), next.nodes().get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean sameLayout(UiNode current, UiNode next) {
        if (current instanceof AlignedTextNode a && next instanceof AlignedTextNode b) {
            return a.boxX() == b.boxX() && a.boxY() == b.boxY()
                    && a.width() == b.width() && a.height() == b.height()
                    && a.depth() == b.depth() && a.alignment() == b.alignment()
                    && a.leftOffset() == b.leftOffset() && a.rightOffset() == b.rightOffset()
                    && a.fontSize() == b.fontSize() && a.contentWidth() == b.contentWidth()
                    && a.verticalAlignment() == b.verticalAlignment()
                    && a.verticalOffset() == b.verticalOffset()
                    && a.shadow() == b.shadow() && a.seeThrough() == b.seeThrough();
        }
        if (current instanceof TextNode a && next instanceof TextNode b) {
            return a.x() == b.x() && a.y() == b.y() && a.depth() == b.depth()
                    && a.lineWidth() == b.lineWidth() && a.scale() == b.scale()
                    && a.alignment() == b.alignment() && a.shadow() == b.shadow()
                    && a.seeThrough() == b.seeThrough();
        }
        return current.equals(next);
    }

    private void applyTextUpdate(UiDocument current, UiDocument next) {
        for (int i = 0; i < next.nodes().size(); i++) {
            UiNode oldNode = current.nodes().get(i);
            UiNode newNode = next.nodes().get(i);
            if (oldNode instanceof AlignedTextNode oldText
                    && newNode instanceof AlignedTextNode text
                    && !oldText.text().equals(text.text())) {
                ((TextDisplay) nodeEntities.get(i)).text(text.text());
            } else if (oldNode instanceof TextNode oldText
                    && newNode instanceof TextNode text
                    && !oldText.text().equals(text.text())) {
                ((TextDisplay) nodeEntities.get(i)).text(text.text());
            }
        }
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
