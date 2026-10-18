package vn.haohan.displayui.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.interaction.UiControlChange;
import vn.haohan.displayui.api.interaction.UiScrollList;
import vn.haohan.displayui.demo.pages.ChooseAppScrollListDemoPage;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChooseAppScrollListDemoPageTest {
    @Test
    @DisplayName("onControlChange should update context appOffset and trigger view update")
    void testControlChangeUpdatesView() {
        DemoContext context = new DemoContext(UUID.randomUUID());
        AtomicBoolean updated = new AtomicBoolean(false);
        vn.haohan.displayui.api.UiHandle handle = mock(vn.haohan.displayui.api.UiHandle.class);
        when(handle.isValid()).thenReturn(true);
        context.handle(handle);
        context.setPageUpdater(ctx -> updated.set(true));

        ChooseAppScrollListDemoPage page = new ChooseAppScrollListDemoPage();
        UiScrollList scrollList = mock(UiScrollList.class);
        when(scrollList.id()).thenReturn("demo_apps");

        UiControlChange change = mock(UiControlChange.class);
        when(change.control()).thenReturn(scrollList);
        when(change.value()).thenReturn(33.0);

        page.onControlChange(context, change);

        assertEquals(33.0f, context.appOffset(), 0.001f);
        assertTrue(updated.get(), "updateView must be invoked on control change");
    }
}
