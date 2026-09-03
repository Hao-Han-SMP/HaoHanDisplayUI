package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.animation.Easings;
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

import java.util.ArrayList;
import java.util.List;

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
                        "Scroll list · Mouse wheel or buttons to scroll",
                        NamedTextColor.GRAY), -86, -43, 110, 9, UiTextAlignment.LEFT)
                .fontSize(4.5f));

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
    public void onShow(DemoContext context) {
        playScrollEntranceAnimation(context);
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
            int prev = context.appOffset();
            int next = Math.max(0, prev - 1);
            if (prev != next) {
                context.appOffset(next);
                context.updateView();
            }
            return true;
        } else if ("app_down".equals(buttonId)) {
            int prev = context.appOffset();
            int next = Math.min(maxOffset(context), prev + 1);
            if (prev != next) {
                context.appOffset(next);
                context.updateView();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onControlChange(DemoContext context, UiControlChange change) {
        if ("demo_apps".equals(change.control().id())) {
            int prev = context.appOffset();
            int next = (int) change.value();
            if (prev != next) {
                context.appOffset(next);
                context.updateView();
            }
        }
    }

    private void playScrollEntranceAnimation(DemoContext context) {
        if (context.handle() == null || !context.handle().isValid()) return;
        int totalNodes = context.handle().nodeCount();
        List<UiAnimation> list = new ArrayList<>(totalNodes);
        for (int i = 0; i < totalNodes; i++) {
            if (i < 2) {
                list.add(UiAnimation.builder().durationTicks(1).build());
            } else {
                int row = (i - 2) / 5;
                int delay = Math.max(0, row * 2);
                list.add(UiAnimation.slideIn(10, UiAnimation.Direction.TOP, 12.0f,
                        Easings.OutCubic).delay(delay));
            }
        }
        context.handle().animateNodes(list);
    }

    private void addAppRow(UiDocument.Builder builder, AppEntry app, float y,
                           String id, boolean selected) {
        Material background = selected ? Material.LIGHT_BLUE_STAINED_GLASS
                : Material.LIGHT_GRAY_STAINED_GLASS;
        builder.add(new BlockNode(background.createBlockData(), -74, y,
                0.002f, 148, 16, 1));
        builder.add(new UiIconNode(new ItemStack(app.icon()), -70, y + 2, 0.005f,
                12, 12, 16, 16, org.bukkit.entity.ItemDisplay.ItemDisplayTransform.FIXED));
        builder.add(new AlignedTextNode(Component.text(selected ? "☑" : "☐",
                        selected ? NamedTextColor.AQUA : NamedTextColor.WHITE),
                -56, y + 3.0f, 10, 10, 0.005f, UiTextAlignment.CENTER,
                0.0f, 0.0f, 6.5f, 10.0f, 0.0f, false, false));
        builder.add(new AlignedTextNode(Component.text(app.name(),
                        selected ? NamedTextColor.AQUA : NamedTextColor.WHITE, TextDecoration.BOLD), -43, y + 2.0f,
                74, 6, 0.005f, UiTextAlignment.LEFT,
                0.0f, 0.0f, 5.0f, 74.0f, 0.0f, true, false));
        builder.add(new AlignedTextNode(Component.text(app.description(),
                        NamedTextColor.GRAY), -43, y + 8.5f, 112, 6, 0.005f,
                UiTextAlignment.LEFT, 0.0f, 0.0f, 3.5f, 112.0f, 0.0f, false, false));
        builder.button(new UiButton(id, -74, y, 148, 16,
                Component.text("Open " + app.name(), NamedTextColor.YELLOW)));
    }

    private void addEmptyAppRow(UiDocument.Builder builder, float y, int row) {
        builder.add(new BlockNode(Material.GRAY_STAINED_GLASS.createBlockData(),
                -74, y, 0.002f, 148, 16, 1));
        builder.add(new UiIconNode(new ItemStack(Material.AIR), -70, y + 2, 0.005f,
                12, 12, 16, 16, org.bukkit.entity.ItemDisplay.ItemDisplayTransform.FIXED));
        builder.add(new AlignedTextNode(Component.empty(), -56, y + 3.0f,
                10, 10, 0.005f, UiTextAlignment.CENTER,
                0.0f, 0.0f, 6.5f, 10.0f, 0.0f, false, false));
        builder.add(new AlignedTextNode(Component.empty(), -43, y + 2.0f,
                74, 6, 0.005f, UiTextAlignment.LEFT,
                0.0f, 0.0f, 5.0f, 74.0f, 0.0f, false, false));
        builder.add(new AlignedTextNode(Component.empty(), -43, y + 8.5f,
                112, 6, 0.005f, UiTextAlignment.LEFT,
                0.0f, 0.0f, 3.5f, 112.0f, 0.0f, false, false));
        builder.button(new UiButton("empty_app_" + row, -74, y, 148, 16,
                Component.empty()));
    }
}
