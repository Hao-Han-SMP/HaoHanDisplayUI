package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiCheckbox;
import vn.haohan.displayui.api.interaction.UiControlChange;
import vn.haohan.displayui.api.interaction.UiSlider;
import vn.haohan.displayui.api.layout.UiRect;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class LiveControlsDemoPage extends BaseDemoPage {
    @Override
    public String title() {
        return "LIVE CONTROLS";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        builder.add(new AlignedTextNode(
                Component.text("MASTER VOLUME", NamedTextColor.WHITE, TextDecoration.BOLD),
                -86, -36, 172, 8, UiTextAlignment.LEFT).fontSize(5));
        builder.add(new AlignedTextNode(
                Component.text((int) Math.round(context.volume() * 100.0) + "%",
                        NamedTextColor.YELLOW, TextDecoration.BOLD),
                -86, -36, 172, 8, UiTextAlignment.RIGHT).fontSize(5));

        UiSlider slider = new UiSlider("demo_volume", -86, -25, 172, 12,
                0.0, 1.0, context.volume(), 0.05,
                Component.text("Adjust master volume"));
        builder.slider(slider);

        UiRect track = slider.trackRect();
        UiRect fill = slider.fillRect(2);
        UiRect thumb = slider.thumbRect(8, 14);

        builder.add(new BlockNode(Material.BLACK_CONCRETE.createBlockData(),
                track.x(), track.y() + 4, 0.001f, track.width(), 4, 1));
        if (fill.width() > 0.0f) {
            builder.add(new BlockNode(Material.LIGHT_BLUE_CONCRETE.createBlockData(),
                    fill.x(), fill.y() + 4, 0.002f, fill.width(), 4, 1));
        }
        builder.add(new BlockNode(Material.WHITE_CONCRETE.createBlockData(),
                thumb.x(), thumb.y(), 0.003f, thumb.width(), thumb.height(), 1));

        builder.add(new AlignedTextNode(
                Component.text("PARTICLE EFFECTS", NamedTextColor.WHITE, TextDecoration.BOLD),
                -86, -7, 172, 8, UiTextAlignment.LEFT).fontSize(5));
        builder.add(new AlignedTextNode(
                Component.text(context.enabled() ? "ENABLED" : "DISABLED",
                        context.enabled() ? NamedTextColor.GREEN : NamedTextColor.RED,
                        TextDecoration.BOLD),
                -86, -7, 172, 8, UiTextAlignment.RIGHT).fontSize(5));

        UiCheckbox checkbox = new UiCheckbox("demo_enabled", -86, 4, 16, 16,
                context.enabled(), Component.text("Toggle particle effects"));
        builder.checkbox(checkbox);

        builder.add(new BlockNode(Material.BLACK_CONCRETE.createBlockData(),
                -86, 4, 0.001f, 16, 16, 1));
        builder.add(new BlockNode((context.enabled() ? Material.LIME_CONCRETE
                : Material.GRAY_CONCRETE).createBlockData(),
                -84, 6, 0.002f, 12, 12, 1));
        builder.add(new AlignedTextNode(
                Component.text(context.enabled() ? "✔" : "✖",
                        context.enabled() ? NamedTextColor.BLACK : NamedTextColor.DARK_GRAY,
                        TextDecoration.BOLD),
                -86, 4, 16, 16, UiTextAlignment.CENTER).fontSize(6).verticalOffset(0));
        builder.add(new AlignedTextNode(
                Component.text("Right-click checkbox or drag slider to mutate state live.",
                        NamedTextColor.GRAY), -62, 7, 148, 10, UiTextAlignment.LEFT)
                .fontSize(4));

        addControlButton(builder, "vol_down", -86, 26, 54, "-10%", "Decrease volume");
        addControlButton(builder, "vol_up", -27, 26, 54, "+10%", "Increase volume");
        addControlButton(builder, "toggle_btn", 32, 26, 54, "TOGGLE", "Toggle particle state");
    }

    @Override
    public boolean onClick(DemoContext context, String buttonId, Player player) {
        switch (buttonId) {
            case "vol_down" -> {
                context.volume(Math.max(0.0, Math.round((context.volume() - 0.1) * 100.0) / 100.0));
                context.updateView();
                return true;
            }
            case "vol_up" -> {
                context.volume(Math.min(1.0, Math.round((context.volume() + 0.1) * 100.0) / 100.0));
                context.updateView();
                return true;
            }
            case "toggle_btn" -> {
                context.enabled(!context.enabled());
                context.updateView();
                return true;
            }
            default -> { return false; }
        }
    }

    @Override
    public void onControlChange(DemoContext context, UiControlChange change) {
        if ("demo_volume".equals(change.control().id())) {
            context.volume(change.value());
            context.updateView();
        } else if ("demo_enabled".equals(change.control().id())) {
            context.enabled(change.checked());
            context.updateView();
        }
    }
}
