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
package vn.haohan.displayui.api;

import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.animation.UiEasing;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiButtonAction;
import vn.haohan.displayui.api.interaction.UiCheckbox;
import vn.haohan.displayui.api.interaction.UiSlider;
import vn.haohan.displayui.api.layout.UiAnchor;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.layout.UiRect;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.TextNode;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.api.text.UiText;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.text.UiTextOpticalPreset;
import vn.haohan.displayui.api.text.UiVerticalAlignment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiModelTest {
    @Test
    void rectDerivesEdgesInsetsAndAnchoredChildren() {
        UiRect panel = UiRect.centered(0, 0, 192, 128);
        assertEquals(-96.0f, panel.left());
        assertEquals(-64.0f, panel.top());
        assertEquals(96.0f, panel.right());
        assertEquals(64.0f, panel.bottom());

        UiRect content = panel.inset(8, 10);
        assertEquals(-88.0f, content.left());
        assertEquals(-54.0f, content.top());
        assertEquals(176.0f, content.width());
        assertEquals(108.0f, content.height());

        UiRect close = panel.place(UiAnchor.TOP_RIGHT, UiAnchor.TOP_RIGHT,
                16, 16, -8, 8);
        assertEquals(72.0f, close.left());
        assertEquals(-56.0f, close.top());
        assertEquals(88.0f, close.right());
        assertEquals(-40.0f, close.bottom());

        AlignedTextNode title = new AlignedTextNode(
                Component.text("Title"), content, UiTextAlignment.LEFT);
        UiButton button = new UiButton("content", content);
        assertEquals(content.left(), title.boxX());
        assertEquals(content.top(), title.boxY());
        assertEquals(content, new UiRect(
                button.x(), button.y(), button.width(), button.height()));
    }

    @Test
    void rectRejectsInvalidGeometry() {
        assertThrows(IllegalArgumentException.class,
                () -> new UiRect(0, 0, 0, 10));
        assertThrows(IllegalArgumentException.class,
                () -> UiRect.centered(0, 0, Float.NaN, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new UiRect(0, 0, 10, 10).inset(6));
    }

    @Test
    void documentDefensivelyCopiesItsNodeList() {
        var source = new ArrayList<UiNode>();
        source.add(TextNode.left(Component.text("Title"), 0, 0, 160));
        UiDocument document = new UiDocument(source);
        source.clear();

        assertEquals(1, document.nodes().size());
        assertThrows(UnsupportedOperationException.class,
                () -> document.nodes().add(TextNode.left(Component.empty(), 0, 0, 1)));
    }

    @Test
    void textNodeRejectsInvalidLayoutValues() {
        assertThrows(IllegalArgumentException.class, () -> new TextNode(
                Component.empty(), 0, 0, 0, 0, 0.5f,
                TextDisplay.TextAlignment.LEFT, false, false));
        assertThrows(IllegalArgumentException.class, () -> new TextNode(
                Component.empty(), 0, 0, 0, 10, 0.0f,
                TextDisplay.TextAlignment.LEFT, false, false));
    }

    @Test
    void defaultOptionsAreSafeForShortRangeUi() {
        UiOptions options = UiOptions.defaults();
        assertEquals(40.0f, options.pixelsPerBlock());
        assertEquals(8.0, options.maxDistance());
        assertEquals("haohan_display_ui", options.scoreboardTag());
        assertEquals(Display.Billboard.FIXED, options.cameraTransform().billboard());
        assertEquals("minecraft:ui.button.click", options.clickSound());
        assertEquals(0.7f, options.clickSoundVolume());
        assertTrue(options.cullItemBackfaces());
    }

    @Test
    void itemBackfaceCullingIsOptIn() {
        UiOptions options = UiOptions.defaults().withItemBackfaceCulling(true);
        assertTrue(options.cullItemBackfaces());
        assertTrue(options.withCameraTransform(
                UiCameraTransform.cameraFacing()).cullItemBackfaces());
    }

    @Test
    void interactionSoundCanBeCustomizedOrDisabled() {
        UiOptions options = UiOptions.defaults()
                .withClickSound("my_pack:menu.tick", 0.4f, 1.2f);
        assertEquals("my_pack:menu.tick", options.clickSound());
        assertEquals(0.4f, options.clickSoundVolume());
        assertEquals(1.2f, options.clickSoundPitch());
        assertEquals(null, options.withoutClickSound().clickSound());
        assertThrows(IllegalArgumentException.class,
                () -> options.withClickSound("bad", -1.0f, 1.0f));
    }

    @Test
    void animationPresetsAreEasyToComposeAndValidate() {
        UiAnimation slide = UiAnimation.slideIn(
                8, UiAnimation.Direction.TOP, 24.0f, UiEasing.QUAD_OUT).delay(2);
        assertEquals(8, slide.durationTicks());
        assertEquals(2, slide.delayTicks());
        assertEquals(-24.0f, slide.offsetY());
        assertEquals(UiEasing.QUAD_OUT, slide.easing());

        UiAnimation combined = UiAnimation.builder()
                .durationTicks(12)
                .easing(UiEasing.EASE_IN_OUT)
                .opacity(0.0f, 1.0f)
                .scale(0.8f, 1.0f)
                .offset(UiAnimation.Direction.BOTTOM, 10.0f)
                .build();
        assertEquals(0.0f, combined.fromOpacity());
        assertEquals(0.8f, combined.fromScale());
        assertEquals(10.0f, combined.offsetY());

        assertThrows(IllegalArgumentException.class,
                () -> UiAnimation.fadeIn(0));
        assertThrows(IllegalArgumentException.class,
                () -> UiAnimation.builder().opacity(-0.1f, 1.0f).build());
    }

    @Test
    void easingCurvesAreNormalizedAtTheirEndpoints() {
        for (UiEasing easing : UiEasing.values()) {
            assertEquals(0.0, easing.apply(0.0), 0.000001, easing.name());
            assertEquals(1.0, easing.apply(1.0), 0.000001, easing.name());
        }
        assertTrue(UiEasing.EASE_OUT.apply(0.5) > 0.5);
        assertTrue(UiEasing.EASE_IN.apply(0.5) < 0.5);
    }

    @Test
    void cameraTransformMapsAxisLocksAndAngles() {
        UiCameraTransform transform = UiCameraTransform.cameraFacing()
                .lockX(true).angleY(45).angleZ(10);
        assertEquals(Display.Billboard.VERTICAL, transform.billboard());
        assertEquals(45.0f, transform.angleY());
        assertEquals(10.0f, transform.angleZ());
        assertEquals(Display.Billboard.HORIZONTAL,
                UiCameraTransform.fixed().locks(false, true, true).billboard());
        assertEquals(Display.Billboard.CENTER,
                UiCameraTransform.cameraFacing().billboard());
    }

    @Test
    void buttonActionsValidateLinksAndCommands() {
        assertEquals(UiButtonAction.Type.RUN_PLAYER_COMMAND,
                UiButtonAction.playerCommand("/say hello").type());
        assertEquals("say hello", UiButtonAction.playerCommand("/say hello").value());
        assertEquals(UiButtonAction.Type.RUN_PLAYER_COMMAND,
                UiButtonAction.executeCommand("/seed").type());
        assertEquals(UiButtonAction.Type.OPEN_URL,
                UiButtonAction.openUrl("https://example.com").type());
        assertThrows(IllegalArgumentException.class,
                () -> UiButtonAction.openUrl("file:///secret"));
    }

    @Test
    void alignedTextCanCreateAnInteractiveButtonFromItsBounds() {
        AlignedTextNode text = new AlignedTextNode(Component.text("Docs"),
                5, 7, 40, 12, UiTextAlignment.LEFT);
        UiButton button = UiButton.forText("docs", text)
                .withAction(UiButtonAction.openUrl("https://example.com"));
        assertEquals(5.0f, button.x());
        assertEquals(7.0f, button.y());
        assertEquals(40.0f, button.width());
        assertEquals(UiButtonAction.Type.OPEN_URL, button.action().type());
    }

    @Test
    void buttonUsesInclusivePixelBounds() {
        UiButton button = new UiButton("next_page", 10, 20, 40, 16);
        assertTrue(button.contains(10, 20));
        assertTrue(button.contains(50, 36));
        assertTrue(button.hitSlop(3).contains(7, 17));
        assertTrue(button.hitSlop(3).contains(53, 39));
        assertThrows(IllegalArgumentException.class, () -> button.hitSlop(-1));
        assertThrows(IllegalArgumentException.class,
                () -> new UiButton("Bad ID", 0, 0, 10, 10));
        UiButton described = button.describedBy(Component.text("Next page"));
        assertEquals(Component.text("Next page"), described.description());
    }

    @Test
    void sliderMapsClicksToClampedAndSteppedValues() {
        UiSlider slider = new UiSlider("volume", 10, 20, 100, 12,
                0.0, 1.0, 0.5, 0.1, Component.text("Volume"));
        assertEquals(0.0, slider.valueAt(0), 0.000001);
        assertEquals(0.5, slider.valueAt(60), 0.000001);
        assertEquals(1.0, slider.valueAt(1000), 0.000001);
        assertEquals(0.5, slider.withValue(0.54).value(), 0.000001);
        assertEquals(0.5, slider.progress(), 0.000001);
        assertEquals(100.0f, slider.trackRect().width());
        assertEquals(50.0f, slider.fillRect(0).width(), 0.000001f);
        assertEquals(60.0f, slider.thumbRect(8, 10).centerX(), 0.000001f);
        assertThrows(IllegalArgumentException.class,
                () -> new UiSlider("bad", 0, 0, 10, 10, 1, 1, 1));
    }

    @Test
    void checkboxIsImmutableAndTogglesCleanly() {
        UiCheckbox checkbox = new UiCheckbox("enabled", 0, 0, 16, 16, false);
        assertTrue(!checkbox.checked());
        assertTrue(checkbox.checked(true).checked());
        assertTrue(checkbox.contains(16, 16));
        assertEquals(16.0f, checkbox.indicatorRect().width());
    }

    @Test
    void documentSupportsControlsAndRejectsDuplicateInteractionIds() {
        UiSlider slider = new UiSlider("value", 0, 0, 100, 12, 0, 10, 5);
        UiCheckbox checkbox = new UiCheckbox("enabled", 0, 20, 16, 16, false);
        UiDocument document = UiDocument.builder()
                .slider(slider)
                .checkbox(checkbox)
                .build();
        assertEquals(2, document.controls().size());
        assertThrows(IllegalArgumentException.class, () -> new UiDocument(
                List.of(), List.of(new UiButton("same", 0, 0, 1, 1)),
                List.of(new UiCheckbox("same", 0, 0, 1, 1, false))));
    }

    @Test
    void alignedTextCalculatesAnchorsLikeABoxLayoutRenderer() {
        Component text = Component.text("Aligned");
        AlignedTextNode left = new AlignedTextNode(
                text, 10, 20, 100, 20, UiTextAlignment.LEFT).offsets(5, 7).contentWidth(40);
        AlignedTextNode right = new AlignedTextNode(
                text, 10, 20, 100, 20, UiTextAlignment.RIGHT).offsets(5, 7).contentWidth(40);
        AlignedTextNode center = new AlignedTextNode(
                text, 10, 20, 100, 20, UiTextAlignment.CENTER).offsets(5, 7).contentWidth(40);

        assertEquals(35.0f, left.x());
        assertEquals(83.0f, right.x());
        assertEquals(60.0f, center.x());
        assertEquals(28.0f, center.y());
        assertEquals(34.0f, left.nudgeX(-1.0f).x());
        assertEquals(34.0f,
                left.opticalPreset(UiTextOpticalPreset.ITALIC).x());
        assertEquals(37.0f,
                left.opticalPreset(UiTextOpticalPreset.BOLD).x());
        assertEquals(38.0f,
                left.opticalPreset(UiTextOpticalPreset.BOLD_GRADIENT).x());
    }

    @Test
    void defaultFontWidthEstimateScalesWithFontSize() {
        Component text = Component.text("Hi!");
        assertEquals(10.0f, UiText.estimateWidth(text, 10.0f));
        assertEquals(5.0f, UiText.estimateWidth(text, 5.0f));
        assertEquals(UiText.estimateWidth(Component.text("Blaze Rod"), 6.8f),
                UiText.estimateWidth(Component.translatable("item.minecraft.blaze_rod")
                        .fallback("Blaze Rod"), 6.8f));
    }

    @Test
    void alignedTextSupportsTopCenterAndBottomPlacement() {
        Component text = Component.text("Label");
        AlignedTextNode node = new AlignedTextNode(
                text, 10, 20, 100, 30, UiTextAlignment.LEFT)
                .fontSize(10)
                .verticalOffset(0);

        assertEquals(25.0f, node.verticalAlignment(UiVerticalAlignment.TOP).y());
        assertEquals(35.0f, node.verticalAlignment(UiVerticalAlignment.CENTER).y());
        assertEquals(45.0f, node.verticalAlignment(UiVerticalAlignment.BOTTOM).y());
    }

    @Test
    void richTextSupportsMultiStopGradientAndIntensityFactor() {
        TextColor red = TextColor.color(255, 0, 0);
        TextColor green = TextColor.color(0, 255, 0);
        TextColor blue = TextColor.color(0, 0, 255);
        Component full = UiText.builder()
                .gradient("abc", new TextColor[] {red, green, blue}, 1.0,
                        net.kyori.adventure.text.format.TextDecoration.BOLD)
                .build();
        Component disabled = UiText.builder()
                .gradient("abc", new TextColor[] {red, green, blue}, 0.0)
                .build();

        assertEquals(red, full.children().getFirst().color());
        assertEquals(blue, full.children().getLast().color());
        assertEquals(red, disabled.children().getLast().color());
        assertEquals(net.kyori.adventure.text.format.TextDecoration.State.TRUE,
                full.children().getFirst().decoration(
                        net.kyori.adventure.text.format.TextDecoration.BOLD));
        assertThrows(IllegalArgumentException.class, () -> UiText.builder()
                .gradient("bad", new TextColor[] {red, blue}, 1.1));
    }
}
