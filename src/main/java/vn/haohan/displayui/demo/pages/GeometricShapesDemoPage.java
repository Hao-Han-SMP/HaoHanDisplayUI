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
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.LineNode;
import vn.haohan.displayui.api.node.ParallelogramNode;
import vn.haohan.displayui.api.node.PolylineNode;
import vn.haohan.displayui.api.node.TriangleNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class GeometricShapesDemoPage extends BaseDemoPage {
    @Override
    public String title() {
        return "GEOMETRIC SHAPES";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        // --- SECTION 1: TRIANGLE MESH & 2D DECOMPOSITION (Left Column: X [-86, -30]) ---
        builder.add(new AlignedTextNode(
                Component.text("TRIANGLE MESH", NamedTextColor.GOLD, TextDecoration.BOLD),
                -86, -36, 56, 8, UiTextAlignment.CENTER).fontSize(4));

        // Triforce / Gem Triangles
        float triCenterX = -58;
        float triCenterY = -14;
        // Top triangle (Golden Yellow)
        builder.add(new TriangleNode(
                triCenterX, triCenterY - 14,
                triCenterX - 10, triCenterY,
                triCenterX + 10, triCenterY,
                0.002f, Color.fromRGB(255, 215, 0), true));
        // Bottom-left triangle (Orange)
        builder.add(new TriangleNode(
                triCenterX - 10, triCenterY,
                triCenterX - 20, triCenterY + 14,
                triCenterX, triCenterY + 14,
                0.002f, Color.fromRGB(255, 140, 0), true));
        // Bottom-right triangle (Amber)
        builder.add(new TriangleNode(
                triCenterX + 10, triCenterY,
                triCenterX, triCenterY + 14,
                triCenterX + 20, triCenterY + 14,
                0.002f, Color.fromRGB(255, 180, 0), true));

        // Inverted inner accent
        builder.add(new TriangleNode(
                triCenterX, triCenterY + 14,
                triCenterX - 10, triCenterY,
                triCenterX + 10, triCenterY,
                0.003f, Color.fromRGB(50, 50, 60), true));

        builder.add(new AlignedTextNode(
                Component.text("3-Piece Triangle", NamedTextColor.GRAY),
                -86, 6, 56, 8, UiTextAlignment.CENTER).fontSize(4));
        builder.add(new AlignedTextNode(
                Component.text("Exact 2D Shearing", NamedTextColor.DARK_GRAY),
                -86, 15, 56, 8, UiTextAlignment.CENTER).fontSize(3));

        // --- SECTION 2: SLANTED QUADS & ROLLED BEAMS (Center Column: X [-24, 26]) ---
        builder.add(new AlignedTextNode(
                Component.text("SLANTED & ROLLED", NamedTextColor.AQUA, TextDecoration.BOLD),
                -24, -36, 52, 8, UiTextAlignment.CENTER).fontSize(4));

        // Cyberpunk style slanted cards
        builder.add(ParallelogramNode.slanted(-22, -26, 48, 12, 6, Color.fromRGB(0, 180, 216), true));
        builder.add(new AlignedTextNode(
                Component.text("CYBER BADGE #1", NamedTextColor.WHITE, TextDecoration.BOLD),
                -20, -24, 44, 8, UiTextAlignment.CENTER).fontSize(4).atDepth(0.003f));

        builder.add(ParallelogramNode.slanted(-22, -10, 48, 12, -6, Color.fromRGB(247, 37, 133), true));
        builder.add(new AlignedTextNode(
                Component.text("SLANTED BADGE #2", NamedTextColor.WHITE, TextDecoration.BOLD),
                -20, -8, 44, 8, UiTextAlignment.CENTER).fontSize(4).atDepth(0.003f));

        // Rolled line beams with roll angles
        builder.add(new LineNode(-20, 12, 22, 12, 2.0f, 0.002f, Color.fromRGB(114, 9, 183), true, 180.0f));
        builder.add(new LineNode(-20, 20, 22, 20, 2.5f, 0.002f, Color.fromRGB(76, 201, 240), true, 225.0f));
        builder.add(new LineNode(-20, 28, 22, 28, 3.0f, 0.002f, Color.fromRGB(67, 97, 238), true, 270.0f));

        builder.add(new AlignedTextNode(
                Component.text("Roll: 0° / 45° / 90°", NamedTextColor.GRAY),
                -24, 34, 52, 8, UiTextAlignment.CENTER).fontSize(3));

        // --- SECTION 3: CLOSED POLYLINES & SINE WAVE (Right Column: X [32, 86]) ---
        builder.add(new AlignedTextNode(
                Component.text("POLYLINES", NamedTextColor.GREEN, TextDecoration.BOLD),
                32, -36, 54, 8, UiTextAlignment.CENTER).fontSize(4));

        // Five-point star outline, built from alternating outer and inner points.
        float starCenterX = 59;
        float starCenterY = -15;
        float outer = 14.0f;
        float inner = 6.2f;
        float[][] starPoints = new float[10][2];
        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(-90.0 + i * 36.0);
            float radius = (i & 1) == 0 ? outer : inner;
            starPoints[i][0] = (float) (starCenterX + radius * Math.cos(angle));
            starPoints[i][1] = (float) (starCenterY + radius * Math.sin(angle));
        }
        for (int i = 0; i < starPoints.length; i++) {
            float[] start = starPoints[i];
            float[] end = starPoints[(i + 1) % starPoints.length];
            float roll = end[0] < start[0] ? 180.0f : 0.0f;
            builder.add(new LineNode(start[0], start[1], end[0], end[1],
                                     1.8f, 0.002f, Color.fromRGB(255, 200, 0), true, roll));
        }

        // Heart / Pulse waveform below star
        PolylineNode pulse = PolylineNode.builder()
                .add(36, 18)
                .add(44, 18)
                .add(48, 8)
                .add(52, 26)
                .add(56, 12)
                .add(60, 18)
                .add(82, 18)
                .thickness(1.5f)
                .color(Color.fromRGB(0, 255, 136))
                .depth(0.002f)
                .doubleSided(true)
                .closed(false)
                .build();
        builder.add(pulse);

        builder.add(new AlignedTextNode(
                Component.text("Closed Star & ECG Pulse", NamedTextColor.GRAY),
                32, 26, 54, 8, UiTextAlignment.CENTER).fontSize(3));
        builder.add(new AlignedTextNode(
                Component.text("Fullbright & DoubleSided", NamedTextColor.DARK_GRAY),
                32, 34, 54, 8, UiTextAlignment.CENTER).fontSize(3));
    }
}
