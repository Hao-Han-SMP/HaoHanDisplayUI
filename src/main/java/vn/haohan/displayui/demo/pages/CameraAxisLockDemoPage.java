package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class CameraAxisLockDemoPage extends BaseDemoPage {
    @Override
    public String title() {
        return "CAMERA + AXIS LOCK";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        builder.add(new AlignedTextNode(Component.text(
                        "Switch between billboard tracking and fixed axis constraints live.",
                        NamedTextColor.GRAY), -86, -34, 172, 10, UiTextAlignment.CENTER)
                .fontSize(4.5f));
        addControlButton(builder, "camera_fixed", -86, -18, "FIXED FLAT",
                "Lock the UI entirely in world space");
        addControlButton(builder, "camera_yaw", 8, -18, "YAW ONLY",
                "Track the player's yaw while keeping pitch flat");
        addControlButton(builder, "camera_pitch", -86, 4, "PITCH ONLY",
                "Track the player's pitch while keeping yaw fixed");
        addControlButton(builder, "camera_full", 8, 4, "FULL BILLBOARD",
                "Fully track the player's camera");
        addControlButton(builder, "camera_x45", -86, 26, 52, "PITCH +45°",
                "Tilt UI down 45 degrees");
        addControlButton(builder, "camera_y45", -26, 26, 52, "YAW +45°",
                "Rotate UI horizontally 45 degrees");
        addControlButton(builder, "camera_z45", 34, 26, 52, "ROLL +45°",
                "Roll UI 45 degrees");
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
            default -> { return false; }
        }
        return true;
    }
}
