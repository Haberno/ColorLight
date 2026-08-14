package me.mrhikmen.colorlight.core.light;

import me.mrhikmen.colorlight.core.util.ColorLightRenderUtil;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.core.BlockPos;

public final class ColorLightDaylightRefresher {

    private static final int REFRESH_INTERVAL_TICKS = 40; // раз в ~2 секунды

    private static int tickCounter = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.level == null || client.player == null)
                return;

            tickCounter++;
            if (tickCounter < REFRESH_INTERVAL_TICKS)
                return;
            tickCounter = 0;

            ColorLightEngine engine = ColorLightEngineHolder.get();
            if (engine == null)
                return;

            int radius = engine.getMaxRangeBlocks() + 1;

            for (BlockPos sourcePos : engine.getSourcePositions()) {
                ColorLightRenderUtil.setBlocksDirty(client.level,
                        sourcePos.getX() - radius, sourcePos.getY() - radius, sourcePos.getZ() - radius,
                        sourcePos.getX() + radius, sourcePos.getY() + radius, sourcePos.getZ() + radius
                );
            }
        });
    }

    private ColorLightDaylightRefresher() {
    }
}

