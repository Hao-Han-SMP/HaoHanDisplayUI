<div align="center">

# HaoHan Display UI

A standalone engine plugin for building interactive in-world Minecraft UIs with Display Entities.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)](https://www.minecraft.net/)
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
visibility, and cleanup for `TextDisplay`, `ItemDisplay`, `BlockDisplay`, and
interactive hit zones.

The plugin does not impose a specific menu or gameplay system. Consumers can use
it for machine panels, guide boards, paginated menus, item lists, command buttons,
documentation links, or per-player camera-facing interfaces.

## Demo Video

![HaoHan Display UI demo](media/Demo.gif)

The GIF above is compatible with GitHub README rendering. [Watch or download the
high-quality MP4 with audio](media/Demo.mp4).

The video demonstrates `/hhdui demo` and its six test pages:

1. Plain, bold, italic, animated gradient, obfuscated, and mixed RGB text.
2. Text lists, icon lists, and icons paired with text.
3. Hover descriptions and clickable icon + text rows.
4. Camera billboards, X/Y/Z axis locks, and 45-degree rotations.
5. URL, player command, console command, and permission-tested command actions.
6. Slider, checkbox, and control callbacks.
7. Shapes, icons, and text with independent random animations.

## Features

| Area | Capabilities |
| --- | --- |
| Rendering | `TextDisplay`, `ItemDisplay`, `BlockDisplay`, and layered panels. |
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

- A Paper or Purpur `1.21.11` Minecraft server.
- Java 21 or newer.
- Gradle 8.x when building the current source tree directly.
- Consumer plugins must declare a dependency on `HaoHanDisplayUI`.
- A resource pack is optional; consumer-specific custom fonts or models may
  require one.

## Installation

1. Build or download `HaoHanDisplayUI-1.0.0.jar`.
2. Copy the JAR into the server's `plugins/` directory.
3. Add the dependency to the consumer plugin's `plugin.yml`:

```yaml
depend: [HaoHanDisplayUI]
```

4. Restart the server.
5. Run `/hhdui info` to confirm that the engine is active.
6. Run `/hhdui demo` in game to open the built-in demonstration.

## Build From Source

Run this command in the project root:

```powershell
gradle clean build
```

The built JAR is generated at:

```text
build/libs/HaoHanDisplayUI-1.0.0.jar
```

For a faster build without tests:

```powershell
gradle clean assemble
```

Publish the API to Maven Local for consumer development:

```powershell
gradle publishToMavenLocal
```

Consumer Gradle dependency:

```groovy
repositories {
    mavenLocal()
}

dependencies {
    compileOnly 'vn.haohan:HaoHanDisplayUI:1.0.0'
}
```

## Commands

Administrative commands require `haohansmp.displayui.admin`. Server operators
receive this permission by default.

| Command | Description |
| --- | --- |
| `/hhdui info` | Shows the active scene count and API service name. |
| `/hhdui demo` | Creates a private five-page demo for the command sender. |
| `/hhdui clear` | Removes all managed demo scenes. |

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `haohansmp.displayui.admin` | OP | Allows `/hhdui info`, `demo`, and `clear`. |

## Basic API

The public API is grouped by responsibility instead of placing every type in one
flat package:

| Package | Responsibility |
| --- | --- |
| `api` | Service, document, handle, and scene options. |
| `api.layout` | Rectangles, anchors, and camera transforms. |
| `api.node` | Renderable text, item, icon, and block nodes. |
| `api.text` | Rich-text builders and text alignment helpers. |
| `api.interaction` | Buttons, actions, and click callbacks. |
| `api.interaction.event` | Bukkit interaction events. |
| `api.icon` | Reusable icon registration. |
| `api.view` | Audience/viewer policies. |

For example, a typical consumer starts with these focused imports:

```java
import vn.haohan.displayui.api.DisplayUiService;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.UiHandle;
import vn.haohan.displayui.api.interaction.UiButton;
import vn.haohan.displayui.api.layout.UiRect;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.node.UiIconNode;
import vn.haohan.displayui.api.text.UiTextAlignment;
```

Load the service from Bukkit's `ServicesManager`:

```java
DisplayUiService ui = Bukkit.getServicesManager().load(DisplayUiService.class);
if (ui == null) {
    throw new IllegalStateException("HaoHanDisplayUI is not installed");
}
```

Create a document and scene:

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

Basic lifecycle operations:

```java
handle.update(nextPageDocument);
handle.move(newOrigin);
handle.audience(newAudience);
handle.cameraTransform(newTransform);
handle.show(player);
handle.hide(player);
handle.remove();
```

### Pager and multi-page navigation

`UiPager` is a small page adapter inspired by the pager/router pattern used by
other UI engines. It stores immutable `UiDocument` pages, while `UiHandle`
continues to own scene diffing and rendering:

```java
UiPager pager = new UiPager(List.of(homePage, settingsPage, helpPage))
    .onPageChange(page -> player.sendActionBar(
        Component.text("Page " + (page + 1))));

UiHandle handle = ui.create("plugin:menu", origin, pager.current());

pager.next();
pager.show(handle);
// Or: pager.previous(), pager.goTo(0), pager.show(handle)
```

The default pager wraps from the last page to the first. Use
`new UiPager(pages, false)` to stop at both ends. It does not create scenes or
own player state, so it works with the existing audience, animation, camera,
and cleanup lifecycle.

### Scroll lists

`UiScrollList` provides a scroll-wheel viewport for a list. The engine does not
assume a row layout; consumers use `offset()` to render visible items and
rebuild the document when the offset changes:

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

Scrolling is detected through Bukkit hotbar changes and is cancelled only while
the player is looking at the scroll area. Use `step(n)` to move several rows per
wheel notch. When a document is rebuilt, the engine preserves the current offset
if the control ID and geometry remain unchanged.

Buttons, sliders, and checkboxes play `minecraft:ui.button.click` by default
after a non-cancelled interaction. You can customize it, including a resource
pack sound, or disable it:

```java
UiOptions options = UiOptions.defaults()
    .withClickSound("my_pack:menu.tick", 0.7f, 1.1f);

UiHandle handle = ui.create("plugin:menu", location, document, options, audience);
// Use options.withoutClickSound() to disable it.
```

Backface culling for `ItemDisplay`/`UiIconNode` is enabled by default, is
implemented per player, and applies to fixed scenes:

```java
UiOptions options = UiOptions.defaults()
    .withItemBackfaceCulling(false); // disable if the UI must show from behind
```

Consumers should retain their `UiHandle` and call `remove()` when the associated
machine or menu is removed. Use `removeOwnedBy(ownerKey)` to clean up every UI
owned by a module.

## Animations and easing

Animations are configured on the handle and run on every node in the scene.
The engine advances them every tick and uses Display Entity interpolation for
smooth client-side motion:

```java
import vn.haohan.displayui.api.animation.UiAnimation;
import vn.haohan.displayui.api.animation.UiEasing;

handle.animate(UiAnimation.slideIn(
    10, UiAnimation.Direction.BOTTOM, 18, UiEasing.EASE_OUT));

// Combine fade, scale, movement, delay, and any easing curve in one builder.
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

Convenience presets include `fadeIn`, `fadeOut`, `slideIn`, and `scaleIn`.
Available curves include linear, quadratic, cubic, ease-in/out, back, and
elastic variants. Opacity is supported by `TextDisplay`; scale and movement
work for text, item, icon, and block nodes.

## Slider and checkbox controls

Controls are immutable and can be added directly to a document. A slider uses
the click position to calculate its value; a checkbox toggles on click. Both
share one callback and one cancellable Bukkit event:

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

The same `UiControl` extension point is used for future controls such as
radio buttons, dropdowns, switches, and text inputs.

Slider updates are optimized in place: changing only the value keeps the same
node geometry and interaction hitbox, so the scene does not respawn or flicker.
Display and item transformations are also updated in place, so a custom thumb,
fill, indicator, or icon can change position and size dynamically.

Style helpers make custom visuals small and predictable:

```java
UiRect track = slider.trackRect();
UiRect fill = slider.fillRect(2);
UiRect thumb = slider.thumbRect(10, 18);
UiRect indicator = checkbox.indicatorRect();
```

Sliders support continuous drag: right-click the slider and move your aim to
update the value every tick. Left-clicking, leaving the slider, changing page,
or quitting ends the drag.
While the drag is held but the player's position/view does not change, raycasts
are skipped; the drag state remains active and resumes on the next movement.

For per-node motion, pass one animation per document node:

```java
handle.animateNodes(List.of(
    UiAnimation.scaleIn(18),
    UiAnimation.slideIn(22, UiAnimation.Direction.LEFT, 16),
    UiAnimation.fadeIn(14)
));
```

The demo moving gradient updates every server tick (up to 20 FPS). Minecraft
does not interpolate `TextDisplay` text content, so server-driven text cannot be
fully independent of game ticks. For truly client-timed animation, use an
animated resource-pack model/texture on an `ItemDisplay`, or a client shader/mod.

### Preset effects

Consumers can use `UiEffects` instead of assembling easing and parameters for
every node:

```java
handle.animate(UiEffects.popIn());
handle.animate(UiEffects.slideInFromLeft());
handle.animateNodes(UiEffects.gallery());
```

Available presets include `fadeIn`, four-direction slides, `popIn`, `scaleIn`,
`scaleOut`, `bounceIn`, `dropIn`, and `softRise`. Looping effects such as
`breathing`, `spin`, `shake`, and `pulse` need a
separate `UiLoopEffect` runtime because they run continuously rather than
having one start/end transition like `UiAnimation`.

Page updates are incremental: unchanged entities are retained, same-type nodes
only update metadata/transformation, and only new or incompatible nodes are
replaced. This prevents the whole page from flashing during navigation.

## Coordinate System and Layers

- `(0, 0)` is the scene origin.
- `x` increases toward the visual right.
- `y` increases downward.
- Layout units are logical pixels.
- `pixelsPerBlock` converts logical pixels into world coordinates.
- Larger `depth` values render closer to the viewer.
- `BlockNode.thickness` extends the panel backward.
- The default scale is `40` logical pixels per block.

A `180 × 116 px` panel at `pixelsPerBlock = 40` is approximately
`4.5 × 2.9 blocks`.

## Node Types

| Node | Purpose |
| --- | --- |
| `AlignedTextNode` | Rectangle-based text with layout and optical correction. |
| `TextNode` | Low-level text with direct anchor, line width, and scale control. |
| `UiIconNode` | Box-based item icon with intrinsic texture dimensions. |
| `ItemNode` | Low-level ItemDisplay with its own scale and transform. |
| `BlockNode` | Background, panel, or block-model layer. |

`UiDocument` is an immutable snapshot. Nodes render in ascending `depth` order.

## Rectangles, anchors, and panel-relative layout

`UiRect` describes bounds in logical pixels, with `x/y` at the visual top-left.
A panel can act as the layout root so child nodes derive their positions from
its edges or anchors instead of unrelated scene coordinates:

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

`place(parentAnchor, childAnchor, ...)` joins the child's anchor to the panel
anchor and then applies an offset. Because the server cannot measure custom
resource-pack glyph bounds exactly, consumers declare the background's logical
size once.

## Third-party custom icon registry

`DisplayUiService.icons()` exposes a shared registry for `ItemStack`-backed
icons. The owning plugin registers a key once; the engine clones items when
building nodes and automatically removes registrations when that plugin is
disabled:

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

The texture/model still belongs in the resource pack, for example
`assets/<namespace>/items/embersteel_ingot.json`. The `UiRect` width/height are
the real layout bounds, so text can start at `icon.right() + gap` regardless of
transparent texture margins. The registry does not use a bitmap-font atlas.

## Text Layout

`AlignedTextNode` accepts an `x, y, width, height` rectangle and calculates its
anchor automatically:

- Left: `x + leftOffset + contentWidth / 2`.
- Right: `x + width - rightOffset - contentWidth / 2`.
- Center: `x + width / 2`.
- Top: `y + fontSize / 2 + verticalOffset`.
- Center Y: `y + height / 2 + verticalOffset`.
- Bottom: `y + height - fontSize / 2 + verticalOffset`.

Example:

```java
AlignedTextNode label = new AlignedTextNode(
    Component.text("Smelt"),
    8, 8,
    176, 18,
    UiTextAlignment.LEFT
)
    .offsets(4, 4)
    .verticalAlignment(UiVerticalAlignment.CENTER)
    .verticalOffset(-1)
    .fontSize(8)
    .shadowed(true);
```

### Optical Alignment

Minecraft font glyphs have different visible side bearings for plain, italic,
bold, and gradient components. The engine provides shared manual presets:

| Preset | X correction |
| --- | ---: |
| `ITALIC` | `-1 px` |
| `PLAIN` | `0 px` |
| `GRADIENT` | `+1 px` |
| `BOLD` | `+2 px` |
| `BOLD_GRADIENT` | `+3 px` |

```java
text.opticalPreset(UiTextOpticalPreset.BOLD_GRADIENT);
text.nudgeX(-0.5f); // Additional custom correction when needed
```

The server cannot inspect a client's custom font metrics. Resource-pack fonts can
be corrected with `.contentWidth(px)` and `.nudgeX(px)`.

## Rich Text and Gradients

```java
Component title = UiText.builder()
    .text("[", NamedTextColor.DARK_GRAY)
    .gradient("HaoHan", new TextColor[] {
        UiText.hex("#FFD700"),
        UiText.hex("#FF7A00"),
        UiText.hex("#C02CFF"),
        UiText.hex("#36E6FF")
    }, 1.0, TextDecoration.BOLD)
    .text("] ", NamedTextColor.DARK_GRAY)
    .text("RANDOM", NamedTextColor.AQUA, TextDecoration.OBFUSCATED)
    .build();
```

All Adventure text decorations are supported:

- `BOLD`.
- `ITALIC`.
- `UNDERLINED`.
- `STRIKETHROUGH`.
- `OBFUSCATED`, equivalent to legacy `§k`.

## Icons, Lists, and Icon + Text

```java
UiIconNode icon = new UiIconNode(
    new ItemStack(Material.DIAMOND),
    8, 40,
    24, 24,
    16, 16
);

AlignedTextNode label = new AlignedTextNode(
    Component.text("Diamond"),
    8, 40,
    176, 24,
    UiTextAlignment.LEFT
)
    .after(icon, 4, UiVerticalAlignment.CENTER)
    .fontSize(7);
```

`after(icon, gap, alignment)` uses the icon's vertical bounds and places text at
`TOP`, `CENTER`, or `BOTTOM`.

## Interaction and Hover

`UiButton` is an invisible hit zone in the document's logical-pixel space. On a
click, the engine:

1. Gets a ray from the player's camera.
2. Projects it onto the billboarded and rotated UI plane.
3. Converts the hit point into `(localX, localY)`.
4. Selects the nearest button containing that point.
5. Fires `UiButtonClickEvent`.
6. Runs the built-in action and scene callbacks if the event was not cancelled.

Hover description:

```java
UiButton next = new UiButton("next_page", 58, 42, 28, 16)
    .describedBy(Component.text("Next page", NamedTextColor.AQUA))
    .hitSlop(3);
```

`hitSlop(3)` expands the clickable area by three pixels on every side without
changing its rendered size. This is useful for small buttons or camera-facing UIs.

Text and icons can generate hit zones from their own bounds:

```java
builder.interactiveText(
    "documentation",
    docsText,
    Component.text("Open documentation"),
    UiButtonAction.openUrl("https://web.haohansmp.io.vn/en")
);

builder.interactiveIcon(
    "give_item",
    itemIcon,
    Component.text("Receive an item"),
    UiButtonAction.executeCommand("kit starter")
);
```

Consumers may also use `UiButton.forText(...)` or `UiButton.forIcon(...)`.

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
| `openUrl` | Sends a clickable chat link for client confirmation. |
| `executeCommand` | Runs immediately as the clicking player with Bukkit permissions. |
| `playerCommand` | Alias with the same behavior as `executeCommand`. |
| `consoleCommand` | Runs as console and supports the `{player}` placeholder. |
| `suggestCommand` | Fills the chat input without executing. |

Minecraft does not allow servers to force clients to open URLs. `openUrl` always
sends a clickable link and leaves confirmation to the player. Console actions must
only be created from trusted code or configuration; never insert raw player input
into a console command.

## Bukkit Events and Callbacks

Scene callback:

```java
handle.onClick(click -> {
    switch (click.button().id()) {
        case "previous_page" -> showPreviousPage(click.player());
        case "next_page" -> showNextPage(click.player());
    }
});
```

Centralized event handling:

```java
@EventHandler
public void onUiButton(UiButtonClickEvent event) {
    if (!event.getHandle().ownerKey()
            .equals("haohanmetallurgy:forge_panel")) return;

    if (!event.getPlayer().hasPermission("haohansmp.metallurgy.use")) {
        event.setCancelled(true);
    }
}
```

Cancelling the event prevents both the built-in action and scene callbacks.

## Camera Transforms and Axis Locks

Scenes are fixed in world space by default:

```java
UiCameraTransform.fixed();
```

Follow the full camera orientation:

```java
UiCameraTransform.cameraFacing();
```

Custom transform:

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

Axis locks map to client billboard modes:

| Locks | Billboard | Behavior |
| --- | --- | --- |
| X and Y | `FIXED` | Does not rotate with the camera. |
| X only | `VERTICAL` | Follows yaw while pitch remains locked. |
| Y only | `HORIZONTAL` | Follows pitch while yaw remains locked. |
| Neither X nor Y | `CENTER` | Follows both yaw and pitch. |

Minecraft cameras do not expose dynamic roll, but `lockZ` and `angleZ` are still
available for local roll control. Use `.angles(x, y, z)`, `.angleX(...)`,
`.angleY(...)`, `.angleZ(...)`, `.angle(...)` (a Y alias), or
`.locks(x, y, z)`.

Rotation is applied consistently to translation, rendering, and raycasting, so
buttons remain aligned with tilted or camera-facing UI content.

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

- `show(player)` forces visibility while world and distance constraints still apply.
- `hide(player)` forces the scene to be hidden.
- `audience(...)` replaces the predicate at runtime.
- `maxDistance` limits both rendering and interaction.
- `requireFront` requires the player to remain in front of the panel.

## Updates, Animation, and Performance

`handle.update(document)` inspects the next layout:

- If only Adventure Components changed, the relevant `TextDisplay` entities are
  updated in place.
- Same-type nodes are updated in place; only new or incompatible nodes are
  replaced.
- Text animation does not respawn the panel or icons and does not flicker.
- Visibility is cached; `showEntity/hideEntity` packets are only sent on changes.

Display Entities are lighter than mobs because they have no AI or pathfinding, but
they are still tracked server entities. A scene containing roughly 10–30 displays
is normally lightweight. Avoid thousands of persistent displays or unnecessarily
high-frequency metadata animations.

Recommendations:

- Only create scenes when needed.
- Use appropriate audiences and `maxDistance` values.
- Call `remove()` when the corresponding machine or menu is removed.
- Prefer text-only updates for animations.
- Do not rebuild a document every tick when its content did not change.
- Use one row-wide button for icon + text instead of overlapping hit zones.

## Built-In Demo

Run:

```text
/hhdui demo
```

Controls:

- Aim at a button to show its action-bar hover description.
- Right-click to interact.
- Use `<` and `>` to change pages.
- Footer buttons use `hitSlop(3)` for reliable camera-facing interaction.

Pages:

| Page | Content |
| ---: | --- |
| 1 | Text styles, animated gradient, `§k`, and mixed RGB. |
| 2 | Text lists, icon lists, and icon + text rows. |
| 3 | Hover/click rows sharing one hit zone. |
| 4 | Fixed, yaw, pitch, camera-facing, and X/Y/Z 45-degree presets. |
| 5 | URL and command action types. |
| 6 | Slider, checkbox, and control callbacks. |
| 7 | Shapes, icons, and text with independent random easing/animation. |

## Tests

Run all tests:

```powershell
gradle test
```

Current coverage includes:

- Document, node, button, and action validation.
- Text anchors, optical presets, and vertical alignment.
- Camera lock and billboard mapping.
- Fixed and tilted-plane raycasting.
- URL and command normalization.

## Project Structure

```text
HaoHanDisplayUI/
├─ src/main/java/dev/haohansmp/displayui/
│  ├─ api/          Public consumer API
│  ├─ api/event/    Bukkit events
│  └─ runtime/      Scene, raycasting, and interaction runtime
├─ src/main/resources/
│  └─ plugin.yml
├─ src/test/java/
├─ media/
│  ├─ Demo.gif      GitHub-compatible inline demo
│  └─ Demo.mp4      High-quality demo with audio
├─ build.gradle
└─ settings.gradle
```

## Operational Notes

- Avoid `/reload` in production when consumers retain complex `UiHandle` state;
  prefer a clean restart.
- The engine removes orphaned displays carrying its persistent scene key on enable.
- `ownerKey` must be namespaced, for example
  `haohanmetallurgy:forge_panel`.
- Player command actions use normal Bukkit permissions and fail normally when the
  player lacks access.
- Console commands have full server authority and must only come from trusted data.
- URLs only accept the `http` and `https` schemes.
- Custom fonts may require manual `contentWidth` and optical-offset corrections.

## License

Copyright (C) 2026 HaoHanSMP.

HaoHanDisplayUI is licensed under the GNU General Public License version 3 or
later (`GPL-3.0-or-later`). See [LICENSE](LICENSE) for the full license text.
