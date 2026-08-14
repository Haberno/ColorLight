package me.mrhikmen.colorlight.core.light;

import me.mrhikmen.colorlight.core.light.gpu.ColorLightGpuEngine;
import me.mrhikmen.colorlight.core.light.gpu.GlComputeLightBackend;
import me.mrhikmen.colorlight.core.light.gpu.ILightComputeBackend;

import net.minecraft.client.multiplayer.ClientLevel;

public final class ColorLightEngineHolder {

    private static ColorLightEngine engine;

    private static int maxRangeBlocks = 15;
    private static boolean useGpu = true;

    private static final ILightComputeBackend GPU_BACKEND = new GlComputeLightBackend();

    public static void configure(int maxRangeBlocks, boolean useGpu) {
        ColorLightEngineHolder.maxRangeBlocks = Math.max(1, maxRangeBlocks);
        ColorLightEngineHolder.useGpu = useGpu;
    }

    public static void set(ClientLevel level) {
        if (level == null) {
            engine = null;
            return;
        }

        if (useGpu && GPU_BACKEND.isSupported()) {
            engine = new ColorLightGpuEngine(level, maxRangeBlocks, GPU_BACKEND);
        } else {
            engine = new ColorLightEngine(level, maxRangeBlocks);
        }
    }

    public static ColorLightEngine get() {
        return engine;
    }

    public static void tick() {
        if (engine instanceof ColorLightGpuEngine gpuEngine) {
            gpuEngine.tick();
        }
    }

    private ColorLightEngineHolder() {
    }
}
