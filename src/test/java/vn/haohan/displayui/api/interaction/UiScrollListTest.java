package vn.haohan.displayui.api.interaction;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UiScrollListTest {
    @Test
    void clampsOffsetAndPreservesScrollConfiguration() {
        UiScrollList list = new UiScrollList(
                "items", 0, 0, 100, 40, 8, 99, 2,
                Component.text("Scroll items"), 1.0f);

        assertEquals(8, list.offset());
        UiScrollList reset = list.withOffset(-4);
        assertEquals(0, reset.offset());
        assertEquals(2, reset.step());
        assertEquals(1.0f, reset.hitSlop());
    }

    @Test
    void rejectsInvalidScrollArea() {
        assertThrows(IllegalArgumentException.class,
                () -> new UiScrollList("items", 0, 0, 0, 40, 1, 0, Component.empty()));
        assertThrows(IllegalArgumentException.class,
                () -> new UiScrollList("items", 0, 0, 100, 40, -1, 0, Component.empty()));
    }
}
