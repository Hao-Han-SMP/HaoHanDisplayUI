package vn.haohan.displayui.demo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.text.UiTextAlignment;

public abstract class BaseDemoPage implements DemoPage {
    public static void addControlButton(UiDocument.Builder builder, String id, float x,
                                         float y, String label, String description) {
        addControlButton(builder, id, x, y, 78, label, description);
    }

    public static void addControlButton(UiDocument.Builder builder, String id, float x,
                                         float y, float width, String label, String description) {
        builder.add(new BlockNode(Material.GRAY_CONCRETE.createBlockData(),
                        x, y, 0.001f, width, 16, 1))
                .add(new AlignedTextNode(Component.text(label, NamedTextColor.AQUA),
                        x, y, width, 16, UiTextAlignment.CENTER)
                        .fontSize(5).verticalOffset(0).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, x, y, width, 16,
                        Component.text(description, NamedTextColor.YELLOW)));
    }

    public static void addSlotFrame(UiDocument.Builder builder, float x, float y, float w, float h) {
        builder.add(new BlockNode(Material.GRAY_STAINED_GLASS.createBlockData(),
                x, y, 0.001f, w, h, 1));
        builder.add(new BlockNode(Material.BLACK_CONCRETE.createBlockData(),
                x + 1, y + 1, 0.002f, w - 2, h - 2, 1));
    }
}
