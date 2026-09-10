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
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.ItemNode;
import vn.haohan.displayui.api.node.LineNode;
import vn.haohan.displayui.api.node.UiBackgroundNode;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.text.UiVerticalAlignment;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder().character('&').hexColors().build();

    private UiDocumentLoader() {}

    /**
     * Loads a UiDocument from a JSON file.
     *
     * @param file the {@code .hhdui.json} file to read
     * @return the parsed UiDocument
     * @throws IOException              if the file cannot be read
     * @throws UiDocumentParseException if the JSON structure is invalid or a node type is unknown
     */
    public static UiDocument load(File file) throws IOException {
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return parse(root);
        }
    }

    /**
     * Loads a UiDocument directly from a JSON string.
     *
     * @param json the raw JSON content
     * @return the parsed UiDocument
     * @throws UiDocumentParseException if the JSON structure is invalid
     */
    public static UiDocument loadFromString(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return parse(root);
    }

    // ── Parser ────────────────────────────────────────────────────────────────

    private static UiDocument parse(JsonObject root) {
        // Parse nodes
        JsonArray nodesArr = root.has("nodes") ? root.getAsJsonArray("nodes") : new JsonArray();
        List<UiNode> nodes = new ArrayList<>();
        Map<String, JsonObject> nodeById = new HashMap<>();

        for (JsonElement el : nodesArr) {
            JsonObject obj = el.getAsJsonObject();
            UiNode node = parseNode(obj);
            nodes.add(node);
            // Track id for button linkage
            if (obj.has("id")) nodeById.put(obj.get("id").getAsString(), obj);
        }

        // Parse buttons
        JsonArray btnsArr = root.has("buttons") ? root.getAsJsonArray("buttons") : new JsonArray();
        List<UiButton> buttons = new ArrayList<>();

        for (JsonElement el : btnsArr) {
            JsonObject obj = el.getAsJsonObject();
            UiButton btn = parseButton(obj, nodes, nodeById);
            if (btn != null) buttons.add(btn);
        }

        return new UiDocument(nodes, buttons);
    }

    // ── Node dispatch ─────────────────────────────────────────────────────────

    private static UiNode parseNode(JsonObject o) {
        String type = getString(o, "type", "<missing>");
        return switch (type) {
            case "background"  -> parseBackground(o);
            case "text"        -> parseText(o);
            case "item"        -> parseItem(o);
            case "block"       -> parseBlock(o);
            case "line"        -> parseLine(o);
            default -> throw new UiDocumentParseException("Unknown node type: " + type);
        };
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

    private static ItemNode parseItem(JsonObject o) {
        String mat  = getString(o, "material", "STONE");
        Material material = Material.matchMaterial(mat);
        if (material == null || material.isAir())
            throw new UiDocumentParseException("Invalid material for item node: " + mat);
        float x     = getFloat(o, "x", 0);
        float y     = getFloat(o, "y", 0);
        float depth = getFloat(o, "depth", 0.003f);
        float scale = getFloat(o, "scale", 0.8f);
        ItemDisplay.ItemDisplayTransform transform = parseTransform(getString(o, "transform", "FIXED"));
        boolean ds  = getBool(o, "doubleSided", false);
        return new ItemNode(new ItemStack(material), x, y, depth, scale, transform, ds);
    }

    private static BlockNode parseBlock(JsonObject o) {
        String mat  = getString(o, "material", "STONE");
        Material material = Material.matchMaterial(mat);
        if (material == null || !material.isBlock())
            throw new UiDocumentParseException("Invalid block material: " + mat);
        float x     = getFloat(o, "x", 0);
        float y     = getFloat(o, "y", 0);
        float depth = getFloat(o, "depth", 0.004f);
        float w     = getFloat(o, "width", 24);
        float h     = getFloat(o, "height", 24);
        float thick = getFloat(o, "thickness", 1.0f);
        boolean ds  = getBool(o, "doubleSided", false);
        return new BlockNode(material.createBlockData(), x, y, depth, w, h, thick, ds);
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
