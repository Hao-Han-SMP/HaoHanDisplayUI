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

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import vn.haohan.displayui.api.DisplayUiService;
import vn.haohan.displayui.api.UiOptions;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.animation.UiEasing;
import vn.haohan.displayui.api.interaction.UiControlChange;
import vn.haohan.displayui.demo.DemoContext;
import vn.haohan.displayui.demo.DemoPage;
import vn.haohan.displayui.demo.DemoUiRenderer;
import vn.haohan.displayui.demo.mobgrid.MobGridShowcase;
import vn.haohan.displayui.demo.pages.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class DisplayUiCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of(
            "demo", "mobgrid", "clear", "stats", "page", "follow", "camera", "reload");
    private static final List<String> FOLLOW_OPTIONS = List.of("none", "smooth", "hard");
    private static final List<String> CAMERA_PRESETS = List.of("fixed", "face_player", "tilt_up", "tilt_down", "rotate_left", "rotate_right", "skew", "reset");

    private final HaoHanDisplayUIPlugin plugin;
    private final DisplayUiService service;
    private final Map<UUID, DemoContext> demos = new HashMap<>();
    private final Map<UUID, MobGridShowcase.Session> mobGrids = new HashMap<>();
    private final List<DemoPage> pages;

    DisplayUiCommand(HaoHanDisplayUIPlugin plugin, DisplayUiService service) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.service = Objects.requireNonNull(service, "service");
        this.pages = List.of(
                new TextStylesDemoPage(),
                new ListLayoutsDemoPage(),
                new LiveControlsDemoPage(),
                new GeometricShapesDemoPage(),
                new PresetEffectGalleryDemoPage(),
                new MixedGrid3DDemoPage(),
                new MobShowcase3DDemoPage(),
                new ChooseAppScrollListDemoPage(),
                new ActionsLinkCommandDemoPage(),
                new CameraAxisLockDemoPage()
        );
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::animateDemos, 1L, 1L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("haohan.displayui.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        String sub = (args.length > 0) ? args[0].toLowerCase() : "demo";
        return switch (sub) {
            case "demo" -> startDemo(sender);
            case "mobgrid" -> mobGrid(sender, args);
            case "clear" -> clear(sender);
            case "stats" -> stats(sender);
            case "page" -> setPage(sender, args);
            case "follow" -> setFollow(sender, args);
            case "camera" -> setCamera(sender, args);
            case "reload" -> reload(sender);
            default -> {
                sender.sendMessage("§cUnknown subcommand. Use /hhdui <demo|mobgrid|clear|stats|page|follow|camera|reload>");
                yield true;
            }
        };
    }

    private boolean startDemo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command must be run by a player.");
            return true;
        }

        cleanupExisting(player.getUniqueId());

        Location origin = player.getEyeLocation()
                .add(player.getEyeLocation().getDirection().multiply(3.0));
        origin.setYaw(player.getLocation().getYaw() + 180.0f);
        origin.setPitch(0.0f);

        DemoContext context = new DemoContext(player.getUniqueId());
        context.setPageUpdater(ctx -> ctx.handle().update(DemoUiRenderer.render(pages, ctx)));

        context.handle(service.create(demoOwner(player.getUniqueId()), origin,
                DemoUiRenderer.render(pages, context),
                new UiOptions(80.0f, 12.0, false, 0.2f, "haohan_display_ui", context.cameraTransform()),
                candidate -> candidate.getUniqueId().equals(player.getUniqueId())));
        context.handle().mirrorSide(context.mirrorSide());

        context.handle().onClick(click -> onDemoClick(context, click.button().id(), click.player()));
        context.handle().onControlChange(change -> onDemoControlChange(context, change));
        context.handle().animate(UiAnimation.fadeIn(12, UiEasing.EASE_OUT));
        demos.put(player.getUniqueId(), context);

        sender.sendMessage("§aDemo UI created. Aim at a row to see its description, "
                + "right-click to interact, and use ‹/› to change page.");
        plugin.getLogger().info(player.getName() + " created a Display UI demo");
        return true;
    }

    private boolean mobGrid(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command must be run by a player.");
            return true;
        }

        int targetPage = 0;
        if (args.length > 1) {
            try {
                targetPage = Math.max(0, Integer.parseInt(args[1]) - 1);
            } catch (NumberFormatException ignored) {}
        }

        cleanupExisting(player.getUniqueId());

        Location origin = player.getEyeLocation()
                .add(player.getEyeLocation().getDirection().multiply(4.0));
        origin.setYaw(player.getLocation().getYaw() + 180.0f);
        origin.setPitch(0.0f);

        MobGridShowcase.Session session = new MobGridShowcase.Session(player.getUniqueId(), targetPage);
        UiHandle handle = service.create(
                "mobgrid:" + player.getUniqueId(),
                origin,
                MobGridShowcase.buildPage(session),
                new UiOptions(80.0f, 12.0, false, 0.2f, "haohan_display_ui_mobgrid", session.cameraTransform()),
                candidate -> candidate.getUniqueId().equals(player.getUniqueId()));

        session.handle(handle);
        handle.onClick(click -> MobGridShowcase.onClick(session, click.button().id(), click.player()));
        handle.animate(UiAnimation.fadeIn(10, UiEasing.EASE_OUT));
        mobGrids.put(player.getUniqueId(), session);

        sender.sendMessage("§aOpened 3D Mob Grid showcase (Page " + (targetPage + 1) + ")");
        return true;
    }

    private void cleanupExisting(UUID playerId) {
        DemoContext oldDemo = demos.remove(playerId);
        if (oldDemo != null && oldDemo.handle() != null) oldDemo.handle().remove();

        MobGridShowcase.Session oldGrid = mobGrids.remove(playerId);
        if (oldGrid != null && oldGrid.handle() != null) oldGrid.handle().remove();
    }

    private void onDemoClick(DemoContext context, String buttonId, Player player) {
        if (context.handle() == null || !context.handle().isValid()) return;

        switch (buttonId) {
            case "previous_page" -> {
                context.page(Math.floorMod(context.page() - 1, pages.size()));
                showPage(context);
                return;
            }
            case "next_page" -> {
                context.page((context.page() + 1) % pages.size());
                showPage(context);
                return;
            }
            case "toggle_doublesided" -> {
                context.doubleSided(!context.doubleSided());
                context.updateView();
                player.sendMessage(context.doubleSided()
                        ? "§6✧ Double-Sided Rendering: §aENABLED (All elements rendered 2-sided)"
                        : "§6✧ Double-Sided Rendering: §cDISABLED (Single-sided standard)");
                return;
            }
            case "toggle_mirrorside" -> {
                context.mirrorSide(!context.mirrorSide());
                context.handle().mirrorSide(context.mirrorSide());
                context.updateView();
                player.sendMessage(context.mirrorSide()
                        ? "§b✦ Mirror Side: §aENABLED (True mirrored layout and bidirectional controls)"
                        : "§b✦ Mirror Side: §cDISABLED (Standard 3D rotation)");
                return;
            }
            default -> {}
        }

        DemoPage activePage = pages.get(context.page());
        boolean handled = activePage.onClick(context, buttonId, player);
        if (!handled && !buttonId.startsWith("open_") && !buttonId.startsWith("player_")
                && !buttonId.startsWith("console_") && !buttonId.startsWith("execute_")) {
            player.sendMessage("§dDisplay UI click: §f" + buttonId);
        }
    }

    private void onDemoControlChange(DemoContext context, UiControlChange change) {
        if (context.handle() == null || !context.handle().isValid()) return;
        DemoPage activePage = pages.get(context.page());
        activePage.onControlChange(context, change);
    }

    private void showPage(DemoContext context) {
        context.updateView();
        DemoPage activePage = pages.get(context.page());
        activePage.onShow(context);
        if (!(activePage instanceof PresetEffectGalleryDemoPage)) {
            context.handle().animate(UiAnimation.builder().durationTicks(10)
                    .easing(UiEasing.CUBIC_OUT).opacity(0.0f, 1.0f)
                    .offset(UiAnimation.Direction.RIGHT, 10.0f).build());
        }
    }

    private void animateDemos() {
        Iterator<DemoContext> demoIterator = demos.values().iterator();
        while (demoIterator.hasNext()) {
            DemoContext context = demoIterator.next();
            Player player = context.player();
            if (player == null || context.handle() == null || !context.handle().isValid()) {
                if (context.handle() != null && context.handle().isValid()) context.handle().remove();
                demoIterator.remove();
                continue;
            }

            context.advanceGradientFrame();
            DemoPage activePage = pages.get(context.page());
            activePage.onTick(context);
        }

        Iterator<MobGridShowcase.Session> gridIterator = mobGrids.values().iterator();
        while (gridIterator.hasNext()) {
            MobGridShowcase.Session session = gridIterator.next();
            Player player = plugin.getServer().getPlayer(session.playerId());
            if (player == null || session.handle() == null || !session.handle().isValid()) {
                if (session.handle() != null && session.handle().isValid()) session.handle().remove();
                gridIterator.remove();
            }
        }
    }

    private boolean clear(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command must be run by a player.");
            return true;
        }

        cleanupExisting(player.getUniqueId());
        sender.sendMessage("§aDemo UI removed.");
        return true;
    }

    private boolean stats(CommandSender sender) {
        sender.sendMessage("§6--- HaoHanDisplayUI Stats ---");
        sender.sendMessage("§7Active Scenes: §f" + service.active().size());
        sender.sendMessage("§7Active Demos: §f" + demos.size());
        sender.sendMessage("§7Active Mob Grids: §f" + mobGrids.size());
        return true;
    }

    private boolean setPage(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command must be run by a player.");
            return true;
        }

        DemoContext context = demos.get(player.getUniqueId());
        if (context == null || context.handle() == null || !context.handle().isValid()) {
            sender.sendMessage("§cYou don't have an active demo UI. Use /hhdui demo first.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /hhdui page <1-" + pages.size() + ">");
            return true;
        }

        try {
            int pageNum = Integer.parseInt(args[1]) - 1;
            if (pageNum < 0 || pageNum >= pages.size()) {
                sender.sendMessage("§cPage number must be between 1 and " + pages.size());
                return true;
            }

            context.page(pageNum);
            showPage(context);
            sender.sendMessage("§aSwitched to page " + (pageNum + 1) + ": " + pages.get(pageNum).title());
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid page number: " + args[1]);
        }
        return true;
    }

    private boolean setFollow(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command must be run by a player.");
            return true;
        }

        DemoContext context = demos.get(player.getUniqueId());
        if (context == null || context.handle() == null || !context.handle().isValid()) {
            sender.sendMessage("§cYou don't have an active demo UI. Use /hhdui demo first.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /hhdui follow <none|smooth|hard>");
            return true;
        }

        String modeStr = args[1].toLowerCase();
        vn.haohan.displayui.api.view.UiFollowMode mode = switch (modeStr) {
            case "smooth" -> vn.haohan.displayui.api.view.UiFollowMode.SMOOTH;
            case "hard" -> vn.haohan.displayui.api.view.UiFollowMode.HARD;
            case "none" -> vn.haohan.displayui.api.view.UiFollowMode.NONE;
            default -> null;
        };

        if (mode == null) {
            sender.sendMessage("§cInvalid follow mode. Choose from: none, smooth, hard");
            return true;
        }

        context.followMode(mode);
        sender.sendMessage("§aFollow mode set to: " + mode.name());
        return true;
    }

    private boolean setCamera(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command must be run by a player.");
            return true;
        }

        DemoContext context = demos.get(player.getUniqueId());
        if (context == null || context.handle() == null || !context.handle().isValid()) {
            sender.sendMessage("§cYou don't have an active demo UI. Use /hhdui demo first.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /hhdui camera <fixed|face_player|tilt_up|tilt_down|rotate_left|rotate_right|skew|reset>");
            return true;
        }

        String preset = args[1].toLowerCase();
        vn.haohan.displayui.api.layout.UiCameraTransform transform = switch (preset) {
            case "fixed", "reset" -> vn.haohan.displayui.api.layout.UiCameraTransform.fixed();
            case "face_player" -> vn.haohan.displayui.api.layout.UiCameraTransform.cameraFacing();
            case "tilt_up" -> vn.haohan.displayui.api.layout.UiCameraTransform.fixed().angleX(-25.0f);
            case "tilt_down" -> vn.haohan.displayui.api.layout.UiCameraTransform.fixed().angleX(25.0f);
            case "rotate_left" -> vn.haohan.displayui.api.layout.UiCameraTransform.fixed().angleY(-30.0f);
            case "rotate_right" -> vn.haohan.displayui.api.layout.UiCameraTransform.fixed().angleY(30.0f);
            case "skew" -> vn.haohan.displayui.api.layout.UiCameraTransform.fixed().angles(15.0f, -20.0f, 5.0f);
            default -> null;
        };

        if (transform == null) {
            sender.sendMessage("§cInvalid camera preset. Choose from: fixed, face_player, tilt_up, tilt_down, rotate_left, rotate_right, skew, reset");
            return true;
        }

        context.cameraTransform(transform);
        sender.sendMessage("§aCamera transform set to: " + preset);
        return true;
    }

    private boolean reload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage("§aHaoHanDisplayUI configuration reloaded.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("haohan.displayui.admin")) return List.of();

        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if ("follow".equals(sub)) {
                return FOLLOW_OPTIONS.stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .toList();
            } else if ("camera".equals(sub)) {
                return CAMERA_PRESETS.stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .toList();
            } else if ("page".equals(sub)) {
                List<String> pageNums = new ArrayList<>();
                for (int i = 1; i <= pages.size(); i++) pageNums.add(String.valueOf(i));
                return pageNums.stream().filter(s -> s.startsWith(args[1])).toList();
            }
        }
        return List.of();
    }

    private String demoOwner(UUID playerId) {
        return "demo:" + playerId;
    }
}
