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
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.animation.UiEasing;
import vn.haohan.displayui.api.animation.UiEffects;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiButtonAction;
import vn.haohan.displayui.api.interaction.UiCheckbox;
import vn.haohan.displayui.api.interaction.UiControlChange;
import vn.haohan.displayui.api.interaction.UiSlider;
import vn.haohan.displayui.api.interaction.UiScrollList;
import vn.haohan.displayui.api.layout.UiRect;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.node.UiBackgroundNode;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

final class DisplayUiCommand implements CommandExecutor {
    private static final String LEGACY_DEMO_OWNER = "haohandisplayui:demo";
    private static final int PAGE_COUNT = 8;
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
        // Text content itself is not client-interpolated. Update the demo
        // gradient every server tick for the smoothest server-side result.
        Bukkit.getScheduler().runTaskTimer(plugin, this::animateDemos, 1L, 1L);
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
        session.handle.onControlChange(change -> onDemoControlChange(session, change));
        session.handle.animate(UiAnimation.fadeIn(12, UiEasing.EASE_OUT));
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
                showPage(session);
            }
            case "next_page" -> {
                session.page = (session.page + 1) % PAGE_COUNT;
                showPage(session);
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
            case "app_0", "app_1", "app_2", "app_3", "app_4", "app_5", "app_6", "app_7" -> {
                session.selectedApp = Integer.parseInt(buttonId.substring(4));
                session.handle.update(buildPage(session));
                player.sendMessage("§dSelected app: §f"
                        + session.appEntries.get(session.selectedApp).name());
            }
            case "app_up" -> {
                session.appOffset = Math.max(0, session.appOffset - 1);
                session.handle.update(buildPage(session));
            }
            case "app_down" -> {
                session.appOffset = Math.min(appMaxOffset(session), session.appOffset + 1);
                session.handle.update(buildPage(session));
            }
            default -> player.sendMessage("§dDisplay UI click: §f" + buttonId);
        }
    }

    private void onDemoControlChange(DemoSession session, UiControlChange change) {
        switch (change.control().id()) {
            case "demo_volume" -> session.volume = change.value();
            case "demo_enabled" -> session.enabled = change.checked();
            case "demo_apps" -> session.appOffset = (int) change.value();
            default -> { return; }
        }
        session.handle.update(buildPage(session));
    }

    private void showPage(DemoSession session) {
        session.handle.update(buildPage(session));
        if (session.page == 6) playPresetNodeAnimations(session);
        else session.handle.animate(UiAnimation.builder().durationTicks(10)
                .easing(UiEasing.CUBIC_OUT).opacity(0.0f, 1.0f)
                .offset(UiAnimation.Direction.RIGHT, 10.0f).build());
    }

    private void setCamera(DemoSession session, UiCameraTransform transform) {
        session.cameraTransform = transform;
        session.handle.cameraTransform(transform);
    }

    private UiDocument buildPage(DemoSession session) {
        UiDocument.Builder builder = UiDocument.builder()
                .add(new UiBackgroundNode(PANEL_X, PANEL_Y, 0.0f,
                        PANEL_WIDTH, PANEL_HEIGHT,
                        org.bukkit.Color.fromARGB(0xB0000000)));
        addHeader(builder, session.page);
        switch (session.page) {
            case 0 -> addTextPage(builder, session.gradientFrame);
            case 1 -> addListPage(builder);
            case 2 -> addInteractivePage(builder);
            case 3 -> addCameraPage(builder);
            case 4 -> addActionPage(builder);
            case 5 -> addControlPage(builder, session);
            case 6 -> addAnimationPage(builder);
            case 7 -> addChooseAppPage(builder, session);
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
                    case 4 -> "LINK + COMMAND ACTIONS";
                    case 5 -> "LIVE CONTROLS";
                    case 6 -> "PRESET EFFECT GALLERY";
                    default -> "CHOOSE APP · SCROLL LIST";
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
        addControlButton(builder, id, x, y, 78, label, description);
    }

    private void addControlButton(UiDocument.Builder builder, String id, float x,
                                  float y, float width, String label, String description) {
        builder.add(new BlockNode(Material.GRAY_CONCRETE.createBlockData(),
                        x, y, 0.001f, width, 16, 1))
                .add(new AlignedTextNode(Component.text(label, NamedTextColor.AQUA),
                        x, y, width, 16, UiTextAlignment.CENTER)
                        .fontSize(5).verticalOffset(0).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, x, y, width, 16,
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

    private void addControlPage(UiDocument.Builder builder, DemoSession session) {
        builder.add(new AlignedTextNode(Component.text("Native item/block controls — click to change",
                        NamedTextColor.GRAY), -86, -35, 172, 9, UiTextAlignment.LEFT)
                .fontSize(5).verticalOffset(-1));

        UiSlider slider = new UiSlider("demo_volume", -42, -23, 104, 14,
                0.0, 1.0, session.volume, 0.05,
                Component.text("Click to set volume"));
        UiRect track = slider.trackRect();
        UiRect fill = slider.fillRect(2.0f);
        UiRect thumb = slider.thumbRect(9.0f, 18.0f);
        builder.add(new AlignedTextNode(Component.text("Volume", NamedTextColor.YELLOW,
                        TextDecoration.BOLD), -86, -25, 38, 18, UiTextAlignment.LEFT)
                .fontSize(6).verticalAlignment(UiVerticalAlignment.CENTER));
        builder.add(new BlockNode(Material.GRAY_CONCRETE.createBlockData(),
                track.x(), track.y() + 4, 0.001f, track.width(), 6, 1));
        builder.add(new BlockNode(Material.BLUE_CONCRETE.createBlockData(),
                fill.x(), fill.y() + 4, 0.004f, fill.width(), 6, 1));
        builder.add(new UiIconNode(new ItemStack(Material.SLIME_BALL),
                thumb.x(), thumb.y(), thumb.width(), thumb.height(), 16, 16));
        builder.add(new AlignedTextNode(Component.text(
                        String.format("%.0f%%", session.volume * 100), NamedTextColor.WHITE),
                66, -25, 26, 18, UiTextAlignment.RIGHT)
                .fontSize(5).verticalAlignment(UiVerticalAlignment.CENTER));
        builder.slider(slider);

        UiCheckbox checkbox = new UiCheckbox("demo_enabled", -86, 3, 20, 20,
                session.enabled, Component.text("Toggle enabled"));
        UiRect indicator = checkbox.indicatorRect();
        builder.add(new BlockNode(session.enabled
                        ? Material.LIME_CONCRETE.createBlockData()
                        : Material.RED_CONCRETE.createBlockData(),
                indicator.x(), indicator.y(), 0.001f,
                indicator.width(), indicator.height(), 1));
        builder.add(new UiIconNode(new ItemStack(session.enabled
                        ? Material.LIME_DYE : Material.GRAY_DYE),
                indicator.x() + 3, indicator.y() + 3, 14, 14, 16, 16));
        builder.add(new AlignedTextNode(Component.text("Enabled · "
                                + (session.enabled ? "checked" : "unchecked"),
                        NamedTextColor.YELLOW, TextDecoration.BOLD),
                -62, 3, 110, 20, UiTextAlignment.LEFT)
                .fontSize(6).verticalAlignment(UiVerticalAlignment.CENTER));
        builder.checkbox(checkbox);
    }

    private void addAnimationPage(UiDocument.Builder builder) {
        builder.add(new BlockNode(Material.BLACKSTONE.createBlockData(),
                -82, -28, 0.001f, 52, 38, 2));
        builder.add(new BlockNode(Material.PURPLE_CONCRETE.createBlockData(),
                -24, -20, 0.002f, 46, 30, 2));
        builder.add(new BlockNode(Material.BLUE_CONCRETE.createBlockData(),
                30, -12, 0.003f, 52, 22, 2));
        builder.add(new UiIconNode(new ItemStack(Material.AMETHYST_SHARD),
                -70, -18, 18, 18, 16, 16));
        builder.add(new UiIconNode(new ItemStack(Material.CLOCK),
                -8, -12, 18, 18, 16, 16));
        builder.add(new UiIconNode(new ItemStack(Material.NETHER_STAR),
                48, -7, 18, 18, 16, 16));
        builder.add(new AlignedTextNode(Component.text("SHAPE", NamedTextColor.YELLOW,
                        TextDecoration.BOLD), -82, 17, 52, 12, UiTextAlignment.CENTER)
                .fontSize(5));
        builder.add(new AlignedTextNode(Component.text("ICON", NamedTextColor.AQUA,
                        TextDecoration.BOLD), -24, 17, 46, 12, UiTextAlignment.CENTER)
                .fontSize(5));
        builder.add(new AlignedTextNode(Component.text("TEXT", NamedTextColor.LIGHT_PURPLE,
                        TextDecoration.BOLD), 30, 17, 52, 12, UiTextAlignment.CENTER)
                .fontSize(5));
        builder.add(new AlignedTextNode(Component.text(
                        "Named presets: fade · slide · pop · elastic · drop · rise.",
                        NamedTextColor.GRAY), -86, 31, 172, 10, UiTextAlignment.CENTER)
                .fontSize(4));
    }

    private void playPresetNodeAnimations(DemoSession session) {
        if (session.page != 6 || !session.handle.isValid()) return;
        UiDocument document = buildPage(session);
        session.handle.update(document);
        List<UiAnimation> animations = new ArrayList<>(document.nodes().size());
        // Keep the gallery on monotonic curves. Bounce/elastic presets are
        // useful for isolated demos, but restarting them on a rotating grid
        // makes adjacent rows look like they are stuttering.
        List<UiAnimation> presets = List.of(
                UiEffects.fadeIn(14),
                UiAnimation.slideIn(14, UiAnimation.Direction.LEFT, 18,
                        UiEasing.CUBIC_OUT),
                UiAnimation.slideIn(14, UiAnimation.Direction.RIGHT, 18,
                        UiEasing.CUBIC_OUT),
                UiEffects.scaleIn(0.82f, UiEasing.QUAD_OUT),
                UiEffects.softRise());
        for (int i = 0; i < document.nodes().size(); i++) {
            UiAnimation preset = presets.get(i % presets.size());
            animations.add(UiEffects.delayed(preset, (i % 5) * 2));
        }
        session.handle.animateNodes(animations);
    }

    private static final AppEntry[] APP_ENTRIES = {
            new AppEntry("Spawn", "Change spawn behavior", Material.COMPASS),
            new AppEntry("Recipes", "Look up crafting recipes", Material.CRAFTING_TABLE),
            new AppEntry("Kits", "Collect custom server kits", Material.CHEST),
            new AppEntry("Homes", "Save and teleport to homes", Material.RED_BED),
            new AppEntry("Warps", "Browse public server warps", Material.ENDER_PEARL),
            new AppEntry("Market", "Buy and sell server items", Material.EMERALD),
            new AppEntry("Teleport", "Send teleport requests", Material.ENDER_EYE),
            new AppEntry("Preferences", "Configure UI preferences", Material.REPEATER)
    };

    private int appMaxOffset() {
        return Math.max(0, APP_ENTRIES.length - 4);
    }

    private int appMaxOffset(DemoSession session) {
        return Math.max(0, session.appEntries.size() - 4);
    }

    private void addChooseAppPage(UiDocument.Builder builder, DemoSession session) {
        final float left = -78;
        final float top = -34;
        final float width = 156;
        final float rowHeight = 18;
        final int visibleRows = 4;

        builder.add(new AlignedTextNode(Component.text(
                        "Scroll over the list · right-click an app row",
                        NamedTextColor.GRAY), left, -42, width, 8, UiTextAlignment.LEFT)
                .fontSize(5));
        builder.add(new UiBackgroundNode(left, top - 2, 0.001f, width, 78,
                org.bukkit.Color.fromARGB(0x40000001)));

        for (int row = 0; row < visibleRows; row++) {
            int index = session.appOffset + row;
            if (index >= session.appEntries.size()) {
                addEmptyAppRow(builder, top + row * rowHeight, row);
            } else {
                AppEntry app = session.appEntries.get(index);
                addAppRow(builder, app, top + row * rowHeight,
                        "app_" + index, index == session.selectedApp);
            }
        }

        builder.scrollList(new UiScrollList("demo_apps", left, top, width,
                visibleRows * rowHeight, appMaxOffset(session), session.appOffset,
                Component.text("Scroll applications")));
        builder.add(new AlignedTextNode(Component.text(
                        (session.appOffset + 1) + "–" + Math.min(session.appEntries.size(),
                                session.appOffset + visibleRows) + " / " + session.appEntries.size(),
                        NamedTextColor.DARK_GRAY), left, 40, width, 8,
                UiTextAlignment.CENTER).fontSize(4));
        addControlButton(builder, "app_up", 76, -29, 18, "▲", "Scroll up");
        addControlButton(builder, "app_down", 76, 19, 18, "▼", "Scroll down");
    }

    private void addAppRow(UiDocument.Builder builder, AppEntry app, float y,
                           String id, boolean selected) {
        Material background = selected ? Material.LIGHT_BLUE_STAINED_GLASS
                : Material.LIGHT_GRAY_STAINED_GLASS;
        builder.add(new BlockNode(background.createBlockData(), -74, y,
                0.004f, 148, 16, 1));
        builder.add(new UiIconNode(new ItemStack(app.icon()), -70, y + 2,
                12, 12, 16, 16));
        builder.add(new AlignedTextNode(Component.text(selected ? "☑" : "☐",
                        selected ? NamedTextColor.AQUA : NamedTextColor.WHITE),
                -56, y + 1, 10, 14, UiTextAlignment.CENTER).fontSize(7));
        builder.add(new AlignedTextNode(Component.text(app.name(),
                        NamedTextColor.WHITE, TextDecoration.BOLD), -43, y + 1,
                74, 8, UiTextAlignment.LEFT).fontSize(5));
        builder.add(new AlignedTextNode(Component.text(app.description(),
                        NamedTextColor.GRAY), -43, y + 8, 112, 6,
                UiTextAlignment.LEFT).fontSize(3.5f));
        builder.button(new UiButton(id, -74, y, 148, 16,
                Component.text("Open " + app.name(), NamedTextColor.YELLOW)));
    }

    private void addEmptyAppRow(UiDocument.Builder builder, float y, int row) {
        builder.add(new BlockNode(Material.GRAY_STAINED_GLASS.createBlockData(),
                -74, y, 0.004f, 148, 16, 1));
        builder.add(new UiIconNode(new ItemStack(Material.AIR), -70, y + 2,
                12, 12, 16, 16));
        builder.add(new AlignedTextNode(Component.empty(), -56, y + 1,
                10, 14, UiTextAlignment.CENTER).fontSize(7));
        builder.add(new AlignedTextNode(Component.empty(), -43, y + 1,
                74, 8, UiTextAlignment.LEFT).fontSize(5));
        builder.add(new AlignedTextNode(Component.empty(), -43, y + 8,
                112, 6, UiTextAlignment.LEFT).fontSize(3.5f));
        builder.button(new UiButton("empty_app_" + row, -74, y, 148, 16,
                Component.empty()));
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
            } else if (session.page == 6 && !session.handle.isAnimating()) {
                playPresetNodeAnimations(session);
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
        private double volume = 0.5;
        private boolean enabled = true;
        private int appOffset;
        private int selectedApp = -1;
        private final List<AppEntry> appEntries = new ArrayList<>();
        private UiCameraTransform cameraTransform = UiCameraTransform.fixed();

        private DemoSession(UUID playerId) {
            this.playerId = playerId;
            appEntries.addAll(List.of(APP_ENTRIES));
            Collections.shuffle(appEntries);
        }
    }

    private record AppEntry(String name, String description, Material icon) { }
}
