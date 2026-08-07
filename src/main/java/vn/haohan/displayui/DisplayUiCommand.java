/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HaoHanDisplayUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HaoHanDisplayUI. If not, see <https://www.gnu.org/licenses/>.
 */
package vn.haohan.displayui;

import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.UiOptions;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiButtonAction;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.text.UiText;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.text.UiTextOpticalPreset;
import vn.haohan.displayui.api.text.UiVerticalAlignment;
import vn.haohan.displayui.runtime.DisplayUiServiceImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

final class DisplayUiCommand implements CommandExecutor {
    private static final String LEGACY_DEMO_OWNER = "haohandisplayui:demo";
    private static final int PAGE_COUNT = 5;
    private static final float PANEL_X = -96;
    private static final float PANEL_Y = -64;
    private static final float PANEL_WIDTH = 192;
    private static final float PANEL_HEIGHT = 128;

    private final HaoHanDisplayUIPlugin plugin;
    private final DisplayUiServiceImpl service;
    private final Map<UUID, DemoSession> demos = new LinkedHashMap<>();

    DisplayUiCommand(HaoHanDisplayUIPlugin plugin, DisplayUiServiceImpl service) {
        this.plugin = plugin;
        this.service = service;
        Bukkit.getScheduler().runTaskTimer(plugin, this::animateDemos, 3L, 3L);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        String action = args.length == 0 ? "info" : args[0].toLowerCase();
        return switch (action) {
            case "info" -> info(sender);
            case "demo" -> demo(sender);
            case "clear" -> clear(sender);
            default -> false;
        };
    }

    private boolean info(CommandSender sender) {
        sender.sendMessage("§dHaoHanDisplayUI §7- active scenes: §f" + service.active().size());
        sender.sendMessage("§7API: §fvn.haohan.displayui.api.DisplayUiService");
        return true;
    }

