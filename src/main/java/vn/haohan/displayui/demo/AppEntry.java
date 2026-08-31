package vn.haohan.displayui.demo;

import org.bukkit.Material;
import java.util.List;

public record AppEntry(String name, String description, Material icon) {
    public static List<AppEntry> defaultApps() {
        return List.of(
                new AppEntry("Spawn", "Change spawn behavior", Material.COMPASS),
                new AppEntry("Recipes", "Look up crafting recipes", Material.CRAFTING_TABLE),
                new AppEntry("Kits", "Collect custom server kits", Material.CHEST),
                new AppEntry("Homes", "Save and teleport to homes", Material.RED_BED),
                new AppEntry("Warps", "Browse public server warps", Material.ENDER_PEARL),
                new AppEntry("Market", "Buy and sell server items", Material.EMERALD),
                new AppEntry("Teleport", "Send teleport requests", Material.ENDER_EYE),
                new AppEntry("Preferences", "Configure UI preferences", Material.REPEATER)
        );
    }
}
