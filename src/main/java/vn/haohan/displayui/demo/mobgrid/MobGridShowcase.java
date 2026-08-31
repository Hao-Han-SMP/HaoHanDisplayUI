package vn.haohan.displayui.demo.mobgrid;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.animation.UiEasing;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.EntityModelNode;
import vn.haohan.displayui.api.node.UiBackgroundNode;
import vn.haohan.displayui.api.text.UiText;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.text.UiTextOpticalPreset;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoUiRenderer;

import java.util.List;
import java.util.UUID;

public final class MobGridShowcase {
    public static final int MOBS_PER_PAGE = 8;

    public static final class Session {
        private final UUID playerId;
        private UiHandle handle;
        private int page;
        private UiCameraTransform cameraTransform = UiCameraTransform.fixed();

        public Session(UUID playerId, int page) {
            this.playerId = playerId;
            this.page = page;
        }

        public UUID playerId() { return playerId; }
        public UiHandle handle() { return handle; }
        public void handle(UiHandle handle) { this.handle = handle; }
        public int page() { return page; }
        public void page(int page) { this.page = page; }
        public UiCameraTransform cameraTransform() { return cameraTransform; }
        public void cameraTransform(UiCameraTransform cameraTransform) { this.cameraTransform = cameraTransform; }
    }

    private MobGridShowcase() {}

    public static int totalPages() {
        return Math.max(1, (int) Math.ceil((double) EntityModelNode.getRegisteredMobNames().size() / MOBS_PER_PAGE));
    }

    public static UiDocument buildPage(Session session) {
        UiDocument.Builder builder = UiDocument.builder()
                .add(new UiBackgroundNode(DemoUiRenderer.PANEL_X, DemoUiRenderer.PANEL_Y, 0.0f,
                        DemoUiRenderer.PANEL_WIDTH, DemoUiRenderer.PANEL_HEIGHT,
                        org.bukkit.Color.fromARGB(0xB0000000)));

        List<String> mobNames = EntityModelNode.getRegisteredMobNames();
        int total = totalPages();
        int curPage = Math.max(0, Math.min(session.page(), total - 1));
        int startIdx = curPage * MOBS_PER_PAGE;
        int endIdx = Math.min(startIdx + MOBS_PER_PAGE, mobNames.size());

        // Header
        builder.add(new AlignedTextNode(
                UiText.builder().gradient("ALL MINECRAFT MOB MODELS", new TextColor[] {
                        UiText.hex("#55FFFF"), UiText.hex("#3B82F6"),
                        UiText.hex("#9333EA")
                }, 1.0, TextDecoration.BOLD).build(),
                -86, -56, 172, 14, UiTextAlignment.LEFT)
                .opticalPreset(UiTextOpticalPreset.BOLD_GRADIENT)
                .fontSize(8.5f).shadowed(true));

        builder.add(new AlignedTextNode(
                Component.text("Page " + (curPage + 1) + " / " + total
                        + " (" + (startIdx + 1) + "-" + endIdx + " of " + mobNames.size() + ")", NamedTextColor.DARK_GRAY),
                -86, -43, 172, 9, UiTextAlignment.RIGHT)
                .fontSize(5).verticalOffset(-1));

        final float slotW = 38;
        final float slotH = 31;
        final float[] colX = {-83, -40, 3, 46};
        final float row1Y = -28;
        final float row2Y = 8;

        for (int i = 0; i < MOBS_PER_PAGE; i++) {
            int col = i % 4;
            int row = i / 4;
            float x = colX[col];
            float y = (row == 0) ? row1Y : row2Y;
            int mobIndex = startIdx + i;

            BaseDemoPage.addSlotFrame(builder, x, y, slotW, slotH);

            if (mobIndex < mobNames.size()) {
                String mobId = mobNames.get(mobIndex);
                int cmdId = 10001 + mobIndex;
                float scale = getMobScale(mobId);

                EntityModelNode mobModel = EntityModelNode.forMob(mobId, x + slotW * 0.5f, y + 9.5f,
                        20, 18, scale).hoverSpin(3.0f);
                builder.entityModel(mobModel);

                builder.button(UiButton.forModel("mobgrid_slot_" + mobIndex, mobModel)
                        .describedBy(Component.text(formatMobName(mobId) + " · CMD #" + cmdId + " (Click for details)", NamedTextColor.YELLOW)));

                builder.add(new AlignedTextNode(Component.text(formatDisplayMobName(mobId),
                        NamedTextColor.WHITE, TextDecoration.BOLD), x, y + 22, slotW, 6, UiTextAlignment.CENTER)
                        .fontSize(3.3f).shadowed(true).atDepth(0.004f));

                builder.add(new AlignedTextNode(Component.text("#" + cmdId, NamedTextColor.GOLD),
                        x, y + 27, slotW, 5, UiTextAlignment.CENTER)
                        .fontSize(2.7f).shadowed(true).atDepth(0.004f));
            } else {
                builder.add(new AlignedTextNode(Component.text("EMPTY", NamedTextColor.DARK_GRAY),
                        x, y + 13, slotW, 8, UiTextAlignment.CENTER).fontSize(3.5f));
            }
        }

        // Footer Pagination Controls
        addFooterButton(builder, "mobgrid_prev_5", -86, "<< -5", "Jump back 5 pages");
        addFooterButton(builder, "mobgrid_prev", -56, "< Prev", "Previous page");

        builder.add(new AlignedTextNode(Component.text((curPage + 1) + " / " + total,
                NamedTextColor.GRAY), -26, 48, 52, 12, UiTextAlignment.CENTER)
                .fontSize(5).verticalOffset(-1));

        addFooterButton(builder, "mobgrid_next", 30, "Next >", "Next page");
        addFooterButton(builder, "mobgrid_next_5", 60, "+5 >>", "Jump forward 5 pages");

        builder.add(new AlignedTextNode(Component.text(
                "Aim/hover over mob to rotate 3D · Right-click to print model & CMD details in chat",
                NamedTextColor.DARK_GRAY), -86, 40, 172, 7, UiTextAlignment.CENTER).fontSize(3.8f));

        return builder.build();
    }

