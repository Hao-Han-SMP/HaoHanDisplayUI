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
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiBackgroundNode;
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;
import vn.haohan.displayui.api.node.TextNode;
import vn.haohan.displayui.api.node.ItemNode;
import vn.haohan.displayui.api.node.LineNode;
import vn.haohan.displayui.api.node.TriangleNode;
import vn.haohan.displayui.api.node.ParallelogramNode;
import vn.haohan.displayui.api.node.PolylineNode;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.api.node.UiShapeNode;
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

    /** Creates a hit zone around any renderable node supported by the engine. */
    public static UiButton forNode(String id, UiNode node) {
        Objects.requireNonNull(node, "node");
        UiRect bounds = switch (node) {
            case AlignedTextNode text -> new UiRect(text.boxX(), text.boxY(), text.width(), text.height());
            case UiIconNode icon -> new UiRect(icon.boxX(), icon.boxY(), icon.width(), icon.height());
            case BlockNode block -> new UiRect(block.x(), block.y(), block.width(), block.height());
            case UiBackgroundNode background -> new UiRect(background.x(), background.y(), background.width(), background.height());
            case UiGradientBackgroundNode gradient -> new UiRect(gradient.x(), gradient.y(), gradient.width(), gradient.height());
            case EntityModelNode model -> centered(model.x(), model.y(), model.width(), model.height());
            case MobEntityNode mob -> centered(mob.x(), mob.y(), mob.width(), mob.height());
            case ItemNode item -> centered(item.x(), item.y(), 16.0f, 16.0f);
            case TextNode text -> new UiRect(text.x(), text.y(), Math.max(1.0f, text.lineWidth()), 10.0f);
            case LineNode line -> bounds(line.x1(), line.y1(), line.x2(), line.y2(), line.thickness());
            case TriangleNode triangle -> bounds(
                    new float[] {triangle.x1(), triangle.x2(), triangle.x3()},
                    new float[] {triangle.y1(), triangle.y2(), triangle.y3()}, 0.0f);
            case ParallelogramNode parallelogram -> bounds(
                    new float[] {parallelogram.x1(), parallelogram.x2(), parallelogram.x3()},
                    new float[] {parallelogram.y1(), parallelogram.y2(), parallelogram.y3()}, 0.0f);
            case PolylineNode polyline -> {
                float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
                float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
                for (PolylineNode.Point point : polyline.points()) {
                    minX = Math.min(minX, point.x()); maxX = Math.max(maxX, point.x());
                    minY = Math.min(minY, point.y()); maxY = Math.max(maxY, point.y());
                }
                float pad = polyline.thickness() * 0.5f;
                yield new UiRect(minX - pad, minY - pad, Math.max(1.0f, maxX - minX + pad * 2),
                        Math.max(1.0f, maxY - minY + pad * 2));
            }
            case UiShapeNode shape -> new UiRect(shape.x(), shape.y(), shape.width(), shape.height());
        };
        return new UiButton(id, bounds);
    }

    private static UiRect centered(float x, float y, float width, float height) {
        return new UiRect(x - width * 0.5f, y - height * 0.5f, width, height);
    }

    private static UiRect bounds(float x1, float y1, float x2, float y2, float padding) {
        return new UiRect(Math.min(x1, x2) - padding, Math.min(y1, y2) - padding,
                Math.max(1.0f, Math.abs(x2 - x1) + padding * 2),
                Math.max(1.0f, Math.abs(y2 - y1) + padding * 2));
    }

    private static UiRect bounds(float[] xs, float[] ys, float padding) {
        float minX = xs[0], maxX = xs[0], minY = ys[0], maxY = ys[0];
        for (int i = 1; i < xs.length; i++) {
            minX = Math.min(minX, xs[i]); maxX = Math.max(maxX, xs[i]);
            minY = Math.min(minY, ys[i]); maxY = Math.max(maxY, ys[i]);
        }
        return new UiRect(minX - padding, minY - padding,
                Math.max(1.0f, maxX - minX + padding * 2),
                Math.max(1.0f, maxY - minY + padding * 2));
    }

    public boolean contains(float localX, float localY) {
        return localX >= x - hitSlop && localX <= x + width + hitSlop
                && localY >= y - hitSlop && localY <= y + height + hitSlop;
    }
}
