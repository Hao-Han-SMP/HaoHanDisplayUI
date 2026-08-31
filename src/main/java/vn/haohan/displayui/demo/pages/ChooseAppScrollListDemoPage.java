package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiControlChange;
import vn.haohan.displayui.api.interaction.UiScrollList;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.AppEntry;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class ChooseAppScrollListDemoPage extends BaseDemoPage {
    private static final float LEFT = -78;
    private static final float TOP = -34;
    private static final float WIDTH = 156;
    private static final float ROW_HEIGHT = 18;
    private static final int VISIBLE_ROWS = 4;

    @Override
    public String title() {
        return "CHOOSE APP · SCROLL LIST";
    }

    public static int maxOffset(DemoContext context) {
        return Math.max(0, context.appEntries().size() - VISIBLE_ROWS);
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        builder.add(new AlignedTextNode(Component.text(
                        "Scroll list · Right-click row to select",
                        NamedTextColor.GRAY), -86, -43, 96, 9, UiTextAlignment.LEFT)
                .fontSize(4.5f));

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            float rowY = TOP + row * ROW_HEIGHT;
            builder.add(new BlockNode(Material.GRAY_STAINED_GLASS.createBlockData(),
                    -74, rowY, 0.001f, 148, 16, 1));
        }

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = context.appOffset() + row;
            if (index >= context.appEntries().size()) {
                addEmptyAppRow(builder, TOP + row * ROW_HEIGHT, row);
            } else {
                AppEntry app = context.appEntries().get(index);
                addAppRow(builder, app, TOP + row * ROW_HEIGHT,
                        "app_" + index, index == context.selectedApp());
            }
        }

        builder.scrollList(new UiScrollList("demo_apps", LEFT, TOP, WIDTH,
                VISIBLE_ROWS * ROW_HEIGHT, maxOffset(context), context.appOffset(),
                Component.text("Scroll applications")));
        builder.add(new AlignedTextNode(Component.text(
                        (context.appOffset() + 1) + "–" + Math.min(context.appEntries().size(),
                                context.appOffset() + VISIBLE_ROWS) + " / " + context.appEntries().size(),
                        NamedTextColor.DARK_GRAY), LEFT, 40, WIDTH, 8,
                UiTextAlignment.CENTER).fontSize(4));
        addControlButton(builder, "app_up", 76, -29, 18, "▲", "Scroll up");
        addControlButton(builder, "app_down", 76, 19, 18, "▼", "Scroll down");
    }

    @Override
    public boolean onClick(DemoContext context, String buttonId, Player player) {
        if (buttonId.startsWith("app_") && !buttonId.equals("app_up") && !buttonId.equals("app_down")) {
            try {
                int selected = Integer.parseInt(buttonId.substring(4));
                context.selectedApp(selected);
                context.updateView();
                player.sendMessage("§dSelected app: §f" + context.appEntries().get(selected).name());
                return true;
            } catch (NumberFormatException ignored) {}
        } else if ("app_up".equals(buttonId)) {
            context.appOffset(Math.max(0, context.appOffset() - 1));
            context.updateView();
            return true;
        } else if ("app_down".equals(buttonId)) {
            context.appOffset(Math.min(maxOffset(context), context.appOffset() + 1));
            context.updateView();
            return true;
        }
        return false;
    }

    @Override
    public void onControlChange(DemoContext context, UiControlChange change) {
        if ("demo_apps".equals(change.control().id())) {
            context.appOffset((int) change.value());
            context.updateView();
        }
    }

    private void addAppRow(UiDocument.Builder builder, AppEntry app, float y,
                           String id, boolean selected) {
        builder.add(new UiIconNode(new ItemStack(app.icon()), -70, y + 2,
                12, 12, 16, 16));
        builder.add(new AlignedTextNode(Component.text(selected ? "☑" : "☐",
                        selected ? NamedTextColor.AQUA : NamedTextColor.WHITE),
                -56, y + 3.0f, 10, 10, UiTextAlignment.CENTER).fontSize(6.5f));
        builder.add(new AlignedTextNode(Component.text(app.name(),
                        selected ? NamedTextColor.AQUA : NamedTextColor.WHITE, TextDecoration.BOLD), -43, y + 2.0f,
                74, 6, UiTextAlignment.LEFT).fontSize(5.0f).shadowed(true));
        builder.add(new AlignedTextNode(Component.text(app.description(),
                        NamedTextColor.GRAY), -43, y + 8.5f, 112, 6,
                UiTextAlignment.LEFT).fontSize(3.5f));
        builder.button(new UiButton(id, -74, y, 148, 16,
                Component.text("Open " + app.name(), NamedTextColor.YELLOW)));
    }

    private void addEmptyAppRow(UiDocument.Builder builder, float y, int row) {
        builder.add(new UiIconNode(new ItemStack(Material.AIR), -70, y + 2,
                12, 12, 16, 16));
        builder.add(new AlignedTextNode(Component.empty(), -56, y + 3.0f,
                10, 10, UiTextAlignment.CENTER).fontSize(6.5f));
        builder.add(new AlignedTextNode(Component.empty(), -43, y + 2.0f,
                74, 6, UiTextAlignment.LEFT).fontSize(5.0f));
        builder.add(new AlignedTextNode(Component.empty(), -43, y + 8.5f,
                112, 6, UiTextAlignment.LEFT).fontSize(3.5f));
        builder.button(new UiButton("empty_app_" + row, -74, y, 148, 16,
                Component.empty()));
    }
}
