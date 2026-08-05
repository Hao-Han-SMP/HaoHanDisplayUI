package dev.haohansmp.displayui.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiModelTest {
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