    private boolean demo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command must be run by a player.");
            return true;
        }

        DemoSession old = demos.remove(player.getUniqueId());
        if (old != null && old.handle != null) old.handle.remove();

        Location origin = player.getEyeLocation()
                .add(player.getEyeLocation().getDirection().multiply(3.0));
        origin.setYaw(player.getLocation().getYaw() + 180.0f);
        origin.setPitch(0.0f);

        DemoSession session = new DemoSession(player.getUniqueId());
        session.handle = service.create(demoOwner(player.getUniqueId()), origin,
                buildPage(session), new UiOptions(80.0f, 12.0, false,
                        0.2f, "haohan_display_ui", session.cameraTransform),
                candidate -> candidate.getUniqueId().equals(player.getUniqueId()));
        session.handle.onClick(click -> onDemoClick(session, click.button().id(), click.player()));
        demos.put(player.getUniqueId(), session);

        sender.sendMessage("§aDemo UI created. Aim at a row to see its description, "
                + "right-click to interact, and use ‹/› to change page.");
        plugin.getLogger().info(player.getName() + " created a Display UI demo");
        return true;
    }

    private void onDemoClick(DemoSession session, String buttonId, Player player) {
        if (!session.handle.isValid()) return;
        switch (buttonId) {
            case "previous_page" -> {
                session.page = Math.floorMod(session.page - 1, PAGE_COUNT);
                session.handle.update(buildPage(session));
            }
            case "next_page" -> {
                session.page = (session.page + 1) % PAGE_COUNT;
                session.handle.update(buildPage(session));
            }
            case "diamond_action" -> player.sendMessage("§bDiamond row clicked.");
            case "gold_action" -> player.sendMessage("§6Gold row clicked.");
            case "emerald_action" -> player.sendMessage("§aEmerald row clicked.");
            case "camera_fixed" -> setCamera(session, UiCameraTransform.fixed());
            case "camera_yaw" -> setCamera(session,
                    UiCameraTransform.fixed().locks(true, false, true));
            case "camera_pitch" -> setCamera(session,
                    UiCameraTransform.fixed().locks(false, true, true));
            case "camera_full" -> setCamera(session, UiCameraTransform.cameraFacing());
            case "camera_x45" -> setCamera(session,
                    UiCameraTransform.fixed().angles(45, 0, 0));
            case "camera_y45" -> setCamera(session,
                    UiCameraTransform.fixed().angles(0, 45, 0));
            case "camera_z45" -> setCamera(session,
                    UiCameraTransform.fixed().angles(0, 0, 45));
            case "open_url", "player_command", "console_command", "execute_command" -> { }
            default -> player.sendMessage("§dDisplay UI click: §f" + buttonId);
        }
    }

    private void setCamera(DemoSession session, UiCameraTransform transform) {
        session.cameraTransform = transform;
        session.handle.cameraTransform(transform);
    }

    private UiDocument buildPage(DemoSession session) {
        UiDocument.Builder builder = UiDocument.builder()
                .add(new BlockNode(Material.BLACK_CONCRETE.createBlockData(),
                        PANEL_X, PANEL_Y, 0.0f, PANEL_WIDTH, PANEL_HEIGHT, 2));
        addHeader(builder, session.page);
        switch (session.page) {
            case 0 -> addTextPage(builder, session.gradientFrame);
            case 1 -> addListPage(builder);
            case 2 -> addInteractivePage(builder);
            case 3 -> addCameraPage(builder);
            case 4 -> addActionPage(builder);
            default -> throw new IllegalStateException("Unknown demo page " + session.page);
        }
        addFooter(builder, session.page);
        return builder.build();
    }

    private void addHeader(UiDocument.Builder builder, int page) {
        builder.add(new AlignedTextNode(
                UiText.builder().gradient("HaoHan Display UI", new TextColor[] {
                        UiText.hex("#FFD700"), UiText.hex("#FF7A00"),
                        UiText.hex("#C02CFF")
                }, 1.0, TextDecoration.BOLD).build(),
                -86, -56, 172, 14, UiTextAlignment.LEFT)
                .opticalPreset(UiTextOpticalPreset.BOLD_GRADIENT)
                .fontSize(9).shadowed(true));
        builder.add(new AlignedTextNode(
                Component.text(switch (page) {
                    case 0 -> "TEXT STYLES";
                    case 1 -> "LIST LAYOUTS";
                    case 2 -> "INTERACTION + HOVER";
                    case 3 -> "CAMERA + AXIS LOCK";
                    default -> "LINK + COMMAND ACTIONS";
                }, NamedTextColor.DARK_GRAY),
                -86, -43, 172, 9, UiTextAlignment.RIGHT)
                .fontSize(5).verticalOffset(-1));
    }

    private void addTextPage(UiDocument.Builder builder, int frame) {
        addTextSample(builder, "Normal text", -34,
                Component.text("Normal text", NamedTextColor.WHITE));
        addTextSample(builder, "Bold", -22,
                Component.text("Bold text", NamedTextColor.GOLD, TextDecoration.BOLD),
                UiTextOpticalPreset.BOLD);
        addTextSample(builder, "Italic", -10,
                Component.text("Italic text", NamedTextColor.LIGHT_PURPLE,
                        TextDecoration.ITALIC), UiTextOpticalPreset.ITALIC);
        addTextSample(builder, "Moving", 2, movingGradient(frame),
                UiTextOpticalPreset.BOLD_GRADIENT);
        addTextSample(builder, "§k", 14,
                Component.text("OBFUSCATED", NamedTextColor.AQUA,
                        TextDecoration.OBFUSCATED));
        addTextSample(builder, "Mixed", 26,
                UiText.builder().text("Bold ", NamedTextColor.RED, TextDecoration.BOLD)
                        .text("+ italic ", NamedTextColor.YELLOW, TextDecoration.ITALIC)
                        .text("+ ", NamedTextColor.AQUA)
                        .gradient("RGB", new TextColor[] {
                                UiText.hex("#FF3030"), UiText.hex("#30FF60"),
                                UiText.hex("#3090FF")
                        }, 1.0, TextDecoration.BOLD).build());
    }

    private void addTextSample(UiDocument.Builder builder, String label, float y,
                               Component sample) {
        addTextSample(builder, label, y, sample, UiTextOpticalPreset.PLAIN);
    }

    private void addTextSample(UiDocument.Builder builder, String label, float y,
                               Component sample, UiTextOpticalPreset opticalPreset) {
        builder.add(new AlignedTextNode(Component.text(label + ":", NamedTextColor.GRAY),
                -86, y, 38, 10, UiTextAlignment.RIGHT).fontSize(6).verticalOffset(-1));
        builder.add(new AlignedTextNode(sample, -43, y, 129, 10,
                UiTextAlignment.LEFT).opticalPreset(opticalPreset)
                .fontSize(6).verticalOffset(-1).shadowed(true));
    }

    private Component movingGradient(int frame) {
        TextColor[] palette = {
                UiText.hex("#FF3B30"), UiText.hex("#FFD60A"),
                UiText.hex("#34C759"), UiText.hex("#32ADE6"),
                UiText.hex("#AF52DE")
        };
        TextColor[] shifted = new TextColor[palette.length];
        for (int i = 0; i < palette.length; i++) {
            shifted[i] = palette[Math.floorMod(i + frame, palette.length)];
        }
        return UiText.builder().gradient("Moving gradient text", shifted, 1.0,
                TextDecoration.BOLD).build();
    }

    private void addListPage(UiDocument.Builder builder) {
        builder.add(new AlignedTextNode(Component.text("Text list", NamedTextColor.YELLOW,
                TextDecoration.BOLD), -86, -34, 74, 10, UiTextAlignment.LEFT)
                .fontSize(6).shadowed(true));
        String[] entries = {"• First entry", "• Second entry", "• Third entry"};
        for (int i = 0; i < entries.length; i++) {
            builder.add(new AlignedTextNode(Component.text(entries[i], NamedTextColor.WHITE),
                    -82, -22 + i * 11, 70, 9, UiTextAlignment.LEFT).fontSize(5));
        }

        builder.add(new AlignedTextNode(Component.text("Icon list", NamedTextColor.YELLOW,
                TextDecoration.BOLD), 2, -34, 84, 10, UiTextAlignment.LEFT)
                .fontSize(6).shadowed(true));
        Material[] materials = {Material.DIAMOND, Material.GOLD_INGOT, Material.EMERALD};
        for (int i = 0; i < materials.length; i++) {
            builder.add(new UiIconNode(new ItemStack(materials[i]),
                    8 + i * 24, -19, 18, 18, 16, 16));
        }

        builder.add(new AlignedTextNode(Component.text("Icon + text", NamedTextColor.YELLOW,
                TextDecoration.BOLD), 2, 4, 84, 10, UiTextAlignment.LEFT)
                .fontSize(6).shadowed(true));
        UiIconNode iron = new UiIconNode(new ItemStack(Material.IRON_INGOT),
                6, 15, 16, 16, 16, 16);
        UiIconNode copper = new UiIconNode(new ItemStack(Material.COPPER_INGOT),
                6, 31, 16, 16, 16, 16);
        builder.add(iron).add(copper)
                .add(new AlignedTextNode(Component.text("Iron ingot", NamedTextColor.WHITE),
                        2, 15, 84, 16, UiTextAlignment.LEFT)
                        .after(iron, 4, UiVerticalAlignment.CENTER).fontSize(5))
                .add(new AlignedTextNode(Component.text("Copper ingot", NamedTextColor.WHITE),
                        2, 31, 84, 16, UiTextAlignment.LEFT)
                        .after(copper, 4, UiVerticalAlignment.CENTER).fontSize(5));
    }

    private void addInteractivePage(UiDocument.Builder builder) {
        addInteractiveRow(builder, "diamond_action", Material.DIAMOND, -34,
                "Diamond action", NamedTextColor.AQUA,
                "Diamond row: text and icon share one clickable hitbox");
        addInteractiveRow(builder, "gold_action", Material.GOLD_INGOT, -8,
                "Gold action", NamedTextColor.GOLD,
                "Gold row: right-click anywhere on this row");
        addInteractiveRow(builder, "emerald_action", Material.EMERALD, 18,
                "Emerald action", NamedTextColor.GREEN,
                "Emerald row: hover description comes from UiButton");
    }

    private void addInteractiveRow(UiDocument.Builder builder, String id, Material material,
                                   float y, String label, NamedTextColor color,
                                   String description) {
        builder.add(new BlockNode(Material.GRAY_CONCRETE.createBlockData(),
                -86, y, 0.001f, 172, 22, 1));
        UiIconNode icon = new UiIconNode(new ItemStack(material),
                -81, y + 3, 16, 16, 16, 16);
        builder.add(icon)
                .add(new AlignedTextNode(Component.text(label, color, TextDecoration.BOLD),
                        -86, y, 172, 22, UiTextAlignment.LEFT)
                        .after(icon, 6, UiVerticalAlignment.CENTER)
                        .fontSize(6).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, -86, y, 172, 22,
                        Component.text(description, NamedTextColor.YELLOW)));
    }

    private void addCameraPage(UiDocument.Builder builder) {
        addControlButton(builder, "camera_fixed", -86, -34, "FIXED",
                "Lock X/Y/Z: the panel stays still");
        addControlButton(builder, "camera_full", 8, -34, "CAMERA",
                "Unlock X/Y: follow camera yaw and pitch");
        addControlButton(builder, "camera_yaw", -86, -15, "YAW ONLY",
                "Lock X, follow camera on Y");
        addControlButton(builder, "camera_pitch", 8, -15, "PITCH ONLY",
                "Lock Y, follow camera on X");
        addControlButton(builder, "camera_x45", -86, 4, "FIXED X 45°",
                "Lock all axes with a 45 degree X angle");
        addControlButton(builder, "camera_y45", 8, 4, "FIXED Y 45°",
                "Lock all axes with a 45 degree Y angle");
        addControlButton(builder, "camera_z45", -86, 23, "FIXED Z 45°",
                "Lock all axes with a 45 degree roll");
        builder.add(new AlignedTextNode(Component.text("Aim and click a preset",
                        NamedTextColor.GRAY), 8, 23, 78, 16, UiTextAlignment.CENTER)
                .fontSize(5).verticalOffset(0));
    }

    private void addControlButton(UiDocument.Builder builder, String id, float x,
                                  float y, String label, String description) {
        builder.add(new BlockNode(Material.GRAY_CONCRETE.createBlockData(),
                        x, y, 0.001f, 78, 16, 1))
                .add(new AlignedTextNode(Component.text(label, NamedTextColor.AQUA),
                        x, y, 78, 16, UiTextAlignment.CENTER)
                        .fontSize(5).verticalOffset(0).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, x, y, 78, 16,
                        Component.text(description, NamedTextColor.YELLOW)));
    }

    private void addActionPage(UiDocument.Builder builder) {
        addActionRow(builder, "open_url", Material.BOOK, -36, "Open URL",
                "Send a safe clickable link prompt",
                UiButtonAction.openUrl("https://web.haohansmp.io.vn/en"));
        addActionRow(builder, "player_command", Material.COMMAND_BLOCK, -16,
                "Player command", "Run /say as the clicking player",
                UiButtonAction.playerCommand("say Testing HaoHan Display UI"));
        addActionRow(builder, "console_command", Material.REDSTONE, 4,
                "Console command", "Run a harmless tellraw from console",
                UiButtonAction.consoleCommand(
                        "tellraw {player} {\"text\":\"Console action works\",\"color\":\"green\"}"));
        addActionRow(builder, "execute_command", Material.PAPER, 24,
                "Execute command", "Run /seed now; normal player permissions apply",
                UiButtonAction.executeCommand("seed"));
    }

    private void addActionRow(UiDocument.Builder builder, String id, Material material,
                              float y, String label, String description,
                              UiButtonAction action) {
        builder.add(new BlockNode(Material.GRAY_CONCRETE.createBlockData(),
                -86, y, 0.001f, 172, 18, 1));
        UiIconNode icon = new UiIconNode(new ItemStack(material),
                -82, y + 1, 16, 16, 16, 16);
        builder.add(icon)
                .add(new AlignedTextNode(Component.text(label, NamedTextColor.WHITE,
                        TextDecoration.BOLD), -86, y, 172, 18, UiTextAlignment.LEFT)
                        .after(icon, 5, UiVerticalAlignment.CENTER)
                        .fontSize(5).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, -86, y, 172, 18,
                        Component.text(description, NamedTextColor.YELLOW), action));
    }

    private void addFooter(UiDocument.Builder builder, int page) {
        addFooterButton(builder, "previous_page", -86, "<", "Previous demo page");
        addFooterButton(builder, "next_page", 62, ">", "Next demo page");
        builder.add(new AlignedTextNode(Component.text((page + 1) + " / " + PAGE_COUNT,
                NamedTextColor.GRAY), -52, 48, 104, 12, UiTextAlignment.CENTER)
                .fontSize(5).verticalOffset(-1));
    }

    private void addFooterButton(UiDocument.Builder builder, String id, float x,
                                 String glyph, String description) {
        builder.add(new BlockNode(Material.GRAY_CONCRETE.createBlockData(),
                x, 46, 0.001f, 24, 14, 1))
                .add(new AlignedTextNode(Component.text(glyph, NamedTextColor.WHITE,
                        TextDecoration.BOLD), x, 46, 24, 14, UiTextAlignment.CENTER)
                        .fontSize(6).verticalOffset(0).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, x, 46, 24, 14,
                        Component.text(description, NamedTextColor.AQUA)).hitSlop(3));
    }

    private void animateDemos() {
        Iterator<DemoSession> iterator = demos.values().iterator();
        while (iterator.hasNext()) {
            DemoSession session = iterator.next();
            Player player = Bukkit.getPlayer(session.playerId);
            if (player == null || session.handle == null || !session.handle.isValid()) {
                if (session.handle != null && session.handle.isValid()) session.handle.remove();
                iterator.remove();
                continue;
            }
            if (session.page == 0) {
                session.gradientFrame++;
                session.handle.update(buildPage(session));
            }
        }
    }

    private boolean clear(CommandSender sender) {
        int removed = 0;
        for (DemoSession session : demos.values()) {
            if (session.handle != null && session.handle.isValid()) {
                session.handle.remove();
                removed++;
            }
        }
        demos.clear();
        removed += service.removeOwnedBy(LEGACY_DEMO_OWNER);
        sender.sendMessage("§aRemoved " + removed + " demo UI scene(s).");
        return true;
    }

    private String demoOwner(UUID playerId) {
        return "haohandisplayui:demo/" + playerId.toString().toLowerCase();
    }

    private static final class DemoSession {
        private final UUID playerId;
        private UiHandle handle;
        private int page;
        private int gradientFrame;
        private UiCameraTransform cameraTransform = UiCameraTransform.fixed();

        private DemoSession(UUID playerId) {
            this.playerId = playerId;
        }
    }
}
