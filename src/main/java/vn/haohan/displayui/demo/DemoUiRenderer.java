package vn.haohan.displayui.demo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.utils.MathUtils;
import vn.haohan.displayui.api.text.UiText;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.text.UiTextOpticalPreset;

import java.util.List;

public final class DemoUiRenderer {
    public static final float PANEL_X = -96;
    public static final float PANEL_Y = -64;
    public static final float PANEL_WIDTH = 192;
    public static final float PANEL_HEIGHT = 128;

    private DemoUiRenderer() {}

    public static UiDocument render(List<DemoPage> pages, DemoContext context) {
        UiGradient rootGrad = context.rootGradient();
        UiGradientBackgroundNode rootBackground = new UiGradientBackgroundNode(
                PANEL_X, PANEL_Y, 0.0f,
                PANEL_WIDTH, PANEL_HEIGHT,
                rootGrad
        ).withGrid(12, 8).withDoubleSided(context.doubleSided());

        UiDocument.Builder builder = UiDocument.builder()
                .gradientBackground(rootBackground);

        int pageIndex = MathUtils.clamp(context.page(), 0, pages.size() - 1);
        DemoPage page = pages.get(pageIndex);

        addHeader(builder, page.title());
        page.build(builder, context);
        addFooter(builder, pageIndex, pages.size(), context.doubleSided(), context.mirrorSide());

        return builder.build();
    }

    private static void addHeader(UiDocument.Builder builder, String pageTitle) {
        builder.add(new AlignedTextNode(
                UiText.builder().gradient("HaoHan Display UI", new TextColor[] {
                        UiText.hex("#FFD700"), UiText.hex("#FF7A00"),
                        UiText.hex("#C02CFF")
                }, 1.0, TextDecoration.BOLD).build(),
                -86, -56, 172, 14, UiTextAlignment.LEFT)
                .opticalPreset(UiTextOpticalPreset.BOLD_GRADIENT)
                .fontSize(9).shadowed(true));

        builder.add(new AlignedTextNode(
                Component.text(pageTitle, NamedTextColor.DARK_GRAY),
                -86, -43, 172, 9, UiTextAlignment.RIGHT)
                .fontSize(5).verticalOffset(-1));
    }

    private static void addFooter(UiDocument.Builder builder, int page, int totalPages,
                                  boolean doubleSided, boolean mirrorSide) {
        addFooterButton(builder, "previous_page", -86, 18, "<", "Previous demo page");
        builder.add(new AlignedTextNode(Component.text((page + 1) + " / " + totalPages,
                NamedTextColor.GRAY), -64, 48, 36, 12, UiTextAlignment.CENTER)
                .fontSize(5).verticalOffset(-1));
        addFooterButton(builder, "next_page", -24, 18, ">", "Next demo page");

        addToggleButton(builder, "toggle_doublesided", 2, doubleSided,
                "◈ 2X", "Enable or disable two-sided rendering");
        addToggleButton(builder, "toggle_mirrorside", 48, mirrorSide,
                "⇄ MIR", "Mirror the back side layout and interaction");
    }

    private static void addToggleButton(UiDocument.Builder builder, String id, float x,
                                        boolean enabled, String label, String description) {
        Material background = enabled ? Material.GREEN_CONCRETE : Material.GRAY_CONCRETE;
        NamedTextColor color = enabled ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY;
        Component text = Component.text(label + (enabled ? "  ON" : "  OFF"), color,
                TextDecoration.BOLD);
        builder.add(new BlockNode(background.createBlockData(), x, 48, 0.001f, 42, 10, 1))
                .add(new AlignedTextNode(text, x, 48, 42, 10, UiTextAlignment.CENTER)
                        .fontSize(3.8f).verticalOffset(-1).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, x, 48, 42, 10,
                        Component.text(description, NamedTextColor.YELLOW)).hitSlop(3));
    }

    private static void addFooterButton(UiDocument.Builder builder, String id, float x,
                                        float width, String label, String description) {
        builder.add(new BlockNode(Material.GRAY_CONCRETE.createBlockData(),
                        x, 48, 0.001f, width, 12, 1))
                .add(new AlignedTextNode(Component.text(label, NamedTextColor.WHITE,
                        TextDecoration.BOLD), x, 48, width, 12, UiTextAlignment.CENTER)
                        .fontSize(6).verticalOffset(-1).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, x, 48, width, 12,
                        Component.text(description, NamedTextColor.YELLOW)).hitSlop(4));
    }
}
