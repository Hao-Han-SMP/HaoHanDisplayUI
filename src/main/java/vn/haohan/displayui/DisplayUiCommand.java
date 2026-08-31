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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vn.haohan.displayui.api.UiOptions;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.animation.UiEasing;
import vn.haohan.displayui.api.interaction.UiControlChange;
import vn.haohan.displayui.api.node.EntityModelNode;
import vn.haohan.displayui.demo.DemoContext;
import vn.haohan.displayui.demo.DemoPage;
import vn.haohan.displayui.demo.DemoUiRenderer;
import vn.haohan.displayui.demo.mobgrid.MobGridShowcase;
import vn.haohan.displayui.demo.pages.*;
import vn.haohan.displayui.runtime.DisplayUiServiceImpl;

import java.util.*;

final class DisplayUiCommand implements CommandExecutor, TabCompleter {
    private static final String LEGACY_DEMO_OWNER = "haohandisplayui:demo";

    private final HaoHanDisplayUIPlugin plugin;
    private final DisplayUiServiceImpl service;
    private final List<DemoPage> pages;
    private final Map<UUID, DemoContext> demos = new LinkedHashMap<>();
    private final Map<UUID, MobGridShowcase.Session> mobGrids = new LinkedHashMap<>();

    DisplayUiCommand(HaoHanDisplayUIPlugin plugin, DisplayUiServiceImpl service) {
        this.plugin = plugin;
        this.service = service;
        this.pages = List.of(
                new TextStylesDemoPage(),
                new ListLayoutsDemoPage(),
                new InteractiveHoverDemoPage(),
                new CameraAxisLockDemoPage(),
                new ActionsLinkCommandDemoPage(),
                new LiveControlsDemoPage(),
                new PresetEffectGalleryDemoPage(),
                new ChooseAppScrollListDemoPage(),
                new MixedGrid3DDemoPage(),
                new MobShowcase3DDemoPage()
        );

        Bukkit.getScheduler().runTaskTimer(plugin, this::animateDemos, 1L, 1L);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        String action = args.length == 0 ? "info" : args[0].toLowerCase();
        return switch (action) {
            case "info" -> info(sender);
            case "demo" -> demo(sender);
            case "mobgrid", "testallmobs", "allmobs" -> mobGrid(sender, args);
            case "clear" -> clear(sender);
            default -> false;
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> sub = List.of("info", "demo", "mobgrid", "testallmobs", "clear");
            String prefix = args[0].toLowerCase();
            return sub.stream().filter(s -> s.startsWith(prefix)).toList();
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("mobgrid") || sub.equals("testallmobs") || sub.equals("allmobs")) {
                int totalPages = MobGridShowcase.totalPages();
                List<String> pageOptions = new ArrayList<>();
                pageOptions.add("1");
                for (int p = 5; p <= totalPages; p += 5) {
                    pageOptions.add(String.valueOf(p));
                }
                if (!pageOptions.contains(String.valueOf(totalPages))) {
                    pageOptions.add(String.valueOf(totalPages));
                }
                return pageOptions.stream().filter(p -> p.startsWith(args[1])).toList();
            }
        }
        return Collections.emptyList();
    }

    private boolean info(CommandSender sender) {
        sender.sendMessage("§dHaoHanDisplayUI §7- active scenes: §f" + service.active().size());
        sender.sendMessage("§7Total Registered Mobs: §f" + EntityModelNode.getRegisteredMobNames().size());
        sender.sendMessage("§7API: §fvn.haohan.displayui.api.DisplayUiService");
        return true;
    }

    private boolean demo(CommandSender sender) {
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

        int totalPages = MobGridShowcase.totalPages();
        targetPage = Math.min(targetPage, totalPages - 1);

        cleanupExisting(player.getUniqueId());

        Location origin = player.getEyeLocation()
                .add(player.getEyeLocation().getDirection().multiply(3.0));
        origin.setYaw(player.getLocation().getYaw() + 180.0f);
        origin.setPitch(0.0f);

        MobGridShowcase.Session session = new MobGridShowcase.Session(player.getUniqueId(), targetPage);
        session.handle(service.create(mobGridOwner(player.getUniqueId()), origin,
                MobGridShowcase.buildPage(session),
                new UiOptions(80.0f, 12.0, false, 0.2f, "haohan_display_ui_mobgrid", session.cameraTransform()),
                candidate -> candidate.getUniqueId().equals(player.getUniqueId())));

        session.handle().onClick(click -> MobGridShowcase.onClick(session, click.button().id(), click.player()));
        session.handle().animate(UiAnimation.fadeIn(12, UiEasing.EASE_OUT));
        mobGrids.put(player.getUniqueId(), session);

        sender.sendMessage("§a[DisplayUI] Opened Minecraft Mobs Grid (Page " + (targetPage + 1) + "/" + totalPages
                + ", " + EntityModelNode.getRegisteredMobNames().size() + " total mobs).");
        sender.sendMessage("§7Hover to 3D spin models · Click any slot to view model file and CMD ID in chat.");
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

            if (context.page() == 0) {
                context.updateView();
            }
        }

        Iterator<MobGridShowcase.Session> gridIterator = mobGrids.values().iterator();
        while (gridIterator.hasNext()) {
            MobGridShowcase.Session session = gridIterator.next();
            Player player = Bukkit.getPlayer(session.playerId());
            if (player == null || session.handle() == null || !session.handle().isValid()) {
                if (session.handle() != null && session.handle().isValid()) session.handle().remove();
                gridIterator.remove();
            }
        }
    }

    private boolean clear(CommandSender sender) {
        int removed = 0;
        for (DemoContext context : demos.values()) {
            if (context.handle() != null && context.handle().isValid()) {
                context.handle().remove();
                removed++;
            }
        }
        demos.clear();

        for (MobGridShowcase.Session session : mobGrids.values()) {
            if (session.handle() != null && session.handle().isValid()) {
                session.handle().remove();
                removed++;
            }
        }
        mobGrids.clear();

        removed += service.removeOwnedBy(LEGACY_DEMO_OWNER);
        sender.sendMessage("§aRemoved " + removed + " active Display UI scene(s).");
        return true;
    }

    private String demoOwner(UUID playerId) {
        return "haohandisplayui:demo/" + playerId.toString().toLowerCase();
    }

    private String mobGridOwner(UUID playerId) {
        return "haohandisplayui:mobgrid/" + playerId.toString().toLowerCase();
    }
}
