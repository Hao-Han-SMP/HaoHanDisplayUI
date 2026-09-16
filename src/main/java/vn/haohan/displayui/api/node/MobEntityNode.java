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
package vn.haohan.displayui.api.node;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import vn.haohan.displayui.api.layout.UiRect;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A UI node representing a living Minecraft entity (LivingEntity) rendered in the canvas space.
 * <p>
 * Supports interactive 3D rotation (hover spin, cursor tracking), scaling, custom hitbox dimensions,
 * and direct programmatic LivingEntity modifications (armor, held items, variant texture, etc.)
 * via a {@link Consumer} callback.
 *
 * @param entityType      the Bukkit {@link EntityType} (must represent a {@link LivingEntity})
 * @param x               horizontal center coordinate on UI canvas (pixels)
 * @param y               vertical center coordinate on UI canvas (pixels)
 * @param depth           Z-depth layer offset (default 0.005f)
 * @param scale           uniform scaling factor
 * @param width           hitbox collision width for mouse interactions (pixels)
 * @param height          hitbox collision height for mouse interactions (pixels)
 * @param yaw             base Yaw rotation angle (degrees)
 * @param pitch           base Pitch rotation angle (degrees)
 * @param hoverRotatable  whether mouse interaction triggers rotation
 * @param rotation        rotation constraints and behavior settings ({@link UiModelRotation})
 * @param customizer      optional consumer callback to configure the spawned entity
 * @param doubleSided     whether back faces are rendered
 */
