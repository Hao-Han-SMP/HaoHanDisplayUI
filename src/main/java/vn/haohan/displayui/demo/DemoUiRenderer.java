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
import vn.haohan.displayui.api.node.UiBackgroundNode;
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
        UiDocument.Builder builder = UiDocument.builder()
                .add(new UiBackgroundNode(PANEL_X, PANEL_Y, 0.0f,
                        PANEL_WIDTH, PANEL_HEIGHT,
                        org.bukkit.Color.fromARGB(0xB0000000)));

        int pageIndex = Math.max(0, Math.min(context.page(), pages.size() - 1));
        DemoPage page = pages.get(pageIndex);

        addHeader(builder, page.title());
        page.build(builder, context);
        addFooter(builder, pageIndex, pages.size());

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

    private static void addFooter(UiDocument.Builder builder, int page, int totalPages) {
        addFooterButton(builder, "previous_page", -86, "<", "Previous demo page");
        addFooterButton(builder, "next_page", 62, ">", "Next demo page");
        builder.add(new AlignedTextNode(Component.text((page + 1) + " / " + totalPages,
                NamedTextColor.GRAY), -52, 48, 104, 12, UiTextAlignment.CENTER)
                .fontSize(5).verticalOffset(-1));
    }

    private static void addFooterButton(UiDocument.Builder builder, String id, float x,
                                        String label, String description) {
        builder.add(new BlockNode(Material.GRAY_CONCRETE.createBlockData(),
                        x, 48, 0.001f, 24, 12, 1))
                .add(new AlignedTextNode(Component.text(label, NamedTextColor.WHITE,
                        TextDecoration.BOLD), x, 48, 24, 12, UiTextAlignment.CENTER)
                        .fontSize(6).verticalOffset(-1).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, x, 48, 24, 12,
                        Component.text(description, NamedTextColor.YELLOW)).hitSlop(4));
    }
}
