package me.mrhikmen.colorlight.config;

import net.minecraft.resources.Identifier;

public class BlockSettings {
    public String block;
    public int r;
    public int g;
    public int b;
    public int light;
    public boolean enable;

    public BlockSettings(Identifier block, int light, boolean enable) {
        this.block = String.valueOf(block);
        this.light = light;
        this.enable = enable;
    }

    public Identifier getBlock() {
        return Identifier.parse(block);
    }
}
