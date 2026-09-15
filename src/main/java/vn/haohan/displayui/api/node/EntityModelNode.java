package vn.haohan.displayui.api.node;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A UI node that renders any 3D Item or Custom Entity Model (via an ItemDisplay entity)
 * on the canvas with rich interactive rotation capabilities (auto-spin, hover-spin,
 * 3D cursor-tracking tilt, pitch/yaw locks, angle snapping).
 * <p>
 * Supports both vanilla item models (swords, bows, blocks) and Custom Model Data (CMD)
 * entity models via resource packs (zombies, cows, dragons, etc.).
 *
 * @param item        the {@link ItemStack} to render (e.g. {@code Material.PAPER} with CMD for mob model)
 * @param x           horizontal center coordinate in UI canvas pixels
 * @param y           vertical center coordinate in UI canvas pixels
 * @param depth       Z-depth layer offset in canvas units (default 0.08f)
 * @param scaleX      X-axis scaling factor
 * @param scaleY      Y-axis scaling factor
 * @param scaleZ      Z-axis scaling factor
 * @param width       hitbox collision width for interaction raycasting (pixels)
 * @param height      hitbox collision height for interaction raycasting (pixels)
 * @param yaw         base Yaw rotation angle in degrees
 * @param pitch       base Pitch rotation angle in degrees
 * @param roll        base Roll rotation angle in degrees
 * @param transform   Minecraft {@link ItemDisplay.ItemDisplayTransform} display transform
 * @param visible     initial visibility flag
 * @param rotation    3D interactive rotation configuration
 * @param doubleSided whether back faces are rendered
 */
