/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package vn.haohan.displayui.runtime.scene;

import org.bukkit.util.Transformation;
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
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.api.node.UiShapeNode;

import java.util.List;

/** Selects the scene transform calculation for each supported node type. */
final class UiNodeTransformations {
    private UiNodeTransformations() {
        throw new AssertionError("utility class");
    }

    static List<Transformation> resolve(UiScene scene, UiNode node, float scale,
                                        float offsetX, float offsetY, float offsetZ) {
        if (node instanceof BlockNode block) {
            return scene.computeBlockTransforms(block, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof AlignedTextNode text) {
            return scene.computeAlignedTextTransforms(text, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof TextNode text) {
            return scene.computeTextTransforms(text, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof ItemNode item) {
            return scene.computeItemTransforms(item, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof UiIconNode icon) {
            return scene.computeIconTransforms(icon, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof EntityModelNode model) {
            return scene.computeModelTransforms(model, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof MobEntityNode mob) {
            EntityModelNode model = EntityModelNode.forMob(
                            mob.entityType().name().toLowerCase(),
                            mob.x(), mob.y(), mob.width(), mob.height(), mob.scale())
                    .withYaw(mob.yaw())
                    .withPitch(mob.pitch())
                    .withDoubleSided(mob.doubleSided());
            return scene.computeModelTransforms(model, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof UiBackgroundNode background) {
            return scene.computeBackgroundTransforms(background, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof UiGradientBackgroundNode gradient) {
            return scene.computeGradientBackgroundTransforms(gradient, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof LineNode line) {
            return scene.computeLineTransforms(line, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof ParallelogramNode parallelogram) {
            return scene.computeParallelogramTransforms(parallelogram, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof TriangleNode triangle) {
            return scene.computeTriangleTransforms(triangle, scale, offsetX, offsetY, offsetZ);
        } else if (node instanceof UiShapeNode shape) {
            List<UiNode> subNodes = shape.decomposeToNodes();
            List<Transformation> list = new java.util.ArrayList<>();
            for (UiNode sub : subNodes) {
                list.addAll(resolve(scene, sub, scale, offsetX, offsetY, offsetZ));
            }
            return list;
        } else if (node instanceof PolylineNode polyline) {
            return scene.computePolylineTransforms(polyline, scale, offsetX, offsetY, offsetZ);
        }
        return List.of();
    }
}
