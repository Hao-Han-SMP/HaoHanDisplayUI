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
package vn.haohan.displayui.demo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.bridge.UiDocumentBridge;
import vn.haohan.displayui.api.component.ButtonComponent;
import vn.haohan.displayui.api.component.TextComponent;
import vn.haohan.displayui.api.container.Container;
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.api.layer.Layer;
import vn.haohan.displayui.api.layer.LayerManager;
import vn.haohan.displayui.api.layout.UiAnchorPoint;
import vn.haohan.displayui.api.text.UiText;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.text.UiTextOpticalPreset;
import vn.haohan.displayui.utils.MathUtils;

import java.util.List;
import java.util.Objects;

/**
 * Modern v2.0 Hierarchical Demo Renderer that structures the in-world demo menu
 * into cleanly separated Layers, nested Containers (Dialog -> Header, Content, Footer),
 * and individual Components with anti-flicker depth allocations.
 */
public final class HierarchicalDemoRenderer {

    public static final float DIALOG_WIDTH = 192.0f;
    public static final float DIALOG_HEIGHT = 128.0f;

    private HierarchicalDemoRenderer() {
    }

    /**
     * Builds the complete modern 3-tier LayerManager for the demo menu.
     */
    public static LayerManager buildLayerManager(List<DemoPage> pages, DemoContext context) {
        Objects.requireNonNull(pages, "pages cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        LayerManager manager = new LayerManager();

        // 1. Layer 0: Background Canvas
        Layer bgLayer = manager.createLayer("background_layer", 0);
        UiGradient rootGrad = context.rootGradient();
        bgLayer.bounds(-DIALOG_WIDTH * 0.5f, -DIALOG_HEIGHT * 0.5f, DIALOG_WIDTH, DIALOG_HEIGHT);
        bgLayer.gradient(rootGrad);
        bgLayer.doubleSided(context.doubleSided());

        // 2. Layer 1: Interactive Menu Dialog
        Layer dialogLayer = manager.createLayer("dialog_layer", 1);

        Container mainDialog = Container.builder("demo_dialog")
                .anchor(UiAnchorPoint.CENTER)
                .origin(UiAnchorPoint.CENTER)
                .size(DIALOG_WIDTH, DIALOG_HEIGHT)
                .build();

        int pageIndex = MathUtils.clamp(context.page(), 0, pages.size() - 1);
        DemoPage activePage = pages.get(pageIndex);

        // SubContainer A: Header
        Container headerSub = buildHeaderSubContainer(activePage.title());
        mainDialog.addContainer(headerSub);

        // SubContainer B: Footer Navigation & Toggles (static chrome registered before dynamic content)
        Container footerSub = buildFooterSubContainer(pageIndex, pages.size(), context.doubleSided(), context.mirrorSide());
        mainDialog.addContainer(footerSub);

        // SubContainer C: Content SubContainer (dynamic scrollable content registered last)
        Container contentSub = Container.builder("demo_content_sub")
                .anchor(UiAnchorPoint.CENTER)
                .origin(UiAnchorPoint.CENTER)
                .size(174.0f, 76.0f)
                .offset(0.0f, 1.0f)
                .build();
        activePage.build(contentSub, context);
        mainDialog.addContainer(contentSub);

        dialogLayer.addContainer(mainDialog);
        return manager;
    }

    /**
     * Renders the demo pages as a compiled {@link UiDocument}.
     */
    public static UiDocument render(List<DemoPage> pages, DemoContext context) {
        LayerManager manager = buildLayerManager(pages, context);
        return UiDocumentBridge.compile(manager);
    }

    private static Container buildHeaderSubContainer(String pageTitle) {
        Container header = Container.builder("header_sub")
                .anchor(UiAnchorPoint.CENTER_TOP)
                .origin(UiAnchorPoint.CENTER_TOP)
                .offset(0.0f, 6.0f)
                .size(174.0f, 18.0f)
                .build();

        TextComponent titleText = TextComponent.builder("header_brand")
                .anchor(UiAnchorPoint.CENTER_LEFT)
                .origin(UiAnchorPoint.CENTER_LEFT)
                .size(110.0f, 16.0f)
                .text(UiText.builder().gradient("HaoHan Display UI", new TextColor[]{
                        UiText.hex("#FFD700"), UiText.hex("#FF7A00"), UiText.hex("#C02CFF")
                }, 1.0, TextDecoration.BOLD).build())
                .fontSize(9.0f)
                .opticalPreset(UiTextOpticalPreset.BOLD_GRADIENT)
                .shadow(true)
                .build();

        TextComponent pageTitleText = TextComponent.builder("header_page_title")
                .anchor(UiAnchorPoint.CENTER_RIGHT)
                .origin(UiAnchorPoint.CENTER_RIGHT)
                .size(60.0f, 14.0f)
                .alignment(UiTextAlignment.RIGHT)
                .text(Component.text(pageTitle, NamedTextColor.DARK_GRAY))
                .fontSize(5.0f)
                .build();

        header.addComponent(titleText);
        header.addComponent(pageTitleText);
        return header;
    }

    private static Container buildFooterSubContainer(int pageIndex, int totalPages,
                                                    boolean doubleSided, boolean mirrorSide) {
        Container footer = Container.builder("footer_sub")
                .anchor(UiAnchorPoint.CENTER_BOTTOM)
                .origin(UiAnchorPoint.CENTER_BOTTOM)
                .offset(0.0f, -6.0f)
                .size(174.0f, 16.0f)
                .build();

        // Prev page button
        ButtonComponent prevBtn = ButtonComponent.builder("previous_page")
                .anchor(UiAnchorPoint.CENTER_LEFT)
                .origin(UiAnchorPoint.CENTER_LEFT)
                .offset(0.0f, 0.0f)
                .size(18.0f, 14.0f)
                .label("<")
                .borderRound(3.0f)
                .build();

        // Page number display
        TextComponent pageNum = TextComponent.builder("page_indicator")
                .anchor(UiAnchorPoint.CENTER_LEFT)
                .origin(UiAnchorPoint.CENTER_LEFT)
                .offset(22.0f, 0.0f)
                .size(36.0f, 14.0f)
                .alignment(UiTextAlignment.CENTER)
                .text(Component.text((pageIndex + 1) + " / " + totalPages, NamedTextColor.GRAY))
                .fontSize(5.0f)
                .build();

        // Next page button
        ButtonComponent nextBtn = ButtonComponent.builder("next_page")
                .anchor(UiAnchorPoint.CENTER_LEFT)
                .origin(UiAnchorPoint.CENTER_LEFT)
                .offset(62.0f, 0.0f)
                .size(18.0f, 14.0f)
                .label(">")
                .borderRound(3.0f)
                .build();

        // Toggle 2X Button
        ButtonComponent twoSidedBtn = ButtonComponent.builder("toggle_doublesided")
                .anchor(UiAnchorPoint.CENTER_RIGHT)
                .origin(UiAnchorPoint.CENTER_RIGHT)
                .offset(-46.0f, 0.0f)
                .size(42.0f, 14.0f)
                .label(doubleSided ? "◈ 2X ON" : "◈ 2X OFF")
                .backgroundColor(doubleSided ? Color.fromRGB(30, 140, 60) : Color.fromRGB(45, 50, 60))
                .borderRound(3.0f)
                .build();

        // Toggle MIR Button
        ButtonComponent mirrorBtn = ButtonComponent.builder("toggle_mirrorside")
                .anchor(UiAnchorPoint.CENTER_RIGHT)
                .origin(UiAnchorPoint.CENTER_RIGHT)
                .offset(0.0f, 0.0f)
                .size(42.0f, 14.0f)
                .label(mirrorSide ? "⇄ MIR ON" : "⇄ MIR OFF")
                .backgroundColor(mirrorSide ? Color.fromRGB(30, 100, 160) : Color.fromRGB(45, 50, 60))
                .borderRound(3.0f)
                .build();

        footer.addComponent(prevBtn);
        footer.addComponent(pageNum);
        footer.addComponent(nextBtn);
        footer.addComponent(twoSidedBtn);
        footer.addComponent(mirrorBtn);
        return footer;
    }
}
