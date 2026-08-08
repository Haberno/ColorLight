package me.mrhikmen.colorlight.config.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class DividerWidget extends AbstractWidget {

    private final int color;

    public DividerWidget(int x, int y, int width, int height, int color) {
        super(x, y, width, height, Component.empty());
        this.color = color;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(),
                this.color, this.color);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}