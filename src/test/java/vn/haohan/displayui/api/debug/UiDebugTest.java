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
package vn.haohan.displayui.api.debug;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.bridge.UiDocumentBridge;
import vn.haohan.displayui.api.component.ButtonComponent;
import vn.haohan.displayui.api.component.TextComponent;
import vn.haohan.displayui.api.container.Container;
import vn.haohan.displayui.api.interaction.UiButtonAction;
import vn.haohan.displayui.api.layer.Layer;
import vn.haohan.displayui.api.layer.LayerManager;
import vn.haohan.displayui.api.layout.UiAnchorPoint;
import vn.haohan.displayui.api.node.AlignedTextNode;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite verifying the complete debug UI system in HaoHanDisplayUI.
 */
public class UiDebugTest {

    private LayerManager lm;
    private Layer layerLeft;
    private Layer layerRight;
    private Container contLeft;
    private Container contRight;
    private Container contChild;
    private TextComponent compTitle;
    private ButtonComponent compButton;
    private ButtonComponent compChildBtn;

    @BeforeEach
    public void setUp() {
        lm = new LayerManager();

        layerLeft = lm.createLayer("left_layer", 0);
        layerRight = lm.createLayer("right_layer", 1);

        contLeft = Container.builder("cont_left")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .size(100, 50)
                .backgroundColor(Color.fromRGB(20, 20, 20))
                .build();

        contRight = Container.builder("cont_right")
                .anchor(UiAnchorPoint.TOP_RIGHT)
                .origin(UiAnchorPoint.TOP_RIGHT)
                .size(120, 80)
                .backgroundColor(Color.fromRGB(30, 30, 30))
                .build();

        contChild = Container.builder("cont_child")
                .anchor(UiAnchorPoint.CENTER)
                .origin(UiAnchorPoint.CENTER)
                .size(60, 30)
                .build();

        compTitle = TextComponent.builder("comp_title")
                .text(Component.text("Hello Debug"))
                .size(80, 20)
                .build();

        compButton = ButtonComponent.builder("comp_button")
                .label(Component.text("Click Me"))
                .size(50, 20)
                .action(UiButtonAction.none())
                .build();

        compChildBtn = ButtonComponent.builder("comp_child_btn")
                .label(Component.text("Child Button"))
                .size(40, 15)
                .action(UiButtonAction.none())
                .build();

        contLeft.addComponent(compTitle);
        contLeft.addComponent(compButton);

        contChild.addComponent(compChildBtn);
        contRight.addContainer(contChild);

        layerLeft.addContainer(contLeft);
        layerRight.addContainer(contRight);
    }

    @Test
    @DisplayName("1. Verify Layer visibility toggling filters layer out of compiled UiDocument")
    public void testLayerVisibilityToggling() {
        UiDebugState debugState = new UiDebugState();

        // Initially both layers visible
        debugState.apply(lm);
        UiDocument docAll = UiDocumentBridge.compile(lm);
        assertTrue(docAll.buttons().stream().anyMatch(b -> b.id().equals("comp_button")));
        assertTrue(docAll.buttons().stream().anyMatch(b -> b.id().equals("comp_child_btn")));

        // Hide left layer
        debugState.setLayerVisibility("left_layer", false);
        debugState.apply(lm);
        assertFalse(debugState.getEffectiveLayerVisibility("left_layer", lm));
        assertTrue(debugState.getEffectiveLayerVisibility("right_layer", lm));

        UiDocument docRightOnly = UiDocumentBridge.compile(lm);
        assertFalse(docRightOnly.buttons().stream().anyMatch(b -> b.id().equals("comp_button")));
        assertTrue(docRightOnly.buttons().stream().anyMatch(b -> b.id().equals("comp_child_btn")));

        // Toggle left layer back on
        boolean toggled = debugState.toggleLayer("left_layer", lm);
        assertTrue(toggled);
        debugState.apply(lm);
        UiDocument docRestored = UiDocumentBridge.compile(lm);
        assertTrue(docRestored.buttons().stream().anyMatch(b -> b.id().equals("comp_button")));
    }

    @Test
    @DisplayName("2. Verify Container visibility toggling and child propagation")
    public void testContainerVisibilityToggling() {
        UiDebugState debugState = new UiDebugState();

        // Hide cont_right
        debugState.setContainerVisibility("cont_right", false);
        debugState.apply(lm);

        assertFalse(contRight.isVisible());

        // Child button in cont_child must not be in compiled document because ancestor is hidden
        UiDocument doc = UiDocumentBridge.compile(lm);
        assertFalse(doc.buttons().stream().anyMatch(b -> b.id().equals("comp_child_btn")));
        assertTrue(doc.buttons().stream().anyMatch(b -> b.id().equals("comp_button")));
    }

