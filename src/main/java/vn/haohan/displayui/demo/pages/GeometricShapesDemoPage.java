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
package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.UiShapeNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

import java.util.List;

/**
 * Interactive showcase displaying all 2D shapes with global master outline toggle,
 * selectable line styles (solid / dashed / dotted), and thickness adjustments.
 */
public final class GeometricShapesDemoPage extends BaseDemoPage {

    private record ShapeShowcase(String type, String label, Color color, float rotation, float cornerRadius) {}

    private static final List<ShapeShowcase> SHOWCASE_SHAPES = List.of(
            // Row 0
            new ShapeShowcase("rect", "Rectangle", Color.fromRGB(239, 71, 111), 0f, 0f),
            new ShapeShowcase("rounded_rect", "Round Rect", Color.fromRGB(247, 140, 107), 0f, 4f),
            new ShapeShowcase("circle", "Circle", Color.fromRGB(255, 209, 102), 0f, 0f),
            new ShapeShowcase("diamond", "Diamond", Color.fromRGB(6, 214, 160), 0f, 0f),
            new ShapeShowcase("trapezoid", "Trapezoid", Color.fromRGB(17, 138, 178), 0f, 0f),
            new ShapeShowcase("parallelogram", "Slanted", Color.fromRGB(7, 59, 76), 0f, 0f),

            // Row 1
            new ShapeShowcase("triangle", "Triangle", Color.fromRGB(131, 56, 236), 0f, 0f),
            new ShapeShowcase("right_triangle", "Right Tri", Color.fromRGB(255, 0, 110), 0f, 0f),
            new ShapeShowcase("pentagon", "Pentagon", Color.fromRGB(58, 134, 255), 0f, 0f),
            new ShapeShowcase("hexagon", "Hexagon", Color.fromRGB(0, 180, 216), 0f, 0f),
            new ShapeShowcase("heptagon", "Heptagon", Color.fromRGB(72, 202, 228), 0f, 0f),
            new ShapeShowcase("octagon", "Octagon", Color.fromRGB(144, 224, 239), 0f, 0f),

            // Row 2
            new ShapeShowcase("star3", "Star 3", Color.fromRGB(255, 183, 3), 0f, 0f),
            new ShapeShowcase("star4", "Star 4", Color.fromRGB(251, 133, 0), 0f, 0f),
            new ShapeShowcase("star5", "Star 5", Color.fromRGB(255, 214, 10), 0f, 0f),
            new ShapeShowcase("star6", "Star 6", Color.fromRGB(244, 140, 6), 0f, 0f),
            new ShapeShowcase("arrow_right", "Arrow R", Color.fromRGB(167, 201, 87), 0f, 0f),
            new ShapeShowcase("arrow_left", "Arrow L", Color.fromRGB(56, 176, 0), 0f, 0f),

            // Row 3
            new ShapeShowcase("chevron_right", "Chevron", Color.fromRGB(0, 114, 0), 0f, 0f),
            new ShapeShowcase("double_arrow", "Double Arr", Color.fromRGB(114, 9, 183), 0f, 0f),
            new ShapeShowcase("cross", "Cross", Color.fromRGB(247, 37, 133), 0f, 0f),
            new ShapeShowcase("heart", "Heart", Color.fromRGB(230, 57, 70), 0f, 0f),
            new ShapeShowcase("speech_bubble", "Bubble", Color.fromRGB(76, 201, 240), 0f, 0f),
            new ShapeShowcase("lightning", "Lightning", Color.fromRGB(255, 220, 0), 0f, 0f)
    );

    @Override
    public String title() {
        return "ALL SHAPES & STYLES";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        // --- TOP MASTER CONTROLS (Y = -39 to -27) ---
        String outlineLabel = context.shapeOutline() ? "OUTLINE: ON" : "OUTLINE: OFF";
        addControlButton(builder, "shape_outline_toggle", -86, -39, 52, outlineLabel, "Toggle outline for all shapes");

        String styleLabel = "STYLE: " + context.lineStyle().toUpperCase();
        addControlButton(builder, "shape_style_cycle", -28, -39, 56, styleLabel, "Cycle outline style (solid / dashed / dotted)");

        String thickLabel = "THICK: " + (int) context.lineThickness() + "px";
        addControlButton(builder, "shape_thick_cycle", 32, -39, 54, thickLabel, "Cycle line thickness (1px / 2px / 3px / 4px)");

        // --- SHAPE GRID (6 columns x 4 rows, Y from -24.5 to +36, comfortably above footer at +48) ---
        float startX = -86.0f;
        float startY = -24.5f;
        float cellW = 26.0f;
        float cellH = 9.5f;
        float gapX = 3.0f;
        float rowStep = 15.0f;

        for (int i = 0; i < SHOWCASE_SHAPES.size(); i++) {
            int col = i % 6;
            int row = i / 6;

            float x = startX + col * (cellW + gapX);
            float y = startY + row * rowStep;

            ShapeShowcase shape = SHOWCASE_SHAPES.get(i);

            // Shape Node
            UiShapeNode node = UiShapeNode.builder(shape.type(), x + 2.0f, y, cellW - 4.0f, cellH)
                    .color(shape.color())
                    .outline(context.shapeOutline())
                    .outlineColor(Color.fromRGB(255, 255, 255))
                    .outlineThickness(context.lineThickness())
                    .outlineStyle(context.lineStyle())
                    .rotation(shape.rotation())
                    .cornerRadius(shape.cornerRadius())
                    .depth(0.002f)
                    .doubleSided(context.doubleSided())
                    .build();
            builder.add(node);

            // Label underneath
            builder.add(new AlignedTextNode(
                    Component.text(shape.label(), NamedTextColor.GRAY),
                    x, y + cellH + 0.5f, cellW, 3.5f, UiTextAlignment.CENTER)
                    .fontSize(2.8f).atDepth(0.003f));
        }
    }

    @Override
    public boolean onClick(DemoContext context, String buttonId, Player player) {
        switch (buttonId) {
            case "shape_outline_toggle" -> {
                context.shapeOutline(!context.shapeOutline());
                context.updateView();
                return true;
            }
            case "shape_style_cycle" -> {
                String current = context.lineStyle().toLowerCase();
                String next = switch (current) {
                    case "solid" -> "dashed";
                    case "dashed" -> "dotted";
                    default -> "solid";
                };
                context.lineStyle(next);
                context.updateView();
                return true;
            }
            case "shape_thick_cycle" -> {
                float current = context.lineThickness();
                float next = current >= 4.0f ? 1.0f : current + 1.0f;
                context.lineThickness(next);
                context.updateView();
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
