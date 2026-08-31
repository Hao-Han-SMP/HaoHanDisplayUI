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
 * A vanilla living mob/entity node capable of rendering in the UI with interactive
 * hover rotation, scale, and custom entity configuration.
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
        Consumer<LivingEntity> customizer
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

    public MobEntityNode(EntityType entityType, float x, float y, float scale) {
        this(entityType, x, y, 0.005f, scale, 32.0f, 32.0f,
                0.0f, 0.0f, true, UiModelRotation.defaults(), null);
    }

    public MobEntityNode(EntityType entityType, float x, float y, float width, float height, float scale) {
        this(entityType, x, y, 0.005f, scale, width, height,
                0.0f, 0.0f, true, UiModelRotation.defaults(), null);
    }

    public MobEntityNode(EntityType entityType, UiRect bounds, float scale) {
        this(entityType, Objects.requireNonNull(bounds, "bounds").centerX(), bounds.centerY(),
                0.005f, scale, bounds.width(), bounds.height(),
                0.0f, 0.0f, true, UiModelRotation.defaults(), null);
    }

    public MobEntityNode withRotation(float yawDegrees, float pitchDegrees) {
        return new MobEntityNode(entityType, x, y, depth, scale, width, height,
                yawDegrees, pitchDegrees, hoverRotatable, rotation, customizer);
    }

    public MobEntityNode withYaw(float yawDegrees) {
        return withRotation(yawDegrees, pitch);
    }

    public MobEntityNode withPitch(float pitchDegrees) {
        return withRotation(yaw, pitchDegrees);
    }

    public MobEntityNode withScale(float newScale) {
        return new MobEntityNode(entityType, x, y, depth, newScale, width, height,
                yaw, pitch, hoverRotatable, rotation, customizer);
    }

    public MobEntityNode withSize(float newWidth, float newHeight) {
        return new MobEntityNode(entityType, x, y, depth, scale, newWidth, newHeight,
                yaw, pitch, hoverRotatable, rotation, customizer);
    }

    public MobEntityNode withDepth(float newDepth) {
        return new MobEntityNode(entityType, x, y, newDepth, scale, width, height,
                yaw, pitch, hoverRotatable, rotation, customizer);
    }

    public MobEntityNode withCustomizer(Consumer<LivingEntity> newCustomizer) {
        return new MobEntityNode(entityType, x, y, depth, scale, width, height,
                yaw, pitch, hoverRotatable, rotation, newCustomizer);
    }

    public MobEntityNode hoverRotatable(boolean rotatable) {
        return new MobEntityNode(entityType, x, y, depth, scale, width, height,
                yaw, pitch, rotatable, rotation, customizer);
    }

    public MobEntityNode withRotationConstraint(UiModelRotation constraint) {
        return new MobEntityNode(entityType, x, y, depth, scale, width, height,
                yaw, pitch, hoverRotatable, constraint, customizer);
    }

    public MobEntityNode lockYaw(boolean locked) {
        return withRotationConstraint(rotation.withLockYaw(locked));
    }

    public MobEntityNode lockPitch(boolean locked) {
        return withRotationConstraint(rotation.withLockPitch(locked));
    }

    public MobEntityNode yawRange(float min, float max) {
        return withRotationConstraint(rotation.withYawRange(min, max));
    }

    public MobEntityNode pitchRange(float min, float max) {
        return withRotationConstraint(rotation.withPitchRange(min, max));
    }

    public MobEntityNode step(float stepAngle) {
        return withRotationConstraint(rotation.withStep(stepAngle));
    }

    public MobEntityNode sensitivity(float factor) {
        return withRotationConstraint(rotation.withSensitivity(factor));
    }

    public MobEntityNode autoSpin(float degreesPerTick) {
        return withRotationConstraint(UiModelRotation.autoSpin(degreesPerTick));
    }

    public MobEntityNode hoverSpin(float degreesPerTick) {
        return withRotationConstraint(UiModelRotation.hoverSpin(degreesPerTick));
    }

    public boolean contains(float localX, float localY) {
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        return localX >= x - halfW && localX <= x + halfW
                && localY >= y - halfH && localY <= y + halfH;
    }
}
