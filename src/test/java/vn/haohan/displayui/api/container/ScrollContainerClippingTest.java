package vn.haohan.displayui.api.container;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.bridge.UiDocumentBridge;
import vn.haohan.displayui.api.node.UiBackgroundNode;

import static org.junit.jupiter.api.Assertions.*;

class ScrollContainerClippingTest {
    @Test
    @DisplayName("Partially visible row background is clamped within ScrollContainer viewport")
    void testPartiallyVisibleRowIsClamped() {
        ScrollContainer scroll = ScrollContainer.builder("scroll")
                .anchor(vn.haohan.displayui.api.layout.UiAnchorPoint.TOP_LEFT)
                .origin(vn.haohan.displayui.api.layout.UiAnchorPoint.TOP_LEFT)
                .size(100.0f, 60.0f)
                .maxScrollOffset(50.0f)
                .scrollOffset(5.0f) // Shifted up by 5px
                .build();

        // Row 0: offset y = 0, height = 20. When scrollOffset = 5, globalY is -5. Visible portion is y=0, h=15.
        Container row0 = Container.builder("row0")
                .anchor(vn.haohan.displayui.api.layout.UiAnchorPoint.TOP_LEFT)
                .origin(vn.haohan.displayui.api.layout.UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 0.0f)
                .size(90.0f, 20.0f)
                .backgroundColor(org.bukkit.Color.RED)
                .build();
        scroll.addContainer(row0);

        UiDocument doc = UiDocumentBridge.compile(scroll);
        UiBackgroundNode bg = doc.nodes().stream()
                .filter(n -> n instanceof UiBackgroundNode)
                .map(n -> (UiBackgroundNode) n)
                .findFirst()
                .orElse(null);

        assertNotNull(bg, "Partially visible row should not be omitted");
        assertEquals(0.0f, bg.y(), 0.01f, "Top must be clamped to viewport boundary");
        assertEquals(15.0f, bg.height(), 0.01f, "Height must be clamped to remaining visible height");
    }
}
