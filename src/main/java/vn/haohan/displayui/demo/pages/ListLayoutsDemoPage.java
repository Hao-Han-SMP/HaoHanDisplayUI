package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class ListLayoutsDemoPage extends BaseDemoPage {
    @Override
    public String title() {
        return "LIST LAYOUTS";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        addListRow(builder, "row_emerald", "Emerald Reward", -34,
                NamedTextColor.GREEN, Material.EMERALD, "Daily quest claim");
        addListRow(builder, "row_diamond", "Diamond Gear", -14,
                NamedTextColor.AQUA, Material.DIAMOND_SWORD, "PvP loadout preset");
        addListRow(builder, "row_gold", "Gold Coins", 6,
                NamedTextColor.GOLD, Material.GOLD_INGOT, "Shop balance · 1,420g");
        addListRow(builder, "row_netherite", "Netherite Ingot", 26,
                NamedTextColor.DARK_PURPLE, Material.NETHERITE_INGOT,
                "Mastery crafting material");
    }

    private void addListRow(UiDocument.Builder builder, String id, String title,
                            float y, NamedTextColor accent, Material icon,
                            String description) {
        builder.add(new BlockNode(Material.GRAY_STAINED_GLASS.createBlockData(),
                        -86, y, 0.001f, 172, 16, 1))
                .add(new UiIconNode(new ItemStack(icon), -82, y + 2, 12, 12, 16, 16))
                .add(new AlignedTextNode(
                        Component.text(title, accent, TextDecoration.BOLD),
                        -66, y + 2.0f, 140, 6, UiTextAlignment.LEFT).fontSize(5.0f).shadowed(true))
                .add(new AlignedTextNode(
                        Component.text(description, NamedTextColor.GRAY),
                        -66, y + 8.5f, 140, 6, UiTextAlignment.LEFT).fontSize(3.5f))
                .button(new UiButton(id, -86, y, 172, 16,
                        Component.text("Action · " + title, NamedTextColor.YELLOW)));
    }
}
