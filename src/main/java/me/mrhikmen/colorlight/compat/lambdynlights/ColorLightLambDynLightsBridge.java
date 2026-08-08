package me.mrhikmen.colorlight.compat.lambdynlights;

import dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;

import net.minecraft.world.entity.Entity;

public final class ColorLightLambDynLightsBridge {

    private static ItemLightSourceManager itemLightSourceManager;
    private static EntityLightSourceManager entityLightSourceManager;

    static void init(ItemLightSourceManager itemManager, EntityLightSourceManager entityManager) {
        itemLightSourceManager = itemManager;
        entityLightSourceManager = entityManager;
    }

    public static boolean isAvailable() {
        return entityLightSourceManager != null;
    }

    public static int getEntityLuminance(Entity entity) {
        if (entityLightSourceManager == null)
            return 0;
        return entityLightSourceManager.getLuminance(entity);
    }

    private ColorLightLambDynLightsBridge() {
    }
}