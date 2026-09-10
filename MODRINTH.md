![HaoHan Display UI banner](https://cdn.modrinth.com/data/cached_images/a94655fef58c2a7a446c6a30b29a1b75871e54c7_0.webp)

# HaoHan Display UI

A standalone engine plugin for building interactive, pixel-precise in-world UIs in Minecraft using Display Entities — for plugin developers targeting Paper, Purpur, Folia, Pufferfish, and Leaves.

> This is an engine for other plugins, not a ready-made gameplay menu. Install it when another plugin requires it, or use the included demo to see what it can do.

---

## What's New in v1.0.2

### Living Vanilla Mobs (`MobEntityNode`)

Render native living entities — Cows, Pigs, Baby Zombies, Allays, and more — directly inside a UI scene.

- No resource pack required. Uses real server-side entities with `NoAI`, `Silent`, and `Invulnerable` flags.
- Supports deep customization through a `Consumer<LivingEntity>` callback: baby mode, armor, equipment, effects, and per-entity attribute scale.
- Interactive hover rotation, auto-spin, and cursor-tracking tilt work identically to `EntityModelNode`.

### 3D Custom Entity and Item Models (`EntityModelNode`)

Renders items, blocks, and CustomModelData resource pack models via `ItemDisplay`.

- Full 3-axis scale, per-axis Euler angles (Yaw, Pitch, Roll), and JOML quaternion transforms.
- Supports `FIXED`, `HEAD`, `GUI`, and `GROUND` item display transforms.

### Interactive 3D Model Rotation

- **Cursor tracking**: Model tilts in real time toward the player's crosshair while hovered.
- **Auto-spin**: Continuous rotation at a configurable speed in degrees per tick.
- **Hover-spin**: Rotation starts on hover and stops when the player looks away.
- **Rotation constraints**: Lock individual axes (`lockYaw`, `lockPitch`, `lockRoll`), clamp angles to a min/max range, and snap to discrete steps.

### Analytical Vector Geometric Shapes

Complex geometry rendered entirely from `TextDisplay` and `BlockDisplay` entities using analytical decomposition.

| Node | Description |
|---|---|
| `TriangleNode` | 3-piece decomposition with exact vertex coordinates |
| `ParallelogramNode` | Slanted cards and angled dividers |
| `LineNode` | Pixel-precise segments with axial roll |
| `PolylineNode` | Closed multi-vertex polygons (stars, waveforms, custom borders) |

### Camera Follow HUD (`handle.follow(...)`)

Attaches a full interactive UI scene to the player's view with configurable damping and Display Entity interpolation. Suitable for mission HUDs, floating diagnostics panels, and per-player private interfaces.

### Hotbar Scroll Navigation (`UiScrollList`)

Intercepts `PlayerItemHeldEvent` to scroll through lists and paginated menus without blocking hotbar usage.

### Document Pagination (`UiPager`)

Wraps an immutable document set with automatic page indexing. Supports bounds clamping and circular wrap-around. Page change callbacks are provided for state-driven updates.

### Multi-Version and Folia Support

- A single JAR runs on Minecraft `1.20.4` through `1.21.x+` without rebuilding.
- Folia is supported via `GlobalRegionScheduler`. Detection is automatic at runtime using class reflection.

### Expanded Demo (`/hhdui demo`)

The built-in demo now covers 11 pages: rich text, icon lists, hover descriptions, camera modes, URL and command actions, interactive controls, geometric shapes, per-node animations, scroll navigation, a mixed 3D mob and item grid, and interactive multi-directional gradient backgrounds.

---

## Main Features

- `TextDisplay`, `ItemDisplay`, `BlockDisplay`, vanilla living mob entities, and multi-directional gradient background panels
- Solid translucent panels (`UiBackgroundNode`) and mathematical multi-slice continuous gradients (`UiGradientBackgroundNode`)
- Adventure Component rich text: RGB, multi-stop gradients, bold, italic, strikethrough, obfuscated
- Box alignment: left, center, right, top, middle, bottom with `UiRect` anchors and optical corrections
- Pixel-precise logical-pixel raycasting with hit-slop and cancellable click events
- Interactive controls: `UiSlider` (continuous drag), `UiCheckbox`, `UiScrollList`
- Tick-based animations with easing curves: linear, quadratic, cubic, back, elastic
- Per-player audience predicates and view distance limits
- In-place text and transformation diffing — only changed nodes generate packets
- Automatic orphaned entity cleanup on server start
- No client mod required

---

## Compatibility

| | |
|---|---|
| Minecraft | `1.20+` (1.20.4 – 1.21.x+) |
| Server software | Paper, Purpur, Folia, Pufferfish, Leaves |
| Java | 21 or newer |
| Dependencies | None |
| Resource pack | Optional |

---

## Try the Demo

```text
/hhdui demo
```

Requires OP or the `haohansmp.displayui.admin` permission. Cycles through all 11 pages.

---

## Documentation

- [Source code and full API documentation](https://github.com/Hao-Han-SMP/HaoHanDisplayUI)
- [HaoHanSMP website](https://web.haohansmp.io.vn/)