    private static float getMobScale(String mobId) {
        String id = mobId.toLowerCase();
        // Huge multi-block entities
        if (id.contains("dragon") || id.equals("giant") || id.equals("ghast")
                || id.equals("elder_guardian") || id.equals("wither")) {
            return 0.18f;
        }
        // Solid block cubes, heads, chests, armor stands, boats, carts, heavy golems
        if (id.contains("double_chest") || id.contains("bed_") || id.contains("boat")
                || id.contains("minecart") || id.equals("iron_golem") || id.equals("ravager")
                || id.equals("warden") || id.equals("shulker") || id.contains("_head")
                || id.contains("_skull") || id.contains("armor_") || id.equals("armor_stand")
                || id.equals("shield") || id.contains("sign_")) {
            return 0.25f;
        }
        // Tiny creatures
        if (id.equals("bee") || id.equals("bat") || id.equals("silverfish")
                || id.equals("endermite") || id.equals("tadpole") || id.equals("cod")
                || id.equals("salmon") || id.contains("pufferfish") || id.contains("tropical_fish")
                || id.equals("frog") || id.equals("rabbit") || id.equals("vex")) {
            return 0.42f;
        }
        // Allay (large base box)
        if (id.equals("allay")) {
            return 0.26f;
        }
        // Standard mobs (Cow, Pig, Zombie, Creeper, Chicken, Skeleton, Spider, etc.)
        return 0.35f;
    }

    public static void showPage(Session session) {
        if (session.handle() == null || !session.handle().isValid()) return;
        session.handle().update(buildPage(session));
        session.handle().animate(UiAnimation.builder().durationTicks(8)
                .easing(UiEasing.CUBIC_OUT).opacity(0.0f, 1.0f)
                .offset(UiAnimation.Direction.RIGHT, 8.0f).build());
    }

    public static void onClick(Session session, String buttonId, Player player) {
        if (session.handle() == null || !session.handle().isValid()) return;
        List<String> mobNames = EntityModelNode.getRegisteredMobNames();
        int total = totalPages();

        switch (buttonId) {
            case "mobgrid_prev_5" -> {
                session.page(Math.max(0, session.page() - 5));
                showPage(session);
            }
            case "mobgrid_prev" -> {
                session.page(Math.floorMod(session.page() - 1, total));
                showPage(session);
            }
            case "mobgrid_next" -> {
                session.page((session.page() + 1) % total);
                showPage(session);
            }
            case "mobgrid_next_5" -> {
                session.page(Math.min(total - 1, session.page() + 5));
                showPage(session);
            }
            default -> {
                if (buttonId.startsWith("mobgrid_slot_")) {
                    try {
                        int index = Integer.parseInt(buttonId.substring("mobgrid_slot_".length()));
                        if (index >= 0 && index < mobNames.size()) {
                            String mobId = mobNames.get(index);
                            int cmdId = 10001 + index;
                            player.sendMessage("§d[DisplayUI] §bMob: §f" + formatMobName(mobId)
                                    + " §7(ID: §a" + mobId + "§7) | §bCMD: §6#" + cmdId
                                    + " §7| §bModel: §eassets/minecraft/models/mob/" + mobId + ".json");
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    private static void addFooterButton(UiDocument.Builder builder, String id, float x,
                                        String label, String description) {
        builder.add(new BlockNode(Material.GRAY_CONCRETE.createBlockData(),
                        x, 46, 0.001f, 26, 14, 1))
                .add(new AlignedTextNode(Component.text(label, NamedTextColor.WHITE,
                        TextDecoration.BOLD), x, 46, 26, 14, UiTextAlignment.CENTER)
                        .fontSize(4.5f).verticalOffset(0).atDepth(0.004f).shadowed(true))
                .button(new UiButton(id, x, 46, 26, 14,
                        Component.text(description, NamedTextColor.AQUA)).hitSlop(2));
    }

    public static String formatMobName(String mobId) {
        if (mobId == null || mobId.isEmpty()) return "";
        String[] parts = mobId.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    private static String formatDisplayMobName(String mobId) {
        String clean = mobId.toUpperCase().replace('_', ' ');
        if (clean.length() > 12) {
            return clean.substring(0, 11) + "..";
        }
        return clean;
    }
}
