package vn.haohan.displayui.demo;

import org.bukkit.entity.Player;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.interaction.UiControlChange;

public interface DemoPage {
    String title();

    void build(UiDocument.Builder builder, DemoContext context);

    default boolean onClick(DemoContext context, String buttonId, Player player) {
        return false;
    }

    default void onControlChange(DemoContext context, UiControlChange change) {}

    default void onShow(DemoContext context) {}

    default void onTick(DemoContext context) {}
}
