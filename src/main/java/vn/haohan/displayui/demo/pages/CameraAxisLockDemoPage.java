/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import vn.haohan.displayui.api.component.ButtonComponent;
import vn.haohan.displayui.api.component.TextComponent;
import vn.haohan.displayui.api.container.Container;
import vn.haohan.displayui.api.layout.UiAnchorPoint;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.view.UiFollowMode;
import vn.haohan.displayui.api.view.UiFollowOptions;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class CameraAxisLockDemoPage extends BaseDemoPage {

    @Override
    public String title() {
        return "CAMERA + HUD FOLLOW";
    }

    @Override
    public void build(Container container, DemoContext context) {
        container.addComponent(TextComponent.builder("cam_subtitle")
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 0.0f)
                .size(174.0f, 6.0f)
                .alignment(UiTextAlignment.CENTER)
                .text(Component.text("Live camera tracking constraints, fixed tilt angles, and dynamic player follow HUD.",
                        NamedTextColor.GRAY))
                .fontSize(3.8f)
                .build());

        // Row 1: Billboards
        container.addComponent(ButtonComponent.builder("camera_fixed")
                .anchor(UiAnchorPoint.TOP_LEFT).origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 8.0f).size(40.0f, 13.0f).label("FIXED").borderRound(3.0f).build());

        container.addComponent(ButtonComponent.builder("camera_yaw")
                .anchor(UiAnchorPoint.TOP_LEFT).origin(UiAnchorPoint.TOP_LEFT)
                .offset(44.0f, 8.0f).size(40.0f, 13.0f).label("YAW").borderRound(3.0f).build());

        container.addComponent(ButtonComponent.builder("camera_pitch")
                .anchor(UiAnchorPoint.TOP_LEFT).origin(UiAnchorPoint.TOP_LEFT)
                .offset(88.0f, 8.0f).size(40.0f, 13.0f).label("PITCH").borderRound(3.0f).build());

        container.addComponent(ButtonComponent.builder("camera_full")
                .anchor(UiAnchorPoint.TOP_RIGHT).origin(UiAnchorPoint.TOP_RIGHT)
                .offset(0.0f, 8.0f).size(42.0f, 13.0f).label("BILLBOARD").borderRound(3.0f).build());

        // Row 2: Angles
        container.addComponent(ButtonComponent.builder("camera_x45")
                .anchor(UiAnchorPoint.TOP_LEFT).origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 24.0f).size(55.0f, 13.0f).label("PITCH +45°").borderRound(3.0f).build());

        container.addComponent(ButtonComponent.builder("camera_y45")
                .anchor(UiAnchorPoint.CENTER_TOP).origin(UiAnchorPoint.CENTER_TOP)
                .offset(0.0f, 24.0f).size(55.0f, 13.0f).label("YAW +45°").borderRound(3.0f).build());

        container.addComponent(ButtonComponent.builder("camera_z45")
                .anchor(UiAnchorPoint.TOP_RIGHT).origin(UiAnchorPoint.TOP_RIGHT)
                .offset(0.0f, 24.0f).size(55.0f, 13.0f).label("ROLL +45°").borderRound(3.0f).build());

        // Row 3: Follow HUD Mode Header
        container.addComponent(TextComponent.builder("hud_header")
                .anchor(UiAnchorPoint.TOP_LEFT).origin(UiAnchorPoint.TOP_LEFT)
                .offset(0.0f, 40.0f).size(174.0f, 6.0f).alignment(UiTextAlignment.CENTER)
                .text(Component.text("PLAYER HUD FOLLOW MODES", NamedTextColor.GOLD, TextDecoration.BOLD))
                .fontSize(4.0f).build());

        // Row 4: Follow Buttons
        UiFollowMode currentMode = context.followMode();
        addFollowButton(container, "follow_off", 0.0f, 48.0f, 40.0f, "OFF", currentMode == UiFollowMode.NONE);
        addFollowButton(container, "follow_hard", 44.0f, 48.0f, 40.0f, "RIGID",
                currentMode == UiFollowMode.FOLLOW && context.followOptions().positionDamping() == 1.0f && context.followOptions().interpolationTicks() == 2);
        addFollowButton(container, "follow_smooth", 88.0f, 48.0f, 40.0f, "SMOOTH",
                currentMode == UiFollowMode.FOLLOW && context.followOptions().positionDamping() < 1.0f);
        addFollowButton(container, "follow_hud", 132.0f, 48.0f, 42.0f, "FAST",
                currentMode == UiFollowMode.FOLLOW && context.followOptions().interpolationTicks() == 1);

        // Row 5: Status text
        String statusText = switch (currentMode) {
            case NONE -> "HUD Status: Stationary (World Anchored)";
            case FOLLOW -> "Follow: dist %.1f, damp %.2f, interp %d ticks"
                    .formatted(context.followOptions().distance(), context.followOptions().positionDamping(),
                            context.followOptions().interpolationTicks());
        };
        container.addComponent(TextComponent.builder("hud_status")
                .anchor(UiAnchorPoint.BOTTOM_LEFT).origin(UiAnchorPoint.BOTTOM_LEFT)
                .offset(0.0f, 0.0f).size(174.0f, 6.0f).alignment(UiTextAlignment.CENTER)
                .text(Component.text(statusText, currentMode == UiFollowMode.NONE ? NamedTextColor.DARK_GRAY : NamedTextColor.GREEN))
                .fontSize(3.8f).build());
    }

    private void addFollowButton(Container container, String id, float x, float y, float width, String label, boolean active) {
        Color bg = active ? Color.fromRGB(30, 140, 60) : Color.fromRGB(45, 50, 60);
        container.addComponent(ButtonComponent.builder(id)
                .anchor(UiAnchorPoint.TOP_LEFT)
                .origin(UiAnchorPoint.TOP_LEFT)
                .offset(x, y)
                .size(width, 13.0f)
                .label(label)
                .backgroundColor(bg)
                .borderRound(3.0f)
                .build());
    }

    @Override
    public boolean onClick(DemoContext context, String buttonId, Player player) {
        switch (buttonId) {
            case "camera_fixed" -> context.cameraTransform(UiCameraTransform.fixed());
            case "camera_yaw" -> context.cameraTransform(UiCameraTransform.fixed().locks(true, false, true));
            case "camera_pitch" -> context.cameraTransform(UiCameraTransform.fixed().locks(false, true, true));
            case "camera_full" -> context.cameraTransform(UiCameraTransform.cameraFacing());
            case "camera_x45" -> context.cameraTransform(UiCameraTransform.fixed().angles(45, 0, 0));
            case "camera_y45" -> context.cameraTransform(UiCameraTransform.fixed().angles(0, 45, 0));
            case "camera_z45" -> context.cameraTransform(UiCameraTransform.fixed().angles(0, 0, 45));
            case "follow_off" -> {
                context.followMode(UiFollowMode.NONE);
                context.updateView();
            }
            case "follow_hard" -> {
                context.followOptions(UiFollowOptions.defaults().damping(1.0f).interpolationTicks(2));
                context.followMode(UiFollowMode.FOLLOW);
                context.updateView();
            }
            case "follow_smooth" -> {
                context.followOptions(UiFollowOptions.defaults());
                context.followMode(UiFollowMode.FOLLOW);
                context.updateView();
            }
            case "follow_hud" -> {
                context.followOptions(UiFollowOptions.defaults().damping(1.0f).interpolationTicks(1));
                context.followMode(UiFollowMode.FOLLOW);
                context.updateView();
            }
            default -> { return false; }
        }
        return true;
    }
}
