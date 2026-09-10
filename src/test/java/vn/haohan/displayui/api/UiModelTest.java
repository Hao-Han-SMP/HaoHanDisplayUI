/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HaoHanDisplayUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HaoHanDisplayUI. If not, see <https://www.gnu.org/licenses/>.
 */
package vn.haohan.displayui.api;

import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiButtonAction;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.EntityModelNode;
import vn.haohan.displayui.api.node.UiModelRotation;
import vn.haohan.displayui.api.text.UiTextAlignment;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class UiModelTest {

    private ItemStack mockItem(Material material) {
        ItemStack item = Mockito.mock(ItemStack.class);
        when(item.getType()).thenReturn(material);
        when(item.clone()).thenReturn(item);
        return item;
    }

    @Test
    void entityModelNodeConstructsCorrectlyAndValidatesInput() {
        ItemStack sword = mockItem(Material.DIAMOND_SWORD);
        EntityModelNode model = new EntityModelNode(sword, 50.0f, 60.0f, 1.5f)
                .withRotation(45.0f, 15.0f, 0.0f)
                .withSize(64.0f, 64.0f)
                .withTransform(ItemDisplay.ItemDisplayTransform.FIXED)
                .lockYaw(false)
                .lockPitch(true)
                .yawRange(-90.0f, 90.0f)
                .step(15.0f)
                .sensitivity(1.2f);

        assertEquals(50.0f, model.x());
        assertEquals(60.0f, model.y());
        assertEquals(1.5f, model.scale());
        assertEquals(1.5f, model.scaleX());
        assertEquals(1.5f, model.scaleY());
        assertEquals(1.5f, model.scaleZ());
        assertEquals(64.0f, model.width());
        assertEquals(64.0f, model.height());
        assertEquals(45.0f, model.yaw());
        assertEquals(15.0f, model.pitch());
        assertEquals(0.0f, model.roll());
        assertEquals(ItemDisplay.ItemDisplayTransform.FIXED, model.transform());
        assertTrue(model.hoverRotatable());
        assertFalse(model.rotation().lockYaw());
        assertTrue(model.rotation().lockPitch());
        assertEquals(-90.0f, model.rotation().minYaw());
        assertEquals(90.0f, model.rotation().maxYaw());
        assertEquals(15.0f, model.rotation().step());
        assertEquals(1.2f, model.rotation().sensitivity());

        // Bounding box hit check
        assertTrue(model.contains(50.0f, 60.0f));
        assertTrue(model.contains(20.0f, 30.0f));
        assertTrue(model.contains(80.0f, 90.0f));
        assertFalse(model.contains(15.0f, 60.0f));
        assertFalse(model.contains(50.0f, 95.0f));

        // Validation rejects
        ItemStack airItem = mockItem(Material.AIR);
        assertThrows(IllegalArgumentException.class, () -> new EntityModelNode(airItem, 0, 0, 1.0f));
        assertThrows(IllegalArgumentException.class, () -> model.withScale(0.0f));
        assertThrows(IllegalArgumentException.class, () -> model.withSize(-10.0f, 10.0f));
    }

    @Test
    void modelRotationHandlesClampingAxisLocksAndStepSnapping() {
        UiModelRotation rotation = UiModelRotation.defaults()
                .withYawRange(-45.0f, 45.0f)
                .withPitchRange(-30.0f, 30.0f)
                .withStep(15.0f);

        // Clamping & Step Snapping
        assertEquals(0.0f, rotation.clampYaw(2.0f), 0.001f);
        assertEquals(15.0f, rotation.clampYaw(12.0f), 0.001f);
        assertEquals(45.0f, rotation.clampYaw(80.0f), 0.001f);
        assertEquals(-45.0f, rotation.clampYaw(-100.0f), 0.001f);
        assertEquals(-30.0f, rotation.clampPitch(-50.0f), 0.001f);

        // Axis locking
        UiModelRotation lockedYaw = rotation.withLockYaw(true);
        assertEquals(0.0f, lockedYaw.clampYaw(30.0f), 0.001f);

        // Presets
        UiModelRotation lockedAll = UiModelRotation.locked();
        assertTrue(lockedAll.lockYaw());
        assertTrue(lockedAll.lockPitch());
        assertTrue(lockedAll.lockRoll());

        UiModelRotation spin = UiModelRotation.autoSpin(3.0f);
        assertEquals(UiModelRotation.Mode.AUTO_SPIN, spin.mode());
        assertEquals(3.0f, spin.autoSpinSpeed());
        // Continuous auto spin wrap around test
        assertEquals(10.0f, spin.clampYaw(370.0f), 0.001f);
        assertEquals(-10.0f, spin.clampYaw(-370.0f), 0.001f);
    }

    @Test
    void interactiveModelButtonIntegratesWithDocumentBuilder() {
        ItemStack helmet = mockItem(Material.NETHERITE_HELMET);
        EntityModelNode model = new EntityModelNode(helmet, 100.0f, 80.0f, 40.0f, 40.0f, 1.0f);
        UiButton button = UiButton.forModel("inspect_helmet", model)
                .describedBy(Component.text("Inspect Helmet"))
                .withAction(UiButtonAction.executeCommand("/inspect helmet"));

        assertEquals(80.0f, button.x());
        assertEquals(60.0f, button.y());
        assertEquals(40.0f, button.width());
        assertEquals(40.0f, button.height());
        assertTrue(button.contains(100.0f, 80.0f));

        UiDocument document = UiDocument.builder()
                .interactiveModel("inspect_helmet", model, Component.text("Inspect Helmet"),
                        UiButtonAction.executeCommand("/inspect helmet"))
                .build();

        assertEquals(1, document.nodes().size());
        assertEquals(1, document.buttons().size());
        assertTrue(document.nodes().get(0) instanceof EntityModelNode);
    }

    @Test
    void alignedTextNodeDefaultVerticalOffsetIsZero() {
        AlignedTextNode node = new AlignedTextNode(
                Component.text("Centered Text"), 0.0f, 0.0f, 100.0f, 20.0f, UiTextAlignment.CENTER);
        assertEquals(0.0f, node.verticalOffset());
        // 0 + 20 * 0.5 + (10.0f * 0.60f) + 0.0 = 16.0f
        assertEquals(16.0f, node.y(), 0.001f);
    }
}
