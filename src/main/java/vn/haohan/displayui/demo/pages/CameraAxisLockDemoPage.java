package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.view.UiFollowMode;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class CameraAxisLockDemoPage extends BaseDemoPage {
    @Override
    public String title() {
        return "CAMERA + HUD FOLLOW";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        builder.add(new AlignedTextNode(Component.text(
                        "Live camera tracking constraints, fixed tilt angles, and dynamic player follow HUD.",
                        NamedTextColor.GRAY), -86, -34, 172, 8, UiTextAlignment.CENTER)
                .fontSize(4.5f));

        // Row 1: Billboards
        addControlButton(builder, "camera_fixed", -86, -24, 38, "FIXED",
                "Lock the UI entirely in world space");
        addControlButton(builder, "camera_yaw", -44, -24, 38, "YAW",
                "Track player's yaw while keeping pitch flat");
        addControlButton(builder, "camera_pitch", -2, -24, 38, "PITCH",
                "Track player's pitch while keeping yaw fixed");
        addControlButton(builder, "camera_full", 40, -24, 46, "BILLBOARD",
                "Fully track the player's camera facing");

        // Row 2: Angles
        addControlButton(builder, "camera_x45", -86, -8, 52, "PITCH +45°",
                "Tilt UI down 45 degrees");
        addControlButton(builder, "camera_y45", -26, -8, 52, "YAW +45°",
                "Rotate UI horizontally 45 degrees");
        addControlButton(builder, "camera_z45", 34, -8, 52, "ROLL +45°",
                "Roll UI 45 degrees");

        // Row 3: Follow HUD Mode Header & Buttons
        builder.add(new AlignedTextNode(Component.text("PLAYER HUD FOLLOW MODES",
                        NamedTextColor.GOLD, TextDecoration.BOLD),
                -86, 9, 172, 8, UiTextAlignment.CENTER).fontSize(4.5f));

        UiFollowMode currentMode = context.followMode();
        addFollowButton(builder, "follow_off", -86, 18, 52, "FOLLOW: OFF",
                "Detach HUD and anchor at current world location",
                currentMode == UiFollowMode.NONE);
        addFollowButton(builder, "follow_hard", -26, 18, 52, "HARD LOCK",
                "Rigid 1:1 camera lock without delay",
                currentMode == UiFollowMode.HARD);
        addFollowButton(builder, "follow_smooth", 34, 18, 52, "SMOOTH LERP",
                "Damped smooth camera follow with trailing inertia",
                currentMode == UiFollowMode.SMOOTH);

        // Row 4: Follow Mode status subtitle
        String statusText = switch (currentMode) {
            case NONE -> "HUD Status: Stationary (World Anchored)";
            case HARD -> "HUD Status: Hard Locked (Exact View Lock)";
            case SMOOTH -> "HUD Status: Smooth Lerp (Damping: 0.22, Delay on Move/Rotate)";
        };
        builder.add(new AlignedTextNode(Component.text(statusText,
                        currentMode == UiFollowMode.NONE ? NamedTextColor.DARK_GRAY : NamedTextColor.GREEN),
                -86, 35, 172, 8, UiTextAlignment.CENTER).fontSize(4.0f));
    }

    private void addFollowButton(UiDocument.Builder builder, String id, float x, float y,
                                 float width, String label, String description, boolean active) {
        Material bg = active ? Material.GREEN_CONCRETE : Material.GRAY_CONCRETE;
        NamedTextColor textColor = active ? NamedTextColor.YELLOW : NamedTextColor.GRAY;
        Component textComp = active
                ? Component.text(label, textColor, TextDecoration.BOLD)
                : Component.text(label, textColor);
        builder.add(new BlockNode(bg.createBlockData(), x, y, 0.001f, width, 14, 1))
                .add(new AlignedTextNode(textComp,
                        x, y, width, 14, UiTextAlignment.CENTER)
                        .fontSize(4.5f).verticalOffset(0).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, x, y, width, 14,
                        Component.text(description, NamedTextColor.YELLOW)));
    }

    @Override
    public boolean onClick(DemoContext context, String buttonId, Player player) {
        switch (buttonId) {
            case "camera_fixed" -> context.cameraTransform(UiCameraTransform.fixed());
            case "camera_yaw" -> context.cameraTransform(
                    UiCameraTransform.fixed().locks(true, false, true));
            case "camera_pitch" -> context.cameraTransform(
                    UiCameraTransform.fixed().locks(false, true, true));
            case "camera_full" -> context.cameraTransform(UiCameraTransform.cameraFacing());
            case "camera_x45" -> context.cameraTransform(
                    UiCameraTransform.fixed().angles(45, 0, 0));
            case "camera_y45" -> context.cameraTransform(
                    UiCameraTransform.fixed().angles(0, 45, 0));
            case "camera_z45" -> context.cameraTransform(
                    UiCameraTransform.fixed().angles(0, 0, 45));
            case "follow_off" -> {
                context.followMode(UiFollowMode.NONE);
                context.updateView();
            }
            case "follow_hard" -> {
                context.followMode(UiFollowMode.HARD);
                context.updateView();
            }
            case "follow_smooth" -> {
                context.followMode(UiFollowMode.SMOOTH);
                context.updateView();
            }
            default -> { return false; }
        }
        return true;
    }
}
