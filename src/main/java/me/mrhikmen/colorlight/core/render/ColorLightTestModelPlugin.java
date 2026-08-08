package me.mrhikmen.colorlight.core.render;

import me.mrhikmen.colorlight.ColorLightClient;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

public class ColorLightTestModelPlugin implements ModelLoadingPlugin {

    @Override
    public void initialize(Context pluginContext) {

        ColorLightClient.LOGGER.info("[ColorLight] ModelLoadingPlugin initialized");

        pluginContext.modifyBlockModelAfterBake().register((model, context) -> new TintedBakedModel(model));
    }
}