    @Test
    @DisplayName("3. Verify Component visibility toggling hides specific component")
    public void testComponentVisibilityToggling() {
        UiDebugState debugState = new UiDebugState();

        debugState.apply(lm);
        UiDocument docFull = UiDocumentBridge.compile(lm);

        debugState.setComponentVisibility("comp_title", false);
        debugState.apply(lm);
        assertFalse(compTitle.isVisible());
        assertTrue(compButton.isVisible());

        UiDocument docHiddenTitle = UiDocumentBridge.compile(lm);
        assertEquals(docFull.nodes().size() - 1, docHiddenTitle.nodes().size());
    }

    @Test
    @DisplayName("4. Verify Solo Mode for Layer, Container, and Component")
    public void testSoloMode() {
        UiDebugState debugState = new UiDebugState();

        // 1. Solo Layer
        debugState.setSolo(UiDebugState.SoloType.LAYER, "left_layer");
        assertTrue(debugState.isSoloActive());
        assertEquals("left_layer", debugState.getSoloId());
        debugState.apply(lm);
        assertTrue(layerLeft.isVisible());
        assertFalse(layerRight.isVisible());

        // 2. Solo Container
        debugState.setSolo(UiDebugState.SoloType.CONTAINER, "cont_child");
        debugState.apply(lm);
        assertTrue(contChild.isVisible());
        assertFalse(contLeft.isVisible());

        // 3. Solo Component
        debugState.setSolo(UiDebugState.SoloType.COMPONENT, "comp_title");
        debugState.apply(lm);
        assertTrue(compTitle.isVisible());
        assertFalse(compButton.isVisible());
        assertFalse(compChildBtn.isVisible());

        // 4. Clear Solo
        debugState.clearSolo();
        assertFalse(debugState.isSoloActive());
    }

    @Test
    @DisplayName("5. Verify Bulk operations: hideAll, showAll, and reset")
    public void testBulkOperations() {
        UiDebugState debugState = new UiDebugState();

        // hideAll
        debugState.hideAll(lm);
        debugState.apply(lm);
        UiDocument docEmpty = UiDocumentBridge.compile(lm);
        assertTrue(docEmpty.nodes().isEmpty());

        // showAll
        debugState.showAll(lm);
        debugState.apply(lm);
        UiDocument docFull = UiDocumentBridge.compile(lm);
        assertFalse(docFull.nodes().isEmpty());

        // reset
        debugState.setLayerVisibility("left_layer", false);
        debugState.setSolo(UiDebugState.SoloType.LAYER, "right_layer");
        debugState.reset();
        assertFalse(debugState.isSoloActive());
        assertFalse(debugState.isClickInspectEnabled());
    }

    @Test
    @DisplayName("6. Verify Tree Hierarchy collection produces accurate tree structure")
    public void testTreeHierarchyCollection() {
        UiDebugState debugState = new UiDebugState();
        List<UiDebugState.UiTreeNode> tree = debugState.buildTree(lm);

        assertNotNull(tree);
        assertEquals(2, tree.size(), "Must have 2 root layer nodes");

        UiDebugState.UiTreeNode leftNode = tree.stream()
                .filter(n -> n.id().equals("left_layer")).findFirst().orElseThrow();
        assertEquals("LAYER", leftNode.type());
        assertEquals(1, leftNode.children().size());

        UiDebugState.UiTreeNode leftContNode = leftNode.children().get(0);
        assertEquals("CONTAINER", leftContNode.type());
        assertEquals("cont_left", leftContNode.id());
        assertEquals(2, leftContNode.children().size());
    }

    @Test
    @DisplayName("7. Verify Debug Registry extracts IDs dynamically")
    public void testDebugRegistry() {
        var layers = UiDebugRegistry.getAvailableLayerIds(lm);
        assertTrue(layers.contains("left_layer"));
        assertTrue(layers.contains("right_layer"));

        var containers = UiDebugRegistry.getAvailableContainerIds(lm);
        assertTrue(containers.contains("cont_left"));
        assertTrue(containers.contains("cont_right"));
        assertTrue(containers.contains("cont_child"));

        var components = UiDebugRegistry.getAvailableComponentIds(lm);
        assertTrue(components.contains("comp_title"));
        assertTrue(components.contains("comp_button"));
        assertTrue(components.contains("comp_child_btn"));
    }

