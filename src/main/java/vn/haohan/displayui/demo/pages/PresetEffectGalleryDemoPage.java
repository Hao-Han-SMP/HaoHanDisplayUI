package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.animation.UiEasing;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

import java.util.ArrayList;
import java.util.List;

public final class PresetEffectGalleryDemoPage extends BaseDemoPage {
    @Override
    public String title() {
        return "PRESET EFFECT GALLERY";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        builder.add(new AlignedTextNode(Component.text(
                        "Click any preset card to trigger its node-by-node animation live.",
                        NamedTextColor.GRAY), -86, -34, 172, 8, UiTextAlignment.CENTER)
                .fontSize(4.5f));

        final float colW = 54;
        final float rowH = 32;
        final float col1 = -86;
        final float col2 = -27;
        final float col3 = 32;
        final float row1 = -24;
        final float row2 = 12;

        addPresetCard(builder, "anim_card_stagger", col1, row1, colW, rowH,
                Material.FEATHER, "Staggered Flow", "Cascades cards top-to-bottom");
        addPresetCard(builder, "anim_card_scale", col2, row1, colW, rowH,
                Material.SLIME_BALL, "Pop & Bounce", "Snappy back-out entrance");
        addPresetCard(builder, "anim_card_cinema", col3, row1, colW, rowH,
                Material.ENDER_EYE, "Cinematic Depth", "3D float into camera plane");

        addPresetCard(builder, "anim_card_slide_left", col1, row2, colW, rowH,
                Material.ARROW, "Slide Sweep", "Left-to-right staggered push");
        addPresetCard(builder, "anim_card_fade", col2, row2, colW, rowH,
                Material.GLOWSTONE_DUST, "Clean Fade", "Pure opacity crossfade");
        addPresetCard(builder, "anim_card_float", col3, row2, colW, rowH,
                Material.AMETHYST_SHARD, "Layer Float", "Differential depth motion");
    }

    @Override
    public void onShow(DemoContext context) {
        playPresetNodeAnimations(context, "anim_card_stagger");
    }

    @Override
    public boolean onClick(DemoContext context, String buttonId, Player player) {
        if (buttonId.startsWith("anim_card_")) {
            playPresetNodeAnimations(context, buttonId);
            return true;
        }
        return false;
    }

    public void playPresetNodeAnimations(DemoContext context, String buttonId) {
        if (context.handle() == null || !context.handle().isValid()) return;
        int totalNodes = context.handle().nodeCount();
        if (totalNodes <= 0) totalNodes = 36;

        List<UiAnimation> list = new ArrayList<>(totalNodes);

        switch (buttonId) {
            case "anim_card_stagger" -> {
                for (int i = 0; i < totalNodes; i++) {
                    int delay = Math.min(12, i * 2);
                    list.add(UiAnimation.slideIn(12, UiAnimation.Direction.TOP, 16.0f, UiEasing.CUBIC_OUT).delay(delay));
                }
            }
            case "anim_card_scale" -> {
                for (int i = 0; i < totalNodes; i++) {
                    int delay = (i % 3) * 2;
                    list.add(UiAnimation.builder().durationTicks(14).delayTicks(delay)
                            .easing(UiEasing.BACK_OUT).opacity(0.0f, 1.0f).scale(0.72f, 1.0f).build());
                }
            }
            case "anim_card_cinema" -> {
                for (int i = 0; i < totalNodes; i++) {
                    int delay = (i % 4) * 2;
                    list.add(UiAnimation.builder().durationTicks(16).delayTicks(delay)
                            .easing(UiEasing.CUBIC_OUT).opacity(0.0f, 1.0f)
                            .offset(UiAnimation.Direction.FRONT, 0.4f).build());
                }
            }
            case "anim_card_slide_left" -> {
                for (int i = 0; i < totalNodes; i++) {
                    int delay = Math.min(10, i);
                    list.add(UiAnimation.slideIn(12, UiAnimation.Direction.LEFT, 24.0f, UiEasing.CUBIC_OUT).delay(delay));
                }
            }
            case "anim_card_fade" -> {
                for (int i = 0; i < totalNodes; i++) {
                    list.add(UiAnimation.fadeIn(10, UiEasing.EASE_OUT));
                }
            }
            case "anim_card_float" -> {
                for (int i = 0; i < totalNodes; i++) {
                    float depthDist = 0.15f + (i % 3) * 0.1f;
                    list.add(UiAnimation.builder().durationTicks(14).delayTicks((i % 3) * 2)
                            .easing(UiEasing.CUBIC_OUT).opacity(0.0f, 1.0f)
                            .offset(0, 10.0f, depthDist).build());
                }
            }
            default -> {
                for (int i = 0; i < totalNodes; i++) {
                    list.add(UiAnimation.fadeIn(10, UiEasing.EASE_OUT));
                }
            }
        }
        context.handle().animateNodes(list);
    }

    private void addPresetCard(UiDocument.Builder builder, String id, float x,
                               float y, float w, float h, Material icon,
                               String title, String subtitle) {
        builder.add(new BlockNode(Material.GRAY_STAINED_GLASS.createBlockData(),
                        x, y, 0.001f, w, h, 1))
                .add(new UiIconNode(new ItemStack(icon), x + 4, y + 4, 12, 12, 16, 16))
                .add(new AlignedTextNode(
                        Component.text(title, NamedTextColor.AQUA, TextDecoration.BOLD),
                        x + 20, y + 4, w - 22, 10, UiTextAlignment.LEFT)
                        .fontSize(4.2f).verticalOffset(0).atDepth(0.004f).shadowed(true))
                .add(new AlignedTextNode(
                        Component.text(subtitle, NamedTextColor.GRAY),
                        x + 4, y + 18, w - 8, 10, UiTextAlignment.LEFT)
                        .fontSize(3.3f).verticalOffset(0).atDepth(0.004f))
                .button(new UiButton(id, x, y, w, h,
                        Component.text("Trigger " + title, NamedTextColor.YELLOW)).hitSlop(2));
    }
}
