/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 */
package vn.haohan.displayui.runtime.scene;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/** Owns conversion of immutable UI nodes into display entities. */
final class UiSceneRenderer {
    private final UiScene scene;
    private final Map<Class<? extends UiNode>, Function<UiNode, List<Display>>> nodeSpawners = new LinkedHashMap<>();

    UiSceneRenderer(UiScene scene) {
        this.scene = scene;
        register(UiBackgroundNode.class, node -> spawnBackground((UiBackgroundNode) node));
        register(UiGradientBackgroundNode.class, node -> spawnGradientBackground((UiGradientBackgroundNode) node));
        register(TextNode.class, node -> spawnText((TextNode) node));
        register(AlignedTextNode.class, node -> spawnAlignedText((AlignedTextNode) node));
        register(ItemNode.class, node -> spawnItem((ItemNode) node));
        register(UiIconNode.class, node -> spawnIcon((UiIconNode) node));
        register(BlockNode.class, node -> spawnBlock((BlockNode) node));
        register(EntityModelNode.class, node -> spawnEntityModel((EntityModelNode) node));
        register(MobEntityNode.class, node -> spawnMobEntity((MobEntityNode) node));
        register(LineNode.class, node -> spawnLine((LineNode) node));
        register(ParallelogramNode.class, node -> spawnParallelogram((ParallelogramNode) node));
        register(TriangleNode.class, node -> spawnTriangle((TriangleNode) node));
        register(PolylineNode.class, node -> spawnPolyline((PolylineNode) node));
        register(UiShapeNode.class, node -> spawnShape((UiShapeNode) node));
    }

    List<Display> spawnNode(UiNode node) {
        for (Map.Entry<Class<? extends UiNode>, Function<UiNode, List<Display>>> entry : nodeSpawners.entrySet()) {
            if (entry.getKey().isInstance(node)) return entry.getValue().apply(node);
        }
        throw new IllegalArgumentException("Unsupported UI node: " + node.getClass().getName());
    }

    void updateNode(List<Display> displays, UiNode node) {
        if (displays == null || displays.isEmpty()) return;
        for (Display display : displays) {
            if (display != null && display.isValid()) {
                display.setInterpolationDelay(0);
                display.setInterpolationDuration(scene.interpolationTicks());
            }
        }
        if (node instanceof UiShapeNode shape) {
            List<UiNode> subNodes = shape.decomposeToNodes();
            int displayIdx = 0;
            for (UiNode sub : subNodes) {
                List<Transformation> subTransforms = UiNodeTransformations.resolve(scene, sub, 1.0f, 0.0f, 0.0f, 0.0f);
                for (int k = 0; k < subTransforms.size() && displayIdx < displays.size(); k++, displayIdx++) {
                    Display display = displays.get(displayIdx);
                    if (display == null || !display.isValid()) continue;
                    updateDisplay(display, sub, subTransforms.get(k), k);
                }
            }
            return;
        }
        List<Transformation> transforms = UiNodeTransformations.resolve(scene, node, 1.0f, 0.0f, 0.0f, 0.0f);
        for (int i = 0; i < displays.size() && i < transforms.size(); i++) {
            Display display = displays.get(i);
            if (display == null || !display.isValid()) continue;
            updateDisplay(display, node, transforms.get(i), i);
        }
    }

