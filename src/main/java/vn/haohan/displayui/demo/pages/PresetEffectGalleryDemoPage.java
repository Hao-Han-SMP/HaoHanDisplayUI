package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.animation.Easings;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

/** Animation laboratory where every test uses one scene-level target. */
public final class PresetEffectGalleryDemoPage extends BaseDemoPage {
    @Override
    public String title() { return "SMOOTH ANIMATION LAB"; }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        builder.add(new AlignedTextNode(Component.text(
                                "Each test moves the complete scene from A to B with one shared progress curve.",
                                NamedTextColor.GRAY),
                        -86, -34, 172, 8, UiTextAlignment.CENTER).fontSize(4.2f));

        addPresetCard(builder, "anim_card_slide", -86, -24, Material.ARROW,
                "Smooth Slide", "One target, cubic-out motion");
        addPresetCard(builder, "anim_card_scale", -27, -24, Material.SLIME_BALL,
                "Smooth Scale", "One target, no stagger");
        addPresetCard(builder, "anim_card_depth", 32, -24, Material.ENDER_EYE,
                "Depth Travel", "Linear movement on Z axis");
        addPresetCard(builder, "anim_card_fade", -86, 12, Material.GLOWSTONE_DUST,
                "Clean Fade", "Opacity only");
        addPresetCard(builder, "anim_card_float", -27, 12, Material.AMETHYST_SHARD,
                "Float", "Small Y and Z translation");
        addPresetCard(builder, "anim_card_linear", 32, 12, Material.FEATHER,
                "Linear A → B", "Constant speed reference");
    }

    @Override
    public void onShow(DemoContext context) { playPresetAnimation(context, "anim_card_fade"); }

    @Override
    public boolean onClick(DemoContext context, String buttonId, Player player) {
        if (!buttonId.startsWith("anim_card_")) return false;
        playPresetAnimation(context, buttonId);
        return true;
    }

    private void playPresetAnimation(DemoContext context, String buttonId) {
        UiHandle handle = context.handle();
        if (handle == null || !handle.isValid()) return;

        // This page diagnoses A -> B smoothness. Do not create competing
        // per-node targets or stagger delays for a single button click.
        handle.animate(animationFor(buttonId));
    }

    private UiAnimation animationFor(String buttonId) {
        return switch (buttonId) {
            case "anim_card_slide" -> UiAnimation.builder().durationTicks(18)
                    .easing(Easings.OutCubic).opacity(0.0f, 1.0f)
                    .offset(UiAnimation.Direction.LEFT, 28.0f).build();
            case "anim_card_scale" -> UiAnimation.builder().durationTicks(18)
                    .easing(Easings.OutCubic).opacity(0.0f, 1.0f)
                    .scale(0.76f, 1.0f).build();
            case "anim_card_depth" -> UiAnimation.builder().durationTicks(20)
                    .easing(Easings.OutCubic).opacity(0.0f, 1.0f)
                    .offset(UiAnimation.Direction.FRONT, 0.55f).build();
            case "anim_card_float" -> UiAnimation.builder().durationTicks(20)
                    .easing(Easings.OutCubic).opacity(0.0f, 1.0f)
                    .offset(0.0f, 12.0f, 0.12f).build();
            case "anim_card_linear" -> UiAnimation.builder().durationTicks(20)
                    .easing(Easings.Linear).opacity(0.0f, 1.0f)
                    .offset(UiAnimation.Direction.RIGHT, 24.0f).build();
            case "anim_card_fade" -> UiAnimation.fadeIn(16, Easings.OutCubic);
            default -> UiAnimation.fadeIn(16, Easings.OutCubic);
        };
    }

    private void addPresetCard(UiDocument.Builder builder, String id, float x, float y,
                               Material icon, String title, String subtitle) {
        final float width = 54.0f;
        final float height = 32.0f;
        builder.add(new BlockNode(Material.GRAY_STAINED_GLASS.createBlockData(),
                                x, y, 0.001f, width, height, 1))
                .add(new UiIconNode(new ItemStack(icon), x + 4, y + 4, 12, 12, 16, 16))
                .add(new AlignedTextNode(
                                Component.text(title, NamedTextColor.AQUA, TextDecoration.BOLD),
                                x + 20, y + 4, width - 22, 10, UiTextAlignment.LEFT)
                        .fontSize(4.2f).atDepth(0.004f).shadowed(true))
                .add(new AlignedTextNode(Component.text(subtitle, NamedTextColor.GRAY),
                                x + 4, y + 18, width - 8, 10, UiTextAlignment.LEFT)
                        .fontSize(3.3f).atDepth(0.004f))
                .button(new UiButton(id, x, y, width, height,
                        Component.text("Run " + title, NamedTextColor.YELLOW)).hitSlop(2));
    }
}
