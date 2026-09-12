package vn.haohan.displayui.demo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.UiBackgroundNode;
import vn.haohan.displayui.api.node.UiShapeNode;
import vn.haohan.displayui.api.text.UiTextAlignment;

public abstract class BaseDemoPage implements DemoPage {
    public static void addControlButton(UiDocument.Builder builder, String id, float x,
                                         float y, String label, String description) {
        addControlButton(builder, id, x, y, 78, label, description);
    }

    public static void addControlButton(UiDocument.Builder builder, String id, float x,
                                         float y, float width, String label, String description) {
        builder.add(UiShapeNode.builder("rounded_rect", x, y, width, 14)
                        .color(Color.fromRGB(35, 45, 65))
                        .cornerRadius(4.0f)
                        .outline(true)
                        .outlineColor(Color.fromRGB(80, 140, 240))
                        .outlineThickness(1.0f)
                        .outlineStyle("solid")
                        .depth(0.001f)
                        .build())
                .add(new AlignedTextNode(Component.text(label, NamedTextColor.AQUA),
                        x, y, width, 14, UiTextAlignment.CENTER)
                        .fontSize(4.5f).verticalOffset(0).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, x, y, width, 14,
                        Component.text(description, NamedTextColor.YELLOW)));
    }

    public static void addSlotFrame(UiDocument.Builder builder, float x, float y, float w, float h) {
        builder.add(new UiBackgroundNode(x, y, 0.001f, w, h, Color.fromRGB(60, 60, 70), false));
        builder.add(new UiBackgroundNode(x + 1, y + 1, 0.002f, w - 2, h - 2, Color.fromRGB(20, 20, 25), false));
    }
}
