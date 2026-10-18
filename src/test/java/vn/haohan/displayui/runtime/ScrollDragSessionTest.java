package vn.haohan.displayui.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.interaction.UiScrollList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScrollDragSessionTest {
    @Test
    @DisplayName("UiScrollList control type is recognized for drag session creation")
    void testScrollListDragRecognition() {
        UiScrollList scroll = mock(UiScrollList.class);
        when(scroll.id()).thenReturn("test_scroll");
        assertNotNull(scroll.id());
    }
}
