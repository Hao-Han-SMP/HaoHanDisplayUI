/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 */
package vn.haohan.displayui.api.container;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.animation.Easings;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.bridge.UiDocumentBridge;
import vn.haohan.displayui.api.component.ButtonComponent;
import vn.haohan.displayui.api.component.CheckboxComponent;
import vn.haohan.displayui.api.component.SliderComponent;
import vn.haohan.displayui.api.component.TextComponent;
import vn.haohan.displayui.api.layout.UiAnchorPoint;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class DropdownContainerTest {

    @Test
    @DisplayName("DropdownContainer inheritance and standard Container characteristics")
    void testDropdownInheritance() {
        DropdownContainer dropdown = DropdownContainer.builder("dropdown_display")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(10.0f, 20.0f)
                .width(160.0f)
                .scale(1.5f)
                .opacity(0.9f)
                .headerHeight(20.0f)
                .headerTitle("Display Settings")
                .indicatorCollapsed("▶")
                .indicatorExpanded("▼")
                .build();

        // 1. Verifies true inheritance from Container
        assertTrue(dropdown instanceof Container, "DropdownContainer must inherit from Container");
        assertEquals("dropdown_display", dropdown.id());
        assertEquals(160.0f, dropdown.width());
        assertEquals(20.0f, dropdown.headerHeight());
        assertEquals(1.5f, dropdown.scale());
        assertEquals(0.9f, dropdown.opacity());
        assertEquals("dropdown_display_toggle", dropdown.toggleButtonId());

        // 2. Scale cascade propagation with parent container
        Container parent = Container.builder("root_card").scale(2.0f).build();
        parent.addContainer(dropdown);
        assertEquals(3.0f, dropdown.effectiveScale(), 1e-5f);
    }

    @Test
    @DisplayName("Collapsed height equals headerHeight; expanded height includes content")
    void testCollapsedAndExpandedHeights() {
        DropdownContainer dropdown = DropdownContainer.builder("dropdown_audio")
                .headerHeight(18.0f)
                .width(150.0f)
                .contentPadding(4.0f)
                .itemSpacing(3.0f)
                .contentBackgroundColor(Color.fromARGB(100, 20, 20, 20))
                .expanded(false)
                .build();

        Container item1 = Container.builder("item_1").size(140.0f, 12.0f).build();
        Container item2 = Container.builder("item_2").size(140.0f, 16.0f).build();
        dropdown.addDropdownItem(item1);
        dropdown.addDropdownItem(item2);

        // Content height: 12 + 16 + 3 (spacing) = 31.0f
        assertEquals(31.0f, dropdown.calculateContentHeight(), 1e-5f);

        // When collapsed: height must strictly equal headerHeight (18.0f)
        assertFalse(dropdown.isExpanded());
        assertEquals(18.0f, dropdown.height(), 1e-5f);
        assertFalse(dropdown.contentContainer().isVisible());

        // When expanded: height must expand to include header + spacing + content + paddings
        dropdown.expand();
        assertTrue(dropdown.isExpanded());
        assertTrue(dropdown.contentContainer().isVisible());
        assertTrue(dropdown.height() > 18.0f);

        // Collapse again
        dropdown.collapse();
        assertFalse(dropdown.isExpanded());
        assertEquals(18.0f, dropdown.height(), 1e-5f);
    }

    @Test
    @DisplayName("Nested child containers and recursive component searching")
    void testNestedChildContainersAndSearch() {
        DropdownContainer dropdown = DropdownContainer.builder("dropdown_controls").build();

        Container rowSlider = Container.builder("row_slider").size(150, 15).build();
        SliderComponent slider = SliderComponent.builder("master_volume").build();
        rowSlider.addComponent(slider);

        Container rowCheck = Container.builder("row_check").size(150, 15).build();
        CheckboxComponent check = CheckboxComponent.builder("mute_audio").build();
        rowCheck.addComponent(check);

        dropdown.addDropdownItem(rowSlider);
        dropdown.addDropdownItem(rowCheck);

        assertEquals(2, dropdown.dropdownItems().size());

        // Recursive search should find nested components inside child containers
        Optional<vn.haohan.displayui.api.component.Component> foundSlider = dropdown.findComponent("master_volume");
        assertTrue(foundSlider.isPresent());
        assertEquals(slider, foundSlider.get());

        Optional<vn.haohan.displayui.api.component.Component> foundCheck = dropdown.findComponent("mute_audio");
        assertTrue(foundCheck.isPresent());
        assertEquals(check, foundCheck.get());

        // Recursive container search
        assertTrue(dropdown.findContainer("row_slider").isPresent());
        assertTrue(dropdown.findContainer("row_check").isPresent());
    }

    @Test
    @DisplayName("Dropdown toggle and event listeners")
    void testToggleAndListeners() {
        DropdownContainer dropdown = DropdownContainer.builder("dropdown_test").expanded(false).build();
        AtomicBoolean toggledState = new AtomicBoolean(false);

        dropdown.onToggle(toggledState::set);

        // Initial state is false
        assertFalse(dropdown.isExpanded());

        // First toggle -> expands
        dropdown.toggle();
        assertTrue(dropdown.isExpanded());
        assertTrue(toggledState.get());

        // Second toggle -> collapses
        dropdown.toggle();
        assertFalse(dropdown.isExpanded());
        assertFalse(toggledState.get());
    }

    @Test
    @DisplayName("Dropdown expand animation triggers scoped cascade on child containers")
    void testDropdownAnimation() {
        DropdownContainer dropdown = DropdownContainer.builder("dropdown_animated")
                .animationType(DropdownAnimationType.SLIDE_AND_FADE)
                .animationDurationTicks(10)
                .animationEasing(Easings.OutCubic)
                .slideDistance(15.0f)
                .expanded(false)
                .build();

        Container item = Container.builder("nested_item").size(100, 20).build();
        TextComponent text = TextComponent.builder("item_text").build();
        item.addComponent(text);
        dropdown.addDropdownItem(item);

        assertFalse(dropdown.contentContainer().isAnimating());
        assertFalse(item.isCascadeAnimating());

        // Expand dropdown -> triggers animation cascade on contentContainer
        dropdown.expand();

        assertTrue(dropdown.contentContainer().isAnimating(), "Content container must have active animation");
        assertTrue(item.isCascadeAnimating(), "Nested item must be in cascade animation");

        UiAnimation anim = dropdown.contentContainer().activeAnimation().orElseThrow();
        assertEquals(10, anim.durationTicks());
        assertEquals(Easings.OutCubic, anim.easing());
        assertEquals(-15.0f, anim.offsetY(), 1e-4f); // TOP direction is negative Y
        assertEquals(0.0f, anim.fromOpacity(), 1e-4f);
        assertEquals(1.0f, anim.toOpacity(), 1e-4f);

        // Collapse dropdown -> stops animation
        dropdown.collapse();
        assertFalse(dropdown.contentContainer().isAnimating());
        assertFalse(item.isCascadeAnimating());
    }

    @Test
    @DisplayName("Auto-layout calculates sequential vertical offsets for child containers")
    void testAutoLayoutOffsets() {
        DropdownContainer dropdown = DropdownContainer.builder("dropdown_autolayout")
                .width(160.0f)
                .headerHeight(16.0f)
                .itemSpacing(4.0f)
                .contentPadding(5.0f)
                .contentBackgroundColor(Color.BLACK)
                .autoLayout(true)
                .expanded(true)
                .build();

        Container item1 = Container.builder("item1").size(100, 10).build();
        Container item2 = Container.builder("item2").size(100, 20).build();
        Container item3 = Container.builder("item3").size(100, 30).build();

        dropdown.addDropdownItem(item1);
        dropdown.addDropdownItem(item2);
        dropdown.addDropdownItem(item3);

        dropdown.refreshLayout();

        // item1 should start at contentPadding = 5.0
        assertEquals(5.0f, item1.y(), 1e-4f);

        // item2 should start at 5 + 10 (item1 height) + 4 (itemSpacing) = 19.0
        assertEquals(19.0f, item2.y(), 1e-4f);

        // item3 should start at 19 + 20 (item2 height) + 4 (itemSpacing) = 43.0
        assertEquals(43.0f, item3.y(), 1e-4f);

        // Total content height without paddings: 10 + 20 + 30 + 4*2 = 68.0
        assertEquals(68.0f, dropdown.calculateContentHeight(), 1e-4f);
    }

    @Test
    @DisplayName("UiDocumentBridge compiles DropdownContainer header and interactive hit zone")
    void testBridgeCompilation() {
        DropdownContainer dropdown = DropdownContainer.builder("dropdown_bridge")
                .headerHeight(16.0f)
                .width(170.0f)
                .headerTitle("Audio Settings")
                .expanded(false)
                .build();

        Container item = Container.builder("item_row").size(160, 12).build();
        ButtonComponent btn = ButtonComponent.builder("btn_inside").label("Click").build();
        item.addComponent(btn);
        dropdown.addDropdownItem(item);

        // 1. Compile when collapsed
        UiDocumentBridge.CompiledUi compiledCollapsed = UiDocumentBridge.compileWithAnimations(dropdown);
        assertNotNull(compiledCollapsed.document());

        // Must have the header toggle button registered
        boolean hasToggleBtn = compiledCollapsed.document().buttons().stream()
                .anyMatch(b -> b.id().equals("dropdown_bridge_toggle"));
        assertTrue(hasToggleBtn, "Header toggle button must be present in document");

        // The button inside the collapsed child item should NOT be present
        boolean hasInnerBtn = compiledCollapsed.document().buttons().stream()
                .anyMatch(b -> b.id().equals("btn_inside"));
        assertFalse(hasInnerBtn, "Inner button must NOT be present when dropdown is collapsed");

        // 2. Expand and re-compile
        dropdown.expand();
        UiDocumentBridge.CompiledUi compiledExpanded = UiDocumentBridge.compileWithAnimations(dropdown);
        assertNotNull(compiledExpanded.document());

        // Now inner button MUST be present
        boolean hasInnerBtnExpanded = compiledExpanded.document().buttons().stream()
                .anyMatch(b -> b.id().equals("btn_inside"));
        assertTrue(hasInnerBtnExpanded, "Inner button MUST be present when dropdown is expanded");

        // And node animations should be generated for the pushed-out child container!
        assertTrue(compiledExpanded.hasAnimations(), "Expanding dropdown must generate node animations for child nodes");
    }

    @Test
    @DisplayName("Dropdown header nodes remain static while nested child nodes animate")
    void testDropdownAnimationTargeting() {
        DropdownContainer dropdown = DropdownContainer.builder("dropdown_targeted")
                .headerHeight(16.0f)
                .width(170.0f)
                .headerTitle("Settings")
                .animationType(DropdownAnimationType.SLIDE_AND_FADE)
                .animationDurationTicks(8)
                .expanded(false)
                .build();

        Container item = Container.builder("item_row").size(160, 12).build();
        item.addComponent(TextComponent.builder("txt").text("Item 1").build());
        dropdown.addDropdownItem(item);

        dropdown.expand();
        UiDocumentBridge.CompiledUi compiled = UiDocumentBridge.compileWithAnimations(dropdown);
        assertNotNull(compiled.document());
        List<UiAnimation> anims = compiled.nodeAnimations();
        assertNotNull(anims);
        assertEquals(compiled.document().nodes().size(), anims.size());

        // Header nodes (first 3: bg, title, arrow) must be static so they never flicker or move
        assertTrue(anims.get(0) == null || anims.get(0).isStatic() || anims.get(0).durationTicks() <= 0);
        assertTrue(anims.get(1) == null || anims.get(1).isStatic() || anims.get(1).durationTicks() <= 0);
        assertTrue(anims.get(2) == null || anims.get(2).isStatic() || anims.get(2).durationTicks() <= 0);

        // Child node must have the expand animation
        boolean childAnimated = false;
        for (int i = 3; i < anims.size(); i++) {
            UiAnimation a = anims.get(i);
            if (a != null && !a.isStatic() && a.durationTicks() > 0) {
                childAnimated = true;
                break;
            }
        }
        assertTrue(childAnimated, "Child nodes must receive the dropdown expand animation");
    }

    @Test
    @DisplayName("DropdownContainer internally clamps height and child layout to parent container bounds")
    void testParentContainerBoundaryClamping() {
        Container parent = Container.builder("parent_frame").size(160.0f, 60.0f).build();

        DropdownContainer dropdown = DropdownContainer.builder("dropdown_clamped")
                .headerHeight(16.0f)
                .offset(0.0f, 10.0f)
                .width(160.0f)
                .contentPadding(2.0f)
                .itemSpacing(2.0f)
                .clampToParent(true)
                .expanded(true)
                .build();

        // 3 items with height 20 each -> raw content height = 60 + spacings = 64
        // Raw total dropdown height = 16 + 2 + 64 + 4 = 86
        // But parent container available height from y=10 is 60 - 10 = 50.0f
        Container item1 = Container.builder("item1").size(150.0f, 20.0f).build();
        Container item2 = Container.builder("item2").size(150.0f, 20.0f).build();
        Container item3 = Container.builder("item3").size(150.0f, 20.0f).build();

        dropdown.addDropdownItem(item1);
        dropdown.addDropdownItem(item2);
        dropdown.addDropdownItem(item3);

        parent.addContainer(dropdown);
        dropdown.refreshLayout();

        // Height must be clamped to at most parent.height() - y() = 50.0f
        assertTrue(dropdown.height() <= 50.0f, "Dropdown height must not exceed parent container boundary (50.0f), got: " + dropdown.height());
        assertTrue(dropdown.contentContainer().height() <= 50.0f - 16.0f, "Content container must not exceed available parent height");

        // The 3rd item should not stick out beyond max allowed bounds
        assertFalse(item3.isVisible(), "Item beyond container bounds should be clipped/hidden");
    }

    @Test
    @DisplayName("Verify DropdownContainer header inherits container active animation during compilation")
    void testDropdownContainerHeaderAnimationInheritance() {
        DropdownContainer dropdown = DropdownContainer.builder("dropdown_test")
                .headerTitle("Test Dropdown")
                .headerHeight(16.0f)
                .width(100.0f)
                .build();

        var anim = UiAnimation.fadeIn(12);
        dropdown.animate(anim);

        UiDocumentBridge.CompiledUi compiled = UiDocumentBridge.compileWithAnimations(dropdown);

        assertTrue(compiled.hasAnimations(), "Compiled dropdown UI must contain animations");
        assertFalse(compiled.nodeAnimations().isEmpty());
        assertTrue(compiled.nodeAnimations().stream().anyMatch(a -> a != null && a.durationTicks() == 12),
                "Dropdown header nodes must receive the dropdown container's active animation");
    }
}
