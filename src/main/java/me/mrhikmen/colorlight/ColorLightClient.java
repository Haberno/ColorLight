package me.mrhikmen.colorlight;

import me.mrhikmen.colorlight.compat.lambdynlights.ColorLightEntityLightTicker;
import me.mrhikmen.colorlight.compat.lambdynlights.ColorLightLambDynLightsCompat;
import me.mrhikmen.colorlight.config.ColorLightConfig;
import me.mrhikmen.colorlight.core.light.ColorLightBlockRegistry;
import me.mrhikmen.colorlight.core.light.ColorLightChunkScanner;
import me.mrhikmen.colorlight.core.light.ColorLightDaylightRefresher;
import me.mrhikmen.colorlight.core.light.ColorLightEngineHolder;
import me.mrhikmen.colorlight.core.light.ColorLightCommand;
import me.mrhikmen.colorlight.core.render.ColorLightTestModelPlugin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorLightClient implements ClientModInitializer {

    public static String ModID = "ColorLight";

    public static final Logger LOGGER = LoggerFactory.getLogger(ModID);

    public static ColorLightConfig config = new ColorLightConfig();

    @Override
    public void onInitializeClient() {
        config.load();

        ColorLightEngineHolder.configure(config.lightRangeBlocks);
        ColorLightBlockRegistry.load(config);
        ColorLightDaylightRefresher.register();

        ModelLoadingPlugin.register(new ColorLightTestModelPlugin());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                ColorLightEngineHolder.set(client.level));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                ColorLightEngineHolder.set(null));

        ColorLightChunkScanner.register();
        ColorLightCommand.register();

        if (ColorLightLambDynLightsCompat.isPresent()) {
            ColorLightEntityLightTicker.register();
            ColorLightClient.LOGGER.info("[ColorLight] LambDynamicLights initialized");
        }

        ColorLightClient.LOGGER.info("[ColorLight] Mod initialized");

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ReloadListener());
    }
}