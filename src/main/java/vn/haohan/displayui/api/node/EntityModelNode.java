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
 * An entity node that renders any 3D Item or custom Entity Model (via ItemDisplay)
 * in the UI canvas with rich interactive rotation capabilities (auto-spin, hover-spin,
 * cursor-tracking 3D tilt, pitch/yaw locks, angle snapping).
 *
 * <p>Supports both vanilla item models (Swords, Bows, Blocks) and Custom Model Data (CMD)
 * entity models mapped through Resource Pack (Zombie, Cow, Dragon, etc.).</p>
 *
 * @param item the {@link ItemStack} to render (e.g. {@code Material.PAPER} with CMD for mob models)
 * @param x horizontal center in canvas units
 * @param y vertical center in canvas units
 * @param depth forward offset in canvas units (default 0.08f)
 * @param scaleX X scale factor
 * @param scaleY Y scale factor
 * @param scaleZ Z scale factor
 * @param width collision hitbox width for interaction
 * @param height collision hitbox height for interaction
 * @param yaw base yaw rotation in degrees
 * @param pitch base pitch rotation in degrees
 * @param roll base roll rotation in degrees
 * @param transform Minecraft {@link ItemDisplay.ItemDisplayTransform} (FIXED, GUI, GROUND, etc.)
 * @param visible initial visibility
 * @param rotation interactive rotation configuration
 * @param doubleSided whether to render symmetrically facing viewers from both sides
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

    public EntityModelNode(ItemStack item, float x, float y, float depth,
                           float scaleX, float scaleY, float scaleZ,
                           float width, float height, float yaw, float pitch, float roll,
                           ItemDisplay.ItemDisplayTransform transform,
                           boolean visible, UiModelRotation rotation) {
        this(item, x, y, depth, scaleX, scaleY, scaleZ, width, height,
                yaw, pitch, roll, transform, visible, rotation, false);
    }

    public EntityModelNode(ItemStack item, float x, float y, float scale) {
        this(item, x, y, 0.08f, scale, scale, scale, 32.0f, 32.0f,
                0.0f, 0.0f, 0.0f, ItemDisplay.ItemDisplayTransform.FIXED,
                true, UiModelRotation.defaults(), false);
    }

    public EntityModelNode(ItemStack item, float x, float y, float width, float height, float scale) {
        this(item, x, y, 0.08f, scale, scale, scale, width, height,
                0.0f, 0.0f, 0.0f, ItemDisplay.ItemDisplayTransform.FIXED,
                true, UiModelRotation.defaults(), false);
    }

    public static EntityModelNode forMob(String mobId, float x, float y, float scale) {
        return forMob(mobId, x, y, 32.0f, 32.0f, scale);
    }

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

    public float scale() {
        return scaleX;
    }

    public boolean contains(float pointX, float pointY) {
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        return pointX >= x - halfW && pointX <= x + halfW
                && pointY >= y - halfH && pointY <= y + halfH;
    }

    public boolean hoverRotatable() {
        return rotation.mode() == UiModelRotation.Mode.HOVER_SPIN
                || rotation.mode() == UiModelRotation.Mode.CURSOR_TRACKING;
    }

    public EntityModelNode withPosition(float newX, float newY) {
        return new EntityModelNode(item, newX, newY, depth, scaleX, scaleY, scaleZ, width, height,
                yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    public EntityModelNode withRotation(float yawDegrees, float pitchDegrees, float rollDegrees) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ, width, height,
                yawDegrees, pitchDegrees, rollDegrees, transform, visible, rotation, doubleSided);
    }

    public EntityModelNode withYaw(float yawDegrees) {
        return withRotation(yawDegrees, pitch, roll);
    }

    public EntityModelNode withPitch(float pitchDegrees) {
        return withRotation(yaw, pitchDegrees, roll);
    }

    public EntityModelNode withRoll(float rollDegrees) {
        return withRotation(yaw, pitch, rollDegrees);
    }

    public EntityModelNode withScale(float uniformScale) {
        return new EntityModelNode(item, x, y, depth, uniformScale, uniformScale, uniformScale,
                width, height, yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    public EntityModelNode withScale(float sx, float sy, float sz) {
        return new EntityModelNode(item, x, y, depth, sx, sy, sz,
                width, height, yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    public EntityModelNode withSize(float newWidth, float newHeight) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ,
                newWidth, newHeight, yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    public EntityModelNode withDepth(float newDepth) {
        return new EntityModelNode(item, x, y, newDepth, scaleX, scaleY, scaleZ,
                width, height, yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    public EntityModelNode withTransform(ItemDisplay.ItemDisplayTransform newTransform) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ,
                width, height, yaw, pitch, roll, Objects.requireNonNull(newTransform, "transform"),
                visible, rotation, doubleSided);
    }

    public EntityModelNode withVisibility(boolean isVisible) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ,
                width, height, yaw, pitch, roll, transform, isVisible, rotation, doubleSided);
    }

    public EntityModelNode withDoubleSided(boolean doubleSided) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ,
                width, height, yaw, pitch, roll, transform, visible, rotation, doubleSided);
    }

    public EntityModelNode doubleSided(boolean doubleSided) {
        return withDoubleSided(doubleSided);
    }

    public EntityModelNode withInteractiveRotation(UiModelRotation newRotation) {
        return new EntityModelNode(item, x, y, depth, scaleX, scaleY, scaleZ,
                width, height, yaw, pitch, roll, transform, visible,
                Objects.requireNonNull(newRotation, "rotation"), doubleSided);
    }

    public EntityModelNode autoSpin(float degreesPerTick) {
        return withInteractiveRotation(rotation.withAutoSpin(degreesPerTick));
    }

    public EntityModelNode hoverSpin(float degreesPerTick) {
        return withInteractiveRotation(rotation.withHoverSpin(degreesPerTick));
    }

    public EntityModelNode cursorTrack() {
        return withInteractiveRotation(rotation.withMode(UiModelRotation.Mode.CURSOR_TRACKING));
    }

    public EntityModelNode yawRange(float min, float max) {
        return withInteractiveRotation(rotation.withYawRange(min, max));
    }

    public EntityModelNode pitchRange(float min, float max) {
        return withInteractiveRotation(rotation.withPitchRange(min, max));
    }

    public EntityModelNode sensitivity(float factor) {
        return withInteractiveRotation(rotation.withSensitivity(factor));
    }

    public EntityModelNode step(float stepDegrees) {
        return withInteractiveRotation(rotation.withStep(stepDegrees));
    }

    public EntityModelNode lockPitch(boolean locked) {
        return withInteractiveRotation(rotation.withLockPitch(locked));
    }

    public EntityModelNode lockYaw(boolean locked) {
        return withInteractiveRotation(rotation.withLockYaw(locked));
    }
}