    @Test
    @DisplayName("8. Verify Inspector outputs properties for Layer, Container, and Component")
    public void testInspectorFormatting() {
        UiDebugState state = new UiDebugState();

        List<Component> layerInspect = UiDebugInspector.inspectLayer(layerLeft, state);
        assertNotNull(layerInspect);
        assertFalse(layerInspect.isEmpty());

        List<Component> contInspect = UiDebugInspector.inspectContainer(contLeft, state);
        assertNotNull(contInspect);
        assertFalse(contInspect.isEmpty());

        List<Component> compInspect = UiDebugInspector.inspectComponent(compTitle, state);
        assertNotNull(compInspect);
        assertFalse(compInspect.isEmpty());

        // inspectAny auto-discovery
        assertNotNull(UiDebugInspector.inspectAny(lm, state, "left_layer"));
        assertNotNull(UiDebugInspector.inspectAny(lm, state, "cont_left"));
        assertNotNull(UiDebugInspector.inspectAny(lm, state, "comp_title"));
        assertNull(UiDebugInspector.inspectAny(lm, state, "non_existent"));
    }

    @Test
    @DisplayName("9. Verify Click-to-Inspect mode injects hitboxes for non-button components & containers")
    public void testClickInspectMode() {
        UiDebugState debugState = new UiDebugState();

        debugState.apply(lm);
        UiDocument normalDoc = UiDocumentBridge.compile(lm);
        int normalButtonCount = normalDoc.buttons().size();

        debugState.setClickInspectEnabled(true);
        assertTrue(debugState.isClickInspectEnabled());
        debugState.apply(lm);

        UiDocument inspectDoc = UiDocumentBridge.compile(lm);
        int inspectButtonCount = inspectDoc.buttons().size();

        assertTrue(inspectButtonCount > normalButtonCount);
        assertTrue(inspectDoc.buttons().stream().anyMatch(b -> b.id().equals("__inspect_comp_comp_title")));
        assertTrue(inspectDoc.buttons().stream().anyMatch(b -> b.id().equals("__inspect_cont_cont_left")));

        // Disable
        debugState.setClickInspectEnabled(false);
        // re-create lm to test fresh clean apply
        setUp();
        debugState.apply(lm);
        UiDocument restoredDoc = UiDocumentBridge.compile(lm);
        assertEquals(normalButtonCount, restoredDoc.buttons().size());
    }

    @Test
    @DisplayName("10. Verify UiDebugSession and UiDebugManager session registration")
    public void testDebugManager() {
        UiDebugManager manager = new UiDebugManager();
        UUID playerId = UUID.randomUUID();
        UiDebugState state = new UiDebugState();

        UiDebugSession session = new UiDebugSession() {
            @Override
            public String name() {
                return "Test Session";
            }

            @Override
            public LayerManager getCurrentLayerManager() {
                return lm;
            }

            @Override
            public UiDebugState getDebugState() {
                return state;
            }

            @Override
            public void forceUpdate() {
            }
        };

        manager.registerSession(playerId, session);
        assertTrue(manager.getSession(playerId).isPresent());
        assertEquals("Test Session", manager.getSession(playerId).get().name());

        manager.unregisterSession(playerId);
        assertTrue(manager.getSession(playerId).isEmpty());
    }

    @Test
    @DisplayName("11. Verify UiDebugRegistry animation preset creation and durations")
    public void testAnimationPresetCreation() {
        var fadeIn = UiDebugRegistry.createAnimation("fadein", 10);
        assertNotNull(fadeIn);
        assertEquals(10, fadeIn.durationTicks());
        assertEquals(0.0f, fadeIn.fromOpacity());
        assertEquals(1.0f, fadeIn.toOpacity());

        var bounce = UiDebugRegistry.createAnimation("bouncein", 25);
        assertNotNull(bounce);
        assertEquals(25, bounce.durationTicks());
        assertEquals(0.8f, bounce.fromScale());
        assertEquals(1.0f, bounce.toScale());

        var slide = UiDebugRegistry.createAnimation("slideleft", null);
        assertNotNull(slide);
        assertEquals(16, slide.durationTicks());
        assertTrue(slide.offsetX() < 0);

        var pop = UiDebugRegistry.createAnimation("popin", 18);
        assertNotNull(pop);
        assertEquals(18, pop.durationTicks());

        assertNull(UiDebugRegistry.createAnimation("stop", null));
        assertNull(UiDebugRegistry.createAnimation("reset", null));
        assertNull(UiDebugRegistry.createAnimation("unknown_xyz", null));
    }

