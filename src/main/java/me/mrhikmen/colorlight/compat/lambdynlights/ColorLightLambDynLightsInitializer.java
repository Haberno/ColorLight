package me.mrhikmen.colorlight.compat.lambdynlights;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;

public class ColorLightLambDynLightsInitializer implements DynamicLightsInitializer {

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {

        ColorLightLambDynLightsBridge.init(
                context.itemLightSourceManager(),
                context.entityLightSourceManager()
        );

        context.itemLightSourceManager().onRegisterEvent().register(registerContext -> {
            for (me.mrhikmen.colorlight.config.BlockSettings entry : me.mrhikmen.colorlight.ColorLightClient.config.blocks) {
                if (!entry.enable)
                    continue;

                net.minecraft.world.level.block.Block block =
                        net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(entry.getBlock());
                if (block == null)
                    continue;

                net.minecraft.world.item.Item item = block.asItem();
                if (item == net.minecraft.world.item.Items.AIR)
                    continue;

                registerContext.register(item, Math.min(15, entry.light));
            }
        });
    }
}