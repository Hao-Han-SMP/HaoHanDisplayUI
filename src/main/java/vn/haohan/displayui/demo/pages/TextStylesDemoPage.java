package vn.haohan.displayui.demo.pages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import vn.haohan.displayui.api.UiDocument;
import vn.haohan.displayui.api.node.AlignedTextNode;
import vn.haohan.displayui.api.text.UiText;
import vn.haohan.displayui.api.text.UiTextAlignment;
import vn.haohan.displayui.api.text.UiTextOpticalPreset;
import vn.haohan.displayui.demo.BaseDemoPage;
import vn.haohan.displayui.demo.DemoContext;

public final class TextStylesDemoPage extends BaseDemoPage {
    @Override
    public String title() {
        return "TEXT STYLES";
    }

    @Override
    public void build(UiDocument.Builder builder, DemoContext context) {
        int frame = context.gradientFrame();
        addTextSample(builder, "Normal text", -34,
                Component.text("Normal text", NamedTextColor.WHITE));
        addTextSample(builder, "Bold", -22,
                Component.text("Bold text", NamedTextColor.GOLD, TextDecoration.BOLD),
                UiTextOpticalPreset.BOLD);
        addTextSample(builder, "Italic", -10,
                Component.text("Italic text", NamedTextColor.LIGHT_PURPLE,
                        TextDecoration.ITALIC), UiTextOpticalPreset.ITALIC);
        addTextSample(builder, "Moving", 2, movingGradient(frame),
                UiTextOpticalPreset.BOLD_GRADIENT);
        addTextSample(builder, "§k", 14,
                Component.text("OBFUSCATED", NamedTextColor.AQUA,
                        TextDecoration.OBFUSCATED));
        addTextSample(builder, "Mixed", 26,
                UiText.builder().text("Bold ", NamedTextColor.RED, TextDecoration.BOLD)
                        .text("+ italic ", NamedTextColor.YELLOW, TextDecoration.ITALIC)
                        .text("underlined", NamedTextColor.GREEN,
                                TextDecoration.UNDERLINED)
                        .build());
    }

    @Override
    public void onTick(DemoContext context) {
        // Rebuild the component so the animated gradient is sent to the
        // existing display entities every server tick.
        // A scene animation owns the display transforms while it is running.
        // Pause the rainbow rebuild during that window so it cannot fight the
        // animation's interpolated frame targets.
        if (context.handle() == null || !context.handle().isAnimating()) {
            context.updateView();
        }
    }

    private void addTextSample(UiDocument.Builder builder, String label, float y,
                               Component sample) {
        addTextSample(builder, label, y, sample, UiTextOpticalPreset.PLAIN);
    }

    private void addTextSample(UiDocument.Builder builder, String label, float y,
                               Component sample, UiTextOpticalPreset preset) {
        builder.add(new AlignedTextNode(
                Component.text(label, NamedTextColor.DARK_GRAY),
                -86, y, 40, 10, UiTextAlignment.RIGHT).fontSize(5));
        builder.add(new AlignedTextNode(sample, -40, y, 126, 10, UiTextAlignment.LEFT)
                .opticalPreset(preset).fontSize(5));
    }

    private Component movingGradient(int frame) {
        float phase = (frame % 80) / 80.0f;
        TextColor c1 = TextColor.color(java.awt.Color.HSBtoRGB(phase, 0.85f, 1.0f) & 0xFFFFFF);
        TextColor c2 = TextColor.color(java.awt.Color.HSBtoRGB((phase + 0.33f) % 1.0f, 0.85f, 1.0f) & 0xFFFFFF);
        TextColor c3 = TextColor.color(java.awt.Color.HSBtoRGB((phase + 0.66f) % 1.0f, 0.85f, 1.0f) & 0xFFFFFF);
        return UiText.builder().gradient("Continuous rainbow motion",
                new TextColor[] {c1, c2, c3}, 1.0, TextDecoration.BOLD).build();
    }
}
