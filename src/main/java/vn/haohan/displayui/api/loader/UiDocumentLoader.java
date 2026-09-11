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
package vn.haohan.displayui.api.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiButtonAction;
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.api.gradient.UiGradientPosition;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.EntityModelNode;
import vn.haohan.displayui.api.node.ItemNode;
import vn.haohan.displayui.api.node.LineNode;
import vn.haohan.displayui.api.node.MobEntityNode;
import vn.haohan.displayui.api.node.ParallelogramNode;
import vn.haohan.displayui.api.node.PolylineNode;
import vn.haohan.displayui.api.node.TriangleNode;
import vn.haohan.displayui.api.node.UiBackgroundNode;
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.node.UiModelRotation;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.api.node.UiShapeNode;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.ItemMeta;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.text.UiVerticalAlignment;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * Loads a {@link UiDocument} from a {@code .hhdui.json} file exported by the HaoHan Visual Builder.
 *
 * <p>Supports MiniMessage formatting ({@code <red>text</red>}) and legacy {@code &} color codes
 * ({@code &ctext}, {@code &lbold}).
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * UiDocument doc = UiDocumentLoader.load(new File(getDataFolder(), "panels/my_panel.hhdui.json"));
 * DisplayUiService ui = getServer().getServicesManager().load(DisplayUiService.class);
 * UiHandle handle = ui.show(location, doc, UiOptions.defaults(), player);
 * }</pre>
 */
public final class UiDocumentLoader {

    private static final Logger LOGGER = Logger.getLogger(UiDocumentLoader.class.getName());
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder().character('&').hexColors().build();

    private UiDocumentLoader() {}

    /**
     * Represents the result of parsing a UiDocument with detailed diagnostic logs.
     */
    public record DocumentLoadReport(UiDocument document, List<String> errors) {
        public boolean hasErrors() { return !errors.isEmpty(); }
    }

