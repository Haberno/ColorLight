package me.mrhikmen.colorlight.core.light;

import me.mrhikmen.colorlight.config.BlockSettings;
import me.mrhikmen.colorlight.config.ColorLightConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public final class ColorLightBlockRegistry {

    private static Map<Block, BlockSettings> byBlock = new HashMap<>();

    public static void load(ColorLightConfig config) {
        Map<Block, BlockSettings> map = new HashMap<>();

        for (BlockSettings entry : config.blocks) {

            if (!entry.enable)
                continue;

            if (entry.light <= 0)
                continue;

            if (entry.r == 0 && entry.g == 0 && entry.b == 0)
                continue;

            try {
                Block block = BuiltInRegistries.BLOCK.getValue(entry.getBlock());
                if (block != null) {
                    map.put(block, entry);
                }
            } catch (Exception e) {
            }
        }

        byBlock = map;
    }

    public static BlockSettings get(Block block) {
        return byBlock.get(block);
    }

    public static boolean isEmpty() {
        return byBlock.isEmpty();
    }

    private ColorLightBlockRegistry() {
    }
}