public record EntityModelNode(
        ItemStack item,
        float x,
        float y,
        float depth,
        float scaleX,
        float scaleY,
        float scaleZ,
        float width,
        float height,
        float yaw,
        float pitch,
        float roll,
        ItemDisplay.ItemDisplayTransform transform,
        boolean visible,
        UiModelRotation rotation,
        boolean doubleSided
) implements UiNode {

    private static final List<String> MOB_NAMES;

    static {
        List<String> loaded = new ArrayList<>();
        try (InputStream in = EntityModelNode.class.getResourceAsStream("/mob_registry.json")) {
            if (in != null) {
                List<String> list = new Gson().fromJson(
                        new InputStreamReader(in, StandardCharsets.UTF_8),
                        new TypeToken<List<String>>() {}.getType()
                );
                if (list != null) loaded.addAll(list);
            }
        } catch (Exception ignored) {}
        if (loaded.isEmpty()) {
            loaded.addAll(List.of(
                    "zombie", "skeleton", "creeper", "spider", "enderman",
                    "cow", "pig", "sheep", "chicken", "allay",
                    "bee", "iron_golem", "villager", "witch", "slime",
                    "blaze", "ghast", "warden", "wither", "wolf",
                    "cat", "fox", "horse", "donkey", "panda"
            ));
        }
        MOB_NAMES = Collections.unmodifiableList(loaded);
    }

    /**
     * Returns an unmodifiable list of registered mob identifiers supported out of the box.
     *
     * @return unmodifiable list containing mob IDs
     */
    public static List<String> getRegisteredMobNames() {
        return MOB_NAMES;
    }

    public EntityModelNode {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(transform, "transform");
        Objects.requireNonNull(rotation, "rotation");
        if (item.getType() == Material.AIR || item.getType().name().endsWith("AIR")) {
            throw new IllegalArgumentException("model item cannot be air");
        }
        if (scaleX <= 0.0f || scaleY <= 0.0f || scaleZ <= 0.0f) {
            throw new IllegalArgumentException("model scale components must be positive");
        }
        if (width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("model hitbox dimensions must be positive");
        }
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(depth)
                || !Float.isFinite(yaw) || !Float.isFinite(pitch) || !Float.isFinite(roll)) {
            throw new IllegalArgumentException("model coordinates and rotation angles must be finite");
        }
        item = item.clone();
    }

    /**
     * Constructs a full EntityModelNode with single-sided rendering enabled by default.
     */
    public EntityModelNode(ItemStack item, float x, float y, float depth,
                           float scaleX, float scaleY, float scaleZ,
                           float width, float height, float yaw, float pitch, float roll,
                           ItemDisplay.ItemDisplayTransform transform,
                           boolean visible, UiModelRotation rotation) {
        this(item, x, y, depth, scaleX, scaleY, scaleZ, width, height,
                yaw, pitch, roll, transform, visible, rotation, false);
    }

    /**
     * Constructs an EntityModelNode with uniform scaling and standard default bounds.
     *
     * @param item  the item stack to render
     * @param x     the center X coordinate
     * @param y     the center Y coordinate
     * @param scale uniform scaling factor
     */
    public EntityModelNode(ItemStack item, float x, float y, float scale) {
        this(item, x, y, 0.08f, scale, scale, scale, 32.0f, 32.0f,
                0.0f, 0.0f, 0.0f, ItemDisplay.ItemDisplayTransform.FIXED,
                true, UiModelRotation.defaults(), false);
    }

    /**
     * Constructs an EntityModelNode with specified hitbox dimensions and uniform scaling.
     *
     * @param item   the item stack to render
     * @param x      the center X coordinate
     * @param y      the center Y coordinate
     * @param width  hitbox width (pixels)
     * @param height hitbox height (pixels)
     * @param scale  uniform scaling factor
     */
    public EntityModelNode(ItemStack item, float x, float y, float width, float height, float scale) {
        this(item, x, y, 0.08f, scale, scale, scale, width, height,
                0.0f, 0.0f, 0.0f, ItemDisplay.ItemDisplayTransform.FIXED,
                true, UiModelRotation.defaults(), false);
    }

    /**
     * Creates an EntityModelNode from a registered Mob identifier (mapping to Custom Model Data).
     *
     * @param mobId mob identifier (e.g. "zombie", "warden", "allay")
     * @param x     center X coordinate
     * @param y     center Y coordinate
     * @param scale scaling factor
     * @return a new {@link EntityModelNode}
     */
    public static EntityModelNode forMob(String mobId, float x, float y, float scale) {
        return forMob(mobId, x, y, 32.0f, 32.0f, scale);
    }

    /**
     * Creates an EntityModelNode from a registered Mob identifier with custom hitbox dimensions.
     *
     * @param mobId  mob identifier (e.g. "dragon", "iron_golem")
     * @param x      center X coordinate
     * @param y      center Y coordinate
     * @param width  hitbox width (pixels)
     * @param height hitbox height (pixels)
     * @param scale  scaling factor
     * @return a new {@link EntityModelNode}
     */
    @SuppressWarnings("deprecation")
    public static EntityModelNode forMob(String mobId, float x, float y, float width, float height, float scale) {
        int index = MOB_NAMES.indexOf(mobId.toLowerCase());
        int cmdId = index >= 0 ? 10001 + index : 10001;
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(cmdId);
            meta.displayName(Component.text("§f" + mobId));
            stack.setItemMeta(meta);
        }
        return new EntityModelNode(stack, x, y, width, height, scale);
    }

    /**
     * Returns the X-axis scaling factor of the model.
     *
     * @return the {@code scaleX} value
     */
    public float scale() {
        return scaleX;
    }

    /**
     * Checks if a point lies within the model's interaction hitbox.
     *
     * @param pointX X coordinate to check
     * @param pointY Y coordinate to check
     * @return {@code true} if point is inside hitbox
     */
    public boolean contains(float pointX, float pointY) {
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        return pointX >= x - halfW && pointX <= x + halfW
                && pointY >= y - halfH && pointY <= y + halfH;
    }

    /**
     * Checks whether the model responds to mouse hover rotation (hover spin or cursor tracking).
     *
     * @return {@code true} if hover rotation is active
     */
    public boolean hoverRotatable() {
        return rotation.mode() == UiModelRotation.Mode.HOVER_SPIN
                || rotation.mode() == UiModelRotation.Mode.CURSOR_TRACKING;
    }

    /**
     * Returns a copy with updated center coordinates.
     *
     * @param newX new center X coordinate (pixels)
     * @param newY new center Y coordinate (pixels)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withPosition(float newX, float newY) {
        return new EntityModelNode(item, newX, newY, depth, scaleX, scaleY, scaleZ, width, height,
                yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    /**
     * Returns a copy with updated base rotation angles across all 3 axes.
     *
     * @param yawDegrees   base yaw angle (degrees)
     * @param pitchDegrees base pitch angle (degrees)
     * @param rollDegrees  base roll angle (degrees)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withRotation(float yawDegrees, float pitchDegrees, float rollDegrees) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ, width, height,
                yawDegrees, pitchDegrees, rollDegrees, transform, visible, rotation, doubleSided);
    }

    /**
     * Returns a copy with updated base Yaw angle.
     *
     * @param yawDegrees base yaw angle (degrees)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withYaw(float yawDegrees) {
        return withRotation(yawDegrees, pitch, roll);
    }

    /**
     * Returns a copy with updated base Pitch angle.
     *
     * @param pitchDegrees base pitch angle (degrees)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withPitch(float pitchDegrees) {
        return withRotation(yaw, pitchDegrees, roll);
    }

    /**
     * Returns a copy with updated base Roll angle.
     *
     * @param rollDegrees base roll angle (degrees)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withRoll(float rollDegrees) {
        return withRotation(yaw, pitch, rollDegrees);
    }

    /**
     * Returns a copy with uniform scale across all 3 axes (X, Y, Z).
     *
     * @param uniformScale uniform scaling factor (> 0.0f)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withScale(float uniformScale) {
        return new EntityModelNode(item, x, y, depth, uniformScale, uniformScale, uniformScale,
                width, height, yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    /**
     * Returns a copy with independent scaling factors along X, Y, and Z axes.
     *
     * @param sx X-axis scale (> 0.0f)
     * @param sy Y-axis scale (> 0.0f)
     * @param sz Z-axis scale (> 0.0f)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withScale(float sx, float sy, float sz) {
        return new EntityModelNode(item, x, y, depth, sx, sy, sz,
                width, height, yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    /**
     * Returns a copy with updated interaction hitbox dimensions.
     *
     * @param newWidth  new hitbox width (pixels)
     * @param newHeight new hitbox height (pixels)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withSize(float newWidth, float newHeight) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ,
                newWidth, newHeight, yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    /**
     * Returns a copy with updated Z-depth layer offset on the canvas.
     *
     * @param newDepth new Z-depth
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withDepth(float newDepth) {
        return new EntityModelNode(item, x, y, newDepth, scaleX, scaleY, scaleZ,
                width, height, yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    /**
     * Returns a copy with an updated Minecraft ItemDisplay transform mode.
     *
     * @param newTransform display transform (FIXED, GUI, GROUND, HEAD, THIRDPERSON, etc.)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withTransform(ItemDisplay.ItemDisplayTransform newTransform) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ,
                width, height, yaw, pitch, roll, Objects.requireNonNull(newTransform, "transform"),
                visible, rotation, doubleSided);
    }

    /**
     * Returns a copy with updated visibility state.
     *
     * @param isVisible {@code true} to display, {@code false} to hide
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withVisibility(boolean isVisible) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ,
                width, height, yaw, pitch, roll, transform, isVisible, rotation, doubleSided);
    }

    /**
     * Returns a copy with updated double-sided rendering state.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withDoubleSided(boolean doubleSided) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ,
                width, height, yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    /**
     * Fluent alias for {@link #withDoubleSided(boolean)}.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode doubleSided(boolean doubleSided) {
        return withDoubleSided(doubleSided);
    }

    /**
     * Returns a copy with updated interactive 3D rotation settings.
     *
     * @param newRotation new rotation configuration
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode withInteractiveRotation(UiModelRotation newRotation) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ,
                width, height, yaw, pitch, roll, transform, visible,
                Objects.requireNonNull(newRotation, "rotation"), doubleSided);
    }

    /**
     * Configures the model to continuously rotate around its vertical axis.
     *
     * @param degreesPerTick rotation step per server tick (degrees/tick)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode autoSpin(float degreesPerTick) {
        return withInteractiveRotation(rotation.withAutoSpin(degreesPerTick));
    }

    /**
     * Configures the model to spin around its vertical axis when hovered by the viewer's reticle.
     *
     * @param degreesPerTick rotation step per tick while hovered (degrees/tick)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode hoverSpin(float degreesPerTick) {
        return withInteractiveRotation(rotation.withHoverSpin(degreesPerTick));
    }

    /**
     * Configures the model to dynamically tilt and orient towards the player's mouse reticle.
     *
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode cursorTrack() {
        return withInteractiveRotation(rotation.withMode(UiModelRotation.Mode.CURSOR_TRACKING));
    }

    /**
     * Sets the allowable angular rotation limits on the Yaw axis.
     *
     * @param min minimum yaw angle (degrees)
     * @param max maximum yaw angle (degrees)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode yawRange(float min, float max) {
        return withInteractiveRotation(rotation.withYawRange(min, max));
    }

    /**
     * Sets the allowable angular rotation limits on the Pitch axis.
     *
     * @param min minimum pitch angle (degrees)
     * @param max maximum pitch angle (degrees)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode pitchRange(float min, float max) {
        return withInteractiveRotation(rotation.withPitchRange(min, max));
    }

    /**
     * Sets the cursor-tracking sensitivity multiplier.
     *
     * @param factor sensitivity factor (> 0.0f)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode sensitivity(float factor) {
        return withInteractiveRotation(rotation.withSensitivity(factor));
    }

    /**
     * Sets the angular quantization step for snapping rotation increments.
     *
     * @param stepDegrees quantization step in degrees (0 for smooth continuous rotation)
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode step(float stepDegrees) {
        return withInteractiveRotation(rotation.withStep(stepDegrees));
    }

    /**
     * Locks or unlocks rotation along the Pitch axis.
     *
     * @param locked {@code true} to lock pitch
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode lockPitch(boolean locked) {
        return withInteractiveRotation(rotation.withLockPitch(locked));
    }

    /**
     * Locks or unlocks rotation along the Yaw axis.
     *
     * @param locked {@code true} to lock yaw
     * @return a new {@link EntityModelNode} instance
     */
    public EntityModelNode lockYaw(boolean locked) {
        return withInteractiveRotation(rotation.withLockYaw(locked));
    }
}
