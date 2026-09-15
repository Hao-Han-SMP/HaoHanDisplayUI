# HaoHanDisplayUI - API Documentation  

---

## Table of Contents

- [1. Core & UI Lifecycle (`vn.haohan.displayui.api`)](#1-core--ui-lifecycle-vnhaohandisplayuiapi)
  - [DisplayUiService](#displayuiservice)
  - [UiHandle](#uihandle)
  - [UiDocument](#uidocument)
  - [UiOptions](#uioptions)
  - [UiHit](#uihit)
  - [UiPager](#uipager)
- [2. Typography & Text Formatting (`vn.haohan.displayui.api.text`)](#2-typography--text-formatting-vnhaohandisplayuiapitext)
  - [UiText](#uitext)
  - [UiTextAlignment & UiVerticalAlignment](#uitextalignment--uiverticalalignment)
  - [UiTextOpticalPreset](#uitextopticalpreset)
- [3. Document Parsing & Loading (`vn.haohan.displayui.api.loader`)](#3-document-parsing--loading-vnhaohandisplayuiapiloader)
  - [UiDocumentLoader](#uidocumentloader)
  - [UiDocumentParseException](#uidocumentparseexception)
- [4. Layout & Viewports (`vn.haohan.displayui.api.layout` & `api.view`)](#4-layout--viewports-vnhaohandisplayuiapilayout--apiview)
  - [UiRect](#uirect)
  - [UiAnchor](#uianchor)
  - [UiCameraTransform](#uicameratransform)
  - [UiAudience](#uiaudience)
  - [UiFollowMode & UiFollowOptions](#uifollowmode--uifollowoptions)
- [5. Controls, Interaction & Events (`vn.haohan.displayui.api.interaction`)](#5-controls-interaction--events-vnhaohandisplayuiapiinteraction)
  - [UiControl & UiButton](#uicontrol--uibutton)
  - [UiCheckbox & UiSlider](#uicheckbox--uislider)
  - [UiScrollList, UiScrollAnimation & UiScrollAnimations](#uiscrolllist-uiscrollanimation--uiscrollanimations)
  - [UiClick, UiClickHandler & UiControlChangeHandler](#uiclick-uiclickhandler--uicontrolchangehandler)
  - [Bukkit Events: UiButtonClickEvent & UiControlChangeEvent](#bukkit-events-uibuttonclickevent--uicontrolchangeevent)
- [6. Node Scene Graph (`vn.haohan.displayui.api.node`)](#6-node-scene-graph-vnhaohandisplayuiapinode)
  - [UiNode](#uinode)
  - [TextNode & AlignedTextNode](#textnode--alignedtextnode)
  - [ItemNode & BlockNode](#itemnode--blocknode)
  - [EntityModelNode & UiModelRotation](#entitymodelnode--uimodelrotation)
  - [MobEntityNode](#mobentitynode)
  - [LineNode & PolylineNode](#linenode--polylinenode)
  - [TriangleNode & ParallelogramNode](#trianglenode--parallelogramnode)
  - [UiBackgroundNode & UiGradientBackgroundNode](#uibackgroundnode--uigradientbackgroundnode)
  - [UiIconNode & UiShapeNode](#uiiconnode--uishapenode)
- [7. Animation, Gradients & Geometry (`animation`, `gradient`, `shape`, `icon`)](#7-animation-gradients--geometry)
  - [Easings, UiAnimation & UiEffects](#easings-uianimation--uieffects)
  - [UiGradient, UiGradientEndpoint & UiGradientPosition](#uigradient-uigradientendpoint--uigradientposition)
  - [DisplayShapeMath & TRSResult](#displayshapemath--trsresult)
  - [UiIconRegistry](#uiiconregistry)
- [8. Practical Examples](#8-practical-examples)

---

## 1. Core & UI Lifecycle (`vn.haohan.displayui.api`)

### DisplayUiService

The central service interface for managing UI creation, updates, raycasting, and lifecycles. Obtain the singleton instance via the Bukkit Services Manager:

```java
DisplayUiService service = Bukkit.getServicesManager().load(DisplayUiService.class);
```

| Method | Description & Purpose | Parameters & Return |
|---|---|---|
| `open(Player, UiDocument, UiOptions)` | Opens a UI bound to a specific player in front of their eyes/camera. | `@param player`: The recipient player<br>`@param document`: The layout document<br>`@param options`: View and display options<br>`@return`: Managing `UiHandle` |
| `open(Location, UiDocument, UiOptions)` | Opens a stationary UI at world coordinates (for all or grouped viewers). | `@param location`: Target world location<br>`@param document`: The layout document<br>`@param options`: View options<br>`@return`: Managing `UiHandle` |
| `open(Player, UiDocument)` | Opens a UI for a player using default options (`UiOptions.defaults()`). | `@return`: `UiHandle` |
| `open(Location, UiDocument)` | Opens a stationary UI at a world location with default options. | `@return`: `UiHandle` |
| `documentLoader()` | Retrieves the document parser and loader service `UiDocumentLoader`. | `@return`: `UiDocumentLoader` |
| `iconRegistry()` | Retrieves the central custom icon registry `UiIconRegistry`. | `@return`: `UiIconRegistry` |
| `findHandle(Entity)` | Resolves the UI handle managing a specific Display or Interaction entity. | `@param entity`: Bukkit entity<br>`@return`: `Optional<UiHandle>` |
| `findControl(Entity)` | Resolves the interactive control (Button/Slider/Checkbox) bound to an entity. | `@param entity`: Collision entity<br>`@return`: `Optional<UiControl>` |
| `raycastControl(Player, double)` | Raycasts from the player's eyes to locate the interactive control under their crosshair. | `@param maxDistance`: Max raycast distance (blocks)<br>`@return`: `Optional<UiHit>` |
| `activeHandles()` | Returns an unmodifiable list of all active UI sessions on the server. | `@return`: `List<UiHandle>` |
| `closeAll()` | Closes and cleans up all currently active UI displays on the server. | - |

---

### UiHandle

Represents an active, live UI session instance in the Minecraft world. Provides real-time scene updates, animations, and safe entity cleanup.

| Method | Description & Purpose | Parameters & Return |
|---|---|---|
| `id()` | Returns the unique session identifier (UUID). | `@return`: `UUID` |
| `document()` | Returns the current canvas layout document. | `@return`: `UiDocument` |
| `options()` | Returns the current display and view options. | `@return`: `UiOptions` |
| `location()` | Returns the current center root world location of the UI canvas. | `@return`: `Location` |
| `isAlive()` | Checks if the UI canvas is still active and rendered in the world. | `@return`: `boolean` |
| `update(UiDocument)` | Updates the UI document (diffs node trees and updates display entities). | `@param newDocument`: Updated layout document |
| `update(UiOptions)` | Updates display options (distance, billboard, camera lock, follow parameters). | `@param newOptions`: Updated view options |
| `update(UiDocument, UiOptions)`| Atomically updates both the layout document and view options. | `@param newDocument`, `@param newOptions` |
| `animate(UiAnimation)` | Plays an entrance/transition animation across the UI scene. | `@param animation`: Animation specification |
| `animate(UiAnimation, Runnable)`| Plays an animation with a completion callback. | `@param onComplete`: Callback executed upon completion |
| `animateControl(UiControl, UiAnimation)` | Plays an animation targeted at an individual control (e.g. click pop). | `@param control`, `@param animation` |
| `setFollowTarget(Player)` | Sets a target player for the UI to smoothly follow in flight. | `@param player`: The player to follow |
| `clearFollowTarget()` | Clears the follow target and anchors the UI at its current world location. | - |
| `lockPitch(boolean)` | Locks or unlocks vertical pitch rotation. | `@param locked`: `true` to lock |
| `lockYaw(boolean)` | Locks or unlocks horizontal yaw rotation. | `@param locked`: `true` to lock |
| `registerClickListener(Consumer)` | Registers a listener callback for player click interactions. | `@param listener`: Callback consuming `UiClick` |
| `registerChangeListener(Consumer)` | Registers a listener callback for control value changes (Checkbox, Slider). | `@param listener`: Callback consuming `UiControlChange` |
| `close()` | Immediately closes the UI and despawns all underlying display entities. | - |

---

### UiDocument

The immutable blueprint defining the UI canvas hierarchy, including canvas dimensions, visual nodes, and interactive controls.

```java
UiDocument doc = UiDocument.builder(200, 150)
    .canvasScale(0.005f)
    .background(Color.fromRGB(20, 20, 25))
    .node(new TextNode(UiText.of("Welcome!"), 100, 20, 1.2f, Color.WHITE))
    .build();
```

| Method / Builder | Description & Purpose |
|---|---|
| `UiDocument.builder(width, height)` | Starts building a document with logical canvas pixel dimensions. |
| `canvasScale(float)` | Scale factor converting canvas pixels to world blocks (default: `0.005f`, where 200px = 1 block). |
| `background(Color)` | Sets a solid background color across the canvas. |
| `node(UiNode)` | Appends a render node to the document tree. |
| `control(UiControl)` | Appends an interactive control (Button, Checkbox, Slider, ScrollList). |
| `button(UiButton)` | Convenience method to append a button. |
| `button(id, bounds, action)` | Creates and adds a button with ID, bounds `UiRect`, and `UiButtonAction`. |
| `checkbox(id, bounds, checked)` | Creates and adds a toggleable Checkbox control. |
| `slider(id, bounds, progress)` | Creates and adds a draggable Slider control. |
| `findControl(id)` | Finds a registered interactive control by its identifier string. |
| `build()` | Completes construction and returns the immutable `UiDocument`. |

---

### UiOptions

Configures rendering, positioning, and viewer audience parameters for a UI display.

- `distance`: Distance in blocks from the viewer's eyes to the canvas (default: `1.8f`).
- `billboard`: Display billboard rotation mode (`CENTER`, `FIXED`, `HORIZONTAL`, `VERTICAL`).
- `camera`: Camera orientation specification via `UiCameraTransform`.
- `audience`: Audience visibility rules via `UiAudience` (public or restricted).
- `spectator`: Whether other players can view this individual player's UI.
- `followMode`: Follow mode behavior (`UiFollowMode.FOLLOW` or `NONE`).
- `followOptions`: Smoothing damping and interpolation physics (`UiFollowOptions`).

Convenience methods: `UiOptions.defaults()`, `withDistance(float)`, `withBillboard(Billboard)`, `withCamera(UiCameraTransform)`, `withFollow(Player, UiFollowOptions)`.

---

### UiHit & UiPager

- **`UiHit(UiControl control, Vector3f hitPoint, float distance)`**: Record storing raycast intersection data, including the hit control, exact 3D intersection point, and distance from the viewer's eyes.
- **`UiPager`**: Utility helper for safe list pagination:
  - `pageCount(int totalItems, int pageSize)`: Calculates total number of pages (minimum 1).
  - `pageItems(List<T> items, int pageIndex, int pageSize)`: Safely extracts elements for a page without out-of-bounds errors.

---

## 2. Typography & Text Formatting (`vn.haohan.displayui.api.text`)

### UiText

Rich text abstraction supporting Kyori Adventure `Component`, MiniMessage formatting, hex codes (`#RRGGBB`), legacy codes (`&` / `§`), and character-level gradients.

```java
// Gold-to-red bold gradient text
UiText text = UiText.builder("Auction House")
    .gradient("#FFAA00", "#FF5555")
    .bold(true)
    .build();
```

| Method / Builder | Description & Purpose |
|---|---|
| `UiText.of(String)` | Creates a `UiText` parsing MiniMessage and legacy formatting codes. |
| `UiText.of(Component)` | Creates a `UiText` directly wrapping a Kyori Adventure `Component`. |
| `UiText.builder(String)` | Opens a builder for advanced text formatting and styling. |
| `color(TextColor)` | Sets the primary text color. |
| `hex(String)` | Sets a hex color code (`#RRGGBB` or `RRGGBB`). |
| `gradient(fromHex, toHex)` | Applies a character-level linear gradient from start to end characters. |
| `bold(boolean)`, `italic(boolean)` | Toggles bold and italic decorations. |
| `shadow(boolean)` | Enables or disables the default Minecraft font drop shadow. |
| `estimateWidth()` | Estimates the physical rendered width in Minecraft font pixels. |

### UiTextAlignment & UiVerticalAlignment

- **`UiTextAlignment`**: Horizontal alignment for text elements:
  - `LEFT`: Left-aligned (`calculateOffset(width)` = 0.0f).
  - `CENTER`: Center-aligned (`calculateOffset(width)` = -width * 0.5f).
  - `RIGHT`: Right-aligned (`calculateOffset(width)` = -width).
  - `toBukkit()`: Converts to native Bukkit `TextDisplay.TextAlignment`.
- **`UiVerticalAlignment`**: Vertical alignment options:
  - `TOP`, `CENTER`, `BOTTOM`.

### UiTextOpticalPreset

Predefined optical vertical offset presets correcting baseline alignment between Minecraft pixel font glyphs and background quads:
- `DEFAULT`: Offset 0.0px.
- `BUTTON`: Offset -0.5px.
- `TITLE`: Offset -1.0px.
- `BADGE`: Offset -0.25px.

---

## 3. Document Parsing & Loading (`vn.haohan.displayui.api.loader`)

### UiDocumentLoader

Loads and compiles UI documents from YAML/JSON files or raw strings.

| Method | Description & Purpose |
|---|---|
| `load(File)` | Reads and parses a configuration file into a `UiDocument`. Throws `UiDocumentParseException` on syntax errors. |
| `loadWithReport(File)` | Reads a file and returns a `DocumentLoadReport` containing non-fatal warnings and notices. |
| `loadFromString(String)` | Directly parses a raw YAML or JSON string into a `UiDocument`. |
| `createItemStackSafe(Material, int, String)` | Safe factory for creating `ItemStack` instances with fallback handling. |
| `createBlockDataSafe(Material, String)` | Safe factory for creating Bukkit `BlockData`. |
| `parseText(String)` | Utility parsing raw strings into Kyori Adventure `Component` instances. |

### UiDocumentParseException

Specialized unchecked exception thrown when parsing or decoding a UI document fails due to structural or semantic invalidity.

---

## 4. Layout & Viewports (`vn.haohan.displayui.api.layout` & `api.view`)

### UiRect

A 2D bounding rectangle in logical canvas pixel coordinates used for positioning, layout calculations, and raycast hit detection.

```java
UiRect rect = new UiRect(10, 20, 100, 30); // x=10, y=20, w=100, h=30
```

| Method | Description & Purpose |
|---|---|
| `centerX()`, `centerY()` | Returns the horizontal and vertical center coordinates. |
| `right()`, `bottom()` | Returns the right edge (`x + width`) and bottom edge (`y + height`). |
| `contains(float px, float py)` | Tests if a 2D canvas point `(px, py)` falls inside this rectangle. |
| `intersects(UiRect other)` | Tests for intersection with another rectangle. |
| `centered(cx, cy, w, h)` | Factory creating a rectangle from a center position `(cx, cy)` and dimensions. |
| `insets(dx, dy)` | Expands or contracts the bounding box by `(dx, dy)` margins. |
| `place(w, h, UiAnchor)` | Calculates aligned child bounds of size `(w, h)` positioned according to an anchor. |

### UiAnchor

Standardized 9-point anchor orientation used for docking and alignment:
`TOP_LEFT`, `TOP_CENTER`, `TOP_RIGHT`, `CENTER_LEFT`, `CENTER`, `CENTER_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_CENTER`, `BOTTOM_RIGHT`.
- `factorX()`: Horizontal factor (0.0f, 0.5f, 1.0f).
- `factorY()`: Vertical factor (0.0f, 0.5f, 1.0f).

### UiCameraTransform

Configures camera alignment and transform modes relative to the viewer:
- `fixed()`: Stationary orientation in world coordinates.
- `cameraFacing()`: Automatically rotates to face the player's eye direction.
- `withPitchLock(boolean)`, `withYawLock(boolean)`: Constrains individual rotational axes.
- `withAngles(float yaw, float pitch, roll)`: Sets explicit Euler rotation angles.

### UiAudience, UiFollowMode & UiFollowOptions

- **`UiAudience`**: Controls visibility permissions:
  - `UiAudience.all()`: Visible to every player in the world.
  - `UiAudience.only(Player...)`: Restricted strictly to specified players.
- **`UiFollowMode`**: Canvas following behavior:
  - `NONE`: Anchored at a static world location.
  - `FOLLOW`: Continuously tracks and moves with the target player.
- **`UiFollowOptions(damping, interpolationTicks, targetDistance)`**: Physics parameters configuring interpolation lag, damping, and follow distance.

---

## 5. Controls, Interaction & Events (`vn.haohan.displayui.api.interaction`)

### UiControl & UiButton

- **`UiControl`**: Sealed base interface for all interactive canvas controls (Button, Checkbox, Slider, ScrollList).
  - `id()`: Unique string identifier.
  - `bounds()`: Hitbox bounding box `UiRect`.
  - `contains(float px, float py)`: Hitbox collision test.
- **`UiButton`**: Interactive button control:
  - `action()`: Bound `UiButtonAction` (OPEN_URL, PLAYER_COMMAND, CONSOLE_COMMAND, SUGGEST_COMMAND, NONE).
  - Factory methods:
    - `UiButton.forNode(id, node, action)`: Wraps an interactive button around any visual node.
    - `UiButton.forText(id, bounds, text, action)`: Text-labeled button.
    - `UiButton.forIcon(id, bounds, item, action)`: Icon button.
    - `UiButton.forModel(id, bounds, modelNode, action)`: Interactive 3D entity model button.
    - `UiButton.forMob(id, bounds, mobNode, action)`: Interactive 3D live mob button.

### UiCheckbox & UiSlider

- **`UiCheckbox`**: Two-state toggle switch (checked/unchecked):
  - `checked()`: Current toggle state (`boolean`).
  - `withChecked(boolean)`: Returns a copy with the updated toggle state.
  - `indicatorRect()`: Calculated visual indicator bounds for checkmarks or dots.
- **`UiSlider`**: Continuous or stepped value slider across range [0.0f - 1.0f]:
  - `progress()`: Normalized progress value (`float`).
  - `step()`: Step quantizer (`0.1f` for discrete increments, or `0.0f` for smooth analog drag).
  - `withProgress(float)`: Returns a copy with the updated progress value.
  - `trackRect()`, `fillRect()`, `thumbRect()`: Layout geometry for track, active fill, and draggable thumb.

### UiScrollList, UiScrollAnimation & UiScrollAnimations

- **`UiScrollList`**: Vertical content scroll container:
  - `contentHeight()`: Total height of inner scrollable content.
  - `scrollOffset()`: Current scroll offset in canvas pixels.
  - `withOffset(float)`: Returns a copy with updated scroll offset.
  - `step()`: Scroll displacement increment per mouse wheel tick.
- **`UiScrollAnimation`**: Interface governing smooth scroll interpolation.
- **`UiScrollAnimations`**: Built-in scroll animation presets:
  - `none()`: Instantaneous scrolling without animation.
  - `slide(int durationTicks, Easings easing)`: Smooth easing slide animation.

### UiClick, UiClickHandler & UiControlChangeHandler

- **`UiClick`**: Immutable click event payload:
  - `player()`: The player who performed the interaction.
  - `control()`: The target `UiControl`.
  - `clickX()`, `clickY()`: Precise 2D canvas pixel coordinates where the interaction landed.
  - `clickType()`: Click variant (`LEFT`, `RIGHT`, `SHIFT_LEFT`, `SHIFT_RIGHT`).
- **`UiClickHandler`**: Functional callback for button click events.
- **`UiControlChange`**: Change event payload for value-bearing controls:
  - `asChecked()`: Extracts boolean state from a Checkbox.
  - `asProgress()`: Extracts float progress from a Slider.
- **`UiControlChangeHandler`**: Functional callback for control value modifications.

### Bukkit Events

Standard cancellable Bukkit events dispatched for server-wide listeners:
- **`UiButtonClickEvent`**: Fired whenever a player clicks a `UiButton`.
- **`UiControlChangeEvent`**: Fired whenever a player modifies a `UiCheckbox` or `UiSlider`.

---

## 6. Node Scene Graph (`vn.haohan.displayui.api.node`)

The visual hierarchy is structured as an immutable Scene Graph using the sealed interface `UiNode`. All nodes support:
- `x()`, `y()`: Logical pixel position on the canvas.
- `depth()`: Z-depth offset used for visual layering and z-fighting prevention.
- `doubleSided()`: Flag enabling back-face rendering (visible from both front and rear).
- `withDoubleSided(boolean)`: Returns a copy with the double-sided flag modified.

```
UiNode (Sealed Interface)
 ├── TextNode / AlignedTextNode
 ├── ItemNode
 ├── BlockNode
 ├── EntityModelNode
 ├── MobEntityNode
 ├── LineNode
 ├── PolylineNode
 ├── TriangleNode
 ├── ParallelogramNode
 ├── UiBackgroundNode
 ├── UiGradientBackgroundNode
 ├── UiIconNode
 └── UiShapeNode
```

### TextNode & AlignedTextNode
- **`TextNode`**: Renders formatted text `UiText` at canvas coordinates with scale and tinting.
- **`AlignedTextNode`**: Extends `TextNode` to provide bounding-box alignment via `UiRect`, `UiTextAlignment`, `UiVerticalAlignment`, and `UiTextOpticalPreset`.

### ItemNode & BlockNode
- **`ItemNode`**: Renders 3D Minecraft items using ItemDisplay with transform modes (`GUI`, `FIXED`, `GROUND`, `HEAD`).
- **`BlockNode`**: Renders raw 3D Minecraft blocks (Chest, Furnace, TNT, Obsidian, etc.) using BlockDisplay entities.

### EntityModelNode & UiModelRotation
- **`EntityModelNode`**: Renders custom 3D entity models (Item or Custom Model Data - CMD) with rich interactive rotation modes:
  - `forMob(String mobId, float x, float y, float scale)`: Creates a preset mob model (zombie, warden, dragon, allay, etc.).
  - `cursorTrack()`: Smoothly tilts and tracks the viewer's crosshair cursor in 3D.
  - `autoSpin(float degreesPerTick)`: Continuously spins around the vertical axis.
  - `hoverSpin(float degreesPerTick)`: Spins dynamically while hovered over.
  - `yawRange(min, max)`, `pitchRange(min, max)`: Restricts rotation boundaries.
  - `lockYaw(boolean)`, `lockPitch(boolean)`: Locks individual axes.
- **`UiModelRotation`**: Configuration record for 3D model rotation constraints (`defaults()`, `free()`, `locked()`, `yawOnly()`, `pitchOnly()`).

### MobEntityNode
Renders live vanilla entities (`LivingEntity`) directly inside the UI canvas:
- Allows deep customization via `withCustomizer(Consumer<LivingEntity>)` (e.g. equipping Netherite armor, glowing enchanted items, custom nameplates).
- Fully supports all rotation modes: `autoSpin`, `hoverSpin`, `cursorTrack`.

### LineNode & PolylineNode
- **`LineNode`**: 2D line segment connecting points `(x1, y1)` and `(x2, y2)` with specified `thickness` and `Color`.
- **`PolylineNode`**: Continuous multi-segment path forming open lines or closed outlines (`closed`), created via `PolylineNode.builder()`.

### TriangleNode & ParallelogramNode
- **`TriangleNode`**: Arbitrary 2D triangle defined by 3 vertex coordinates, rasterized using sheared TextDisplay quads.
- **`ParallelogramNode`**: Slanted quadrilateral or card badge with customizable `skewX` horizontal shear.

### UiBackgroundNode & UiGradientBackgroundNode
- **`UiBackgroundNode`**: Solid or translucent rectangular flat background panel.
- **`UiGradientBackgroundNode`**: High-fidelity multi-slice gradient panel supporting mesh subdivisions via `withGrid(slicesX, slicesY)` or `withSlices(int)`.

### UiIconNode & UiShapeNode
- **`UiIconNode`**: Item icon node with independent UV source dimensions (`uWidth`, `vHeight`) and rendered canvas dimensions (`width`, `height`).
- **`UiShapeNode`**: Versatile 2D vector shape node supporting diverse geometry:
  - Shapes: `"rect"`, `"rounded_rect"`, `"circle"`, `"diamond"`, `"trapezoid"`, `"triangle"`, `"parallelogram"`, `"pentagon"`, `"hexagon"`, `"octagon"`, `"star4"`, `"star5"`, `"arrow_right"`, `"cross"`, `"heart"`, `"speech_bubble"`, etc.
  - Supports corner radiuses (`cornerRadius`), outlines (`outlineColor`, `outlineThickness`, styles `solid`/`dashed`/`dotted`), and 2D rotation (`rotation`).
  - `decomposeToNodes()`: Automatically decomposes complex shapes into optimized render primitives.

---

## 7. Animation, Gradients & Geometry

### Easings, UiAnimation & UiEffects
- **`Easings`**: Over 20 mathematical easing curves: `Linear`, `InQuad`, `OutQuad`, `InOutQuad`, `InCubic`, `OutCubic`, `InOutCubic`, `InExpo`, `OutExpo`, `BackOut`, `ElasticOut`, `BounceOut`.
- **`UiAnimation`**: Transformation keyframe record specifying duration (`durationTicks`), delay (`delayTicks`), curve (`easing`), opacity transitions (`fromOpacity -> toOpacity`), scale transitions (`fromScale -> toScale`), and translation offsets (`offsetX, offsetY, offsetZ`).
- **`UiEffects`**: Factory providing ready-to-use animation presets:
  - `fadeIn()`, `slideInFromLeft()`, `slideInFromRight()`, `slideInFromTop()`, `slideInFromBottom()`, `popIn()`, `scaleIn()`, `scaleOut()`, `bounceIn()`, `dropIn()`, `softRise()`.

### UiGradient, UiGradientEndpoint & UiGradientPosition
- **`UiGradient`**: Analytical two-stop linear gradient model:
  - Presets: `horizontal()`, `vertical()`, `diagonal()`, `centerToBottomRight()`.
  - `evaluate(u, v)`: Evaluates and interpolates the precise color at arbitrary normalized coordinates.
- **`UiGradientEndpoint`**: Endpoint record holding `(u, v)` coordinates and a `Color`.
- **`UiGradientPosition`**: Predefined 2D boundary anchors (`TOP_LEFT`, `CENTER`, `BOTTOM_RIGHT`, etc.).

### DisplayShapeMath & TRSResult
- **`DisplayShapeMath`**: Analytical geometric matrix decomposition engine:
  - `computeLineTRS()`: Calculates TRS decomposition for 3D lines.
  - `computeParallelogramTRS()`: Calculates TRS decomposition for parallelograms.
  - `computeTriangleTRS()`: Calculates TRS decomposition for 3D triangles.
  - `decompose(Matrix4f)`: General-purpose polar/SVD decomposition for 4x4 matrices.
- **`TRSResult`**: Decomposed TRS components (Translation, LeftRotation, Scale, RightRotation), convertible to native Bukkit `Transformation`.

### UiIconRegistry
Central repository for shared custom item icons:
```java
UiIconRegistry registry = service.iconRegistry();
registry.register(myPlugin, new NamespacedKey(myPlugin, "ruby"), () -> new ItemStack(Material.EMERALD));
```

---

## 8. Practical Examples

### Example 1: Creating a Simple Notice Banner

```java
DisplayUiService service = Bukkit.getServicesManager().load(DisplayUiService.class);

UiDocument doc = UiDocument.builder(180, 80)
    .canvasScale(0.005f)
    .background(Color.fromRGB(25, 25, 30))
    // Title
    .node(new TextNode(UiText.of("§6§lSYSTEM NOTIFICATION"), 90, 15, 1.2f, Color.WHITE))
    // Body text
    .node(new TextNode(UiText.of("§7Server restart scheduled in 10 minutes!"), 90, 45, 0.9f, Color.WHITE))
    .build();

// Open in front of the player with a downward slide entrance animation
UiHandle handle = service.open(player, doc);
handle.animate(UiEffects.slideInFromTop());
```

### Example 2: Interactive Menu With Buttons

```java
UiRect buttonBounds = new UiRect(40, 50, 100, 25);

UiButton acceptButton = UiButton.forText(
    "btn_accept",
    buttonBounds,
    UiText.of("§a§lCONFIRM"),
    UiButtonAction.suggestCommand("/warp dungeon")
);

UiDocument menuDoc = UiDocument.builder(180, 100)
    .background(Color.fromRGB(30, 30, 35))
    .node(new TextNode(UiText.of("§eJoin Dungeon Queue?"), 90, 20, 1.1f, Color.WHITE))
    .button(acceptButton)
    .build();

UiHandle handle = service.open(player, menuDoc);

// Handle click events
handle.registerClickListener(click -> {
    if ("btn_accept".equals(click.control().id())) {
        click.player().sendMessage("§aYou confirmed your participation!");
        // Play pop animation on the clicked button
        handle.animateControl(click.control(), UiEffects.popIn());
    }
});
```

### Example 3: Form Controls With Checkbox & Slider

```java
UiDocument formDoc = UiDocument.builder(200, 120)
    .background(Color.fromRGB(20, 22, 28))
    .node(new TextNode(UiText.of("§b§lAUDIO SETTINGS"), 100, 15, 1.1f, Color.WHITE))
    // Toggle BGM Checkbox
    .checkbox("chk_bgm", new UiRect(20, 40, 20, 20), true)
    .node(new TextNode(UiText.of("§fBackground Music"), 80, 50, 0.9f, Color.WHITE))
    // Volume Slider [0.0f to 1.0f] (default 0.7f)
    .slider("sld_volume", new UiRect(20, 75, 160, 16), 0.7f)
    .build();

UiHandle handle = service.open(player, formDoc);

handle.registerChangeListener(change -> {
    if ("chk_bgm".equals(change.control().id())) {
        boolean enabled = change.asChecked();
        player.sendMessage("§7BGM: " + (enabled ? "§aEnabled" : "§cDisabled"));
    } else if ("sld_volume".equals(change.control().id())) {
        float volume = change.asProgress();
        player.sendMessage("§7Volume: §e" + Math.round(volume * 100) + "%");
    }
});
```

### Example 4: Interactive 3D Mob Exhibition

```java
// Display a live Warden mob tilted and tracking the player's crosshair cursor
MobEntityNode wardenNode = new MobEntityNode(EntityType.WARDEN, 100, 70, 1.0f)
    .cursorTrack()
    .withCustomizer(living -> {
        living.setCustomName("§4§lDungeon Warden Boss");
        living.setCustomNameVisible(true);
    });

UiDocument mobDoc = UiDocument.builder(200, 150)
    .background(Color.fromRGB(15, 15, 20))
    .node(wardenNode)
    .build();

service.open(player, mobDoc);
```

### Example 5: Dynamic Mesh Gradient Canvas

```java
// Create a vibrant diagonal gradient from Blue to Purple
UiGradient gradient = UiGradient.diagonal(
    Color.fromRGB(0, 150, 255),
    Color.fromRGB(180, 0, 255)
);

UiGradientBackgroundNode bgNode = new UiGradientBackgroundNode(
    new UiRect(0, 0, 220, 120),
    0.001f,
    gradient
).withSlices(16);

UiDocument gradientDoc = UiDocument.builder(220, 120)
    .node(bgNode)
    .node(new TextNode(UiText.of("§f§lPREMIUM DISPLAY UI"), 110, 60, 1.3f, Color.WHITE))
    .build();

UiHandle handle = service.open(player, gradientDoc);
handle.animate(UiEffects.fadeIn(20));
```