public record MobEntityNode(
        EntityType entityType,
        float x,
        float y,
        float depth,
        float scale,
        float width,
        float height,
        float yaw,
        float pitch,
        boolean hoverRotatable,
        UiModelRotation rotation,
        Consumer<LivingEntity> customizer,
        boolean doubleSided
) implements UiNode {
    public MobEntityNode {
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(rotation, "rotation");
        if (entityType.getEntityClass() != null && !LivingEntity.class.isAssignableFrom(entityType.getEntityClass())) {
            throw new IllegalArgumentException("entityType must be a LivingEntity, got: " + entityType);
        }
        if (scale <= 0.0f) {
            throw new IllegalArgumentException("mob scale must be positive");
        }
        if (width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("mob hitbox dimensions must be positive");
        }
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(depth)
                || !Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            throw new IllegalArgumentException("mob coordinates and rotation angles must be finite");
        }
    }

    /**
     * Constructs a full MobEntityNode with single-sided rendering enabled by default.
     */
    public MobEntityNode(EntityType entityType, float x, float y, float depth,
                         float scale, float width, float height,
                         float yaw, float pitch, boolean hoverRotatable,
                         UiModelRotation rotation, Consumer<LivingEntity> customizer) {
        this(entityType, x, y, depth, scale, width, height, yaw, pitch,
                hoverRotatable, rotation, customizer, false);
    }

    /**
     * Constructs a MobEntityNode with position and uniform scale using default hitbox dimensions.
     *
     * @param entityType living entity type
     * @param x          center X coordinate (pixels)
     * @param y          center Y coordinate (pixels)
     * @param scale      scaling factor
     */
    public MobEntityNode(EntityType entityType, float x, float y, float scale) {
        this(entityType, x, y, 0.005f, scale, 32.0f, 32.0f,
                0.0f, 0.0f, true, UiModelRotation.defaults(), null, false);
    }

    /**
     * Constructs a MobEntityNode with custom hitbox dimensions and uniform scale.
     *
     * @param entityType living entity type
     * @param x          center X coordinate (pixels)
     * @param y          center Y coordinate (pixels)
     * @param width      hitbox width (pixels)
     * @param height     hitbox height (pixels)
     * @param scale      scaling factor
     */
    public MobEntityNode(EntityType entityType, float x, float y, float width, float height, float scale) {
        this(entityType, x, y, 0.005f, scale, width, height,
                0.0f, 0.0f, true, UiModelRotation.defaults(), null, false);
    }

    /**
     * Constructs a MobEntityNode positioned and sized according to a bounding rectangle {@link UiRect}.
     *
     * @param entityType living entity type
     * @param bounds     bounding rectangle determining center and dimensions
     * @param scale      scaling factor
     */
    public MobEntityNode(EntityType entityType, UiRect bounds, float scale) {
        this(entityType, Objects.requireNonNull(bounds, "bounds").centerX(), bounds.centerY(),
                0.005f, scale, bounds.width(), bounds.height(),
                0.0f, 0.0f, true, UiModelRotation.defaults(), null, false);
    }

    /**
     * Returns a copy with updated base Yaw and Pitch angles.
     *
     * @param yawDegrees   yaw angle (degrees)
     * @param pitchDegrees pitch angle (degrees)
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode withRotation(float yawDegrees, float pitchDegrees) {
        return new MobEntityNode(entityType, x, y, depth, scale, width, height,
                yawDegrees, pitchDegrees, hoverRotatable, rotation, customizer, doubleSided);
    }

    /**
     * Returns a copy with an updated base Yaw angle.
     *
     * @param yawDegrees yaw angle (degrees)
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode withYaw(float yawDegrees) {
        return withRotation(yawDegrees, pitch);
    }

    /**
     * Returns a copy with an updated base Pitch angle.
     *
     * @param pitchDegrees pitch angle (degrees)
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode withPitch(float pitchDegrees) {
        return withRotation(yaw, pitchDegrees);
    }

    /**
     * Returns a copy with an updated scale factor.
     *
     * @param newScale new scale factor (> 0.0f)
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode withScale(float newScale) {
        return new MobEntityNode(entityType, x, y, depth, newScale, width, height,
                yaw, pitch, hoverRotatable, rotation, customizer, doubleSided);
    }

    /**
     * Returns a copy with updated hitbox collision dimensions.
     *
     * @param newWidth  new hitbox width (pixels)
     * @param newHeight new hitbox height (pixels)
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode withSize(float newWidth, float newHeight) {
        return new MobEntityNode(entityType, x, y, depth, scale, newWidth, newHeight,
                yaw, pitch, hoverRotatable, rotation, customizer, doubleSided);
    }

    /**
     * Returns a copy with an updated Z-depth offset.
     *
     * @param newDepth new Z-depth
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode withDepth(float newDepth) {
        return new MobEntityNode(entityType, x, y, newDepth, scale, width, height,
                yaw, pitch, hoverRotatable, rotation, customizer, doubleSided);
    }

    /**
     * Returns a copy with an updated entity customization consumer.
     *
     * @param newCustomizer callback accepting {@link LivingEntity} post-spawn
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode withCustomizer(Consumer<LivingEntity> newCustomizer) {
        return new MobEntityNode(entityType, x, y, depth, scale, width, height,
                yaw, pitch, hoverRotatable, rotation, newCustomizer, doubleSided);
    }

    /**
     * Returns a copy with toggled hover rotation responsiveness.
     *
     * @param rotatable {@code true} to enable hover rotation
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode hoverRotatable(boolean rotatable) {
        return new MobEntityNode(entityType, x, y, depth, scale, width, height,
                yaw, pitch, rotatable, rotation, customizer, doubleSided);
    }

    /**
     * Returns a copy with an updated rotation constraint configuration {@link UiModelRotation}.
     *
     * @param constraint new rotation settings
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode withRotationConstraint(UiModelRotation constraint) {
        return new MobEntityNode(entityType, x, y, depth, scale, width, height,
                yaw, pitch, hoverRotatable, constraint, customizer, doubleSided);
    }

    /**
     * Returns a copy with updated double-sided rendering state.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode withDoubleSided(boolean doubleSided) {
        return new MobEntityNode(entityType, x, y, depth, scale, width, height,
                yaw, pitch, hoverRotatable, rotation, customizer, doubleSided);
    }

    /**
     * Fluent alias for {@link #withDoubleSided(boolean)}.
     *
     * @param doubleSided {@code true} to render double-sided
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode doubleSided(boolean doubleSided) {
        return withDoubleSided(doubleSided);
    }

    /**
     * Locks or unlocks the Yaw axis.
     *
     * @param locked {@code true} to lock yaw
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode lockYaw(boolean locked) {
        return withRotationConstraint(rotation.withLockYaw(locked));
    }

    /**
     * Locks or unlocks the Pitch axis.
     *
     * @param locked {@code true} to lock pitch
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode lockPitch(boolean locked) {
        return withRotationConstraint(rotation.withLockPitch(locked));
    }

    /**
     * Sets allowable angular limits for the Yaw axis.
     *
     * @param min minimum yaw (degrees)
     * @param max maximum yaw (degrees)
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode yawRange(float min, float max) {
        return withRotationConstraint(rotation.withYawRange(min, max));
    }

    /**
     * Sets allowable angular limits for the Pitch axis.
     *
     * @param min minimum pitch (degrees)
     * @param max maximum pitch (degrees)
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode pitchRange(float min, float max) {
        return withRotationConstraint(rotation.withPitchRange(min, max));
    }

    /**
     * Sets the angular quantization snap step.
     *
     * @param stepAngle snap step angle (degrees)
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode step(float stepAngle) {
        return withRotationConstraint(rotation.withStep(stepAngle));
    }

    /**
     * Sets the cursor-tracking sensitivity multiplier.
     *
     * @param factor sensitivity factor (> 0.0f)
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode sensitivity(float factor) {
        return withRotationConstraint(rotation.withSensitivity(factor));
    }

    /**
     * Configures continuous automatic spin around the vertical axis.
     *
     * @param degreesPerTick rotation step per tick
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode autoSpin(float degreesPerTick) {
        return withRotationConstraint(UiModelRotation.autoSpin(degreesPerTick));
    }

    /**
     * Configures automatic spin triggered when the viewer hovers over the hitbox.
     *
     * @param degreesPerTick rotation step per tick while hovered
     * @return a new {@link MobEntityNode} instance
     */
    public MobEntityNode hoverSpin(float degreesPerTick) {
        return withRotationConstraint(UiModelRotation.hoverSpin(degreesPerTick));
    }

    /**
     * Checks if a canvas point (px, py) lies within the mob's hitbox.
     *
     * @param px X coordinate to test
     * @param py Y coordinate to test
     * @return {@code true} if point is inside hitbox
     */
    public boolean contains(float px, float py) {
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        return px >= x - halfW && px <= x + halfW && py >= y - halfH && py <= y + halfH;
    }
}
