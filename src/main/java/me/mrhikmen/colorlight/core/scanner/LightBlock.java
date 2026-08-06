package me.mrhikmen.colorlight.core.scanner;

import me.mrhikmen.colorlight.ColorLightClient;
import me.mrhikmen.colorlight.config.BlockSettings;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class LightBlock {
    public LightBlock() {
        for (Block block : BuiltInRegistries.BLOCK) {
            int maxLight = 0;
            for (BlockState nowstate : block.getStateDefinition().getPossibleStates()) {
                if (nowstate.getLightEmission() > maxLight) {
                    maxLight = nowstate.getLightEmission();
                }
            }
            boolean enable = true;
            if (maxLight > 0) {
                ColorLightClient.config.blocks.add(new BlockSettings(BuiltInRegistries.BLOCK.getKey(block), maxLight, enable));
            }
        }
    }
}
