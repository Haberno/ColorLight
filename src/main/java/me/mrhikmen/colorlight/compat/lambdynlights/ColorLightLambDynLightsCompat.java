package me.mrhikmen.colorlight.compat.lambdynlights;

import net.fabricmc.loader.api.FabricLoader;

public final class ColorLightLambDynLightsCompat {

    private static final boolean PRESENT =
            FabricLoader.getInstance().isModLoaded("lambdynlights");

    public static boolean isPresent() {
        return PRESENT;
    }

    private ColorLightLambDynLightsCompat() {
    }
}
