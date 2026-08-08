package me.mrhikmen.colorlight.compat.lambdynlights;

import me.mrhikmen.colorlight.ColorLightClient;
import me.mrhikmen.colorlight.config.BlockSettings;
import me.mrhikmen.colorlight.core.light.ColorLightEngine;
import me.mrhikmen.colorlight.core.light.ColorLightEngineHolder;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public final class ColorLightEntityLightTicker {

    private static final int CHECK_RADIUS_BLOCKS = 32;

    private record TrackedSource(BlockPos pos, BlockSettings settings) {
    }

    private static final Map<Integer, TrackedSource> ACTIVE_SOURCES = new HashMap<>();

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            ClientLevel level = client.level;
            if (level == null || client.player == null)
                return;

            ColorLightEngine engine = ColorLightEngineHolder.get();
            if (engine == null)
                return;

            BlockPos playerPos = client.player.blockPosition();

            for (Entity entity : level.entitiesForRendering()) {
                if (!isTrackableSource(entity))
                    continue;

                if (entity.blockPosition().distManhattan(playerPos) > CHECK_RADIUS_BLOCKS)
                    continue;

                processEntity(engine, entity);
            }

            ACTIVE_SOURCES.keySet().removeIf(id -> {
                Entity e = level.getEntity(id);
                boolean shouldRemove = (e == null)
                        || e.blockPosition().distManhattan(playerPos) > CHECK_RADIUS_BLOCKS;
                if (shouldRemove) {
                    BlockPos pos = ACTIVE_SOURCES.get(id).pos();
                    engine.removeSource(pos);
                    markDirtyAround(engine, pos);
                }
                return shouldRemove;
            });
        });
    }

    private static boolean isTrackableSource(Entity entity) {
        return entity instanceof LivingEntity
                || entity instanceof ItemEntity
                || entity instanceof ItemFrame
                || entity instanceof AbstractMinecart;
    }

    private static void processEntity(ColorLightEngine engine, Entity entity) {
        BlockSettings settings = findGlowingBlockSettings(entity);

        if (settings == null) {
            removeIfTracked(engine, entity.getId());
            return;
        }

        BlockPos currentPos = entity.blockPosition();
        TrackedSource previous = ACTIVE_SOURCES.get(entity.getId());

        if (previous != null && previous.pos().equals(currentPos) && previous.settings() == settings)
            return;

        if (previous != null) {
            engine.removeSource(previous.pos());
        }

        engine.addSource(currentPos, settings.r, settings.g, settings.b, Math.min(15, settings.light));
        ACTIVE_SOURCES.put(entity.getId(), new TrackedSource(currentPos, settings));

        markDirtyAround(engine, currentPos);
        if (previous != null && !previous.pos().equals(currentPos)) {
            markDirtyAround(engine, previous.pos());
        }
    }

    private static BlockSettings findGlowingBlockSettings(Entity entity) {
        if (entity instanceof LivingEntity living) {
            BlockSettings fromMain = settingsOf(living.getMainHandItem());
            if (fromMain != null)
                return fromMain;
            return settingsOf(living.getOffhandItem());
        }

        if (entity instanceof ItemEntity itemEntity) {
            return settingsOf(itemEntity.getItem());
        }

        if (entity instanceof ItemFrame itemFrame) {
            return settingsOf(itemFrame.getItem());
        }

        if (entity instanceof AbstractMinecart minecart) {
            return settingsOf(minecart.getDisplayBlockState());
        }

        return null;
    }

    private static BlockSettings settingsOf(ItemStack stack) {
        if (stack.isEmpty())
            return null;
        if (!(stack.getItem() instanceof BlockItem blockItem))
            return null;

        return settingsOf(blockItem.getBlock().defaultBlockState());
    }

    private static BlockSettings settingsOf(BlockState state) {
        if (state == null || state.isAir())
            return null;

        Block block = state.getBlock();
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (blockId == null)
            return null;

        for (BlockSettings entry : ColorLightClient.config.blocks) {
            if (entry.enable && entry.getBlock().equals(blockId)) {
                return entry;
            }
        }
        return null;
    }

    private static void removeIfTracked(ColorLightEngine engine, int entityId) {
        TrackedSource tracked = ACTIVE_SOURCES.remove(entityId);
        if (tracked != null) {
            engine.removeSource(tracked.pos());
            markDirtyAround(engine, tracked.pos());
        }
    }

    private static void markDirtyAround(ColorLightEngine engine, BlockPos pos) {
        var client = Minecraft.getInstance();
        if (client.level == null || pos == null)
            return;

        int radius = engine.getMaxRangeBlocks() + 1;
        Minecraft.getInstance().levelRenderer.setBlocksDirty(
                pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius
        );
    }

    private ColorLightEntityLightTicker() {
    }
}