package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.EntityModelNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class MixedGrid3DDemoPage extends BaseDemoPage {
    @Override
    public String title() {
        return "3D ROTATING GRID (ITEMS & MOBS)";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        final float slotW = 38;
        final float slotH = 31;
        final float col0 = -83;
        final float col1 = -40;
        final float col2 = 3;
        final float col3 = 46;
        final float row1Y = -28;
        final float row2Y = 8;

        addSlotFrame(builder, col0, row1Y, slotW, slotH);
        EntityModelNode helmet = new EntityModelNode(new ItemStack(Material.NETHERITE_HELMET),
                col0 + slotW * 0.5f, row1Y + 9, 20, 20, 0.22f)
                .withTransform(ItemDisplay.ItemDisplayTransform.FIXED)
                .cursorTrack();
        builder.entityModel(helmet);
        builder.button(UiButton.forModel("showcase_helmet", helmet)
                .describedBy(Component.text("Netherite Helmet · 3D Cursor tilt", NamedTextColor.YELLOW)));
        builder.add(new AlignedTextNode(Component.text("HELMET (TILT)", NamedTextColor.WHITE,
                TextDecoration.BOLD), col0, row1Y + 18, slotW, 6, UiTextAlignment.CENTER)
                .fontSize(3.5f).shadowed(true).atDepth(0.004f));

        addSlotFrame(builder, col1, row1Y, slotW, slotH);
        EntityModelNode trident = new EntityModelNode(new ItemStack(Material.TRIDENT),
                col1 + slotW * 0.5f, row1Y + 9, 20, 20, 0.18f)
                .withTransform(ItemDisplay.ItemDisplayTransform.FIXED)
                .autoSpin(2.5f);
        builder.entityModel(trident);
        builder.button(UiButton.forModel("showcase_trident", trident)
                .describedBy(Component.text("Trident · Continuous 360° spin", NamedTextColor.AQUA)));
        builder.add(new AlignedTextNode(Component.text("TRIDENT (SPIN)", NamedTextColor.AQUA,
                TextDecoration.BOLD), col1, row1Y + 18, slotW, 6, UiTextAlignment.CENTER)
                .fontSize(3.5f).shadowed(true).atDepth(0.004f));

        addSlotFrame(builder, col2, row1Y, slotW, slotH);
        EntityModelNode sword = new EntityModelNode(new ItemStack(Material.DIAMOND_SWORD),
                col2 + slotW * 0.5f, row1Y + 9, 20, 20, 0.18f)
                .withTransform(ItemDisplay.ItemDisplayTransform.FIXED)
                .yawRange(-45, 45)
                .pitchRange(-25, 25);
        builder.entityModel(sword);
        builder.button(UiButton.forModel("showcase_sword", sword)
                .describedBy(Component.text("Diamond Sword · Dynamic tilt", NamedTextColor.LIGHT_PURPLE)));
        builder.add(new AlignedTextNode(Component.text("SWORD (TILT)", NamedTextColor.LIGHT_PURPLE,
                TextDecoration.BOLD), col2, row1Y + 18, slotW, 6, UiTextAlignment.CENTER)
                .fontSize(3.5f).shadowed(true).atDepth(0.004f));

        addSlotFrame(builder, col3, row1Y, slotW, slotH);
        EntityModelNode spyglass = new EntityModelNode(new ItemStack(Material.SPYGLASS),
                col3 + slotW * 0.5f, row1Y + 9, 20, 20, 0.18f)
                .withTransform(ItemDisplay.ItemDisplayTransform.FIXED);
        builder.entityModel(spyglass);
        builder.button(UiButton.forModel("showcase_spyglass", spyglass)
                .describedBy(Component.text("Spyglass · Fixed orientation", NamedTextColor.GOLD)));
        builder.add(new AlignedTextNode(Component.text("SPYGLASS", NamedTextColor.GOLD,
                TextDecoration.BOLD), col3, row1Y + 18, slotW, 6, UiTextAlignment.CENTER)
                .fontSize(3.5f).shadowed(true).atDepth(0.004f));

        addSlotFrame(builder, col0, row2Y, slotW, slotH);
        EntityModelNode cow = EntityModelNode.forMob("cow", col0 + slotW * 0.5f, row2Y + 9,
                20, 18, 0.28f)
                .hoverSpin(3.0f);
        builder.entityModel(cow);
        builder.button(UiButton.forModel("showcase_cow", cow)
                .describedBy(Component.text("Cow · Hover spin", NamedTextColor.GREEN)));
        builder.add(new AlignedTextNode(Component.text("COW", NamedTextColor.GREEN,
                TextDecoration.BOLD), col0, row2Y + 23, slotW, 6, UiTextAlignment.CENTER)
                .fontSize(3.5f).shadowed(true).atDepth(0.004f));

        addSlotFrame(builder, col1, row2Y, slotW, slotH);
        EntityModelNode pig = EntityModelNode.forMob("pig", col1 + slotW * 0.5f, row2Y + 9,
                20, 18, 0.28f)
                .autoSpin(2.0f);
        builder.entityModel(pig);
        builder.button(UiButton.forModel("showcase_pig", pig)
                .describedBy(Component.text("Pig · Auto spin", NamedTextColor.LIGHT_PURPLE)));
        builder.add(new AlignedTextNode(Component.text("PIG", NamedTextColor.LIGHT_PURPLE,
                TextDecoration.BOLD), col1, row2Y + 23, slotW, 6, UiTextAlignment.CENTER)
                .fontSize(3.5f).shadowed(true).atDepth(0.004f));

        addSlotFrame(builder, col2, row2Y, slotW, slotH);
        EntityModelNode zombie = EntityModelNode.forMob("zombie", col2 + slotW * 0.5f, row2Y + 9,
                20, 18, 0.30f)
                .yawRange(-50, 50)
                .pitchRange(-30, 30);
        builder.entityModel(zombie);
        builder.button(UiButton.forModel("showcase_zombie", zombie)
                .describedBy(Component.text("Zombie · 3D Cursor tilt", NamedTextColor.RED)));
        builder.add(new AlignedTextNode(Component.text("ZOMBIE", NamedTextColor.RED,
                TextDecoration.BOLD), col2, row2Y + 23, slotW, 6, UiTextAlignment.CENTER)
                .fontSize(3.5f).shadowed(true).atDepth(0.004f));

        addSlotFrame(builder, col3, row2Y, slotW, slotH);
        EntityModelNode creeper = EntityModelNode.forMob("creeper", col3 + slotW * 0.5f, row2Y + 9,
                20, 18, 0.30f)
                .autoSpin(2.5f);
        builder.entityModel(creeper);
        builder.button(UiButton.forModel("showcase_creeper", creeper)
                .describedBy(Component.text("Creeper · Auto 360° spin", NamedTextColor.DARK_GREEN)));
        builder.add(new AlignedTextNode(Component.text("CREEPER", NamedTextColor.DARK_GREEN,
                TextDecoration.BOLD), col3, row2Y + 23, slotW, 6, UiTextAlignment.CENTER)
                .fontSize(3.5f).shadowed(true).atDepth(0.004f));

        builder.add(new AlignedTextNode(Component.text(
                "Hold left-click to drag & rotate · 3D Models tilt, pitch and roll seamlessly",
                NamedTextColor.GRAY), -86, 41, 172, 8, UiTextAlignment.CENTER).fontSize(4.0f));
    }
}
