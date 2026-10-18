package vn.haohan.displayui.runtime.scene;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.node.UiBackgroundNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UiSceneScrollDragTest {
    @Test
    @DisplayName("isStructurallySimilar allows 20px scroll drift without respawning")
    void testScrollDriftAllowsIncrementalUpdate() {
        UiDocument doc1 = UiDocument.builder()
                .addAll(List.of(
                        new UiBackgroundNode(0.0f, 0.0f, 0.0f, 100.0f, 20.0f, org.bukkit.Color.RED),
                        new UiBackgroundNode(0.0f, 25.0f, 0.0f, 100.0f, 20.0f, org.bukkit.Color.BLUE)
                )).build();

        // Shifted by 20px
        UiDocument doc2 = UiDocument.builder()
                .addAll(List.of(
                        new UiBackgroundNode(0.0f, 20.0f, 0.0f, 100.0f, 20.0f, org.bukkit.Color.RED),
                        new UiBackgroundNode(0.0f, 45.0f, 0.0f, 100.0f, 20.0f, org.bukkit.Color.BLUE)
                )).build();

        assertTrue(UiScene.isStructurallySimilar(doc1, doc2),
                "Should recognize 20px drift as structurally similar for smooth incremental update");
    }
}
