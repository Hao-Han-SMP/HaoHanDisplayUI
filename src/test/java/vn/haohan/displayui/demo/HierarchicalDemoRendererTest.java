package vn.haohan.displayui.demo;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.UiNode;
import vn.haohan.displayui.demo.pages.ChooseAppScrollListDemoPage;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HierarchicalDemoRendererTest {

    @Test
    @DisplayName("Footer chrome nodes remain at fixed immutable indices when scrolling child content")
    void testFooterChromeNodeIndicesRemainImmutableDuringScroll() {
        DemoContext context = new DemoContext(UUID.randomUUID());
        List<DemoPage> pages = List.of(new ChooseAppScrollListDemoPage());
        context.page(0);

        // 1. Initial render at offset 0.0f
        context.appOffset(0.0f);
        UiDocument doc0 = HierarchicalDemoRenderer.render(pages, context);

        int prevIndex0 = -1;
        int pageIndex0 = -1;
        int nextIndex0 = -1;

        for (int i = 0; i < doc0.nodes().size(); i++) {
            UiNode node = doc0.nodes().get(i);
            if (node instanceof AlignedTextNode atn) {
                String text = atn.text().toString();
                if (text.contains("<")) prevIndex0 = i;
                else if (text.contains("1 / 1")) pageIndex0 = i;
                else if (text.contains(">")) nextIndex0 = i;
            }
        }

        assertTrue(prevIndex0 >= 0, "previous_page must exist in doc0");
        assertTrue(pageIndex0 >= 0, "page_indicator must exist in doc0");
        assertTrue(nextIndex0 >= 0, "next_page must exist in doc0");

        // 2. Render with scroll offset 5.0f (which introduces partially visible rows into the ScrollContainer)
        context.appOffset(5.0f);
        UiDocument doc5 = HierarchicalDemoRenderer.render(pages, context);

        int prevIndex5 = -1;
        int pageIndex5 = -1;
        int nextIndex5 = -1;

        for (int i = 0; i < doc5.nodes().size(); i++) {
            UiNode node = doc5.nodes().get(i);
            if (node instanceof AlignedTextNode atn) {
                String text = atn.text().toString();
                if (text.contains("<")) prevIndex5 = i;
                else if (text.contains("1 / 1")) pageIndex5 = i;
                else if (text.contains(">")) nextIndex5 = i;
            }
        }

        assertEquals(prevIndex0, prevIndex5, "previous_page index must not shift when scroll content changes");
        assertEquals(pageIndex0, pageIndex5, "page_indicator index must not shift when scroll content changes");
        assertEquals(nextIndex0, nextIndex5, "next_page index must not shift when scroll content changes");

        assertEquals(doc0.nodes().get(prevIndex0), doc5.nodes().get(prevIndex5), "previous_page node must be identical");
        assertEquals(doc0.nodes().get(pageIndex0), doc5.nodes().get(pageIndex5), "page_indicator node must be identical");
        assertEquals(doc0.nodes().get(nextIndex0), doc5.nodes().get(nextIndex5), "next_page node must be identical");
    }
}
