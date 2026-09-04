package vn.haohan.displayui.demo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.layout.UiCameraTransform;
import vn.haohan.displayui.api.view.UiFollowMode;
import vn.haohan.displayui.api.view.UiFollowOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class DemoContext {
    private final UUID playerId;
    private UiHandle handle;
    private int page;
    private int gradientFrame;
    private double volume = 0.65;
    private boolean enabled = true;
    private int appOffset;
    private int selectedApp;
    private final List<AppEntry> appEntries = new ArrayList<>(AppEntry.defaultApps());
    private UiCameraTransform cameraTransform = UiCameraTransform.fixed();
    private UiFollowMode followMode = UiFollowMode.NONE;
    private UiFollowOptions followOptions = UiFollowOptions.defaults();
    private boolean doubleSided = false;
    private boolean mirrorSide = true;
    private Consumer<DemoContext> pageUpdater;

    public DemoContext(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID playerId() { return playerId; }

    public Player player() { return Bukkit.getPlayer(playerId); }

    public UiHandle handle() { return handle; }
    public void handle(UiHandle handle) { this.handle = handle; }

    public int page() { return page; }
    public void page(int page) { this.page = page; }

    public int gradientFrame() { return gradientFrame; }
    public void advanceGradientFrame() { this.gradientFrame++; }

    public double volume() { return volume; }
    public void volume(double volume) { this.volume = volume; }

    public boolean enabled() { return enabled; }
    public void enabled(boolean enabled) { this.enabled = enabled; }

    public int appOffset() { return appOffset; }
    public void appOffset(int appOffset) { this.appOffset = appOffset; }

    public int selectedApp() { return selectedApp; }
    public void selectedApp(int selectedApp) { this.selectedApp = selectedApp; }

    public List<AppEntry> appEntries() { return appEntries; }

    public boolean doubleSided() { return doubleSided; }
    public void doubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
        if (handle != null && handle.isValid()) handle.doubleSided(doubleSided);
    }

    public boolean mirrorSide() { return mirrorSide; }
    public void mirrorSide(boolean mirrorSide) { this.mirrorSide = mirrorSide; }

    public UiCameraTransform cameraTransform() { return cameraTransform; }
    public void cameraTransform(UiCameraTransform cameraTransform) {
        this.cameraTransform = cameraTransform;
        if (handle != null && handle.isValid()) {
            handle.cameraTransform(cameraTransform);
        }
    }

    public UiFollowMode followMode() { return followMode; }
    public void followMode(UiFollowMode followMode) {
        this.followMode = followMode;
        if (handle != null && handle.isValid()) {
            Player p = player();
            if (p != null) {
                if (followMode == UiFollowMode.NONE) {
                    handle.stopFollow();
                } else {
                    handle.follow(p, followOptions);
                }
            }
        }
    }

    public UiFollowOptions followOptions() { return followOptions; }
    public void followOptions(UiFollowOptions options) {
        this.followOptions = options;
        if (followMode == UiFollowMode.FOLLOW) followMode(UiFollowMode.FOLLOW);
    }

    public void setPageUpdater(Consumer<DemoContext> updater) {
        this.pageUpdater = updater;
    }

    public void updateView() {
        if (pageUpdater != null && handle != null && handle.isValid()) {
            pageUpdater.accept(this);
        }
    }
}
