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

import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.node.MobEntityNode;
import vn.haohan.displayui.api.node.UiModelRotation;

import static org.junit.jupiter.api.Assertions.*;

public class MobEntityNodeTest {

    @Test
    void testAttribute() {
        assertNotNull(Attribute.GENERIC_SCALE);
    }

    @Test
    void testMobEntityNodeCreation() {
        MobEntityNode node = new MobEntityNode(EntityType.COW, 0, 0, 1.0f)
                .withYaw(45.0f)
                .withPitch(10.0f)
                .autoSpin(3.0f);

        assertEquals(EntityType.COW, node.entityType());
        assertEquals(0, node.x());
        assertEquals(0, node.y());
        assertEquals(1.0f, node.scale());
        assertEquals(45.0f, node.yaw());
        assertEquals(10.0f, node.pitch());
        assertEquals(UiModelRotation.Mode.AUTO_SPIN, node.rotation().mode());
        assertEquals(3.0f, node.rotation().autoSpinSpeed());
    }

    @Test
    void testMobCursorTrackingAndHitbox() {
        MobEntityNode node = new MobEntityNode(EntityType.PIG, 20, 30, 40, 40, 0.8f)
                .yawRange(-45, 45)
                .pitchRange(-20, 20)
                .lockPitch(true)
                .step(15)
                .sensitivity(1.5f);

        assertEquals(20, node.x());
        assertEquals(30, node.y());
        assertEquals(40, node.width());
        assertEquals(40, node.height());
        assertEquals(0.8f, node.scale());
        assertTrue(node.rotation().lockPitch());
        assertEquals(15.0f, node.rotation().step());
        assertEquals(1.5f, node.rotation().sensitivity());

        UiButton button = UiButton.forMob("test_mob", node);
        assertEquals("test_mob", button.id());
        assertEquals(0, button.x()); // 20 - 40/2
        assertEquals(10, button.y()); // 30 - 40/2
        assertEquals(40, button.width());
        assertEquals(40, button.height());
    }

    @Test
    void testDocumentBuilderWithMob() {
        MobEntityNode node = new MobEntityNode(EntityType.ALLAY, 0, 0, 0.5f).hoverSpin(2.0f);
        UiDocument doc = UiDocument.builder()
                .interactiveMob("allay_btn", node, Component.text("Hover Allay"), null)
                .build();

        assertEquals(1, doc.nodes().size());
        assertEquals(1, doc.buttons().size());
        assertSame(node, doc.nodes().get(0));
        assertEquals("allay_btn", doc.buttons().get(0).id());
    }
}
