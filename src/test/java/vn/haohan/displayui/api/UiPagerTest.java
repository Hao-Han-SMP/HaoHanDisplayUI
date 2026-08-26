package vn.haohan.displayui.api;

import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.node.TextNode;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class UiPagerTest {
    private static UiDocument page(String text) {
        return new UiDocument(List.of(TextNode.left(Component.text(text), 0, 0, 40)));
    }

    @Test
    void wrapsAroundAndNotifiesOnlyWhenPageChanges() {
        UiPager pager = new UiPager(List.of(page("one"), page("two"), page("three")));
        AtomicInteger changedTo = new AtomicInteger(-1);
        pager.onPageChange(changedTo::set);

        assertTrue(pager.next());
        assertEquals(1, pager.index());
        assertEquals(1, changedTo.get());
        assertTrue(pager.goTo(-1));
        assertEquals(2, pager.index());
        assertFalse(pager.goTo(2));
    }

    @Test
    void boundedPagerRejectsOutOfRangePages() {
        UiPager pager = new UiPager(List.of(page("one"), page("two")), false);

        assertFalse(pager.previous());
        assertFalse(pager.goTo(2));
        assertEquals(0, pager.index());
        assertTrue(pager.goTo(1));
    }
}
