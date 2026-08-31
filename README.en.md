<div align="center">

# HaoHan Display UI

A standalone engine plugin for building interactive in-world Minecraft UIs with Display Entities.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1+-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/Paper-API-222222?style=for-the-badge&logo=paper&logoColor=white)](https://papermc.io/)
[![Purpur](https://img.shields.io/badge/Purpur-Compatible-8A4FFF?style=for-the-badge)](https://purpurmc.org/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![Adventure](https://img.shields.io/badge/Adventure-Components-6F42C1?style=for-the-badge)](https://docs.advntr.dev/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)

Language: [Tiếng Việt](README.md) | English

</div>

## Overview

HaoHan Display UI is a Paper/Purpur engine plugin that lets other plugins describe
3D interfaces as immutable documents. The engine manages spawning, updating,
visibility, and cleanup for `TextDisplay`, `ItemDisplay`, `BlockDisplay`, mob entities, and
interactive pixel-precise raycast hit zones.

The plugin does not impose a specific menu or gameplay system. Consumers can use
it for machine panels, guide boards, paginated menus, item lists, command buttons,
documentation links, 3D custom entity/item models, live Vanilla mobs, or per-player camera-facing interfaces.

## Demo Video

![HaoHan Display UI demo](media/Demo.gif)

The GIF above is compatible with GitHub README rendering. [Watch or download the
high-quality MP4 with audio](media/Demo.mp4).

The video demonstrates `/hhdui demo` and its test pages:

1. Plain, bold, italic, animated gradient, obfuscated, and mixed RGB text.
2. Text lists, icon lists, and icons paired with text.
3. Hover descriptions and clickable icon + text rows.
4. Camera billboards, X/Y/Z axis locks, and 45-degree rotations.
5. URL, player command, console command, and permission-tested command actions.
6. Slider, checkbox, and control callbacks.
7. Shapes, icons, and text with independent random animations.
8. Application navigation scroll list (Choose App).
9. **Mixed 3D Mobs & Items Grid (8 Slots)**: Compact cute living mobs (Baby Cow, Baby Pig, Allay, Baby Zombie) mixed with 3D items (Diamond Sword, Trident, Helmet, Totem) in framed slot boxes with hover spin, auto spin, 3D tilt, and angle snapping.
10. **3D Entity Showcase & Inspector**: Full-size living mob inspector (Living Cow, Dragon Head 360°, Diamond Knight Zombie).

## Features

| Area | Capabilities |
| --- | --- |
| Rendering | `TextDisplay`, `ItemDisplay`, `BlockDisplay`, and layered panels. |
| 3D Custom Models | 3D custom item and mob model rendering (`EntityModelNode`) with 3-axis scale, transforms (HEAD/FIXED/GUI), and Euler rotation angles (Yaw, Pitch, Roll). |
| Vanilla Living Mobs | Native Minecraft living mob rendering (`MobEntityNode`) like Cows, Zombies, Allays... with `NoAI`, `Silent`, `Invulnerable`, custom scale (`GENERIC_SCALE`), and customizer consumers without needing resource packs. |
| Model Rotation | Interactive cursor tracking on hover, continuous auto-spinning, and hover-triggered spinning. |
| Angle Constraints | Per-axis locking (`lockYaw`, `lockPitch`, `lockRoll`), angle clamping ranges (`min..max`), step angle snapping, and sensitivity. |
| Text | Adventure Components, RGB, multi-stop gradients, bold, italic, underline, strikethrough, and obfuscated `§k`. |
| Layout | Left/center/right box alignment, top/center/bottom placement, offsets, optical presets, and icon + text rows. |
| Interaction | Logical-pixel raycasting, hover descriptions, hit slop, callbacks, and Bukkit events. |
| Actions | Safe URL prompts, player commands, console commands, and suggested commands. |
| Camera | Fixed, yaw-only, pitch-only, camera-facing, per-axis locks, and X/Y/Z angle offsets. |
| Lifecycle | Per-player audiences, view distance, chunk respawn, update, move, and owner-based cleanup. |
| Optimization | In-place text updates, visibility caching, and metadata updates only for changed nodes. |

## Tech Stack

| Toolkit | Role |
| --- | --- |
| Paper API | Server API and Display Entity support. |
| Purpur | Compatible and recommended server runtime. |
| Java 21 | Main language and runtime. |
| Gradle | Dependency management, tests, builds, and API publishing. |
| Adventure | Rich text, RGB, hover, and clickable chat components. |
| JOML | Quaternions and X/Y/Z transformations. |
| JUnit 5 | Model, layout, and raycasting tests. |

## Requirements

- A Paper or Purpur `1.21.1+` (or `1.21.11`) Minecraft server.
- Java 21 or newer.
- Gradle 8.x when building the current source tree directly.
- Consumer plugins must declare a dependency on `HaoHanDisplayUI`.
- A resource pack is optional; consumer-specific custom fonts or models may
  require one.

## Installation

1. Build or download `HaoHanDisplayUI-1.0.1.jar`.
2. Copy the JAR into the server's `plugins/` directory.
3. Add the dependency to the consumer plugin's `plugin.yml`:

```yaml
depend: [HaoHanDisplayUI]
```

4. Restart the server.
5. Run `/hhdui info` to confirm that the engine is active.
6. Run `/hhdui demo` in game to open the built-in 10-page demonstration.

## Build From Source

Run in the project's root directory:

```powershell
./gradlew clean build
```

The output JAR:

```text
build/libs/HaoHanDisplayUI-1.0.1.jar
```

Fast assemble without tests:

```powershell
./gradlew clean assemble
```

Publish the API to Maven Local for consumer plugin builds:

```powershell
./gradlew publishToMavenLocal
```

Consumer Gradle dependency:

```groovy
repositories {
    mavenLocal()
}

dependencies {
    compileOnly 'vn.haohan:HaoHanDisplayUI:1.0.1'
}
```

## Commands

Administrative commands require the `haohansmp.displayui.admin` permission.
Server operators receive this permission by default.

| Command | Description |
| --- | --- |
| `/hhdui info` | Displays active scene count and the service registration name. |
| `/hhdui demo` | Creates a private 10-page demonstration UI for the sender. |
| `/hhdui clear` | Removes all active demonstration scenes. |

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `haohansmp.displayui.admin` | OP | Grants access to `/hhdui info`, `demo`, and `clear`. |

## Basic API

The public API is organized by responsibility:

| Package | Purpose |
| --- | --- |
| `api` | Service, documents, handles, and scene options. |
| `api.layout` | Rectangles, anchors, and camera transforms. |
| `api.node` | Text, item, icon, block, 3D entity model, and vanilla mob entity nodes. |
| `api.text` | Rich text builders and alignment helpers. |
| `api.interaction` | Buttons, actions, and click callbacks. |
| `api.interaction.event` | Interaction Bukkit events. |
| `api.icon` | Reusable icon registries. |
| `api.view` | Audience and viewer policies. |

Example consumer entry imports:

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
import vn.haohan.displayui.api.node.EntityModelNode;
import vn.haohan.displayui.api.node.MobEntityNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
```

Load the service from Bukkit:

```java
DisplayUiService ui = Bukkit.getServicesManager().load(DisplayUiService.class);
if (ui == null) {
    throw new IllegalStateException("HaoHanDisplayUI is not installed");
}
```

Build a document and scene:

```java
AlignedTextNode title = new AlignedTextNode(
    Component.text("Ancient Forge", NamedTextColor.GOLD),
    -80, -48,
    160, 18,
    UiTextAlignment.LEFT
)
    .fontSize(10)
    .shadowed(true);

UiIconNode icon = new UiIconNode(
    new ItemStack(Material.GOLD_INGOT),
    -76, -18,
    24, 24,
    16, 16
);

UiDocument document = UiDocument.builder()
    .add(new BlockNode(
        Material.BLACK_CONCRETE.createBlockData(),
        -90, -58, 0,
        180, 116, 2
    ))
    .add(title)
    .add(icon)
    .button(UiButton.forIcon("gold", icon)
        .describedBy(Component.text("Gold ingot", NamedTextColor.YELLOW)))
    .build();

UiHandle handle = ui.create(
    "haohanmetallurgy:forge_panel",
    panelLocation,
    document,
    UiOptions.defaults(),
    player -> player.hasPermission("haohansmp.metallurgy.use")
);

handle.onClick(click -> {
    if (click.button().id().equals("gold")) {
        click.player().sendMessage("Gold clicked");
    }
});
```

Basic handle lifecycle:

```java
handle.update(nextPageDocument);
handle.move(newOrigin);
handle.audience(newAudience);
handle.cameraTransform(newTransform);
handle.show(player);
handle.hide(player);
handle.remove();
```

### Pager and Multi-Page Navigation

`UiPager` is a lightweight page adapter inspired by pager and router patterns
in other UI engines. The pager holds immutable `UiDocument` instances, while the
`UiHandle` continues to handle scene diffing and rendering:

```java
UiPager pager = new UiPager(List.of(homePage, settingsPage, helpPage))
    .onPageChange(page -> player.sendActionBar(
        Component.text("Page " + (page + 1))));

UiHandle handle = ui.create("plugin:menu", origin, pager.current());

// In a UiButton click callback:
pager.next();
pager.show(handle);
// Or: pager.previous(), pager.goTo(0), pager.show(handle)
```

By default the pager loops around when crossing bounds. Use
`new UiPager(pages, false)` to clamp at ends. The pager does not spawn new
scenes, does not store player state, and does not replace handle lifecycle.

### Scroll lists

`UiScrollList` provides a scroll hitbox for lists. The engine does not guess
row layouts; the consumer uses `offset()` to render visible items and updates the
document when the offset changes:

```java
UiScrollList scroll = new UiScrollList(
    "items", -80, -40, 160, 80,
    Math.max(0, items.size() - visibleRows), 0,
    Component.text("Scroll list"));

UiDocument page = UiDocument.builder()
    .addAll(renderRows(items, scroll.offset(), visibleRows))
    .scrollList(scroll)
    .build();

handle.onControlChange(change -> {
    if (change.control().id().equals("items")) {
        handle.update(buildItemsPage(items, (int) change.value()));
    }
});
```

Hotbar scrolling is intercepted via Bukkit inventory slot changes and only
cancelled when the player looks directly at the scroll hitbox. `step(n)` allows
multiple rows per tick step.

### Audio and Scene Options (`UiOptions`)

By default, button/slider/checkbox interactions trigger the `minecraft:ui.button.click` sound
when not cancelled. You can customize the sound (including custom resource pack sounds) or disable it completely:

```java
UiOptions options = UiOptions.defaults()
    .withClickSound("my_pack:menu.tick", 0.7f, 1.1f);

UiHandle handle = ui.create("plugin:menu", location, document, options, audience);
// options.withoutClickSound() to disable click sounds.
```

Backface culling for `ItemDisplay`/`UiIconNode` is enabled by default as a software
culling check per player on fixed scenes:

```java
UiOptions options = UiOptions.defaults()
    .withItemBackfaceCulling(false); // disable if UI needs to be seen from the back
```

---

## 3D Mob & Entity Model Display (2 Approaches)

HaoHan Display UI supports **two flexible approaches** for displaying 3D mobs and models:
1. **Vanilla Living Mobs (`MobEntityNode`)**: For native Minecraft mobs (`Cow`, `Zombie`, `Allay`, etc.) with zero resource packs required.
2. **Custom 3D Item/Mob Models (`EntityModelNode`)**: For custom models created in Blockbench, ModelEngine, or Resource Packs with 1.21+ `item_model` definitions.

---

### Approach 1: Native Vanilla Living Mobs (`MobEntityNode`)

`MobEntityNode` spawns a live Bukkit `LivingEntity` on the UI scene. The engine automatically manages:
- `setAI(false)`, `setSilent(true)`, `setInvulnerable(true)`, `setGravity(false)`, `setCollidable(false)`, and `setVisibleByDefault(false)` (packets only sent to authorized viewers).
- Smooth entity scaling via `Attribute.GENERIC_SCALE`.
- Customization callback via `.withCustomizer(Consumer<LivingEntity>)` (set baby state, armor/weapon equipment, sheep wool colors, villager professions, etc.).
- Rotation modes: static Euler angles (Yaw, Pitch), continuous spinning (`autoSpin`), hover-triggered spinning (`hoverSpin`), or interactive cursor tracking (`yawRange`, `pitchRange`).

#### Code Example:

```java
// 1. Baby Cow that spins on cursor hover
MobEntityNode babyCow = new MobEntityNode(EntityType.COW, -60, 0, 36, 36, 0.75f)
    .withCustomizer(mob -> {
        if (mob instanceof Cow cow) {
            cow.setBaby();
        }
    })
    .hoverSpin(3.0f);

// 2. Geared Zombie with 15° snapped cursor tracking
MobEntityNode gearedZombie = new MobEntityNode(EntityType.ZOMBIE, 60, 0, 36, 36, 0.65f)
    .withCustomizer(mob -> {
        if (mob instanceof Zombie z) {
            var eq = z.getEquipment();
            if (eq != null) {
                eq.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                eq.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                eq.setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
            }
        }
    })
    .yawRange(-60, 60)
    .lockPitch(true)
    .step(15);

UiDocument document = UiDocument.builder()
    .mob(babyCow)
    .interactiveMob("inspect_cow", babyCow,
        Component.text("Inspect Baby Cow", NamedTextColor.GREEN),
        UiButtonAction.playerCommand("say What a cute baby cow!"))
    .mob(gearedZombie)
    .build();
```

---

### Approach 2: Custom 3D Item / Mob Models (`EntityModelNode`)

`EntityModelNode` enables rendering 3D item/entity models (weapons, armor, mob heads, custom item models from Blockbench or ModelEngine) on the UI with full JOML transformation matrices and Euler angles (Yaw, Pitch, Roll):

- **Interactive cursor tracking on hover (`CURSOR_TRACKING`)**: When players move their cursor across the model hitbox, the model tilts and rotates smoothly to follow the aim, decaying back to neutral on cursor exit.
- **Continuous auto-spinning (`AUTO_SPIN`)** or hover-triggered spinning (`HOVER_SPIN`).
- **Per-axis rotation locks (`lockYaw`, `lockPitch`, `lockRoll`)**: Lock any rotation axis so the model rotates strictly along desired axes.
- **Angle clamping ranges (`yawRange`, `pitchRange`, `rollRange`)**: Bound minimum and maximum rotation angles (e.g. `-45° .. +45°`).
- **Angle quantizing (`step`)**: Snap rotation angles to discrete steps (e.g. 15° or 45°).
- **Sensitivity and display transforms** (`HEAD`, `FIXED`, `GUI`, `GROUND`, etc.).

#### Code Example:

```java
EntityModelNode sword = new EntityModelNode(
    new ItemStack(Material.DIAMOND_SWORD),
    -40, 0, // center (x, y)
    48, 48, // hover hitbox bounds (width, height)
    1.5f    // 3-axis scale
)
    .withTransform(ItemDisplay.ItemDisplayTransform.FIXED)
    .withRotation(0, 0, -45) // initial Euler angles (yaw, pitch, roll)
    .yawRange(-60, 60)       // clamp hover yaw range
    .lockPitch(true)         // lock pitch axis
    .step(15)                // snap to 15° angle steps
    .sensitivity(1.2f);

EntityModelNode showcaseHelmet = new EntityModelNode(
    new ItemStack(Material.NETHERITE_HELMET),
    40, 0, 1.2f
)
    .autoSpin(2.5f); // auto-spin 2.5° per tick

UiDocument document = UiDocument.builder()
    .entityModel(sword)
    .interactiveModel("inspect_sword", sword,
        Component.text("Inspect Sword", NamedTextColor.AQUA),
        UiButtonAction.executeCommand("inspect sword"))
    .entityModel(showcaseHelmet)
    .build();
```

### Rotation Modes and Presets (`UiModelRotation`)

| Mode | Description |
| --- | --- |
| `CURSOR_TRACKING` | Model rotates smoothly toward the player's aim point on the hitbox and decays back on cursor exit. |
| `HOVER_SPIN` | Model continuously spins around its axis only while hovered by the player. |
| `AUTO_SPIN` | Model continuously spins at all times without requiring hover. |

Factory presets:
- `UiModelRotation.defaults()`: Free cursor tracking.
- `UiModelRotation.locked()`: Locks all rotation axes.
- `UiModelRotation.yawOnly()`: Allows rotation around the Y axis (yaw) only.
- `UiModelRotation.pitchOnly()`: Allows rotation around the X axis (pitch) only.
- `UiModelRotation.autoSpin(speed)`: Continuously spins at `speed` degrees/tick.
- `UiModelRotation.hoverSpin(speed)`: Spins on hover at `speed` degrees/tick.

## Animations and easing

Animations are configured on the handle and run on every node in the scene:

```java
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.animation.UiEasing;

handle.animate(UiAnimation.slideIn(
    10, UiAnimation.Direction.BOTTOM, 18, UiEasing.EASE_OUT));

handle.animate(UiAnimation.builder()
    .durationTicks(14)
    .delayTicks(2)
    .easing(UiEasing.BACK_OUT)
    .opacity(0.0f, 1.0f)
    .scale(0.85f, 1.0f)
    .offset(UiAnimation.Direction.BOTTOM, 12.0f)
    .build());

handle.stopAnimation();
```

Built-in presets: `fadeIn`, `fadeOut`, `slideIn`, `scaleIn`. Supported easings:
`LINEAR`, quadratic, cubic, ease-in/out, `BACK_OUT`, and `ELASTIC_OUT`.

## Slider and checkbox controls

```java
UiSlider volume = new UiSlider("volume", -70, 24, 140, 14,
    0.0, 1.0, 0.5, 0.05, Component.text("Volume"));
UiCheckbox enabled = new UiCheckbox("enabled", -70, 44, 16, 16, true);

UiDocument page = UiDocument.builder()
    .add(panel)
    .slider(volume)
    .checkbox(enabled)
    .build();

handle = ui.create("plugin:settings", location, page);
handle.onControlChange(change -> {
    if (change.control().id().equals("volume")) {
        plugin.setVolume(change.value());
    }
});
```

Helper methods for customized control styling:

```java
UiRect track = slider.trackRect();
UiRect fill = slider.fillRect(2);
UiRect thumb = slider.thumbRect(10, 18);
UiRect indicator = checkbox.indicatorRect();
```

## Coordinate System and Layers

- `(0, 0)` is the scene origin.
- `x` increases toward the visual right.
- `y` increases downward.
- Layout units are logical pixels.
- `pixelsPerBlock` converts logical pixels into world coordinates.
- Larger `depth` values render closer to the viewer.
- `BlockNode.thickness` extends the panel backward.
- The default scale is `40` logical pixels per block.

## Node Types

| Node | Purpose |
| --- | --- |
| `MobEntityNode` | Native living mob (`LivingEntity`) with NoAI, scale, customizer, yaw/pitch, and hover-spin. |
| `EntityModelNode` | 3D custom entity/item model with hover-spin, cursor tracking, Euler rotation, and angle locks. |
| `AlignedTextNode` | Rectangle-based text with layout and optical correction. |
| `TextNode` | Low-level text with direct anchor, line width, and scale control. |
| `UiIconNode` | Box-based item icon with intrinsic texture dimensions. |
| `ItemNode` | Low-level ItemDisplay with its own scale and transform. |
| `BlockNode` | Background, panel, or block-model layer. |

## Rectangles, anchors, and panel-relative layout

```java
UiRect panel = UiRect.centered(0, 0, 180, 116);
UiRect content = panel.inset(8);
UiRect closeButton = panel.place(
    UiAnchor.TOP_RIGHT, UiAnchor.TOP_RIGHT,
    16, 16, -8, 8
);

AlignedTextNode title = new AlignedTextNode(
    Component.text("Ancient Forge"),
    panel.place(UiAnchor.TOP_LEFT, UiAnchor.TOP_LEFT, 140, 18, 8, 8),
    UiTextAlignment.LEFT
);
```

## Interaction, Raycast and Hover

```java
UiButton next = new UiButton("next_page", 58, 42, 28, 16)
    .describedBy(Component.text("Next page", NamedTextColor.AQUA))
    .hitSlop(3);
```

`hitSlop(3)` expands the hitbox by 3 px on each side without altering the visual render.

Text, Icon, 3D Model, and Mob nodes can register interactive hitboxes directly via the builder:

```java
UiDocument document = UiDocument.builder()
    .interactiveText(
        "documentation",
        docsText,
        Component.text("Open Documentation"),
        UiButtonAction.openUrl("https://web.haohansmp.io.vn/en")
    )
    .interactiveIcon(
        "give_item",
        itemIcon,
        Component.text("Claim Item"),
        UiButtonAction.executeCommand("kit starter")
    )
    .interactiveModel(
        "sword_inspect",
        swordModel,
        Component.text("Inspect Sword"),
        UiButtonAction.playerCommand("inspect")
    )
    .interactiveMob(
        "pet_cow",
        babyCowMob,
        Component.text("Pet Baby Cow"),
        UiButtonAction.playerCommand("pet")
    )
    .build();
```

Consumers can also use `UiButton.forText(...)`, `UiButton.forIcon(...)`, `UiButton.forModel(...)`, or `UiButton.forMob(...)`.

## Button Actions

```java
button.withAction(UiButtonAction.openUrl(
    "https://web.haohansmp.io.vn/en"));

button.withAction(UiButtonAction.executeCommand("warp spawn"));
button.withAction(UiButtonAction.playerCommand("warp spawn"));
button.withAction(UiButtonAction.consoleCommand("give {player} diamond"));
button.withAction(UiButtonAction.suggestCommand("msg {player} hello"));
```

| Action | Behavior |
| --- | --- |
| `openUrl` | Sends a clickable chat link prompt for the client to confirm opening. |
| `executeCommand` | Executes immediately as the clicking player with Bukkit permissions. |
| `playerCommand` | Alias for `executeCommand`. |
| `consoleCommand` | Executes through the console; supports the `{player}` placeholder. |
| `suggestCommand` | Inserts the command into the player's chat input without executing. |

## Bukkit Events and Callbacks

Scene-level callbacks:

```java
handle.onClick(click -> {
    switch (click.button().id()) {
        case "previous_page" -> showPreviousPage(click.player());
        case "next_page" -> showNextPage(click.player());
    }
});

handle.onControlChange(change -> {
    if (change.control().id().equals("volume")) {
        setVolume(change.player(), change.value());
    }
});
```

Centralized Bukkit event handling:

```java
@EventHandler
public void onUiButton(UiButtonClickEvent event) {
    if (!event.getHandle().ownerKey().equals("haohanmetallurgy:forge_panel")) return;

    if (!event.getPlayer().hasPermission("haohansmp.metallurgy.use")) {
        event.setCancelled(true);
    }
}

@EventHandler
public void onUiControlChange(UiControlChangeEvent event) {
    if (event.getControl().id().equals("master_volume")) {
        // Handle control change centrally
    }
}
```

Cancelling the event halts both the built-in action and scene callbacks.

## Camera Transforms and Axis Locking

Fixed in world:

```java
UiCameraTransform.fixed();
```

Camera-facing billboard:

```java
UiCameraTransform.cameraFacing();
```

Custom billboard angles and axis locks:

```java
UiCameraTransform transform = UiCameraTransform.cameraFacing()
    .lockX(true)
    .lockY(false)
    .lockZ(true)
    .angleX(0)
    .angleY(45)
    .angleZ(0);

handle.cameraTransform(transform);
```

Billboard mapping:

| Locks | Billboard | Behavior |
| --- | --- | --- |
| X and Y | `FIXED` | Does not rotate with camera. |
| X only | `VERTICAL` | Follows yaw, locks pitch. |
| Y only | `HORIZONTAL` | Follows pitch, locks yaw. |
| Neither X nor Y | `CENTER` | Follows both yaw and pitch. |

## Audience and Visibility

```java
UiAudience ownerOnly = candidate ->
    candidate.getUniqueId().equals(ownerId);

UiHandle handle = ui.create(
    "example:private_panel",
    origin,
    document,
    UiOptions.defaults(),
    ownerOnly
);
```

- `show(player)`: Force displays the UI if the player meets world/distance checks.
- `hide(player)`: Force hides the UI.
- `audience(...)`: Updates the runtime audience predicate.
- `maxDistance`: Restricts rendering and interaction distance.
- `requireFront`: Requires the player to be in front of the panel.

## Third-Party Custom Icon Registry

```java
DisplayUiService ui = Bukkit.getServicesManager().load(DisplayUiService.class);
NamespacedKey iconKey = new NamespacedKey(plugin, "icon/embersteel_ingot");

ItemStack customIcon = new ItemStack(Material.IRON_INGOT);
ItemMeta meta = customIcon.getItemMeta();
meta.setItemModel(new NamespacedKey(plugin, "embersteel_ingot"));
customIcon.setItemMeta(meta);

ui.icons().register(plugin, iconKey, customIcon);
UiIconNode node = ui.icons().createNode(iconKey, new UiRect(10, 10, 32, 32));
```

## Performance & In-Place Updates

`handle.update(document)` checks the diff:

- If only Adventure Components changed, only the affected `TextDisplay` updates.
- If only control values or display transformations changed, updates happen in-place without respawning.
- If geometry, node types, or buttons changed, the scene respawns cleanly.
- Visibility is cached; entity packets are only dispatched on actual state transitions.

Best practices:
- Only create scenes when needed.
- Use audiences and reasonable `maxDistance` limits.
- Call `remove()` when menus or machines are destroyed.
- Combine icons and text into single button hitboxes rather than layering overlapping hitboxes.

## Project Structure

```text
HaoHanDisplayUI/
├─ src/main/java/vn/haohan/displayui/
│  ├─ api/                  Public API for consumer plugins (Document, Handle, Service, Options, Pager)
│  │  ├─ animation/         UiAnimation, UiEasing, UiEffects
│  │  ├─ icon/              UiIconRegistry
│  │  ├─ interaction/       UiButton, UiButtonAction, UiSlider, UiCheckbox, UiScrollList
│  │  │  └─ event/          UiButtonClickEvent, UiControlChangeEvent
│  │  ├─ layout/            UiRect, UiAnchor, UiCameraTransform
│  │  ├─ node/              MobEntityNode, EntityModelNode, AlignedTextNode, TextNode, UiIconNode, ItemNode, BlockNode
│  │  ├─ text/              UiText, UiTextAlignment, UiVerticalAlignment, UiTextOpticalPreset
│  │  └─ view/              UiAudience
│  ├─ runtime/              Scene, raycaster, packet visibility, model rotation & interaction runtime
│  ├─ DisplayUiCommand.java Administrative commands (/hhdui demo, info, clear)
│  └─ HaoHanDisplayUIPlugin.java Plugin entry point
├─ src/main/resources/
│  └─ plugin.yml
├─ src/test/java/           JUnit 5 test suite (Model, Pager, Raycast, MobEntity, etc.)
├─ media/
│  ├─ Demo.gif              Demo animation for GitHub README
│  └─ Demo.mp4              High-quality video with sound
├─ build.gradle
└─ settings.gradle
```

## Operational Notes

- Avoid `/reload` on production servers when consumer plugins retain `UiHandle` instances across reload boundaries; prefer a clean restart.
- The engine cleans up orphaned display entities bearing persistent scene keys on startup.
- `ownerKey` strings should follow standard namespace conventions, e.g. `haohanmetallurgy:forge_panel`.
- Player commands execute under the player's own Bukkit permissions.
- URLs must use standard `http` or `https` schemes.

## License

HaoHan Display UI is licensed under the GNU General Public License v3.0 (GPLv3).
See [LICENSE](LICENSE) for details.

---

<div align="center">
Developed with ❤️ by the <b>HaoHan SMP</b> team.
</div>