    private void updateDisplay(Display display, UiNode node, Transformation transform, int index) {
        if (node instanceof UiBackgroundNode background && display instanceof TextDisplay text) {
            text.setBackgroundColor(background.background());
        } else if (node instanceof UiGradientBackgroundNode gradient && display instanceof TextDisplay text) {
            text.setBackgroundColor(gradient.colorForDisplayIndex(index));
        } else if (node instanceof TextNode textNode && display instanceof TextDisplay text) {
            text.text(textNode.text());
            text.setShadowed(textNode.shadow());
            text.setSeeThrough(textNode.seeThrough());
            text.setAlignment(textNode.alignment());
            text.setLineWidth(textNode.lineWidth());
        } else if (node instanceof AlignedTextNode textNode && display instanceof TextDisplay text) {
            text.text(textNode.text());
            text.setShadowed(textNode.shadow());
            text.setSeeThrough(textNode.seeThrough());
            text.setLineWidth(Math.max(1, Math.round(textNode.width() * 20.0f / textNode.fontSize())));
        } else if ((node instanceof ItemNode || node instanceof UiIconNode)
                && display instanceof ItemDisplay itemDisplay) {
            if (node instanceof ItemNode itemNode) {
                itemDisplay.setItemStack(itemNode.item());
                itemDisplay.setItemDisplayTransform(itemNode.transform());
            } else if (node instanceof UiIconNode iconNode) {
                itemDisplay.setItemStack(iconNode.item());
                itemDisplay.setItemDisplayTransform(iconNode.transform());
            }
        } else if (node instanceof BlockNode block && display instanceof BlockDisplay blockDisplay) {
            blockDisplay.setBlock(block.block());
        } else if (node instanceof EntityModelNode model && display instanceof ItemDisplay itemDisplay) {
            itemDisplay.setItemStack(model.item());
            itemDisplay.setItemDisplayTransform(model.transform());
        } else if (node instanceof MobEntityNode mob && display instanceof ItemDisplay itemDisplay) {
            EntityModelNode model = EntityModelNode.forMob(
                            mob.entityType().name().toLowerCase(), mob.x(), mob.y(), mob.width(), mob.height(), mob.scale())
                    .withYaw(mob.yaw()).withPitch(mob.pitch()).withDoubleSided(mob.doubleSided());
            itemDisplay.setItemStack(model.item());
            itemDisplay.setItemDisplayTransform(model.transform());
        } else if (node instanceof LineNode line && display instanceof TextDisplay text) {
            text.setBackgroundColor(line.color());
        } else if (node instanceof ParallelogramNode shape && display instanceof TextDisplay text) {
            text.setBackgroundColor(shape.color());
        } else if (node instanceof TriangleNode shape && display instanceof TextDisplay text) {
            text.setBackgroundColor(shape.color());
        } else if (node instanceof PolylineNode shape && display instanceof TextDisplay text) {
            text.setBackgroundColor(shape.color());
        } else if (node instanceof UiShapeNode shape && display instanceof TextDisplay text) {
            text.setBackgroundColor(shape.color());
        }
        if (!scene.isAnimating()) {
            display.setTransformation(transform);
        }
    }

    private <T extends UiNode> void register(Class<T> type, Function<UiNode, List<Display>> spawner) {
        nodeSpawners.put(type, spawner);
    }

    private <T extends Display> List<Display> spawnDisplays(Class<T> type,
                                                            List<Transformation> transforms,
                                                            Consumer<T> configureDisplay) {
        List<Display> displays = new ArrayList<>(transforms.size());
        for (Transformation transform : transforms) {
            displays.add(scene.world().spawn(scene.renderOrigin(), type, display -> {
                configureDisplay.accept(display);
                display.setTransformation(transform);
            }));
        }
        return displays;
    }

    private List<Display> spawnColoredShape(UiNode node, List<Transformation> transforms, Color color) {
        return spawnDisplays(TextDisplay.class, transforms, display -> {
            scene.configure(display, node);
            display.text(Component.text(" "));
            display.setBackgroundColor(color);
        });
    }

    private List<Display> spawnBackground(UiBackgroundNode node) {
        List<Transformation> transforms = scene.computeBackgroundTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        return spawnDisplays(TextDisplay.class, transforms, display -> {
            scene.configure(display, node);
            display.text(Component.text(" "));
            display.setTextOpacity((byte) 0);
            display.setAlignment(TextDisplay.TextAlignment.LEFT);
            display.setBackgroundColor(node.background());
        });
    }

