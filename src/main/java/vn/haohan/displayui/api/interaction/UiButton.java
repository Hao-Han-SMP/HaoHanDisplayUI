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
package vn.haohan.displayui.api.interaction;

import vn.haohan.displayui.api.layout.UiRect;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.EntityModelNode;
import vn.haohan.displayui.api.node.MobEntityNode;
import vn.haohan.displayui.api.node.UiIconNode;
import net.kyori.adventure.text.Component;

import java.util.Objects;

/** Invisible rectangular hit zone in the same logical-pixel space as UI nodes. */
public record UiButton(
        String id,
        float x,
        float y,
        float width,
        float height,
        Component description,
        UiButtonAction action,
        float hitSlop
) implements UiControl {
    public UiButton {
        Objects.requireNonNull(id, "id");
        description = Objects.requireNonNullElse(description, Component.empty());
        action = Objects.requireNonNullElse(action, UiButtonAction.none());
        if (!id.matches("[a-z0-9_.-]+")) {
            throw new IllegalArgumentException("button id must contain only [a-z0-9_.-]");
        }
        if (width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("button dimensions must be positive");
        }
        if (hitSlop < 0.0f || !Float.isFinite(hitSlop)) {
            throw new IllegalArgumentException("button hitSlop must be finite and non-negative");
        }
    }

    public UiButton(String id, float x, float y, float width, float height) {
        this(id, x, y, width, height, Component.empty(), UiButtonAction.none(), 0.0f);
    }

    public UiButton(String id, UiRect bounds) {
        this(id, Objects.requireNonNull(bounds, "bounds").x(), bounds.y(),
                bounds.width(), bounds.height());
    }

    public UiButton(String id, float x, float y, float width, float height,
                    Component description) {
        this(id, x, y, width, height, description, UiButtonAction.none(), 0.0f);
    }

    public UiButton(String id, float x, float y, float width, float height,
                    Component description, UiButtonAction action) {
        this(id, x, y, width, height, description, action, 0.0f);
    }

    public UiButton describedBy(Component description) {
        return new UiButton(id, x, y, width, height,
                description != null ? description : Component.empty(), action, hitSlop);
    }

    public UiButton withAction(UiButtonAction action) {
        return new UiButton(id, x, y, width, height, description,
                action != null ? action : UiButtonAction.none(), hitSlop);
    }

    public UiButton hitSlop(float pixels) {
        return new UiButton(id, x, y, width, height, description, action, pixels);
    }

    public static UiButton forText(String id, AlignedTextNode text) {
        Objects.requireNonNull(text, "text");
        return new UiButton(id, text.boxX(), text.boxY(), text.width(), text.height());
    }

    public static UiButton forIcon(String id, UiIconNode icon) {
        Objects.requireNonNull(icon, "icon");
        return new UiButton(id, icon.boxX(), icon.boxY(), icon.width(), icon.height());
    }

    public static UiButton forModel(String id, EntityModelNode model) {
        Objects.requireNonNull(model, "model");
        return new UiButton(id, model.x() - model.width() * 0.5f,
                model.y() - model.height() * 0.5f, model.width(), model.height());
    }

    public static UiButton forMob(String id, MobEntityNode mob) {
        Objects.requireNonNull(mob, "mob");
        return new UiButton(id, mob.x() - mob.width() * 0.5f,
                mob.y() - mob.height() * 0.5f, mob.width(), mob.height());
    }

    public boolean contains(float localX, float localY) {
        return localX >= x - hitSlop && localX <= x + width + hitSlop
                && localY >= y - hitSlop && localY <= y + height + hitSlop;
    }
}
