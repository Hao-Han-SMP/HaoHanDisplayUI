package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.MobEntityNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class MobShowcase3DDemoPage extends BaseDemoPage {
    @Override
    public String title() {
        return "3D MOB SHOWCASE";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        final float slotW = 52;
        final float slotH = 44;
        final float col1 = -84;
        final float col2 = -26;
        final float col3 = 32;
        final float rowY = -30;

        addMobShowcaseSlot(builder, "mob_allay", EntityType.ALLAY, "ALLAY", NamedTextColor.AQUA,
                col1, rowY, slotW, slotH, 0.36f,
                "Allay · Interactive hover & rotation");

        addMobShowcaseSlot(builder, "mob_warden", EntityType.WARDEN, "WARDEN", NamedTextColor.DARK_AQUA,
                col2, rowY, slotW, slotH, 0.46f,
                "Warden · Full 3D model with custom scale");

        addMobShowcaseSlot(builder, "mob_bee", EntityType.BEE, "BEE", NamedTextColor.GOLD,
                col3, rowY, slotW, slotH, 0.60f,
                "Bee · 3D pollinator entity model");

        builder.add(new AlignedTextNode(Component.text(
                "Real-time 3D Minecraft mob entities integrated seamlessly into Display UI",
                NamedTextColor.DARK_GRAY), -86, 18, 172, 7, UiTextAlignment.CENTER).fontSize(4.0f));

        addControlButton(builder, "mob_cmd_info", -60, 28, 120, "MODEL PACKS INFO",
                "Click for details on custom model packs");
    }

    @Override
    public boolean onClick(DemoContext context, String buttonId, Player player) {
        if ("mob_cmd_info".equals(buttonId)) {
            player.sendMessage("§d[DisplayUI] §7Mob models use custom item models or native display entities.");
            return true;
        }
        return false;
    }

    private void addMobShowcaseSlot(UiDocument.Builder builder, String id, EntityType entityType,
                                    String label, NamedTextColor accent, float x, float y,
                                    float w, float h, float scale, String description) {
        builder.add(new BlockNode(Material.GRAY_STAINED_GLASS.createBlockData(),
                x, y, 0.001f, w, h, 1));
        builder.add(new BlockNode(Material.BLACK_CONCRETE.createBlockData(),
                x + 1, y + 1, 0.002f, w - 2, h - 2, 1));

        MobEntityNode mob = new MobEntityNode(entityType, x + w * 0.5f, y + 14, scale);
        builder.add(mob);

        builder.button(new UiButton(id, x, y, w, h,
                Component.text(description, NamedTextColor.YELLOW)));

        builder.add(new AlignedTextNode(Component.text(label, accent, TextDecoration.BOLD),
                x, y + 33, w, 7, UiTextAlignment.CENTER)
                .fontSize(4.5f).shadowed(true).atDepth(0.004f));
    }
}
