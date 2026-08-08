package me.mrhikmen.colorlight.config.gui.screen;

public final class ColorPickerMath {

    public float hue;
    public float saturation;
    public float value;

    public ColorPickerMath(int r, int g, int b) {
        setFromRgb(r, g, b);
    }

    public void setFromRgb(int r, int g, int b) {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        this.value = max;
        this.saturation = (max == 0) ? 0 : delta / max;

        if (delta == 0) {
            this.hue = 0;
        } else if (max == rf) {
            this.hue = (((gf - bf) / delta) % 6f) / 6f;
        } else if (max == gf) {
            this.hue = (((bf - rf) / delta) + 2f) / 6f;
        } else {
            this.hue = (((rf - gf) / delta) + 4f) / 6f;
        }

        if (this.hue < 0) this.hue += 1f;
    }

    public int[] toRgb() {
        float h = hue * 6f;
        int i = (int) Math.floor(h) % 6;
        float f = h - (float) Math.floor(h);

        float p = value * (1f - saturation);
        float q = value * (1f - f * saturation);
        float t = value * (1f - (1f - f) * saturation);

        float r, g, b;
        switch (i) {
            case 0 -> { r = value; g = t; b = p; }
            case 1 -> { r = q; g = value; b = p; }
            case 2 -> { r = p; g = value; b = t; }
            case 3 -> { r = p; g = q; b = value; }
            case 4 -> { r = t; g = p; b = value; }
            default -> { r = value; g = p; b = q; }
        }

        return new int[]{
                Math.round(r * 255f),
                Math.round(g * 255f),
                Math.round(b * 255f)
        };
    }

    public void onSquareClick(float relX, float relY) {
        this.saturation = clamp01(relX);
        this.value = clamp01(1f - relY);
    }

    public void onHueBarClick(float relY) {
        this.hue = clamp01(relY);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}

