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
import vn.haohan.displayui.api.component.ButtonComponent;
import vn.haohan.displayui.api.component.IconComponent;
import vn.haohan.displayui.api.component.TextComponent;
import vn.haohan.displayui.api.container.Container;
import vn.haohan.displayui.api.container.ScrollContainer;
import vn.haohan.displayui.api.interaction.UiControlChange;
import vn.haohan.displayui.api.layout.UiAnchorPoint;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.AppEntry;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class ChooseAppScrollListDemoPage extends BaseDemoPage {

    private static final int VISIBLE_ROWS = 4;
    private static final float ROW_HEIGHT = 16.5f;

    @Override
    public String title() {
        return "CHOOSE APP · SCROLL LIST";
    }

    public static int maxOffset(DemoContext context) {
        return Math.max(0, context.appEntries().size() - VISIBLE_ROWS);
    }

    @Override
    public void build(Container container, DemoContext context) {
        float listHeight = VISIBLE_ROWS * ROW_HEIGHT; // 66.0f: perfectly bounds exactly 4 rows
        float maxScroll = Math.max(0.0f, (context.appEntries().size() - VISIBLE_ROWS) * ROW_HEIGHT);

        // ScrollContainer hosting all app rows with native scrollbar and strict clipping
        ScrollContainer scroll = ScrollContainer.builder("demo_apps")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 0.0f)
                .size(174.0f, listHeight)
                .maxScrollOffset(maxScroll)
                .scrollOffset(context.appOffset())
                .scrollStep(ROW_HEIGHT) // Scroll by 1 row height per step
                .showScrollbar(true)
                .scrollbarWidth(3.0f)
                .scrollbarPadding(2.0f)
                .scrollbarTrackColor(Color.fromARGB(100, 20, 25, 35))
                .scrollbarThumbColor(Color.fromARGB(220, 75, 130, 200))
                .build();

        // Add all app rows into the ScrollContainer (UiDocumentBridge will automatically clip out-of-view rows!)
        for (int i = 0; i < context.appEntries().size(); i++) {
            AppEntry app = context.appEntries().get(i);
            addAppRow(scroll, app, i * ROW_HEIGHT, "app_" + i, i == context.selectedApp());
        }

        container.addContainer(scroll);

        // Indicator centered below the ScrollContainer
        int currentFirstRow = (int) Math.round(context.appOffset() / ROW_HEIGHT);
        String indicator = (currentFirstRow + 1) + "–"
                + Math.min(context.appEntries().size(), currentFirstRow + VISIBLE_ROWS)
                + " / " + context.appEntries().size();
        container.addComponent(TextComponent.builder("scroll_ind")
                .anchor(UiAnchorPoint.CENTER_BOTTOM)
                .origin(UiAnchorPoint.CENTER_BOTTOM)
                .offset(0.0f, 0.0f)
                .size(174.0f, 8.0f)
                .alignment(UiTextAlignment.CENTER)
                .text(Component.text(indicator, NamedTextColor.DARK_GRAY))
                .fontSize(3.5f)
                .build());
    }

    private void addAppRow(Container container, AppEntry app, float y, String id, boolean selected) {
        Color bgColor = selected ? Color.fromRGB(35, 60, 95) : Color.fromRGB(25, 30, 40);
        Container row = Container.builder(id + "_row")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, y)
                .size(164.0f, 15.0f)
                .backgroundColor(bgColor)
                .borderRound(3.0f)
                .build();

        // Icon
        row.addComponent(IconComponent.builder(id + "_icon")
                .anchor(UiAnchorPoint.CENTER_LEFT)
                .origin(UiAnchorPoint.CENTER_LEFT)
                .offset(4.0f, 0.0f)
                .size(12.0f, 12.0f)
                .material(app.icon())
                .build());

        // Selection marker
        row.addComponent(TextComponent.builder(id + "_check")
                .anchor(UiAnchorPoint.CENTER_LEFT)
                .origin(UiAnchorPoint.CENTER_LEFT)
                .offset(20.0f, 0.0f)
                .size(10.0f, 10.0f)
                .alignment(UiTextAlignment.CENTER)
                .text(Component.text(selected ? "☑" : "☐", selected ? NamedTextColor.AQUA : NamedTextColor.WHITE))
                .fontSize(4.5f)
                .build());

        // Name
        row.addComponent(TextComponent.builder(id + "_name")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(32.0f, 1.5f)
                .size(90.0f, 6.0f)
                .alignment(UiTextAlignment.LEFT)
                .text(Component.text(app.name(), selected ? NamedTextColor.AQUA : NamedTextColor.WHITE, TextDecoration.BOLD))
                .fontSize(4.2f)
                .shadow(true)
                .build());

        // Description
        row.addComponent(TextComponent.builder(id + "_desc")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(32.0f, 8.0f)
                .size(90.0f, 5.0f)
                .alignment(UiTextAlignment.LEFT)
                .text(Component.text(app.description(), NamedTextColor.GRAY))
                .fontSize(3.2f)
                .build());

        // Clickable button
        row.addComponent(ButtonComponent.builder(id)
                .anchor(UiAnchorPoint.CENTER_RIGHT)
                .origin(UiAnchorPoint.CENTER_RIGHT)
                .offset(-3.0f, 0.0f)
                .size(32.0f, 11.0f)
                .label("SELECT")
                .borderRound(2.0f)
                .build());

        container.addContainer(row);
    }

    @Override
    public boolean onClick(DemoContext context, String buttonId, Player player) {
        if (buttonId.startsWith("app_") && !buttonId.contains("_row") && !buttonId.contains("_icon")) {
            try {
                int selected = Integer.parseInt(buttonId.substring(4));
                context.selectedApp(selected);
                context.updateView();
                player.sendMessage("§dSelected app: §f" + context.appEntries().get(selected).name());
                return true;
            } catch (NumberFormatException ignored) {}
        }
        return false;
    }

    @Override
    public void onControlChange(DemoContext context, UiControlChange change) {
        if ("demo_apps".equals(change.control().id())) {
            context.appOffset((float) change.value());
            context.updateView();
        }
    }
}
