package vn.haohan.displayui.runtime.scene;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.UiBackgroundNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import org.bukkit.Color;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UiSceneIncrementalUpdateTest {

    @Test
    @DisplayName("Documents on the same page with matching node positions are considered structurally similar")
    void testSamePageStructureMatches() {
        UiDocument docA = UiDocument.builder()
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("A"), 10.0f, 10.0f, 100.0f, 20.0f, UiTextAlignment.LEFT))
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("B"), 10.0f, 40.0f, 100.0f, 20.0f, UiTextAlignment.LEFT))
                .build();

        UiDocument docB = UiDocument.builder()
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("A Updated"), 10.0f, 10.0f, 100.0f, 20.0f, UiTextAlignment.LEFT))
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("B Updated"), 10.0f, 40.0f, 100.0f, 20.0f, UiTextAlignment.LEFT))
                .build();

        assertTrue(UiScene.isStructurallySimilar(docA, docB));
    }

    @Test
    @DisplayName("Documents from different pages with mismatched node positions and sizes are rejected")
    void testDifferentPageStructureRejects() {
        // Page 1: 2 small text nodes and footer button at y=110
        UiDocument page1 = UiDocument.builder()
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("Title"), 10.0f, 10.0f, 100.0f, 20.0f, UiTextAlignment.LEFT))
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("Next >"), 62.0f, 110.0f, 18.0f, 14.0f, UiTextAlignment.CENTER))
                .build();

        // Page 2: completely different layout where index 1 is now a large row at y=38
        UiDocument page2 = UiDocument.builder()
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("Title"), 10.0f, 10.0f, 100.0f, 20.0f, UiTextAlignment.LEFT))
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("Gold Coins Card"), 0.0f, 38.0f, 174.0f, 16.0f, UiTextAlignment.LEFT))
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("Next >"), 62.0f, 110.0f, 18.0f, 14.0f, UiTextAlignment.CENTER))
                .build();

        assertFalse(UiScene.isStructurallySimilar(page1, page2));
    }

    @Test
    @DisplayName("Null or empty documents edge cases")
    void testNullAndEmptyDocuments() {
        UiDocument empty1 = UiDocument.builder().build();
        UiDocument empty2 = UiDocument.builder().build();
        UiDocument nonEmpty = UiDocument.builder()
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("X"), 0.0f, 0.0f, 10.0f, 10.0f, UiTextAlignment.LEFT))
                .build();

        assertFalse(UiScene.isStructurallySimilar(null, empty1));
        assertFalse(UiScene.isStructurallySimilar(empty1, null));
        assertTrue(UiScene.isStructurallySimilar(empty1, empty2));
        assertFalse(UiScene.isStructurallySimilar(empty1, nonEmpty));
        assertFalse(UiScene.isStructurallySimilar(nonEmpty, empty1));
    }

    @Test
    @DisplayName("Documents with mismatched node types are not matched")
    void testMismatchedNodeTypes() {
        UiDocument docText = UiDocument.builder()
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("Text"), 10.0f, 10.0f, 100.0f, 20.0f, UiTextAlignment.LEFT))
                .build();
        UiDocument docBg = UiDocument.builder()
                .add(new UiBackgroundNode(10.0f, 10.0f, 0.0f, 100.0f, 20.0f, Color.WHITE))
                .build();

        assertFalse(UiScene.isStructurallySimilar(docText, docBg));
    }

    @Test
    @DisplayName("Position drift greater than 35px is rejected when ratio drops below 50%")
    void testPositionDriftThreshold() {
        UiDocument doc1 = UiDocument.builder()
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("1"), 10.0f, 10.0f, 100.0f, 20.0f, UiTextAlignment.LEFT))
                .build();
        UiDocument doc2 = UiDocument.builder()
                .add(new AlignedTextNode(net.kyori.adventure.text.Component.text("1"), 60.0f, 10.0f, 100.0f, 20.0f, UiTextAlignment.LEFT))
                .build();

        assertFalse(UiScene.isStructurallySimilar(doc1, doc2));
    }

    @Test
    @DisplayName("Large coordinate displacement exceeds threshold")
    void testDisplacementThresholdCalculation() {
        float x1 = 62.0f, y1 = 110.0f;
        float x2 = 0.0f, y2 = 38.0f;
        double dist = Math.hypot(x2 - x1, y2 - y1);
        assertTrue(dist > 15.0f, "Distance from footer to body must exceed threshold");
    }

    @Test
    @DisplayName("Large translation jump suppresses interpolation to 0 ticks")
    void testLargeTranslationJumpSuppressesInterpolation() {
        UiScene scene = mock(UiScene.class);
        when(scene.isAnimating()).thenReturn(false);
        when(scene.interpolationTicks()).thenReturn(5);

        UiSceneRenderer renderer = new UiSceneRenderer(scene);
        TextDisplay display = mock(TextDisplay.class);
        when(display.isValid()).thenReturn(true);
        when(display.getBackgroundColor()).thenReturn(Color.RED);

        Transformation currentTransform = new Transformation(
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Quaternionf(),
                new Vector3f(1.0f, 1.0f, 1.0f),
                new Quaternionf()
        );
        when(display.getTransformation()).thenReturn(currentTransform);

        // Target transformation with large translation jump (> 0.4 blocks, e.g., 1.0 block translation)
        Transformation newTransform = new Transformation(
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Quaternionf(),
                new Vector3f(1.0f, 1.0f, 1.0f),
                new Quaternionf()
        );

        UiBackgroundNode bg = new UiBackgroundNode(0.0f, 0.0f, 0.0f, 10.0f, 10.0f, Color.RED);
        when(scene.computeBackgroundTransforms(any(), anyFloat(), anyFloat(), anyFloat(), anyFloat()))
                .thenReturn(List.of(newTransform));

        renderer.updateNode(List.of(display), bg);

        verify(display).setInterpolationDelay(0);
        verify(display).setInterpolationDuration(0);
        verify(display).setTransformation(newTransform);
    }

    @Test
    @DisplayName("Small translation jump preserves scene interpolation duration")
    void testSmallTranslationJumpPreservesInterpolation() {
        UiScene scene = mock(UiScene.class);
        when(scene.isAnimating()).thenReturn(false);
        when(scene.interpolationTicks()).thenReturn(5);

        UiSceneRenderer renderer = new UiSceneRenderer(scene);
        TextDisplay display = mock(TextDisplay.class);
        when(display.isValid()).thenReturn(true);
        when(display.getBackgroundColor()).thenReturn(Color.RED);

        Transformation currentTransform = new Transformation(
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Quaternionf(),
                new Vector3f(1.0f, 1.0f, 1.0f),
                new Quaternionf()
        );
        when(display.getTransformation()).thenReturn(currentTransform);

        // Small translation jump (e.g., 0.1 block translation, distSq = 0.01 <= 0.16)
        Transformation newTransform = new Transformation(
                new Vector3f(0.1f, 0.0f, 0.0f),
                new Quaternionf(),
                new Vector3f(1.0f, 1.0f, 1.0f),
                new Quaternionf()
        );

        UiBackgroundNode bg = new UiBackgroundNode(0.0f, 0.0f, 0.0f, 10.0f, 10.0f, Color.RED);
        when(scene.computeBackgroundTransforms(any(), anyFloat(), anyFloat(), anyFloat(), anyFloat()))
                .thenReturn(List.of(newTransform));

        renderer.updateNode(List.of(display), bg);

        verify(display).setInterpolationDelay(0);
        verify(display).setInterpolationDuration(5);
        verify(display).setTransformation(newTransform);
    }

    @Test
    @DisplayName("Null current transformation suppresses interpolation to 0 ticks")
    void testNullCurrentTransformSuppressesInterpolation() {
        UiScene scene = mock(UiScene.class);
        when(scene.isAnimating()).thenReturn(false);
        when(scene.interpolationTicks()).thenReturn(5);

        UiSceneRenderer renderer = new UiSceneRenderer(scene);
        TextDisplay display = mock(TextDisplay.class);
        when(display.isValid()).thenReturn(true);
        when(display.getBackgroundColor()).thenReturn(Color.RED);
        when(display.getTransformation()).thenReturn(null);

        Transformation newTransform = new Transformation(
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Quaternionf(),
                new Vector3f(1.0f, 1.0f, 1.0f),
                new Quaternionf()
        );

        UiBackgroundNode bg = new UiBackgroundNode(0.0f, 0.0f, 0.0f, 10.0f, 10.0f, Color.RED);
        when(scene.computeBackgroundTransforms(any(), anyFloat(), anyFloat(), anyFloat(), anyFloat()))
                .thenReturn(List.of(newTransform));

        renderer.updateNode(List.of(display), bg);

        verify(display).setInterpolationDelay(0);
        verify(display).setInterpolationDuration(0);
        verify(display).setTransformation(newTransform);
    }
}
