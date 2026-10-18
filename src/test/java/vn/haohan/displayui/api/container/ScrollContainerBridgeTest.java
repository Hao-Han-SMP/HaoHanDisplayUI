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
package vn.haohan.displayui.api.container;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.bridge.UiDocumentBridge;
import vn.haohan.displayui.api.component.TextComponent;
import vn.haohan.displayui.api.layer.Layer;
import vn.haohan.displayui.api.layer.LayerManager;
import vn.haohan.displayui.api.layout.UiAnchorPoint;

import static org.junit.jupiter.api.Assertions.*;

class ScrollContainerBridgeTest {

    @Test
    @DisplayName("Should compile ScrollContainer with hit area, scrollbar, and clip out-of-bounds children")
    void testScrollContainerCompilationAndClipping() {
        LayerManager lm = new LayerManager();
        Layer layer = lm.createLayer("main_layer", 0);

        ScrollContainer scroll = ScrollContainer.builder("test_scroll")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 0.0f)
                .size(100.0f, 50.0f)
                .maxScrollOffset(100.0f)
                .scrollOffset(30.0f)
                .showScrollbar(true)
                .scrollbarWidth(4.0f)
                .scrollbarTrackColor(Color.fromRGB(20, 20, 20))
                .scrollbarThumbColor(Color.fromRGB(200, 200, 200))
                .build();

        // Item 1: at y = 10 -> with scrollOffset=30 -> compGlobalY = 10 - 30 = -20. Height is 10 -> -20+10 = -10 < 0 -> Clipped!
        TextComponent itemAbove = TextComponent.builder("item_above")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 10.0f)
                .size(80.0f, 10.0f)
                .text(Component.text("Above"))
                .build();

        // Item 2: at y = 40 -> with scrollOffset=30 -> compGlobalY = 40 - 30 = 10. Height is 20 -> 10..30 inside 0..50 -> Visible!
        TextComponent itemVisible = TextComponent.builder("item_visible")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 40.0f)
                .size(80.0f, 20.0f)
                .text(Component.text("Visible"))
                .build();

        // Item 3: at y = 100 -> with scrollOffset=30 -> compGlobalY = 100 - 30 = 70. 70 > 50 -> Clipped!
        TextComponent itemBelow = TextComponent.builder("item_below")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 100.0f)
                .size(80.0f, 20.0f)
                .text(Component.text("Below"))
                .build();

        scroll.addComponent(itemAbove);
        scroll.addComponent(itemVisible);
        scroll.addComponent(itemBelow);
        layer.addContainer(scroll);

        UiDocument doc = UiDocumentBridge.compile(lm);

        // Verify ScrollList control registered
        assertTrue(doc.controls().stream().anyMatch(c -> c.id().equals("test_scroll")));

        // Verify itemVisible is present, but itemAbove and itemBelow are clipped
        boolean hasVisible = doc.nodes().stream().anyMatch(n -> n instanceof vn.haohan.displayui.api.node.AlignedTextNode at && at.text().toString().contains("Visible"));
        boolean hasAbove = doc.nodes().stream().anyMatch(n -> n instanceof vn.haohan.displayui.api.node.AlignedTextNode at && at.text().toString().contains("Above"));
        boolean hasBelow = doc.nodes().stream().anyMatch(n -> n instanceof vn.haohan.displayui.api.node.AlignedTextNode at && at.text().toString().contains("Below"));

        assertTrue(hasVisible, "Item within viewport should be compiled");
        assertFalse(hasAbove, "Item above viewport should be clipped");
        assertFalse(hasBelow, "Item below viewport should be clipped");
    }

    @Test
    @DisplayName("Should clip child container components that overflow viewport bounds")
    void testStrictViewportClippingOmitsPartiallyOverflowingContainers() {
        LayerManager lm = new LayerManager();
        Layer layer = lm.createLayer("main_layer", 0);

        ScrollContainer scroll = ScrollContainer.builder("test_scroll_partial")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 0.0f)
                .size(100.0f, 50.0f)
                .scrollOffset(0.0f)
                .build();

        // Container 1: Fully inside: y = 10, h = 20 -> 10..30 <= 50 -> Visible!
        Container inside = Container.builder("row_inside")
                .anchor(UiAnchorPoint.TOP_LEFT).origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 10.0f).size(100.0f, 20.0f).build();
        inside.addComponent(TextComponent.builder("txt_inside")
                .anchor(UiAnchorPoint.TOP_LEFT).origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 0.0f).size(80.0f, 10.0f)
                .text(Component.text("InsideRow"))
                .build());

        // Container 2: Partially overflowing at bottom: y = 40, h = 20
        // txt_partial is at offset y = 15.0f -> global Y = 55.0f > 50.0f -> Should be clipped!
        Container partialBottom = Container.builder("row_partial")
                .anchor(UiAnchorPoint.TOP_LEFT).origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 40.0f).size(100.0f, 20.0f).build();
        partialBottom.addComponent(TextComponent.builder("txt_partial")
                .anchor(UiAnchorPoint.TOP_LEFT).origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 15.0f).size(80.0f, 10.0f)
                .text(Component.text("PartialRow"))
                .build());

        scroll.addContainer(inside);
        scroll.addContainer(partialBottom);
        layer.addContainer(scroll);

        UiDocument doc = UiDocumentBridge.compile(lm);

        boolean hasInside = doc.nodes().stream().anyMatch(n -> n instanceof vn.haohan.displayui.api.node.AlignedTextNode at && at.text().toString().contains("InsideRow"));
        boolean hasPartial = doc.nodes().stream().anyMatch(n -> n instanceof vn.haohan.displayui.api.node.AlignedTextNode at && at.text().toString().contains("PartialRow"));

        assertTrue(hasInside, "Inside child container should be rendered");
        assertFalse(hasPartial, "Overflowing component within child container should be clipped to prevent bleeding");
    }
}
