package vn.haohan.displayui.runtime.screen;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.runtime.scene.UiScene;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UiScreenModeHotbarTest {

    @Test
    @DisplayName("clearAndSaveHotbar saves slots 0..8 and clears them, restoreHotbar restores them")
    void testClearAndRestoreHotbar() {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(inventory);

        UiScene scene = mock(UiScene.class);
        when(scene.origin()).thenReturn(new Location(null, 0, 0, 0));

        ItemStack slot0 = mock(ItemStack.class);
        ItemStack slot0Clone = mock(ItemStack.class);
        when(slot0.clone()).thenReturn(slot0Clone);

        ItemStack slot4 = mock(ItemStack.class);
        ItemStack slot4Clone = mock(ItemStack.class);
        when(slot4.clone()).thenReturn(slot4Clone);

        when(inventory.getItem(0)).thenReturn(slot0);
        when(inventory.getItem(4)).thenReturn(slot4);

        UiScreenMode mode = new UiScreenMode(player, scene);

        mode.clearAndSaveHotbar();

        for (int i = 0; i < 9; i++) {
            verify(inventory).setItem(eq(i), isNull());
        }

        mode.restoreHotbar();

        verify(inventory).setItem(0, slot0Clone);
        verify(inventory).setItem(4, slot4Clone);
        for (int i = 0; i < 9; i++) {
            if (i != 0 && i != 4) {
                verify(inventory, times(2)).setItem(eq(i), isNull());
            }
        }

        // Calling restoreHotbar again should be a no-op
        clearInvocations(inventory);
        mode.restoreHotbar();
        verifyNoInteractions(inventory);
    }
}
