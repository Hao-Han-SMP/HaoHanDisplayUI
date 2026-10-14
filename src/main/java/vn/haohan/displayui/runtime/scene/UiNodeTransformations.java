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
            boolean isScaled = Math.abs(scale - 1.0f) > 1e-6f;
            float cx = shape.x() + shape.width() * 0.5f;
            float cy = shape.y() + shape.height() * 0.5f;

            for (UiNode sub : subNodes) {
                if (!isScaled) {
                    list.addAll(resolve(scene, sub, 1.0f, offsetX, offsetY, offsetZ));
                } else if (sub instanceof UiBackgroundNode bg) {
                    float subW = bg.width() * scale;
                    float subH = bg.height() * scale;
                    float subCx = bg.x() + bg.width() * 0.5f;
                    float subCy = bg.y() + bg.height() * 0.5f;
                    float newX = cx + (subCx - cx) * scale - subW * 0.5f;
                    float newY = cy + (subCy - cy) * scale - subH * 0.5f;
                    UiBackgroundNode scaledBg = new UiBackgroundNode(newX, newY, bg.depth(), subW, subH, bg.background(), bg.doubleSided());
                    list.addAll(resolve(scene, scaledBg, 1.0f, offsetX, offsetY, offsetZ));
                } else if (sub instanceof TriangleNode tri) {
                    float x1 = cx + (tri.x1() - cx) * scale;
                    float y1 = cy + (tri.y1() - cy) * scale;
                    float x2 = cx + (tri.x2() - cx) * scale;
                    float y2 = cy + (tri.y2() - cy) * scale;
                    float x3 = cx + (tri.x3() - cx) * scale;
                    float y3 = cy + (tri.y3() - cy) * scale;
                    TriangleNode scaledTri = new TriangleNode(x1, y1, x2, y2, x3, y3, tri.depth(), tri.color(), tri.doubleSided());
                    list.addAll(resolve(scene, scaledTri, 1.0f, offsetX, offsetY, offsetZ));
                } else if (sub instanceof ParallelogramNode para) {
                    float x1 = cx + (para.x1() - cx) * scale;
                    float y1 = cy + (para.y1() - cy) * scale;
                    float x2 = cx + (para.x2() - cx) * scale;
                    float y2 = cy + (para.y2() - cy) * scale;
                    float x3 = cx + (para.x3() - cx) * scale;
                    float y3 = cy + (para.y3() - cy) * scale;
                    ParallelogramNode scaledPara = new ParallelogramNode(x1, y1, x2, y2, x3, y3, para.depth(), para.color(), para.doubleSided());
                    list.addAll(resolve(scene, scaledPara, 1.0f, offsetX, offsetY, offsetZ));
                } else if (sub instanceof LineNode line) {
                    float x1 = cx + (line.x1() - cx) * scale;
                    float y1 = cy + (line.y1() - cy) * scale;
                    float x2 = cx + (line.x2() - cx) * scale;
                    float y2 = cy + (line.y2() - cy) * scale;
                    LineNode scaledLine = new LineNode(x1, y1, x2, y2, line.thickness() * scale, line.depth(), line.color(), line.doubleSided());
                    list.addAll(resolve(scene, scaledLine, 1.0f, offsetX, offsetY, offsetZ));
                } else if (sub instanceof PolylineNode poly) {
                    List<PolylineNode.Point> scaledPts = new java.util.ArrayList<>(poly.points().size());
                    for (PolylineNode.Point pt : poly.points()) {
                        scaledPts.add(new PolylineNode.Point(cx + (pt.x() - cx) * scale, cy + (pt.y() - cy) * scale));
                    }
                    PolylineNode scaledPoly = new PolylineNode(scaledPts, poly.thickness() * scale, poly.depth(), poly.color(), poly.doubleSided(), poly.closed());
                    list.addAll(resolve(scene, scaledPoly, 1.0f, offsetX, offsetY, offsetZ));
                } else {
                    list.addAll(resolve(scene, sub, scale, offsetX, offsetY, offsetZ));
                }
            }
            return list;
        } else if (node instanceof PolylineNode polyline) {
            return scene.computePolylineTransforms(polyline, scale, offsetX, offsetY, offsetZ);
        }
        return List.of();
    }
}
