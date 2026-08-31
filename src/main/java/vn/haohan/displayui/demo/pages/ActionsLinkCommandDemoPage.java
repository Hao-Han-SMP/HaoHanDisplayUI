package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiButtonAction;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class ActionsLinkCommandDemoPage extends BaseDemoPage {
    @Override
    public String title() {
        return "LINK + COMMAND ACTIONS";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        addActionRow(builder, "open_url", "Open Documentation Link", -34,
                NamedTextColor.AQUA, Material.BOOK,
                "Opens wiki in client chat link prompt",
                UiButtonAction.openUrl("https://github.com/Hao-Han-SMP/HaoHanDisplayUI"));
        addActionRow(builder, "player_command", "Run /help as Player", -14,
                NamedTextColor.GREEN, Material.COMMAND_BLOCK,
                "Dispatches a command as the clicking player",
                UiButtonAction.playerCommand("/help"));
        addActionRow(builder, "console_command", "Run Console Command", 6,
                NamedTextColor.GOLD, Material.REPEATING_COMMAND_BLOCK,
                "Runs a privileged command from console",
                UiButtonAction.consoleCommand("say Player interacted with DisplayUI demo"));
        addActionRow(builder, "execute_command", "Run /say as UI Action", 26,
                NamedTextColor.LIGHT_PURPLE, Material.CHAIN_COMMAND_BLOCK,
                "Directly executes command template",
                UiButtonAction.executeCommand("/say Hello from DisplayUI actions"));
    }

    private void addActionRow(UiDocument.Builder builder, String id, String title,
                              float y, NamedTextColor accent, Material icon,
                              String description, UiButtonAction action) {
        builder.add(new BlockNode(Material.GRAY_STAINED_GLASS.createBlockData(),
                        -86, y, 0.001f, 172, 16, 1))
                .add(new UiIconNode(new ItemStack(icon), -82, y + 2, 12, 12, 16, 16))
                .add(new AlignedTextNode(
                        Component.text(title, accent, TextDecoration.BOLD),
                        -66, y + 2.0f, 150, 6, UiTextAlignment.LEFT).fontSize(5.0f))
                .add(new AlignedTextNode(
                        Component.text(description, NamedTextColor.GRAY),
                        -66, y + 8.5f, 150, 6, UiTextAlignment.LEFT).fontSize(3.5f))
                .button(new UiButton(id, -86, y, 172, 16,
                        Component.text("Trigger " + title, NamedTextColor.YELLOW))
                        .withAction(action));
    }
}
