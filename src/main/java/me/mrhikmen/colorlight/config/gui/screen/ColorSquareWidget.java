package me.mrhikmen.colorlight.config.gui.screen;

import me.mrhikmen.colorlight.config.Translatable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

public class ColorSquareWidget extends AbstractWidget {

    private static final int COLUMN_STEP = 2;

    private final ColorPickerMath color;
    private final Runnable onChange;

    public ColorSquareWidget(int x, int y, int width, int height, ColorPickerMath color, Runnable onChange) {
        super(x, y, width, height, Translatable.COLORED_LIGHTING_2);
        this.color = color;
        this.onChange = onChange;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        int x0 = this.getX();
        int y0 = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        for (int dx = 0; dx < w; dx += COLUMN_STEP) {
            float t = dx / (float) w; // 0 = белый край, 1 = чистый hue

            int hueColor = hueToRgb(this.color.hue);
            int topColor = lerpArgb(0xFFFFFFFF, hueColor, t);
            int bottomColor = 0xFF000000; // низ всегда чёрный

            int colW = Math.min(COLUMN_STEP, w - dx);
            graphics.fillGradient(x0 + dx, y0, x0 + dx + colW, y0 + h, topColor, bottomColor);
        }

        // маркер текущей выбранной точки
        int markerX = x0 + Math.round(this.color.saturation * w);
        int markerY = y0 + Math.round((1f - this.color.value) * h);
        graphics.fillGradient(markerX - 1, markerY - 1, markerX + 1, markerY + 1, 0xFFFFFFFF, 0xFFFFFFFF);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        applyFromMouse(event);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        applyFromMouse(event);
    }

    private void applyFromMouse(MouseButtonEvent event) {
        float relX = (float) ((event.x() - this.getX()) / (double) this.getWidth());
        float relY = (float) ((event.y() - this.getY()) / (double) this.getHeight());
        this.color.onSquareClick(Mth.clamp(relX, 0f, 1f), Mth.clamp(relY, 0f, 1f));
        this.onChange.run();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }

    private static int lerpArgb(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int bl = Math.round(ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    private static int hueToRgb(float hue) {
        float h = hue * 6f;
        int i = (int) Math.floor(h) % 6;
        float f = h - (float) Math.floor(h);
        float p = 0f, q = 1f - f, t = f;

        float r, g, b;
        switch (i) {
            case 0 -> { r = 1f; g = t; b = p; }
            case 1 -> { r = q; g = 1f; b = p; }
            case 2 -> { r = p; g = 1f; b = t; }
            case 3 -> { r = p; g = q; b = 1f; }
            case 4 -> { r = t; g = p; b = 1f; }
            default -> { r = 1f; g = p; b = q; }
        }

        int ir = Math.round(r * 255f), ig = Math.round(g * 255f), ib = Math.round(b * 255f);
        return 0xFF000000 | (ir << 16) | (ig << 8) | ib;
    }
}