    private List<Display> spawnGradientBackground(UiGradientBackgroundNode node) {
        List<Transformation> transforms = scene.computeGradientBackgroundTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        List<Display> displays = new ArrayList<>(transforms.size());
        for (int i = 0; i < transforms.size(); i++) {
            Transformation transform = transforms.get(i);
            Color color = node.colorForDisplayIndex(i);
            displays.add(scene.world().spawn(scene.renderOrigin(), TextDisplay.class, display -> {
                scene.configure(display, node);
                display.text(Component.text(" "));
                display.setTextOpacity((byte) 0);
                display.setAlignment(TextDisplay.TextAlignment.LEFT);
                display.setBackgroundColor(color);
                display.setTransformation(transform);
            }));
        }
        return displays;
    }

    private List<Display> spawnText(TextNode node) {
        List<Transformation> transforms = scene.computeTextTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        return spawnDisplays(TextDisplay.class, transforms, display -> {
            scene.configure(display, node);
            display.text(node.text());
            display.setShadowed(node.shadow());
            display.setSeeThrough(node.seeThrough());
            display.setAlignment(node.alignment());
            display.setLineWidth(node.lineWidth());
        });
    }

    private List<Display> spawnAlignedText(AlignedTextNode node) {
        List<Transformation> transforms = scene.computeAlignedTextTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        return spawnDisplays(TextDisplay.class, transforms, display -> {
            scene.configure(display, node);
            display.text(node.text());
            display.setAlignment(TextDisplay.TextAlignment.CENTER);
            display.setLineWidth(Math.max(1, Math.round(node.width() * 20.0f / node.fontSize())));
            display.setShadowed(node.shadow());
            display.setSeeThrough(node.seeThrough());
            display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            display.setTextOpacity((byte) 255);
        });
    }

    private List<Display> spawnItem(ItemNode node) {
        List<Transformation> transforms = scene.computeItemTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        return spawnDisplays(ItemDisplay.class, transforms, display -> {
            scene.configure(display, node);
            display.setItemStack(node.item());
            display.setItemDisplayTransform(node.transform());
        });
    }

    private List<Display> spawnIcon(UiIconNode node) {
        List<Transformation> transforms = scene.computeIconTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        return spawnDisplays(ItemDisplay.class, transforms, display -> {
            scene.configure(display, node);
            display.setItemStack(node.item());
            display.setItemDisplayTransform(node.transform());
        });
    }

    private List<Display> spawnEntityModel(EntityModelNode node) {
        List<Transformation> transforms = scene.computeModelTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        return spawnDisplays(ItemDisplay.class, transforms, display -> {
            scene.configure(display, node);
            display.setItemStack(node.item());
            display.setItemDisplayTransform(node.transform());
        });
    }

    private List<Display> spawnMobEntity(MobEntityNode node) {
        EntityModelNode model = EntityModelNode.forMob(
                        node.entityType().name().toLowerCase(),
                        node.x(), node.y(), node.width(), node.height(), node.scale())
                .withYaw(node.yaw())
                .withPitch(node.pitch())
                .withDoubleSided(node.doubleSided());
        return spawnEntityModel(model);
    }

    private List<Display> spawnBlock(BlockNode node) {
        List<Transformation> transforms = scene.computeBlockTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f);
        return spawnDisplays(BlockDisplay.class, transforms, display -> {
            scene.configure(display, node);
            display.setBlock(node.block());
        });
    }

    private List<Display> spawnLine(LineNode node) {
        return spawnColoredShape(node, scene.computeLineTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f), node.color());
    }

    private List<Display> spawnParallelogram(ParallelogramNode node) {
        return spawnColoredShape(node, scene.computeParallelogramTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f), node.color());
    }

    private List<Display> spawnTriangle(TriangleNode node) {
        return spawnColoredShape(node, scene.computeTriangleTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f), node.color());
    }

    private List<Display> spawnPolyline(PolylineNode node) {
        return spawnColoredShape(node, scene.computePolylineTransforms(node, 1.0f, 0.0f, 0.0f, 0.0f), node.color());
    }

    private List<Display> spawnShape(UiShapeNode node) {
        List<UiNode> subNodes = node.decomposeToNodes();
        List<Display> displays = new ArrayList<>();
        for (UiNode sub : subNodes) {
            displays.addAll(spawnNode(sub));
        }
        return displays;
    }
}
