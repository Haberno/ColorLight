package me.mrhikmen.colorlight.core.light;

import me.mrhikmen.colorlight.ColorLightClient;
import me.mrhikmen.colorlight.config.BlockSettings;
import me.mrhikmen.colorlight.core.util.ColorLightRenderUtil;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ColorLightChunkScanner {

    private static final ExecutorService SCAN_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() - 1)),
            runnable -> {
                Thread thread = new Thread(runnable, "ColorLight Chunk Scanner");
                thread.setDaemon(true);
                return thread;
            });

    private static final ExecutorService APPLY_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ColorLight Source Apply");
        thread.setDaemon(true);
        return thread;
    });

    private record FoundSource(BlockPos pos, int r, int g, int b, int strength) {
    }

    public static void register() {
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
            if (!ColorLightClient.config.ENABLE)
                return;
            if (ColorLightBlockRegistry.isEmpty())
                return;

            SCAN_EXECUTOR.submit(() -> scanChunk(level, chunk));
        });
    }

    public static void rescanAll(ClientLevel level) {
        if (level == null)
            return;

        var player = Minecraft.getInstance().player;
        if (player == null)
            return;

        int renderDistance = Minecraft.getInstance().options.renderDistance().get();
        ChunkPos center = player.chunkPosition();

        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {

                int x = center.x + dx;
                int z = center.z + dz;

                if (level.hasChunk(x, z)) {
                    LevelChunk chunk = level.getChunk(x, z);
                    SCAN_EXECUTOR.submit(() -> scanChunk(level, chunk));
                }
            }
        }
    }

    private static void scanChunk(ClientLevel level, LevelChunk chunk) {

        if (ColorLightBlockRegistry.isEmpty())
            return;

        ColorLightEngine engine = ColorLightEngineHolder.get();
        if (engine == null)
            return;

        LevelChunkSection[] sections = chunk.getSections();
        int minSectionY = level.getMinSectionY();

        int chunkBlockX = chunk.getPos().x << 4;
        int chunkBlockZ = chunk.getPos().z << 4;

        List<FoundSource> found = new ArrayList<>();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {

            LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir())
                continue;

            boolean mayHaveSource = section.maybeHas(
                    state -> state.getLightEmission() > 0 && ColorLightBlockRegistry.get(state.getBlock()) != null
            );
            if (!mayHaveSource)
                continue;

            int sectionBlockY = (minSectionY + sectionIndex) << 4;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {

                        BlockState state = section.getBlockState(x, y, z);

                        int emission = state.getLightEmission();
                        if (emission <= 0)
                            continue;

                        BlockSettings entry = ColorLightBlockRegistry.get(state.getBlock());
                        if (entry == null)
                            continue;

                        BlockPos pos = new BlockPos(chunkBlockX + x, sectionBlockY + y, chunkBlockZ + z);

                        if (engine.hasSource(pos))
                            continue;

                        int strength = Math.min(entry.light, emission);
                        found.add(new FoundSource(pos, entry.r, entry.g, entry.b, strength));
                    }
                }
            }
        }

        if (!found.isEmpty()) {
            APPLY_EXECUTOR.submit(() -> applyFound(level, chunk, engine, found));
        }
    }

    private static void applyFound(ClientLevel level, LevelChunk chunk, ColorLightEngine engine, List<FoundSource> found) {
        boolean changed = false;

        for (FoundSource source : found) {
            if (engine.hasSource(source.pos()))
                continue;

            engine.addSource(source.pos(), source.r(), source.g(), source.b(), source.strength());
            changed = true;
        }

        if (changed) {
            markChunkDirtyOnMainThread(level, chunk, engine.getMaxRangeBlocks());
        }
    }

    private static void markChunkDirtyOnMainThread(ClientLevel level, LevelChunk chunk, int rangeBlocks) {
        Minecraft.getInstance().execute(() -> {

            int radius = rangeBlocks + 1;
            int chunkBlockX = chunk.getPos().x << 4;
            int chunkBlockZ = chunk.getPos().z << 4;

            ColorLightRenderUtil.setBlocksDirtySafe(level,
                    chunkBlockX - radius, level.getMinY(), chunkBlockZ - radius,
                    chunkBlockX + 16 + radius, level.getMaxY(), chunkBlockZ + 16 + radius
            );
        });
    }

    private ColorLightChunkScanner() {
    }
}
