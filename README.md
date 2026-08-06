<div align="center">

# HaoHan Display UI

Plugin engine độc lập để dựng UI tương tác trong thế giới Minecraft bằng Display Entity.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/Paper-API-222222?style=for-the-badge&logo=paper&logoColor=white)](https://papermc.io/)
[![Purpur](https://img.shields.io/badge/Purpur-Compatible-8A4FFF?style=for-the-badge)](https://purpurmc.org/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![Adventure](https://img.shields.io/badge/Adventure-Components-6F42C1?style=for-the-badge)](https://docs.advntr.dev/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)

Ngôn ngữ: Tiếng Việt | [English](README.en.md)

</div>

## Tổng quan

HaoHan Display UI là plugin engine dành cho Paper/Purpur, cho phép plugin khác mô
tả giao diện 3D bằng static document. Engine quản lý việc spawn, cập nhật,
ẩn/hiện và cleanup `TextDisplay`, `ItemDisplay`, `BlockDisplay` cùng hitbox tương
tác.

Plugin không áp đặt menu hoặc gameplay cụ thể. Consumer có thể dùng engine để dựng
machine panel, bảng hướng dẫn, menu nhiều trang, danh sách vật phẩm, nút command,
link tài liệu hoặc UI theo camera của từng người chơi.

## Video demo

![HaoHan Display UI demo](media/Demo.gif)

[Demo.MP4](media/Demo.mp4).

Video trình bày lệnh `/hhdui demo` và các trang thử nghiệm:

1. Text thường, bold, italic, gradient động, obfuscated và mixed RGB.
2. Text list, icon list và icon đi kèm text.
3. Hover description và các hàng icon + text có thể click.
4. Camera billboard, khóa trục X/Y/Z và góc xoay 45°.
5. URL, player command, console command và command permission test.

## Tính năng

| Nhóm | Khả năng |
| --- | --- |
| Render | `TextDisplay`, `ItemDisplay`, `BlockDisplay` và panel nhiều layer. |
| Text | Adventure Component, RGB, multi-stop gradient, bold, italic, underline, strikethrough và obfuscated `§k`. |
| Layout | Box alignment trái/giữa/phải, top/center/bottom, offset, optical preset và icon + text. |
| Interaction | Raycast chính xác theo logical pixel, hover description, hit slop và click callback/event. |
| Actions | Mở URL an toàn, chạy player command, console command hoặc suggest command. |
| Camera | Fixed, yaw-only, pitch-only, camera-facing, khóa từng trục và offset góc X/Y/Z. |
| Lifecycle | Audience riêng, view distance, chunk respawn, update, move và cleanup theo owner. |
| Tối ưu | Text-only update tại chỗ, visibility cache và chỉ gửi metadata cho node thay đổi. |

## Công nghệ sử dụng

| Toolkit | Vai trò |
| --- | --- |
| Paper API | API server và Display Entity. |
| Purpur | Môi trường server tương thích/khuyến nghị. |
| Java 21 | Ngôn ngữ và runtime của plugin. |
| Gradle | Dependency, test, build và publish API. |
| Adventure | Rich text, RGB, hover và clickable chat component. |
| JOML | Quaternion và transformation X/Y/Z. |
| JUnit 5 | Unit test cho model, layout và raycast. |

## Yêu cầu

- Minecraft server chạy Paper hoặc Purpur `1.21.11`.
- Java 21 trở lên.
- Gradle 8.x nếu build trực tiếp từ source hiện tại.
- Plugin consumer phải khai báo phụ thuộc vào `HaoHanDisplayUI`.
- Không bắt buộc resource pack; custom font/model có thể cần resource pack riêng
  của consumer.

## Cài đặt

1. Build hoặc tải `HaoHanDisplayUI-1.0.0.jar`.
2. Copy file JAR vào thư mục `plugins/` của server.
3. Trong `plugin.yml` của plugin consumer, thêm dependency:

```yaml
depend: [HaoHanDisplayUI]
```

4. Khởi động lại server.
5. Chạy `/hhdui info` để xác nhận engine hoạt động.
6. Chạy `/hhdui demo` trong game để mở UI thử nghiệm.

## Build từ mã nguồn

Chạy tại thư mục gốc của dự án:

```powershell
gradle clean build
```

JAR đầu ra:

```text
build/libs/HaoHanDisplayUI-1.0.0.jar
```

Build nhanh không chạy test:

```powershell
gradle clean assemble
```

Publish API vào Maven Local để plugin consumer sử dụng:

```powershell
gradle publishToMavenLocal
```

Dependency Gradle phía consumer:

```groovy
repositories {
    mavenLocal()
}

dependencies {
    compileOnly 'dev.haohansmp:HaoHanDisplayUI:1.0.0'
}
```

## Lệnh

Các lệnh quản trị dùng permission `haohansmp.displayui.admin`. Người chơi OP nhận
permission này theo mặc định.

| Lệnh | Mô tả |
| --- | --- |
| `/hhdui info` | Hiển thị số scene đang hoạt động và tên API service. |
| `/hhdui demo` | Tạo UI demo năm trang riêng cho người chạy lệnh. |
| `/hhdui clear` | Xóa toàn bộ scene demo đang được quản lý. |

## Permission

| Permission | Mặc định | Mô tả |
| --- | --- | --- |
| `haohansmp.displayui.admin` | OP | Cho phép dùng `/hhdui info`, `demo` và `clear`. |

## API cơ bản

Public API được nhóm theo trách nhiệm thay vì đặt toàn bộ type trong một package
phẳng:

| Package | Trách nhiệm |
| --- | --- |
| `api` | Service, document, handle và tùy chọn của scene. |
| `api.layout` | Rectangle, anchor và camera transform. |
| `api.node` | Các node text, item, icon và block có thể render. |
| `api.text` | Builder rich text và helper căn chỉnh text. |
| `api.interaction` | Button, action và click callback. |
| `api.interaction.event` | Bukkit event của interaction. |
| `api.icon` | Đăng ký icon tái sử dụng. |
| `api.view` | Chính sách audience/viewer. |

Ví dụ, consumer thông thường bắt đầu với các import tập trung sau:

```java
import dev.haohansmp.displayui.api.DisplayUiService;
import dev.haohansmp.displayui.api.UiDocument;
import dev.haohansmp.displayui.api.UiHandle;
import dev.haohansmp.displayui.api.interaction.UiButton;
import dev.haohansmp.displayui.api.layout.UiRect;
import dev.haohansmp.displayui.api.node.AlignedTextNode;
import dev.haohansmp.displayui.api.node.UiIconNode;
import dev.haohansmp.displayui.api.text.UiTextAlignment;
```

Lấy service từ Bukkit `ServicesManager`:

```java
DisplayUiService ui = Bukkit.getServicesManager().load(DisplayUiService.class);
if (ui == null) {
    throw new IllegalStateException("HaoHanDisplayUI is not installed");
}
```

Tạo document và scene:

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

Lifecycle cơ bản:

```java
handle.update(nextPageDocument);
handle.move(newOrigin);
handle.audience(newAudience);
handle.cameraTransform(newTransform);
handle.show(player);
handle.hide(player);
handle.remove();
```

Consumer nên giữ `UiHandle` và gọi `remove()` khi machine/menu tương ứng bị xóa.
Có thể dùng `removeOwnedBy(ownerKey)` để cleanup toàn bộ UI của một module.

## Hệ tọa độ và layer

- `(0, 0)` là origin của scene.
- `x` tăng sang phải trên màn hình.
- `y` tăng xuống dưới trên màn hình.
- Đơn vị layout là logical pixel.
- `pixelsPerBlock` chuyển logical pixel sang tọa độ world.
- `depth` lớn hơn nằm gần người xem hơn.
- `BlockNode.thickness` kéo nền về phía sau mặt panel.
- Mặc định engine dùng `40` logical pixel mỗi block.

Ví dụ panel `180 × 116 px` với `pixelsPerBlock = 40` có kích thước khoảng
`4.5 × 2.9 block`.

## Các loại node

| Node | Mục đích |
| --- | --- |
| `AlignedTextNode` | Text theo rectangle, hỗ trợ layout và optical correction. |
| `TextNode` | API text cấp thấp với anchor, line width và scale trực tiếp. |
| `UiIconNode` | Item icon theo box và kích thước texture nội tại. |
| `ItemNode` | ItemDisplay cấp thấp với scale và transform riêng. |
| `BlockNode` | Nền/panel/block-model layer. |

`UiDocument` là immutable snapshot. Node render theo thứ tự `depth` tăng dần.

## Rectangle, anchor và layout theo panel

`UiRect` mô tả bounds theo logical pixel, với `x/y` là góc trên-trái. Một panel
có thể làm layout root; node con lấy vị trí từ cạnh hoặc anchor của panel thay
vì dùng tọa độ scene rời rạc:

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

`place(parentAnchor, childAnchor, ...)` ghép anchor của node con vào anchor của
panel rồi áp dụng offset. Vì bounds của glyph/resource-pack không thể đo chính
xác ở server, consumer cần khai báo kích thước logic của background một lần.

## Registry custom icon cho plugin bên thứ ba

`DisplayUiService.icons()` cung cấp registry dùng chung cho icon dựa trên
`ItemStack`. Plugin sở hữu đăng ký một key một lần; engine clone item khi tạo
node và tự gỡ toàn bộ registration khi plugin đó bị disable:

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

Texture/model vẫn phải nằm trong resource pack, ví dụ item model
`assets/<namespace>/items/embersteel_ingot.json`. `width/height` của `UiRect`
là kích thước layout thật; text có thể đặt bằng `icon.right() + gap` và không
phụ thuộc vùng alpha bên trong texture. Registry không dùng bitmap-font atlas.

## Text layout

`AlignedTextNode` nhận rectangle `x, y, width, height` và tự tính anchor:

- Left: `x + leftOffset + contentWidth / 2`.
- Right: `x + width - rightOffset - contentWidth / 2`.
- Center: `x + width / 2`.
- Top: `y + fontSize / 2 + verticalOffset`.
- Center Y: `y + height / 2 + verticalOffset`.
- Bottom: `y + height - fontSize / 2 + verticalOffset`.

Ví dụ:

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

### Optical alignment

Minecraft font có side-bearing khác nhau giữa plain, italic, bold và component
gradient. Engine cung cấp preset chỉnh tay:

| Preset | X correction |
| --- | ---: |
| `ITALIC` | `-1 px` |
| `PLAIN` | `0 px` |
| `GRADIENT` | `+1 px` |
| `BOLD` | `+2 px` |
| `BOLD_GRADIENT` | `+3 px` |

```java
text.opticalPreset(UiTextOpticalPreset.BOLD_GRADIENT);
text.nudgeX(-0.5f); // custom correction nếu cần
```

Server không thể đọc chính xác custom font trên client. Với resource-pack font,
consumer có thể gọi `.contentWidth(px)` và `.nudgeX(px)` để hiệu chỉnh.

## Rich text và gradient

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

Hỗ trợ toàn bộ Adventure decoration:

- `BOLD`.
- `ITALIC`.
- `UNDERLINED`.
- `STRIKETHROUGH`.
- `OBFUSCATED` — tương đương `§k`.

## Icon, list và icon + text

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

`after(icon, gap, alignment)` lấy bounds dọc của icon để căn text theo `TOP`,
`CENTER` hoặc `BOTTOM`.

## Interaction và hover

`UiButton` là hit-zone vô hình trong cùng logical-pixel space với document. Khi
người chơi click, engine:

1. Lấy ray từ camera.
2. Chiếu ray lên mặt phẳng UI đã áp dụng billboard/rotation.
3. Chuyển hit point thành `(localX, localY)`.
4. Chọn button gần nhất chứa điểm đó.
5. Phát `UiButtonClickEvent`.
6. Nếu event không bị cancel, chạy built-in action và callback của scene.

Hover description:

```java
UiButton next = new UiButton("next_page", 58, 42, 28, 16)
    .describedBy(Component.text("Trang tiếp theo", NamedTextColor.AQUA))
    .hitSlop(3);
```

`hitSlop(3)` nới hitbox thêm 3 px ở mỗi cạnh mà không đổi kích thước render. Tính
năng này hữu ích cho button nhỏ hoặc UI xoay theo camera.

Text và icon có thể tạo hitbox trực tiếp từ bounds:

```java
builder.interactiveText(
    "documentation",
    docsText,
    Component.text("Mở tài liệu"),
    UiButtonAction.openUrl("https://web.haohansmp.io.vn/en")
);

builder.interactiveIcon(
    "give_item",
    itemIcon,
    Component.text("Nhận vật phẩm"),
    UiButtonAction.executeCommand("kit starter")
);
```

Consumer cũng có thể dùng `UiButton.forText(...)` hoặc `UiButton.forIcon(...)`.

## Button action

```java
button.withAction(UiButtonAction.openUrl(
    "https://web.haohansmp.io.vn/en"));

button.withAction(UiButtonAction.executeCommand("warp spawn"));
button.withAction(UiButtonAction.playerCommand("warp spawn"));
button.withAction(UiButtonAction.consoleCommand("give {player} diamond"));
button.withAction(UiButtonAction.suggestCommand("msg {player} hello"));
```

| Action | Hành vi |
| --- | --- |
| `openUrl` | Gửi chat component có link để client xác nhận mở. |
| `executeCommand` | Chạy ngay bằng người click và giữ permission Bukkit. |
| `playerCommand` | Alias/hành vi tương đương `executeCommand`. |
| `consoleCommand` | Chạy bằng console; hỗ trợ placeholder `{player}`. |
| `suggestCommand` | Điền command vào chat, chưa thực thi. |

Minecraft không cho server ép client tự mở URL. `openUrl` luôn gửi link có thể bấm
và để người chơi xác nhận. Console action chỉ nên được tạo từ code/config đáng tin
cậy; không đưa input thô của người chơi vào console command.

## Bukkit event và callback

Callback theo scene:

```java
handle.onClick(click -> {
    switch (click.button().id()) {
        case "previous_page" -> showPreviousPage(click.player());
        case "next_page" -> showNextPage(click.player());
    }
});
```

Event xử lý tập trung:

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

Cancel event sẽ ngăn cả built-in action lẫn callback của scene.

## Camera transform và khóa trục

Mặc định scene đứng yên trong world:

```java
UiCameraTransform.fixed();
```

Theo toàn bộ camera:

```java
UiCameraTransform.cameraFacing();
```

Tùy chỉnh:

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

Mapping lock sang billboard:

| Lock | Billboard | Hành vi |
| --- | --- | --- |
| X và Y | `FIXED` | Không quay theo camera. |
| Chỉ X | `VERTICAL` | Theo yaw, khóa pitch. |
| Chỉ Y | `HORIZONTAL` | Theo pitch, khóa yaw. |
| Không khóa X/Y | `CENTER` | Theo yaw và pitch. |

Camera Minecraft không cung cấp roll động, nhưng `lockZ` và `angleZ` vẫn có trong
API để điều khiển roll cục bộ. Có thể dùng `.angles(x, y, z)`, `.angleX(...)`,
`.angleY(...)`, `.angleZ(...)`, `.angle(...)` (alias cho Y) hoặc
`.locks(x, y, z)`.

Rotation được áp dụng đồng bộ cho translation, model và raycast. UI nghiêng hoặc
theo camera vẫn giữ button tại đúng vị trí nhìn thấy.

## Audience và visibility

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

- `show(player)` ép hiện nếu người chơi vẫn thỏa world/distance.
- `hide(player)` ép ẩn.
- `audience(...)` thay predicate runtime.
- `maxDistance` giới hạn render và interaction.
- `requireFront` yêu cầu người chơi ở phía trước panel.

## Update, animation và hiệu năng

`handle.update(document)` tự kiểm tra layout:

- Nếu chỉ Adventure Component đổi, engine cập nhật đúng `TextDisplay` liên quan.
- Nếu geometry, node type hoặc button đổi, engine respawn scene.
- Animation text không respawn panel/icon và không gây nhấp nháy toàn UI.
- Visibility được cache; `showEntity/hideEntity` chỉ gửi khi trạng thái đổi.

Display Entity nhẹ hơn mob vì không có AI/pathfinding, nhưng vẫn là entity được
server quản lý và client track. Một scene khoảng 10–30 display thường nhẹ. Nên tránh
hàng nghìn display cùng tồn tại hoặc animation metadata tần suất quá cao.

Khuyến nghị:

- Chỉ tạo scene khi cần.
- Dùng audience và `maxDistance` hợp lý.
- Gọi `remove()` khi menu/machine bị xóa.
- Ưu tiên text-only update cho animation.
- Không rebuild document mỗi tick nếu nội dung không đổi.
- Gom icon + text vào một button row thay vì tạo nhiều hitbox chồng nhau.

## Demo tích hợp

Chạy:

```text
/hhdui demo
```

Điều khiển:

- Nhìn vào button để xem hover description ở action bar.
- Click chuột phải để tương tác.
- Dùng `<` và `>` để đổi trang.
- Button footer có `hitSlop(3)` để dễ click khi UI theo camera.

Các trang:

| Trang | Nội dung |
| ---: | --- |
| 1 | Text styles, moving gradient, `§k` và mixed RGB. |
| 2 | Text list, icon list và icon + text. |
| 3 | Hover/click rows với hitbox dùng chung. |
| 4 | Fixed, yaw, pitch, camera-facing và X/Y/Z 45°. |
| 5 | URL và các loại command action. |

## Test

Chạy toàn bộ test:

```powershell
gradle test
```

Test hiện bao phủ:

- Validation của document, node, button và action.
- Text anchor, optical preset và vertical alignment.
- Camera lock/billboard mapping.
- Raycast fixed và tilted plane basis.
- URL/command normalization.

## Cấu trúc dự án

```text
HaoHanDisplayUI/
├─ src/main/java/dev/haohansmp/displayui/
│  ├─ api/          Public API cho plugin consumer
│  ├─ api/event/    Bukkit events
│  └─ runtime/      Scene, raycast và interaction runtime
├─ src/main/resources/
│  └─ plugin.yml
├─ src/test/java/
├─ media/
│  ├─ Demo.gif      Demo hiển thị trực tiếp trên GitHub
│  └─ Demo.mp4      Video chất lượng cao có âm thanh
├─ build.gradle
└─ settings.gradle
```

## Ghi chú vận hành

- Không dùng `/reload` trên production nếu plugin consumer giữ `UiHandle` trong
  state phức tạp; ưu tiên restart sạch.
- Engine tự xóa orphan display có persistent scene key khi enable.
- `ownerKey` phải có namespace, ví dụ `haohanmetallurgy:forge_panel`.
- Command action chạy với permission của player; thiếu quyền thì command tự thất
  bại theo Bukkit.
- Console command có toàn quyền server và chỉ nên đến từ cấu hình đáng tin cậy.
- URL chỉ chấp nhận scheme `http` hoặc `https`.
- Custom font cần tự hiệu chỉnh `contentWidth`/optical offset nếu metric khác font
  Minecraft mặc định.

## Giấy phép

Copyright (C) 2026 HaoHanSMP.

HaoHanDisplayUI được phát hành theo GNU General Public License phiên bản 3 hoặc
mới hơn (`GPL-3.0-or-later`). Xem [LICENSE](LICENSE) để biết toàn bộ điều khoản.