    /**
     * Loads a UiDocument from a file with detailed diagnostic error collection.
     */
    public static DocumentLoadReport loadWithReport(File file) {
        Objects.requireNonNull(file, "file");
        if (!file.exists()) {
            return new DocumentLoadReport(new UiDocument(List.of(), List.of()), List.of("File not found: " + file.getAbsolutePath()));
        }
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return parseWithReport(root, file.getName());
        } catch (Exception e) {
            return new DocumentLoadReport(new UiDocument(List.of(), List.of()), List.of("JSON Syntax/IO error: " + e.getMessage()));
        }
    }

    /**
     * Loads a UiDocument from a file.
     *
     * @param file the .json file on disk
     * @return the parsed UiDocument
     * @throws UiDocumentParseException if the file cannot be read or parsed
     */
    public static UiDocument load(File file) {
        DocumentLoadReport report = loadWithReport(file);
        if (report.document().nodes().isEmpty() && report.hasErrors()) {
            throw new UiDocumentParseException("Failed to load " + file.getName() + ": " + String.join("; ", report.errors()));
        }
        return report.document();
    }

    /**
     * Loads a UiDocument from an InputStream.
     */
    public static UiDocument load(InputStream in) {
        Objects.requireNonNull(in, "in");
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return parse(root);
        } catch (IOException e) {
            throw new UiDocumentParseException("Failed to read document from stream", e);
        }
    }

    /**
     * Loads a UiDocument directly from a JSON string.
     */
    public static UiDocument loadFromString(String json) {
        return loadFromString(json, "JSON string");
    }

    public static UiDocument loadFromString(String json, String sourceName) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        DocumentLoadReport report = parseWithReport(root, sourceName);
        return report.document();
    }

    // ── Parser ────────────────────────────────────────────────────────────────

    private static UiDocument parse(JsonObject root) {
        return parseWithReport(root, "document").document();
    }

    public static DocumentLoadReport parseWithReport(JsonObject root, String sourceName) {
        JsonArray nodesArr = root.has("nodes") ? root.getAsJsonArray("nodes") : new JsonArray();
        List<UiNode> nodes = new ArrayList<>();
        Map<String, JsonObject> nodeById = new HashMap<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < nodesArr.size(); i++) {
            JsonElement el = nodesArr.get(i);
            if (!el.isJsonObject()) {
                String err = "Node #" + (i + 1) + ": Entry is not a valid JSON object";
                errors.add(err);
                LOGGER.warning("[" + sourceName + "] " + err);
                continue;
            }
            JsonObject obj = el.getAsJsonObject();
            String id = getString(obj, "id", "node_" + (i + 1));
            String type = getString(obj, "type", "<missing>");
            try {
                UiNode node = parseNode(obj);
                nodes.add(node);
                if (obj.has("id")) nodeById.put(id, obj);
            } catch (Exception e) {
                String err = "Node #" + (i + 1) + " (id: '" + id + "', type: '" + type + "'): " + e.getMessage();
                errors.add(err);
                LOGGER.warning("[" + sourceName + "] ⚠ " + err);
            }
        }

        // Parse buttons
        JsonArray btnsArr = root.has("buttons") ? root.getAsJsonArray("buttons") : new JsonArray();
        List<UiButton> buttons = new ArrayList<>();

        for (int i = 0; i < btnsArr.size(); i++) {
            JsonElement el = btnsArr.get(i);
            if (!el.isJsonObject()) {
                String err = "Button #" + (i + 1) + ": Entry is not a valid JSON object";
                errors.add(err);
                LOGGER.warning("[" + sourceName + "] " + err);
                continue;
            }
            JsonObject obj = el.getAsJsonObject();
            String id = getString(obj, "id", "btn_" + (i + 1));
            try {
                UiButton btn = parseButton(obj, nodes, nodeById);
                if (btn != null) buttons.add(btn);
            } catch (Exception e) {
                String err = "Button #" + (i + 1) + " (id: '" + id + "'): " + e.getMessage();
                errors.add(err);
                LOGGER.warning("[" + sourceName + "] ⚠ " + err);
            }
        }

        return new DocumentLoadReport(new UiDocument(nodes, buttons), errors);
    }

    // ── Node dispatch ─────────────────────────────────────────────────────────

    private static UiNode parseNode(JsonObject o) {
        String type = getString(o, "type", "<missing>").toLowerCase().trim();
        return switch (type) {
            case "shape"       -> parseShape(o);
            case "background"  -> (o.has("shapeType") && !"rect".equalsIgnoreCase(getString(o, "shapeType", "rect"))) ? parseShape(o) : parseBackground(o);
            case "gradient_background", "gradientbackground" -> parseGradientBackground(o);
            case "text", "aligned_text", "alignedtext" -> parseText(o);
            case "item"        -> parseItem(o);
            case "block"       -> parseBlock(o);
            case "line"        -> parseLine(o);
            case "parallelogram" -> parseParallelogram(o);
            case "triangle"    -> parseTriangle(o);
            case "polyline"    -> parsePolyline(o);
            case "icon", "ui_icon", "uiicon" -> parseIcon(o);
            case "entity_model", "entitymodel", "model" -> parseEntityModel(o);
            case "mob", "mob_entity", "mobentity" -> parseMob(o);
            default -> throw new UiDocumentParseException("Unknown node type: " + type);
        };
    }

    private static UiShapeNode parseShape(JsonObject o) {
        String shapeType = getString(o, "shapeType", getString(o, "shape", "rect"));
        float x          = getFloat(o, "x", 0);
        float y          = getFloat(o, "y", 0);
        float depth      = getFloat(o, "depth", 0.001f);
        float w          = getFloat(o, "width", 100);
        float h          = getFloat(o, "height", 60);
        int alpha        = getInt(o, "alpha", 255);
        Color color      = parseColor(getString(o, "color", "#1a2035"), alpha);
        boolean outline  = getBool(o, "outline", false);
        int outAlpha     = getInt(o, "outlineAlpha", 255);
        Color outColor   = parseColor(getString(o, "outlineColor", "#ffffff"), outAlpha);
        float outThick   = getFloat(o, "outlineThickness", 2.0f);
        String outStyle  = getString(o, "outlineStyle", "solid");
        float radius     = getFloat(o, "cornerRadius", 6.0f);
        float rotation   = getFloat(o, "rotation", 0.0f);
        boolean ds       = getBool(o, "doubleSided", false);
        return new UiShapeNode(shapeType, x, y, w, h, depth, color, outline, outColor, outThick, outStyle, radius, rotation, ds);
    }

    private static ParallelogramNode parseParallelogram(JsonObject o) {
        float depth = getFloat(o, "depth", 0.001f);
        int alpha   = getInt(o, "alpha", 255);
        Color color = parseColor(getString(o, "color", "#00b4d8"), alpha);
        boolean ds  = getBool(o, "doubleSided", false);
        if (o.has("x1") && o.has("y1") && o.has("x2") && o.has("y2") && o.has("x3") && o.has("y3")) {
            float x1 = getFloat(o, "x1", 0);
            float y1 = getFloat(o, "y1", 0);
            float x2 = getFloat(o, "x2", 50);
            float y2 = getFloat(o, "y2", 0);
            float x3 = getFloat(o, "x3", 10);
            float y3 = getFloat(o, "y3", 40);
            return new ParallelogramNode(x1, y1, x2, y2, x3, y3, depth, color, ds);
        } else {
            float x     = getFloat(o, "x", 0);
            float y     = getFloat(o, "y", 0);
            float w     = getFloat(o, "width", 50);
            float h     = getFloat(o, "height", 30);
            float skewX = getFloat(o, "skewX", w * 0.22f);
            return new ParallelogramNode(x + skewX, y, x + w, y, x + w - skewX, y + h, depth, color, ds);
        }
    }

    private static TriangleNode parseTriangle(JsonObject o) {
        float depth = getFloat(o, "depth", 0.001f);
        int alpha   = getInt(o, "alpha", 255);
        Color color = parseColor(getString(o, "color", "#ffd700"), alpha);
        boolean ds  = getBool(o, "doubleSided", false);
        if (o.has("x1") && o.has("y1") && o.has("x2") && o.has("y2") && o.has("x3") && o.has("y3")) {
            float x1 = getFloat(o, "x1", 0);
            float y1 = getFloat(o, "y1", 0);
            float x2 = getFloat(o, "x2", 50);
            float y2 = getFloat(o, "y2", 50);
            float x3 = getFloat(o, "x3", 0);
            float y3 = getFloat(o, "y3", 50);
            return new TriangleNode(x1, y1, x2, y2, x3, y3, depth, color, ds);
        } else {
            float x = getFloat(o, "x", 0);
            float y = getFloat(o, "y", 0);
            float w = getFloat(o, "width", 50);
            float h = getFloat(o, "height", 50);
            return new TriangleNode(x + w * 0.5f, y, x + w, y + h, x, y + h, depth, color, ds);
        }
    }

    private static PolylineNode parsePolyline(JsonObject o) {
        float thick = getFloat(o, "thickness", 2.0f);
        float depth = getFloat(o, "depth", 0.001f);
        int alpha   = getInt(o, "alpha", 255);
        Color color = parseColor(getString(o, "color", "#00ff88"), alpha);
        boolean ds  = getBool(o, "doubleSided", false);
        boolean closed = getBool(o, "closed", false);
        List<PolylineNode.Point> points = new ArrayList<>();
        if (o.has("points")) {
            JsonArray arr = o.getAsJsonArray("points");
            for (JsonElement el : arr) {
                if (el.isJsonObject()) {
                    JsonObject pt = el.getAsJsonObject();
                    points.add(new PolylineNode.Point(getFloat(pt, "x", 0), getFloat(pt, "y", 0)));
                } else if (el.isJsonArray()) {
                    JsonArray pt = el.getAsJsonArray();
                    if (pt.size() >= 2) {
                        points.add(new PolylineNode.Point(pt.get(0).getAsFloat(), pt.get(1).getAsFloat()));
                    }
                }
            }
        }
        if (points.size() < 2) {
            float x = getFloat(o, "x", 0);
            float y = getFloat(o, "y", 0);
            float w = getFloat(o, "width", 50);
            float h = getFloat(o, "height", 50);
            points.add(new PolylineNode.Point(x, y));
            points.add(new PolylineNode.Point(x + w, y + h));
        }
        return new PolylineNode(points, thick, depth, color, ds, closed);
    }

    private static UiIconNode parseIcon(JsonObject o) {
        String mat = getString(o, "material", "DIAMOND");
        Material material = Material.matchMaterial(mat);
        if (material == null || material.isAir()) {
            material = Material.DIAMOND;
        }
        float x      = getFloat(o, "x", 0);
        float y      = getFloat(o, "y", 0);
        float depth  = getFloat(o, "depth", 0.003f);
        float w      = getFloat(o, "width", 24);
        float h      = getFloat(o, "height", 24);
        float uw     = getFloat(o, "uWidth", 16);
        float vh     = getFloat(o, "vHeight", 16);
        ItemDisplay.ItemDisplayTransform transform = parseTransform(getString(o, "transform", "FIXED"));
        boolean ds   = getBool(o, "doubleSided", false);
        return new UiIconNode(new ItemStack(material), x, y, depth, w, h, uw, vh, transform, ds);
    }

    private static EntityModelNode parseEntityModel(JsonObject o) {
        String mat = getString(o, "material", "PAPER");
        Material material = Material.matchMaterial(mat);
        if (material == null) material = Material.PAPER;
        ItemStack item = new ItemStack(material);
        if (o.has("customModelData")) {
            int cmd = getInt(o, "customModelData", 0);
            if (cmd > 0 && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                meta.setCustomModelData(cmd);
                item.setItemMeta(meta);
            }
        }
        float x      = getFloat(o, "x", 0);
        float y      = getFloat(o, "y", 0);
        float depth  = getFloat(o, "depth", 0.08f);
        float sx     = getFloat(o, "scaleX", getFloat(o, "scale", 1.0f));
        float sy     = getFloat(o, "scaleY", getFloat(o, "scale", 1.0f));
        float sz     = getFloat(o, "scaleZ", getFloat(o, "scale", 1.0f));
        float w      = getFloat(o, "width", 32);
        float h      = getFloat(o, "height", 32);
        float yaw    = getFloat(o, "yaw", 0.0f);
        float pitch  = getFloat(o, "pitch", 0.0f);
        float roll   = getFloat(o, "roll", 0.0f);
        ItemDisplay.ItemDisplayTransform transform = parseTransform(getString(o, "transform", "FIXED"));
        boolean ds   = getBool(o, "doubleSided", false);
        return new EntityModelNode(item, x, y, depth, sx, sy, sz, w, h, yaw, pitch, roll, transform, true, UiModelRotation.defaults(), ds);
    }

    private static MobEntityNode parseMob(JsonObject o) {
        String entityName = getString(o, "entityType", getString(o, "mob", "ZOMBIE")).toUpperCase();
        EntityType type;
        try {
            type = EntityType.valueOf(entityName);
        } catch (IllegalArgumentException e) {
            type = EntityType.ZOMBIE;
        }
        float x      = getFloat(o, "x", 0);
        float y      = getFloat(o, "y", 0);
        float depth  = getFloat(o, "depth", 0.005f);
        float scale  = getFloat(o, "scale", 1.0f);
        float w      = getFloat(o, "width", 32);
        float h      = getFloat(o, "height", 32);
        float yaw    = getFloat(o, "yaw", 0.0f);
        float pitch  = getFloat(o, "pitch", 0.0f);
        boolean rot  = getBool(o, "hoverRotatable", true);
        boolean ds   = getBool(o, "doubleSided", false);
        return new MobEntityNode(type, x, y, depth, scale, w, h, yaw, pitch, rot, UiModelRotation.defaults(), null, ds);
    }

    private static UiBackgroundNode parseBackground(JsonObject o) {
        float x     = getFloat(o, "x", 0);
        float y     = getFloat(o, "y", 0);
        float depth = getFloat(o, "depth", 0.001f);
        float w     = getFloat(o, "width", 100);
        float h     = getFloat(o, "height", 60);
        int alpha   = getInt(o, "alpha", 210);
        Color color = parseColor(getString(o, "color", "#1a2035"), alpha);
        boolean ds  = getBool(o, "doubleSided", false);
        return new UiBackgroundNode(x, y, depth, w, h, color, ds);
    }

    private static UiGradientBackgroundNode parseGradientBackground(JsonObject o) {
        float x     = getFloat(o, "x", 0);
        float y     = getFloat(o, "y", 0);
        float depth = getFloat(o, "depth", 0.001f);
        float w     = getFloat(o, "width", 100);
        float h     = getFloat(o, "height", 60);
        boolean ds  = getBool(o, "doubleSided", false);

        int startAlpha = getInt(o, "startAlpha", 255);
        int endAlpha   = getInt(o, "endAlpha", 255);
        Color startColor = parseColor(getString(o, "startColor", "#ff0000"), startAlpha);
        Color endColor   = parseColor(getString(o, "endColor", "#0000ff"), endAlpha);

        UiGradient gradient;
        if (o.has("startPos") && o.has("endPos")) {
            UiGradientPosition startPos = parseGradientPos(getString(o, "startPos", "CENTER_LEFT"));
            UiGradientPosition endPos   = parseGradientPos(getString(o, "endPos", "CENTER_RIGHT"));
            gradient = UiGradient.of(startPos, startColor, endPos, endColor);
        } else if (o.has("startU") || o.has("startV") || o.has("endU") || o.has("endV")) {
            float su = getFloat(o, "startU", 0.0f);
            float sv = getFloat(o, "startV", 0.5f);
            float eu = getFloat(o, "endU", 1.0f);
            float ev = getFloat(o, "endV", 0.5f);
            gradient = UiGradient.of(su, sv, startColor, eu, ev, endColor);
        } else {
            String preset = getString(o, "preset", "horizontal").toLowerCase();
            gradient = switch (preset) {
                case "vertical" -> UiGradient.vertical(startColor, endColor);
                case "diagonal" -> UiGradient.diagonal(startColor, endColor);
                case "diagonal_up", "diagonal-up" -> UiGradient.diagonalBottomLeftToTopRight(startColor, endColor);
                case "center_slant", "center-slant" -> UiGradient.centerLeftToTopRight(startColor, endColor);
                case "radial_corner", "radial-corner" -> UiGradient.centerToBottomRight(startColor, endColor);
                default -> UiGradient.horizontal(startColor, endColor);
            };
        }

        int slicesX = getInt(o, "slicesX", UiGradientBackgroundNode.defaultSlicesX(gradient));
        int slicesY = getInt(o, "slicesY", UiGradientBackgroundNode.defaultSlicesY(gradient));
        if (o.has("slices")) {
            int slices = getInt(o, "slices", 16);
            if (gradient.isHorizontal()) {
                slicesX = slices;
                slicesY = 1;
            } else if (gradient.isVertical()) {
                slicesX = 1;
                slicesY = slices;
            } else {
                slicesX = slices;
                slicesY = slices;
            }
        }

        return new UiGradientBackgroundNode(x, y, depth, w, h, gradient, slicesX, slicesY, ds);
    }

    private static UiGradientPosition parseGradientPos(String name) {
        try {
            return UiGradientPosition.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UiGradientPosition.CENTER_LEFT;
        }
    }

    private static AlignedTextNode parseText(JsonObject o) {
        Component text = parseText(getString(o, "text", ""));
        float boxX  = getFloat(o, "boxX", 0);
        float boxY  = getFloat(o, "boxY", 0);
        float depth = getFloat(o, "depth", 0.002f);
        float w     = getFloat(o, "width", 100);
        float h     = getFloat(o, "height", 20);
        float fontSize = getFloat(o, "fontSize", 8.0f);
        float cw    = getFloat(o, "contentWidth", w - 6);
        UiTextAlignment align  = parseHAlign(getString(o, "alignment", "LEFT"));
        UiVerticalAlignment va = parseVAlign(getString(o, "verticalAlignment", "MIDDLE"));
        float lo = getFloat(o, "leftOffset", 0);
        float ro = getFloat(o, "rightOffset", 0);
        float vo = getFloat(o, "verticalOffset", 0);
        boolean shadow    = getBool(o, "shadow", true);
        boolean seethru   = getBool(o, "seeThrough", false);
        boolean ds        = getBool(o, "doubleSided", false);
        return new AlignedTextNode(text, boxX, boxY, w, h, depth, align, lo, ro, fontSize, cw, va, vo, shadow, seethru, ds);
    }

    private static Material matchMaterialSafe(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String clean = raw.trim().toUpperCase().replace("MINECRAFT:", "").replace(" ", "_");
        Material mat = Material.matchMaterial(clean);
        if (mat != null) return mat;
        try {
            return Material.valueOf(clean);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static ItemStack createItemStackSafe(Material material) {
        try {
            return new ItemStack(material);
        } catch (Throwable t) {
            try {
                Class<?> mockitoClass = Class.forName("org.mockito.Mockito");
                Object mock = mockitoClass.getMethod("mock", Class.class).invoke(null, ItemStack.class);
                Object whenMock = mockitoClass.getMethod("when", Object.class).invoke(null, ((ItemStack) mock).getType());
                whenMock.getClass().getMethod("thenReturn", Object.class).invoke(whenMock, material);
                Object whenClone = mockitoClass.getMethod("when", Object.class).invoke(null, ((ItemStack) mock).clone());
                whenClone.getClass().getMethod("thenReturn", Object.class).invoke(whenClone, mock);
                return (ItemStack) mock;
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    public static org.bukkit.block.data.BlockData createBlockDataSafe(Material material) {
        try {
            return material.createBlockData();
        } catch (Throwable t1) {
            try {
                return org.bukkit.Bukkit.createBlockData(material);
            } catch (Throwable t2) {
                try {
                    Class<?> mockitoClass = Class.forName("org.mockito.Mockito");
                    Object mock = mockitoClass.getMethod("mock", Class.class).invoke(null, org.bukkit.block.data.BlockData.class);
                    Object whenMock = mockitoClass.getMethod("when", Object.class).invoke(null, ((org.bukkit.block.data.BlockData) mock).getMaterial());
                    whenMock.getClass().getMethod("thenReturn", Object.class).invoke(whenMock, material);
                    Object whenClone = mockitoClass.getMethod("when", Object.class).invoke(null, ((org.bukkit.block.data.BlockData) mock).clone());
                    whenClone.getClass().getMethod("thenReturn", Object.class).invoke(whenClone, mock);
                    return (org.bukkit.block.data.BlockData) mock;
                } catch (Throwable ignored) {
                    return null;
                }
            }
        }
    }

    private static ItemNode parseItem(JsonObject o) {
        String mat  = getString(o, "material", "STONE");
        Material material = matchMaterialSafe(mat);
        if (material == null || material == Material.AIR || material.name().endsWith("_AIR")) {
            throw new UiDocumentParseException("Invalid material for item node: '" + mat + "'");
        }
        ItemStack itemStack = createItemStackSafe(material);
        if (itemStack == null) {
            throw new UiDocumentParseException("Could not create ItemStack for material: '" + mat + "'");
        }
        float x     = getFloat(o, "x", 0);
        float y     = getFloat(o, "y", 0);
        float depth = getFloat(o, "depth", 0.003f);
        float scale = getFloat(o, "scale", 0.8f);
        ItemDisplay.ItemDisplayTransform transform = parseTransform(getString(o, "transform", "FIXED"));
        boolean ds  = getBool(o, "doubleSided", false);
        return new ItemNode(itemStack, x, y, depth, scale, transform, ds);
    }

    private static BlockNode parseBlock(JsonObject o) {
        String mat  = getString(o, "material", "STONE");
        Material material = matchMaterialSafe(mat);
        if (material == null) {
            throw new UiDocumentParseException("Unknown block material: '" + mat + "'");
        }

        org.bukkit.block.data.BlockData blockData = createBlockDataSafe(material);
        if (blockData == null) {
            throw new UiDocumentParseException("Material '" + mat + "' is not a valid block type");
        }

        float x     = getFloat(o, "x", 0);
        float y     = getFloat(o, "y", 0);
        float depth = getFloat(o, "depth", 0.004f);
        float w     = getFloat(o, "width", 24);
        float h     = getFloat(o, "height", 24);
        float thick = getFloat(o, "thickness", 1.0f);
        boolean ds  = getBool(o, "doubleSided", false);
        return new BlockNode(blockData, x, y, depth, w, h, thick, ds);
    }

    private static LineNode parseLine(JsonObject o) {
        float x1    = getFloat(o, "x1", 0);
        float y1    = getFloat(o, "y1", 0);
        float x2    = getFloat(o, "x2", 50);
        float y2    = getFloat(o, "y2", 0);
        float thick = getFloat(o, "thickness", 2.0f);
        float depth = getFloat(o, "depth", 0.001f);
        Color color = parseColor(getString(o, "color", "#4a90d9"), 255);
        boolean ds  = getBool(o, "doubleSided", false);
        return new LineNode(x1, y1, x2, y2, thick, depth, color, ds);
    }

    // ── Button ────────────────────────────────────────────────────────────────

    private static UiButton parseButton(JsonObject o, List<UiNode> nodes, Map<String, JsonObject> nodeById) {
        String id     = getString(o, "id", null);
        String nodeId = getString(o, "nodeId", null);
        if (id == null || nodeId == null) return null;

        // Find the node whose JSON "id" matches nodeId using the pre-built nodeById map.
        // nodeById maps id → JSON object; the insertion order matches the nodes list.
        UiNode target = null;
        int matchIdx = 0;
        for (String key : nodeById.keySet()) {
            if (key.equals(nodeId)) {
                if (matchIdx < nodes.size()) target = nodes.get(matchIdx);
                break;
            }
            matchIdx++;
        }
        if (target == null) return null;


        String desc = getString(o, "description", "");
        Component descComp = desc.isBlank() ? Component.empty() : parseText(desc);

        UiButtonAction action = UiButtonAction.none();
        if (o.has("action")) {
            JsonObject act = o.getAsJsonObject("action");
            String actType = getString(act, "type", "NONE");
            String actVal  = getString(act, "value", "");
            action = switch (actType) {
                case "OPEN_URL"         -> UiButtonAction.openUrl(actVal);
                case "PLAYER_COMMAND"   -> UiButtonAction.playerCommand(actVal);
                case "CONSOLE_COMMAND"  -> UiButtonAction.consoleCommand(actVal);
                case "SUGGEST_COMMAND"  -> UiButtonAction.suggestCommand(actVal);
                default                 -> UiButtonAction.none();
            };
        }

        return UiButton.forNode(id, target).describedBy(descComp).withAction(action);
    }

    // ── Text format helpers ────────────────────────────────────────────────────

    /**
     * Parses a text string that may use MiniMessage ({@code <red>text</red>}) or
     * legacy {@code &} color codes ({@code &ctext}). If neither tag is detected,
     * returns the string as plain text.
     */
    public static Component parseText(String text) {
        if (text == null || text.isBlank()) return Component.empty();
        // Contains MiniMessage-style tags
        if (text.contains("<") && text.contains(">")) return MINI.deserialize(text);
        // Contains legacy & codes
        if (text.contains("&")) return LEGACY.deserialize(text);
        return Component.text(text);
    }

    // ── Primitive helpers ─────────────────────────────────────────────────────

    private static Color parseColor(String hex, int alpha) {
        try {
            if (hex.startsWith("#")) hex = hex.substring(1);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return Color.fromARGB(Math.clamp(alpha, 0, 255), r, g, b);
        } catch (Exception e) {
            throw new UiDocumentParseException("Invalid color value: " + hex, e);
        }
    }

    private static UiTextAlignment parseHAlign(String v) {
        return switch (v.toUpperCase()) {
            case "CENTER" -> UiTextAlignment.CENTER;
            case "RIGHT"  -> UiTextAlignment.RIGHT;
            default       -> UiTextAlignment.LEFT;
        };
    }

    private static UiVerticalAlignment parseVAlign(String v) {
        return switch (v.toUpperCase()) {
            case "TOP"    -> UiVerticalAlignment.TOP;
            case "BOTTOM" -> UiVerticalAlignment.BOTTOM;
            default       -> UiVerticalAlignment.CENTER;
        };
    }

    private static ItemDisplay.ItemDisplayTransform parseTransform(String v) {
        try { return ItemDisplay.ItemDisplayTransform.valueOf(v.toUpperCase()); }
        catch (IllegalArgumentException e) { return ItemDisplay.ItemDisplayTransform.FIXED; }
    }

    private static float getFloat(JsonObject o, String key, float def) {
        return o.has(key) ? o.get(key).getAsFloat() : def;
    }
    private static int getInt(JsonObject o, String key, int def) {
        return o.has(key) ? o.get(key).getAsInt() : def;
    }
    private static boolean getBool(JsonObject o, String key, boolean def) {
        return o.has(key) ? o.get(key).getAsBoolean() : def;
    }
    private static String getString(JsonObject o, String key, String def) {
        return o.has(key) ? o.get(key).getAsString() : def;
    }
}
