<div align="center">

# HaoHan Display UI

A standalone engine plugin for building interactive, pixel-precise in-world UIs in Minecraft using Display Entities — for plugin developers targeting Paper, Purpur, Folia, Pufferfish, and Leaves.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20+-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/Paper-API-222222?style=for-the-badge&logo=paper&logoColor=white)](https://papermc.io/)
[![Purpur](https://img.shields.io/badge/Purpur-Compatible-8A4FFF?style=for-the-badge)](https://purpurmc.org/)
[![Folia](https://img.shields.io/badge/Folia-Supported-00B4D8?style=for-the-badge)](https://github.com/PaperMC/Folia)
[![Pufferfish](https://img.shields.io/badge/Pufferfish-Compatible-F4A261?style=for-the-badge)](https://github.com/pufferfish-gg/Pufferfish)
[![Leaves](https://img.shields.io/badge/Leaves-Compatible-52B788?style=for-the-badge)](https://leavesmc.org/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![Adventure](https://img.shields.io/badge/Adventure-Components-6F42C1?style=for-the-badge)](https://docs.advntr.dev/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)

Language: [Tiếng Việt](README.md) | English

</div>

## Overview

**HaoHan Display UI** is a Paper/Purpur engine plugin that lets other plugins describe 3D interfaces as **static, immutable documents**. The engine manages the full lifecycle — spawning, diffing, updating, visibility, and cleanup — for `TextDisplay`, `ItemDisplay`, `BlockDisplay`, live vanilla mob entities, and pixel-precise raycast hit zones.

The plugin imposes **no specific menu or gameplay logic**. Consumer plugins supply the document; the engine handles rendering.

**Typical use cases:** machine panels, guide boards, paginated menus, item lists, command buttons, documentation links, 3D custom models, live vanilla mobs, and per-player camera-facing HUDs.

---

## Demo Video

![HaoHan Display UI demo](media/Demo.gif)

[Watch or download the high-quality MP4](media/Demo.mp4).

The video demonstrates `/hhdui demo` across its 11 built-in test pages:

1. Plain, bold, italic, animated gradient, obfuscated, and mixed RGB text (`TextStylesDemoPage`).
2. Text lists, icon lists, and icons paired with text (`ListLayoutsDemoPage`).
3. Slider, checkbox, and live control callbacks (`LiveControlsDemoPage`).
4. **Geometric shapes** — `TriangleNode` (3-piece decomposition), `ParallelogramNode` (cyberpunk slanted badges), `LineNode` (axial roll), `PolylineNode` (closed star/pulse waveform) (`GeometricShapesDemoPage`).
5. Preset effect gallery, independent per-node animations, and easing curves (`PresetEffectGalleryDemoPage`).
6. **Mixed 3D Mobs & Items grid (8 slots)** — baby mobs and 3D items with hover-spin, auto-spin, 3D tilt, and angle snapping (`MixedGrid3DDemoPage`).
7. **3D Entity Showcase** — full-size living mob inspector (Cow, Ender Dragon Head 360°, Diamond Knight Zombie) (`MobShowcase3DDemoPage`).
8. Application navigation scroll list with mouse wheel / hotbar interception (`ChooseAppScrollListDemoPage`).
9. Safe URL prompts, player commands, console commands, and permission-tested actions (`ActionsLinkCommandDemoPage`).
10. Camera billboards, X/Y/Z axis locks, and 45° rotations (`CameraAxisLockDemoPage`).
11. **Gradient Backgrounds** — multi-directional continuous gradient panels (Horizontal, Vertical, Diagonal, Center Slant, Radial Corner), seamless slice decomposition, and click-to-apply root gradient (`GradientBackgroundDemoPage`).

---

## Tech Stack

| Component | Version / Details |
|---|---|
| **Language** | Java 21 |
| **Build tool** | Gradle 8.x (`java-library`, `maven-publish` plugins) |
| **Server API** | Paper API `1.21.1-R0.1-SNAPSHOT` |
| **Rich text** | Adventure API (bundled with Paper) |
| **3D math** | JOML — quaternions and TRS transformations |
| **Test framework** | JUnit 5 `5.10.2` + Mockito `5.11.0` |
| **Compatible servers** | Paper · Purpur · Folia · Pufferfish · Leaves (`1.20+` / 1.20.4 – 1.21.x+) |

> **Folia note:** The engine uses `GlobalRegionScheduler` to remain compatible with multi-threaded region servers.

---

## Features

| Area | Capabilities |
|---|---|
| **Rendering** | `TextDisplay`, `ItemDisplay`, `BlockDisplay`, 2D/3D shapes, layered panels. |
| **Background & Gradients** | Solid color panels (`UiBackgroundNode`) and continuous multi-slice gradients (`UiGradientBackgroundNode`) with 9 normalized anchors, presets (horizontal, vertical, diagonal), 2D mathematical interpolation, and configurable slice resolution. |
| **Shapes & Geometry** | Analytical 3-piece triangles (`TriangleNode`), parallelograms/slanted badges (`ParallelogramNode`), closed polylines (`PolylineNode`), axial rolled lines (`LineNode.roll`). |
| **Vanilla Living Mobs** | `MobEntityNode` — native mobs with `NoAI`, `Silent`, `Invulnerable`, `GENERIC_SCALE`, and a customizer consumer. No resource pack required. |
| **Custom 3D Models** | `EntityModelNode` — item/entity models via `ItemDisplay` with full JOML matrix and Euler angles (Yaw, Pitch, Roll). |
| **Model Rotation** | Cursor tracking, continuous auto-spin, hover-spin; per-axis locking, angle clamping, step snapping, and sensitivity control. |
| **Text** | Adventure Components, RGB, multi-stop gradients, bold, italic, underline, strikethrough, obfuscated. |
| **Layout** | Left/center/right alignment, top/center/bottom placement, `UiRect` anchors, icon + text rows, optical corrections. |
| **Interaction** | Logical-pixel raycasting, hover descriptions, hit-slop expansion, click callbacks, cancellable Bukkit events. |
| **Controls** | `UiSlider` (drag with continuous update), `UiCheckbox`, `UiScrollList` (hotbar scroll interception). |
| **Pager** | `UiPager` — immutable-document page adapter; wraps or clamps at bounds. |
| **Actions** | Safe URL prompts, player commands, console commands (`{player}` placeholder), suggested commands. |
| **Camera** | Fixed, yaw-only, pitch-only, camera-facing (`CENTER` / `VERTICAL` / `HORIZONTAL` / `FIXED` billboard). |
| **Follow** | `handle.follow(player, UiFollowOptions)` — unified damping + Display Entity interpolation. |
| **Animations** | `UiAnimation` with presets (`fadeIn`, `fadeOut`, `slideIn`, `scaleIn`) and easings (linear, quadratic, cubic, `BACK_OUT`, `ELASTIC_OUT`). |
| **Lifecycle** | Per-player audience predicates, view distance, chunk respawn, update/move/remove, owner-keyed cleanup. |
| **Optimizations** | In-place text/transformation updates, diff-based respawning, visibility caching, metadata packets only for changed nodes. |

---

## Non-Goals

HaoHan Display UI deliberately **does not**:

- Provide ready-made menus, shops, or game UI templates. It is an **engine**, not a UI kit.
- Handle client-side rendering, shaders, or client-mod animations beyond what Display Entity interpolation supports.
- Act as a general GUI library for inventory/chest menus — it is exclusively for **world-space Display Entity UIs**.
- Bundle a resource pack. Consumers that need custom fonts or `item_model` definitions must supply their own.
- Replace chunk-persistent NPC or hologram plugins for static, always-visible text.
- Support Minecraft versions below `1.20` (Display Entities require 1.19.4+).

---

## Project Structure

```text
HaoHanDisplayUI/
├── src/main/java/vn/haohan/displayui/
│   ├── api/                  Public consumer API (UiDocument, UiHandle, DisplayUiService, UiOptions, UiPager)
│   │   ├── animation/         UiAnimation, UiEasing — tick-based animations with easing curves
│   │   ├── gradient/          UiGradient, UiGradientPosition, UiGradientEndpoint — 2D linear gradient math
│   │   ├── icon/              UiIconRegistry — shared, plugin-scoped icon registration
│   │   ├── interaction/       UiButton, UiButtonAction, UiSlider, UiCheckbox, UiScrollList
│   │   │   └── event/          UiButtonClickEvent, UiControlChangeEvent (cancellable Bukkit events)
│   │   ├── layout/            UiRect, UiAnchor, UiCameraTransform
│   │   ├── node/              All renderable node types (text, icon, block, shape, model, mob, backgrounds)
│   │   ├── shape/             DisplayShapeMath, TRSResult — analytical geometry for Display Entities
│   │   ├── text/              UiText, UiTextAlignment, UiVerticalAlignment, UiTextOpticalPreset
│   │   └── view/              UiAudience, UiFollowOptions — visibility and player-follow policies
│   ├── runtime/              Internal scene graph, raycaster, packet dispatcher, rotation runtime
│   ├── DisplayUiCommand.java  /hhdui command handler (demo, info, clear)
│   └── HaoHanDisplayUIPlugin.java  Plugin entry point and Bukkit service registration
├── src/main/resources/
│   ├── plugin.yml             Plugin manifest (version injected at build time)
│   └── mob_registry.json      Mob scale/offset configuration for MobEntityNode
├── src/test/java/             JUnit 5 test suite (shape math, layout, raycast, pager, mob entity)
├── media/
│   ├── Demo.gif               Animated demo for GitHub README rendering
│   └── Demo.mp4               High-quality demo video with audio
├── build.gradle               Gradle build config, dependencies, and Maven publish setup
└── settings.gradle            Project name declaration
```

---

## Prerequisites & Setup

### Requirements

| Requirement | Minimum Version |
|---|---|
| Java (JDK) | **21** |
| Gradle | **8.x** (Gradle wrapper included — use `./gradlew`) |
| Minecraft server | Paper / Purpur / Folia / Pufferfish / Leaves **1.20+** (1.20.4 – 1.21.x+) |

> A resource pack is **optional**. It is only needed if your consumer plugin uses custom fonts or `item_model` definitions.

### Build Commands

```bash
# Full build including tests
./gradlew clean build
# Output JAR: build/libs/HaoHanDisplayUI-1.0.2.jar

# Fast assemble — skips test execution
./gradlew clean assemble

# Run tests only
./gradlew test

# Publish API to Maven Local (enables consumer plugin builds)
./gradlew publishToMavenLocal
```

### Consumer Plugin Integration

**Step 1 — `plugin.yml`**
```yaml
depend: [HaoHanDisplayUI]
```

**Step 2 — `build.gradle`** (after running `publishToMavenLocal`)
```groovy
repositories {
    mavenLocal()
}
dependencies {
    compileOnly 'vn.haohan:HaoHanDisplayUI:1.0.2'
}
```

**Step 3 — Load the service in Java**
```java
DisplayUiService ui = Bukkit.getServicesManager().load(DisplayUiService.class);
if (ui == null) {
    throw new IllegalStateException("HaoHanDisplayUI is not installed");
}
```

---

## Environment Variables

This project has **no `.env` file** and requires no runtime environment variables. All configuration is done through:

| Location | Purpose |
|---|---|
| `build.gradle` | Group ID, version, and dependency declarations |
| `plugin.yml` | Plugin name, Bukkit API version, and default permissions |
| `UiOptions` (Java API) | Runtime per-scene config: click sound, backface culling, double-sided rendering |

---

## Commands & Permissions

Administrative commands require the `haohansmp.displayui.admin` permission (granted to OPs by default). Command alias: `/displayui`.

| Command | Description |
|---|---|
| `/hhdui info` | Shows active scene count and the service registration name. |
| `/hhdui demo` | Creates a private 11-page demonstration UI for the sender. |
| `/hhdui clear` | Removes all active demonstration scenes. |

| Permission | Default | Description |
|---|---|---|
| `haohansmp.displayui.admin` | OP | Grants access to `/hhdui info`, `demo`, and `clear`. |

---

## Running Tests

```bash
./gradlew test
```

Test coverage includes:

- **Shape math** — `DisplayShapeTest`: `TriangleNode`, `ParallelogramNode`, `LineNode`, `PolylineNode` geometry.
- **Layout** — `UiRect`, anchor placement, and pixels-per-block scaling.
- **Raycasting** — pixel-precise hitbox detection and hit-slop expansion.
- **Pager** — wrap-around, clamping at bounds, and `onPageChange` callbacks.
- **MobEntityNode** — entity attribute configuration and scale application.

---

## API Usage Examples

### Minimal Scene

```java
import vn.haohan.displayui.api.DisplayUiService;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.UiOptions;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.interaction.UiButtonAction;
import vn.haohan.displayui.api.layout.UiRect;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.BlockNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.text.UiTextAlignment;

AlignedTextNode title = new AlignedTextNode(
    Component.text("Ancient Forge", NamedTextColor.GOLD),
    -80, -48, 160, 18,
    UiTextAlignment.LEFT
).fontSize(10).shadowed(true);

UiIconNode icon = new UiIconNode(new ItemStack(Material.GOLD_INGOT), -76, -18, 24, 24, 16, 16);

UiDocument document = UiDocument.builder()
    .add(new BlockNode(Material.BLACK_CONCRETE.createBlockData(), -90, -58, 0, 180, 116, 2))
    .add(title)
    .add(icon)
    .button(UiButton.forIcon("gold", icon)
        .describedBy(Component.text("Gold ingot", NamedTextColor.YELLOW)))
    .build();

UiHandle handle = ui.create(
    "myplugin:forge_panel",   // namespaced owner key — required
    panelLocation,
    document,
    UiOptions.defaults(),
    player -> player.hasPermission("myplugin.forge.use")
);

handle.onClick(click -> {
    if (click.button().id().equals("gold")) {
        click.player().sendMessage("Gold clicked");
    }
});
```

### Handle Lifecycle

```java
handle.update(nextPageDocument);       // diff-based update (only respawns changed nodes)
handle.move(newOrigin);                // reposition in world
handle.audience(newAudience);          // change audience predicate at runtime
handle.cameraTransform(newTransform);  // change billboard mode
handle.show(player);                   // force-show to a specific player
handle.hide(player);                   // force-hide from a specific player
handle.remove();                       // despawn and clean up all entities
```

### Multi-Page Navigation (`UiPager`)

```java
UiPager pager = new UiPager(List.of(homePage, settingsPage, helpPage))
    .onPageChange(page -> player.sendActionBar(Component.text("Page " + (page + 1))));

UiHandle handle = ui.create("plugin:menu", origin, pager.current());

// In a UiButton click callback:
pager.next();
pager.show(handle);
// Also: pager.previous(), pager.goTo(0)
// Use new UiPager(pages, false) to clamp at ends instead of wrapping.
```

### Scroll List

```java
UiScrollList scroll = new UiScrollList(
    "items", -80, -40, 160, 80,
    Math.max(0, items.size() - visibleRows), 0,
    Component.text("Scroll list")
);

UiDocument page = UiDocument.builder()
    .addAll(renderRows(items, scroll.offset(), visibleRows))
    .scrollList(scroll)
    .build();

handle.onControlChange(change -> {
    if (change.control().id().equals("items")) {
        handle.update(buildPage(items, (int) change.value()));
    }
});
```

### Vanilla Living Mob (`MobEntityNode`)

```java
// Baby Cow — hover-spin
MobEntityNode babyCow = new MobEntityNode(EntityType.COW, -60, 0, 36, 36, 0.75f)
    .withCustomizer(mob -> { if (mob instanceof Cow c) c.setBaby(); })
    .hoverSpin(3.0f);

// Diamond Knight Zombie — 15° snapped cursor tracking
MobEntityNode knight = new MobEntityNode(EntityType.ZOMBIE, 60, 0, 36, 36, 0.65f)
    .withCustomizer(mob -> {
        if (mob instanceof Zombie z && z.getEquipment() != null) {
            z.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            z.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        }
    })
    .yawRange(-60, 60).lockPitch(true).step(15);

UiDocument document = UiDocument.builder()
    .mob(babyCow)
    .interactiveMob("inspect_cow", babyCow,
        Component.text("Inspect Baby Cow", NamedTextColor.GREEN),
        UiButtonAction.playerCommand("say Hello!"))
    .mob(knight)
    .build();
```

### Custom 3D Model (`EntityModelNode`)

```java
EntityModelNode sword = new EntityModelNode(new ItemStack(Material.DIAMOND_SWORD), -40, 0, 48, 48, 1.5f)
    .withTransform(ItemDisplay.ItemDisplayTransform.FIXED)
    .withRotation(0, 0, -45)   // initial Euler angles (yaw, pitch, roll)
    .yawRange(-60, 60)          // clamp hover tracking yaw
    .lockPitch(true)            // lock pitch axis
    .step(15)                   // snap to 15° increments
    .sensitivity(1.2f);

EntityModelNode helmet = new EntityModelNode(new ItemStack(Material.NETHERITE_HELMET), 40, 0, 1.2f)
    .autoSpin(2.5f);            // auto-spin 2.5° per tick

UiDocument document = UiDocument.builder()
    .entityModel(sword)
    .interactiveModel("inspect_sword", sword,
        Component.text("Inspect Sword", NamedTextColor.AQUA),
        UiButtonAction.executeCommand("inspect sword"))
    .entityModel(helmet)
    .build();
```

### Animations

```java
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.animation.UiEasing;

// Preset
handle.animate(UiAnimation.slideIn(10, UiAnimation.Direction.BOTTOM, 18, UiEasing.EASE_OUT));

// Custom builder
handle.animate(UiAnimation.builder()
    .durationTicks(14)
    .delayTicks(2)
    .easing(UiEasing.BACK_OUT)
    .opacity(0.0f, 1.0f)
    .scale(0.85f, 1.0f)
    .offset(UiAnimation.Direction.BOTTOM, 12.0f)
    .build());

handle.stopAnimation();
// Built-in presets: fadeIn, fadeOut, slideIn, scaleIn
// Easings: LINEAR, EASE_IN, EASE_OUT, quadratic, cubic, BACK_OUT, ELASTIC_OUT
```

### Player Follow (HUD)

```java
import vn.haohan.displayui.api.view.UiFollowOptions;

handle.follow(player, UiFollowOptions.defaults()
    .distance(3.0)
    .positionDamping(0.35f)
    .rotationDamping(0.25f)
    .interpolationTicks(8));   // Display Entity client-side blend window (not extra server ticks)

handle.stopFollow();
```

### Slider & Checkbox

```java
UiSlider volume    = new UiSlider("volume",  -70, 24, 140, 14, 0.0, 1.0, 0.5, 0.05, Component.text("Volume"));
UiCheckbox enabled = new UiCheckbox("enabled", -70, 44, 16, 16, true);

UiDocument page = UiDocument.builder()
    .slider(volume)
    .checkbox(enabled)
    .build();

handle.onControlChange(change -> {
    if (change.control().id().equals("volume")) plugin.setVolume(change.value());
});
```

### Bukkit Events (Centralized Handling)

```java
@EventHandler
public void onUiButton(UiButtonClickEvent event) {
    if (!event.getHandle().ownerKey().equals("myplugin:forge_panel")) return;
    if (!event.getPlayer().hasPermission("myplugin.forge.use")) {
        event.setCancelled(true); // halts both built-in action and scene callback
    }
}

@EventHandler
public void onUiControlChange(UiControlChangeEvent event) {
    if (event.getControl().id().equals("master_volume")) {
        // centralized control handling
    }
}
```

---

## Backgrounds & Gradients (`UiGradientBackgroundNode`, `UiBackgroundNode`)

Display UI provides two specialized flat panel types rendered by glyphless, background-only `TextDisplay` entities:
1. **Solid Color Background (`UiBackgroundNode`)**: Uniform color fill supporting ARGB opacity (`Color.fromARGB`) and double-sided viewing (`doubleSided`).
2. **Multi-Directional Gradient (`UiGradientBackgroundNode`)**: Smooth color transition calculated via 2D analytical vector projection between two endpoints. The engine decomposes the panel into an edge-matched `TextDisplay` slice grid, mathematically interpolating colors across $(u, v) \in [0, 1] \times [0, 1]$.

### 1. Basic Gradient via Presets

```java
import vn.haohan.displayui.api.gradient.UiGradient;
import vn.haohan.displayui.api.node.UiGradientBackgroundNode;
import org.bukkit.Color;

// Horizontal gradient (left -> right)
UiGradientBackgroundNode horizontalBg = new UiGradientBackgroundNode(
    -90, -50, 0.001f, 180, 100,
    UiGradient.horizontal(Color.fromRGB(220, 20, 60), Color.fromRGB(25, 25, 112))
);

// Vertical gradient (top -> bottom)
UiGradientBackgroundNode verticalBg = new UiGradientBackgroundNode(
    -90, -50, 0.001f, 180, 100,
    UiGradient.vertical(Color.fromRGB(46, 204, 113), Color.fromRGB(22, 160, 133))
);

// Diagonal gradient (top-left -> bottom-right)
UiGradientBackgroundNode diagonalBg = new UiGradientBackgroundNode(
    -90, -50, 0.001f, 180, 100,
    UiGradient.diagonal(Color.fromRGB(155, 89, 182), Color.fromRGB(241, 196, 15))
);
```

### 2. Available `UiGradient` Presets

| Preset | Direction |
|---|---|
| `UiGradient.horizontal(left, right)` | `CENTER_LEFT` → `CENTER_RIGHT` |
| `UiGradient.horizontalReverse(right, left)` | `CENTER_RIGHT` → `CENTER_LEFT` |
| `UiGradient.vertical(top, bottom)` | `CENTER_TOP` → `CENTER_BOTTOM` |
| `UiGradient.verticalReverse(bottom, top)` | `CENTER_BOTTOM` → `CENTER_TOP` |
| `UiGradient.diagonal(topLeft, bottomRight)` | `TOP_LEFT` → `BOTTOM_RIGHT` |
| `UiGradient.diagonalBottomLeftToTopRight(bl, tr)` | `BOTTOM_LEFT` → `TOP_RIGHT` |
| `UiGradient.diagonalTopRightToBottomLeft(tr, bl)` | `TOP_RIGHT` → `BOTTOM_LEFT` |
| `UiGradient.diagonalBottomRightToTopLeft(br, tl)` | `BOTTOM_RIGHT` → `TOP_LEFT` |
| `UiGradient.centerLeftToTopRight(cl, tr)` | `CENTER_LEFT` → `TOP_RIGHT` |
| `UiGradient.centerToBottomRight(center, br)` | `CENTER` → `BOTTOM_RIGHT` (corner flare) |

### 3. Custom Anchors (`UiGradientPosition`) & Normalized $(u, v)$

Define gradients between any two endpoints using the 9 standard enum positions (`TOP_LEFT`, `CENTER_LEFT`, `BOTTOM_LEFT`, `TOP_RIGHT`, `CENTER_RIGHT`, `BOTTOM_RIGHT`, `CENTER_TOP`, `CENTER_BOTTOM`, `CENTER`) or explicit normalized $(u, v)$ floats:

```java
import vn.haohan.displayui.api.gradient.UiGradientPosition;

UiGradientBackgroundNode customPosBg = new UiGradientBackgroundNode(
    -90, -50, 0.001f, 180, 100,
    UiGradientPosition.CENTER_LEFT, Color.fromRGB(255, 215, 0),
    UiGradientPosition.TOP_RIGHT, Color.fromRGB(30, 144, 255)
);

// Or arbitrary (u, v) normalized coordinates in [0.0, 1.0]
UiGradient customGrad = UiGradient.of(
    0.2f, 0.0f, Color.RED,
    0.8f, 1.0f, Color.BLUE
);
```

### 4. Slice Grid Resolution & Double-Sided Rendering

By default, slice count is automatically determined (1D gradients use 16 slices; 2D/diagonal gradients use an 8×8 grid). You can fine-tune slice density or enable double-sided visibility:

```java
// Set slice count along the dominant gradient axis
UiGradientBackgroundNode smooth1D = horizontalBg.withSlices(24);

// Explicit 2D grid resolution (X by Y slices)
UiGradientBackgroundNode smoothGrid = diagonalBg.withGrid(12, 12);

// Enable back-face display entities for 360-degree viewing
UiGradientBackgroundNode doubleSided = horizontalBg.withDoubleSided(true);
```

### 5. Builder & Static Factory Helpers

```java
UiDocument doc = UiDocument.builder()
    .gradientBackground(
        -90, -50, 0.001f, 180, 100,
        UiGradientPosition.CENTER_LEFT, Color.fromARGB(220, 20, 20, 60),
        UiGradientPosition.CENTER_RIGHT, Color.fromARGB(240, 60, 20, 90)
    )
    .background(-80, -40, 0.002f, 160, 80, Color.fromARGB(160, 0, 0, 0))
    .build();

// Alternative factory via UiBackgroundNode:
UiGradientBackgroundNode node = UiBackgroundNode.gradient(
    new UiRect(-90, -50, 180, 100), 0.001f,
    UiGradient.horizontal(Color.RED, Color.BLUE)
);
```

---

## Coordinate System

| Concept | Detail |
|---|---|
| Origin `(0, 0)` | Center of the UI panel |
| `x` axis | Increases rightward on screen |
| `y` axis | Increases downward |
| Layout unit | Logical pixel |
| Default scale | `40` logical pixels = 1 block |
| `depth` | Higher values render closer to the viewer |
| `BlockNode.thickness` | Extends the panel backward (into the wall) |

**Example:** a `180 × 116 px` panel at `40 px/block` ≈ `4.5 × 2.9 blocks`.

---

## Node Reference

| Node | Purpose |
|---|---|
| `MobEntityNode` | Native `LivingEntity` with NoAI, scale, customizer, hover-spin, and cursor tracking. |
| `EntityModelNode` | 3D item/entity model via `ItemDisplay` with Euler rotation, axis locks, and angle clamping. |
| `TriangleNode` | Analytical 3-piece triangle with exact 2D shearing. |
| `ParallelogramNode` | Parallelogram and cyberpunk slanted badge quad panels. |
| `LineNode` | 2D/3D line segment with thickness and axial roll. |
| `PolylineNode` | Multi-vertex continuous polyline with optional closed loop. |
| `UiGradientBackgroundNode` | Continuous multi-slice 2D/1D gradient background panel with mathematical interpolation and configurable slice grids. |
| `UiBackgroundNode` | Solid translucent color panel (`Color.fromARGB`) rendered via background-only `TextDisplay`. |
| `AlignedTextNode` | Rectangle-bound text with box alignment and optical correction. |
| `TextNode` | Low-level text with direct anchor, line width, and scale. |
| `UiIconNode` | Box-bound item icon with intrinsic texture dimensions. |
| `ItemNode` | Low-level `ItemDisplay` with its own scale and transform. |
| `BlockNode` | Background, panel, or block-model layer. |

`UiDocument` is an **immutable snapshot**. Nodes render in ascending `depth` order.

---

## Operational Notes

- **Avoid `/reload`** on production servers when consumer plugins hold `UiHandle` references; prefer a clean restart.
- The engine cleans up orphaned display entities bearing persistent scene keys on startup.
- `ownerKey` must follow namespace conventions, e.g. `myplugin:my_panel`.
- `openUrl` always shows a client-side confirmation prompt — Minecraft does not allow servers to force-open URLs.
- `consoleCommand` executes with full server permissions; only use with trusted, static input — never with raw player input.
- URLs must use the `http` or `https` scheme.
- Custom fonts require manually tuning `contentWidth`/optical offset if glyph metrics differ from Minecraft's default font.

---

## License

HaoHan Display UI is licensed under the **GNU General Public License v3.0 (GPLv3)**.
See [LICENSE](LICENSE) for full terms.

---

<div align="center">
Developed with ❤️ by the <b>HaoHan SMP</b> team
</div>
