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
package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.api.gradient.UiGradientPosition;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

import java.util.List;

/**
 * Visual demo gallery for 2-parameter gradient backgrounds using normalized positions.
 * Allows right-clicking any card to apply its gradient to the main root background.
 */
public final class GradientBackgroundDemoPage extends BaseDemoPage {

    private record CardInfo(String title, String fromLabel, String toLabel,
                            UiGradientPosition startPos, Color startColor,
                            UiGradientPosition endPos, Color endColor) {
        public String fullDirection() {
            return fromLabel + " → " + toLabel;
        }
    }

    private static final List<CardInfo> CARDS = List.of(
            new CardInfo("Horizontal", "Left", "Right",
                    UiGradientPosition.CENTER_LEFT, Color.fromRGB(220, 20, 60),
                    UiGradientPosition.CENTER_RIGHT, Color.fromRGB(25, 25, 112)),
            new CardInfo("Vertical", "Top", "Bottom",
                    UiGradientPosition.CENTER_TOP, Color.fromRGB(46, 204, 113),
                    UiGradientPosition.CENTER_BOTTOM, Color.fromRGB(22, 160, 133)),
            new CardInfo("Diagonal Down", "Top-Left", "Bottom-Right",
                    UiGradientPosition.TOP_LEFT, Color.fromRGB(155, 89, 182),
                    UiGradientPosition.BOTTOM_RIGHT, Color.fromRGB(241, 196, 15)),
            new CardInfo("Diagonal Up", "Bottom-Left", "Top-Right",
                    UiGradientPosition.BOTTOM_LEFT, Color.fromRGB(255, 105, 180),
                    UiGradientPosition.TOP_RIGHT, Color.fromRGB(0, 206, 209)),
            new CardInfo("Center Slant", "Center-Left", "Top-Right",
                    UiGradientPosition.CENTER_LEFT, Color.fromRGB(255, 215, 0),
                    UiGradientPosition.TOP_RIGHT, Color.fromRGB(30, 144, 255)),
            new CardInfo("Radial Corner", "Center", "Bottom-Right",
                    UiGradientPosition.CENTER, Color.fromRGB(255, 255, 255),
                    UiGradientPosition.BOTTOM_RIGHT, Color.fromRGB(44, 62, 80))
    );

    @Override
    public String title() {
        return "GRADIENT BACKGROUNDS";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        // Page helper subtitle
        builder.add(new AlignedTextNode(
                Component.text("Click any gradient card below to apply it to the root background",
                        NamedTextColor.GRAY),
                -86.0f, -34.0f, 172.0f, 7.0f, UiTextAlignment.CENTER).fontSize(3.8f));

        float panelW = 54.0f;
        float panelH = 32.0f;
        float depth = 0.002f;

        float[] colsX = {-86.0f, -27.0f, 32.0f};
        float[] rowsY = {-25.0f, 12.0f};

        for (int i = 0; i < CARDS.size(); i++) {
            CardInfo card = CARDS.get(i);
            float x = colsX[i % 3];
            float y = rowsY[i / 3];

            addGradientCard(builder, i, x, y, panelW, panelH, depth, card);
        }
    }

    private void addGradientCard(UiDocument.Builder builder, int index, float x, float y,
                                 float w, float h, float depth, CardInfo card) {
        // Line 1: Bold Gold Title
        builder.add(new AlignedTextNode(
                Component.text(card.title(), NamedTextColor.GOLD, TextDecoration.BOLD),
                x, y, w, 5.5f, UiTextAlignment.CENTER)
                .fontSize(3.8f).shadowed(true));

        // Line 2: Subtle Direction with Aqua arrow indicator
        Component directionComponent = Component.text()
                .append(Component.text(card.fromLabel(), NamedTextColor.GRAY))
                .append(Component.text(" → ", NamedTextColor.AQUA))
                .append(Component.text(card.toLabel(), NamedTextColor.GRAY))
                .build();
        builder.add(new AlignedTextNode(
                directionComponent,
                x, y + 5.5f, w, 4.5f, UiTextAlignment.CENTER)
                .fontSize(2.8f).shadowed(true));

        // Gradient preview swatch
        UiGradientBackgroundNode node = new UiGradientBackgroundNode(
                x, y + 11.0f, depth, w, h - 11.0f,
                card.startPos(), card.startColor(),
                card.endPos(), card.endColor());

        builder.gradientBackground(node);

        // Clickable button covering the full card bounds
        builder.button(new UiButton("apply_root_" + index, x, y, w, h,
                Component.text("Click to apply " + card.title() + " (" + card.fullDirection() + ") to root background",
                        NamedTextColor.YELLOW)).hitSlop(1));
    }

    @Override
    public boolean onClick(DemoContext context, String buttonId, Player player) {
        if (buttonId.startsWith("apply_root_")) {
            int cardIndex = Integer.parseInt(buttonId.substring("apply_root_".length()));
            if (cardIndex >= 0 && cardIndex < CARDS.size()) {
                CardInfo card = CARDS.get(cardIndex);
                // Create a darkened translucent root gradient based on the selected card
                UiGradient rootGrad = UiGradient.of(
                        card.startPos(),
                        Color.fromARGB(215,
                                Math.min(255, (int)(card.startColor().getRed() * 0.45f + 15)),
                                Math.min(255, (int)(card.startColor().getGreen() * 0.45f + 15)),
                                Math.min(255, (int)(card.startColor().getBlue() * 0.45f + 25))),
                        card.endPos(),
                        Color.fromARGB(235,
                                Math.min(255, (int)(card.endColor().getRed() * 0.40f + 10)),
                                Math.min(255, (int)(card.endColor().getGreen() * 0.40f + 10)),
                                Math.min(255, (int)(card.endColor().getBlue() * 0.40f + 20)))
                );
                context.rootGradient(rootGrad);
                context.updateView();
                player.sendMessage("§a✦ Root background gradient updated to: §e" + card.title() + " §7(" + card.fullDirection() + ")");
                return true;
            }
        }
        return false;
    }
}
