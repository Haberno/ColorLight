package me.mrhikmen.colorlight.config.gui.screen;

import me.mrhikmen.colorlight.config.BlockSettings;
import me.mrhikmen.colorlight.config.Translatable;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ColorLightBlockConfigScreen extends Screen {

    private final Screen parent;
    private final BlockSettings entry;
    private final Runnable onSave;

    private final ColorPickerMath colorMath;

    private static final int MARGIN = 20;
    private static final int LEFT_COLUMN_WIDTH = 150;
    private static final int DIVIDER_GAP = 16;
    private static final int DIVIDER_WIDTH = 2;
    private static final int ROW_HEIGHT = 24;
    private static final int SQUARE_SIZE = 140;
    private static final int HUE_BAR_WIDTH = 20;
    private static final int HUE_BAR_GAP = 8;
    private static final int DIVIDER_COLOR = 0xFF73EFFF;

    public ColorLightBlockConfigScreen(Screen parent, BlockSettings entry, Runnable onSave) {
        super(Component.translatable("block." + entry.getBlock().toLanguageKey()));
        this.parent = parent;
        this.entry = entry;
        this.onSave = onSave;
        this.colorMath = new ColorPickerMath(entry.r, entry.g, entry.b);
    }

    @Override
    protected void init() {
        int leftX = MARGIN;
        int topY = MARGIN + 20;

        int dividerX = leftX + LEFT_COLUMN_WIDTH + DIVIDER_GAP;
        int rightX = dividerX + DIVIDER_WIDTH + DIVIDER_GAP;

        int contentBottom = topY + ROW_HEIGHT + ROW_HEIGHT + 16 + SQUARE_SIZE;
        int dividerHeight = contentBottom - topY;

        int iconSize = 64;
        int iconX = leftX + (LEFT_COLUMN_WIDTH - iconSize) / 2;

        DividerWidget iconPlaceholder = new DividerWidget(iconX, topY, iconSize, iconSize, 0xFF222222);
        this.addRenderableWidget(iconPlaceholder);

        StringWidget nameLabel = new StringWidget(
                leftX, topY + iconSize + 12, LEFT_COLUMN_WIDTH, 20,
                this.getTitle(), this.font
        );
        this.addRenderableWidget(nameLabel);

        DividerWidget divider = new DividerWidget(dividerX, topY, DIVIDER_WIDTH, dividerHeight, DIVIDER_COLOR);
        this.addRenderableWidget(divider);

        Checkbox enableCheckbox = Checkbox.builder(Translatable.BLOCK_Tooltip, this.font)
                .pos(rightX, topY)
                .selected(this.entry.enable)
                .onValueChange((checkbox, value) -> {
                    this.entry.enable = value;
                    this.onSave.run();
                })
                .build();
        this.addRenderableWidget(enableCheckbox);

        int sliderY = topY + ROW_HEIGHT;
        LightRangeSlider lightSlider = new LightRangeSlider(rightX, sliderY, 260, 20, this.entry, this.onSave);
        this.addRenderableWidget(lightSlider);

        int colorLabelY = sliderY + ROW_HEIGHT;
        StringWidget colorLabel = new StringWidget(
                rightX, colorLabelY, 260, 16,
                Translatable.COLORED_LIGHTING, this.font
        );
        this.addRenderableWidget(colorLabel);

        int pickerY = colorLabelY + 16 + 8;

        ColorSquareWidget square = new ColorSquareWidget(
                rightX, pickerY, SQUARE_SIZE, SQUARE_SIZE,
                this.colorMath, this::applyColorFromPicker
        );
        this.addRenderableWidget(square);

        HueBarWidget hueBar = new HueBarWidget(
                rightX + SQUARE_SIZE + HUE_BAR_GAP, pickerY, HUE_BAR_WIDTH, SQUARE_SIZE,
                this.colorMath, this::applyColorFromPicker
        );
        this.addRenderableWidget(hueBar);

        Button doneButton = Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .pos(rightX, pickerY + SQUARE_SIZE + 16)
                .size(150, 20)
                .build();
        this.addRenderableWidget(doneButton);
    }

    private void applyColorFromPicker() {
        int[] rgb = this.colorMath.toRgb();
        this.entry.r = rgb[0];
        this.entry.g = rgb[1];
        this.entry.b = rgb[2];
        this.onSave.run();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(this.parent);
    }

    private static class LightRangeSlider extends AbstractSliderButton {

        private static final int MIN = 1;
        private static final int MAX = 15;

        private final BlockSettings entry;
        private final Runnable onSave;

        LightRangeSlider(int x, int y, int width, int height, BlockSettings entry, Runnable onSave) {
            super(x, y, width, height, Component.empty(), toNormalized(entry.light));
            this.entry = entry;
            this.onSave = onSave;
            this.updateMessage();
        }

        private static double toNormalized(int real) {
            return (real - MIN) / (double) (MAX - MIN);
        }

        private int toReal() {
            return MIN + (int) Math.round(this.value * (MAX - MIN));
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.empty().append(Translatable.LIGHT_RANGE).append(Component.literal(": " + toReal())));
        }

        @Override
        protected void applyValue() {
            this.entry.light = toReal();
            this.onSave.run();
        }
    }
}