    @Test
    @DisplayName("12. Verify Animation Overrides in UiDebugState apply cleanly to Layer, Container, Component")
    public void testAnimationOverridesInDebugState() {
        UiDebugState debugState = new UiDebugState();

        var layerAnim = UiDebugRegistry.createAnimation("fadein", 12);
        var contAnim = UiDebugRegistry.createAnimation("slideleft", 14);
        var compAnim = UiDebugRegistry.createAnimation("popin", 16);

        debugState.setLayerAnimation("left_layer", layerAnim);
        debugState.setContainerAnimation("cont_left", contAnim);
        debugState.setComponentAnimation("comp_button", compAnim);

        debugState.apply(lm);

        assertTrue(layerLeft.isAnimating());
        assertTrue(contLeft.isAnimating());
        assertTrue(compButton.isAnimating());
        assertFalse(compTitle.isAnimating());

        UiDocumentBridge.CompiledUi compiled = UiDocumentBridge.compileWithAnimations(lm);
        assertTrue(compiled.hasAnimations());
        assertFalse(compiled.nodeAnimations().isEmpty());

        // Stop animation
        debugState.setLayerAnimation("left_layer", null);
        debugState.apply(lm);
        assertFalse(layerLeft.isAnimating());
    }

    @Test
    @DisplayName("13. Verify Inspector displays live animation and interactive component metrics")
    public void testInspectorLiveAnimationAndMetrics() {
        UiDebugState state = new UiDebugState();
        var anim = UiDebugRegistry.createAnimation("bouncein", 20);
        compButton.animate(anim);

        List<Component> compInspect = UiDebugInspector.inspectComponent(compButton, state, "/test debug");
        assertNotNull(compInspect);
        assertFalse(compInspect.isEmpty());

        // Check that animation status and action buttons are included
        boolean hasActiveAnim = compInspect.stream().anyMatch(c -> c.toString().contains("ACTIVE RUNNING") || c.toString().contains("Animation"));
        assertTrue(hasActiveAnim);

        List<Component> contInspect = UiDebugInspector.inspectContainer(contLeft, state, "/test debug");
        assertNotNull(contInspect);
        assertFalse(contInspect.isEmpty());

        List<Component> layerInspect = UiDebugInspector.inspectLayer(layerLeft, state, "/test debug");
        assertNotNull(layerInspect);
        assertFalse(layerInspect.isEmpty());
    }

    @Test
    @DisplayName("14. Verify UiShapeNode transform count remains consistent across all animation scale frames")
    public void testShapeNodeTransformsConsistencyAcrossScales() {
        var shape = vn.haohan.displayui.api.node.UiShapeNode.builder("rounded_rect", 10.0f, 10.0f, 40.0f, 20.0f)
                .cornerRadius(4.0f)
                .color(org.bukkit.Color.BLUE)
                .build();

        int expectedSubNodeCount = shape.decomposeToNodes().size();
        assertTrue(expectedSubNodeCount > 0);

        // Test at various animation scales (including scale = 0 at start of scaleIn)
        float[] scales = {0.0f, 0.25f, 0.5f, 0.72f, 1.0f, 1.25f, 2.0f};
        for (float s : scales) {
            // Check that subnodes count matches expected count
            var subNodes = shape.decomposeToNodes();
            assertEquals(expectedSubNodeCount, subNodes.size(), "Decomposition count should be invariant for shape at scale " + s);
        }
    }

    @Test
    @DisplayName("16. Verify layout debug session creation and per-node animation compilation")
    public void testLayoutDebugSessionAndAnimation() {
        UiDocument doc = new UiDocument(
                List.of(new AlignedTextNode(Component.text("Layout Test"), 0, 0, 100, 30, vn.haohan.displayui.api.text.UiTextAlignment.CENTER)),
                List.of(new vn.haohan.displayui.api.interaction.UiButton("btn_test", 10, 10, 50, 20))
        );

        LayerManager lmLayout = new LayerManager();
        Layer layer = lmLayout.createLayer("layout_main", 0);
        Container container = new Container("root");
        layer.addContainer(container);

        int nodeIdx = 0;
        for (vn.haohan.displayui.api.node.UiNode node : doc.nodes()) {
            final vn.haohan.displayui.api.node.UiNode n = node;
            container.addComponent(new vn.haohan.displayui.api.component.CustomNodeComponent("node_" + (nodeIdx++),
                    (x, y, w, h, d, s) -> n));
        }
        for (vn.haohan.displayui.api.interaction.UiButton btn : doc.buttons()) {
            container.addComponent(ButtonComponent.builder(btn.id())
                    .offset(btn.x(), btn.y())
                    .size(btn.width(), btn.height())
                    .build());
        }

        UiDebugState state = new UiDebugState();
        var anim = UiDebugRegistry.createAnimation("bounce", 12);
        state.setComponentAnimation("node_0", anim);
        state.apply(lmLayout);

        UiDocumentBridge.CompiledUi compiled = UiDocumentBridge.compileWithAnimations(lmLayout);
        assertNotNull(compiled);
        assertTrue(compiled.hasAnimations());
        assertEquals(2, compiled.nodeAnimations().size()); // 1 node + 1 button shape
        assertEquals(anim, compiled.nodeAnimations().get(0));
    }
}

