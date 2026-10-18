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

import org.bukkit.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.component.TextComponent;
import vn.haohan.displayui.api.layout.UiAnchorPoint;

import static org.junit.jupiter.api.Assertions.*;

class ScrollContainerTest {

    @Test
    @DisplayName("Should initialize ScrollContainer with defaults")
    void testDefaults() {
        ScrollContainer scroll = ScrollContainer.builder("test_scroll")
                .size(100.0f, 60.0f)
                .build();

        assertEquals("test_scroll", scroll.id());
        assertEquals(100.0f, scroll.width());
        assertEquals(60.0f, scroll.height());
        assertEquals(0.0f, scroll.scrollOffset());
        assertEquals(0.0f, scroll.maxScrollOffset());
        assertEquals(10.0f, scroll.scrollStep());
        assertTrue(scroll.showScrollbar());
        assertEquals(3.0f, scroll.scrollbarWidth());
    }

    @Test
    @DisplayName("Should configure custom scroll bounds and clamp offset correctly")
    void testScrollBounds() {
        ScrollContainer scroll = ScrollContainer.builder("scroll_bounds")
                .size(120.0f, 80.0f)
                .maxScrollOffset(150.0f)
                .scrollOffset(50.0f)
                .scrollStep(15.0f)
                .scrollbarWidth(4.0f)
                .scrollbarTrackColor(Color.fromRGB(20, 20, 20))
                .scrollbarThumbColor(Color.fromRGB(100, 150, 255))
                .build();

        assertEquals(150.0f, scroll.maxScrollOffset());
        assertEquals(50.0f, scroll.scrollOffset());
        assertEquals(15.0f, scroll.scrollStep());
        assertEquals(4.0f, scroll.scrollbarWidth());

        scroll.scrollOffset(200.0f);
        assertEquals(150.0f, scroll.scrollOffset(), "Should clamp to maxScrollOffset");

        scroll.scrollOffset(-10.0f);
        assertEquals(0.0f, scroll.scrollOffset(), "Should clamp to 0.0f");
    }

    @Test
    @DisplayName("Should manage child components and containers properly")
    void testChildrenManagement() {
        ScrollContainer scroll = ScrollContainer.builder("scroll_parent")
                .size(100.0f, 100.0f)
                .build();

        TextComponent item = TextComponent.builder("item1")
                .text(net.kyori.adventure.text.Component.text("Hello"))
                .build();
        scroll.addComponent(item);

        Container childContainer = Container.builder("sub_row").build();
        scroll.addContainer(childContainer);

        assertTrue(scroll.components().contains(item));
        assertTrue(scroll.childContainers().contains(childContainer));
    }

    @Test
    @DisplayName("Should shift child container global position by parent scroll offset")
    void testChildGlobalPositionShiftedByScrollOffset() {
        ScrollContainer scroll = ScrollContainer.builder("scroll")
                .anchor(UiAnchorPoint.TOP_LEFT).origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 10.0f).size(100.0f, 100.0f)
                .maxScrollOffset(100.0f)
                .scrollOffset(25.0f)
                .build();

        Container child = Container.builder("child")
                .anchor(UiAnchorPoint.TOP_LEFT).origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 50.0f).size(100.0f, 20.0f)
                .build();
        scroll.addContainer(child);

        float[] pos = child.computeGlobalPosition();
        // scroll is at Y=10, scrollOffset=25, child local Y=50 -> global Y = 10 - 25 + 50 = 35.0f
        assertEquals(35.0f, pos[1], 0.001f);
    }
}
