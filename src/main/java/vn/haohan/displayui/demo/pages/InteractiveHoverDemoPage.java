package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class InteractiveHoverDemoPage extends BaseDemoPage {
    @Override
    public String title() {
        return "INTERACTION + HOVER";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        builder.add(new AlignedTextNode(Component.text(
                        "Aim at any button to reveal its description overlay.",
                        NamedTextColor.GRAY), -86, -34, 172, 10, UiTextAlignment.CENTER)
                .fontSize(4.5f));
        addActionButton(builder, "diamond_action", -86, -14,
                Material.DIAMOND, "Diamond action", NamedTextColor.AQUA,
                "Right-click to claim diamonds");
        addActionButton(builder, "gold_action", 4, -14,
                Material.GOLD_INGOT, "Gold action", NamedTextColor.GOLD,
                "Right-click to spend gold");
        addActionButton(builder, "emerald_action", -86, 12,
                Material.EMERALD, "Emerald action", NamedTextColor.GREEN,
                "Right-click to trade emeralds");
        addActionButton(builder, "custom_action", 4, 12,
                Material.NETHER_STAR, "Special action", NamedTextColor.LIGHT_PURPLE,
                "Right-click for custom event");
    }

    @Override
    public boolean onClick(DemoContext context, String buttonId, Player player) {
        return switch (buttonId) {
            case "diamond_action" -> {
                player.sendMessage("§bDiamond row clicked.");
                yield true;
            }
            case "gold_action" -> {
                player.sendMessage("§6Gold row clicked.");
                yield true;
            }
            case "emerald_action" -> {
                player.sendMessage("§aEmerald row clicked.");
                yield true;
            }
            case "custom_action" -> {
                player.sendMessage("§dCustom special action clicked.");
                yield true;
            }
            default -> false;
        };
    }

    private void addActionButton(UiDocument.Builder builder, String id, float x,
                                 float y, Material icon, String label,
                                 NamedTextColor color, String description) {
        builder.add(new BlockNode(Material.GRAY_CONCRETE.createBlockData(),
                        x, y, 0.001f, 82, 20, 1))
                .add(new UiIconNode(new ItemStack(icon), x + 4, y + 4, 12, 12, 16, 16))
                .add(new AlignedTextNode(
                        Component.text(label, color, TextDecoration.BOLD),
                        x + 20, y + 3, 58, 14, UiTextAlignment.LEFT)
                        .fontSize(5.0f).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, x, y, 82, 20,
                        Component.text(description, NamedTextColor.YELLOW)).hitSlop(2));
    }
}
