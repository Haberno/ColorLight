package me.mrhikmen.colorlight.core.light.gpu;

import me.mrhikmen.colorlight.ColorLightClient;
import me.mrhikmen.colorlight.core.light.ColorLightEngine;
import me.mrhikmen.colorlight.core.light.ColorLightUtil;
import me.mrhikmen.colorlight.core.util.ColorLightRenderUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ColorLightGpuEngine extends ColorLightEngine {

    private static final int MAX_REGION_SIZE = 96;

    private static final int MERGE_BUDGET_PER_TICK = 6;

    private final ILightComputeBackend backend;
    private final ConcurrentLinkedQueue<long[]> pendingDirtyBoxes = new ConcurrentLinkedQueue<>();

    private final ConcurrentHashMap<Long, Integer> smoothData = new ConcurrentHashMap<>();

    public ColorLightGpuEngine(LevelAccessor level, int maxRangeBlocks, ILightComputeBackend backend) {
        super(level, maxRangeBlocks);
        this.backend = backend;
    }

    private void markDirty(BlockPos center) {
        int r = getMaxRangeBlocks() + 1;
        pendingDirtyBoxes.add(new long[]{
                center.getX() - r, center.getY() - r, center.getZ() - r,
                center.getX() + r, center.getY() + r, center.getZ() + r
        });
    }

    @Override
    public void addSource(BlockPos pos, int r, int g, int b, int strength) {
        float scale = ColorLightUtil.clamp01(strength / 15f);
        int packed = ColorLightUtil.pack(Math.round(r * scale), Math.round(g * scale), Math.round(b * scale));

        long key = pos.asLong();
        sources.put(key, packed);
        data.put(key, packed);

        markDirty(pos);
    }

    @Override
    public void removeSource(BlockPos pos) {
        long key = pos.asLong();
        if (sources.remove(key) == null)
            return;

        data.remove(key);
        smoothData.remove(key);

        markDirty(pos);
    }

    @Override
    public void onBlockChanged(BlockPos pos) {
        if (sources.containsKey(pos.asLong()))
            return;

        markDirty(pos);
    }

    @Override
    public void clearAll() {
        super.clearAll();
        pendingDirtyBoxes.clear();
        smoothData.clear();
    }

    @Override
    public int sampleSmoothColor(BlockPos pos, Direction face, float vx, float vy, float vz) {
        BlockPos facePos = (face != null) ? pos.relative(face) : pos;

        Integer cachedFace = smoothData.get(facePos.asLong());
        if (cachedFace == null)
            return super.sampleSmoothColor(pos, face, vx, vy, vz);

        return ColorLightUtil.max(cachedFace, getColor(pos));
    }

    public void tick() {
        if (pendingDirtyBoxes.isEmpty())
            return;

        if (!backend.isSupported())
            return;

        List<long[]> batch = new ArrayList<>(MERGE_BUDGET_PER_TICK);
        long[] box;
        while (batch.size() < MERGE_BUDGET_PER_TICK && (box = pendingDirtyBoxes.poll()) != null) {
            batch.add(box);
        }
        if (batch.isEmpty())
            return;

        long[] region = mergeAndClamp(batch);

        try {
            processRegion(region);
        } catch (Exception e) {
            ColorLightClient.LOGGER.error("[ColorLight] GPU calculation error in region lighting, " + "region will be recalculated upon the next change.", e);
        }
    }

    private long[] mergeAndClamp(List<long[]> boxes) {
        long[] result = boxes.get(0).clone();
        for (int i = 1; i < boxes.size(); i++) {
            long[] b = boxes.get(i);
            result[0] = Math.min(result[0], b[0]);
            result[1] = Math.min(result[1], b[1]);
            result[2] = Math.min(result[2], b[2]);
            result[3] = Math.max(result[3], b[3]);
            result[4] = Math.max(result[4], b[4]);
            result[5] = Math.max(result[5], b[5]);
        }

        for (int axis = 0; axis < 3; axis++) {
            long min = result[axis];
            long max = result[axis + 3];
            if (max - min > MAX_REGION_SIZE) {
                long center = (min + max) / 2;
                result[axis] = center - MAX_REGION_SIZE / 2L;
                result[axis + 3] = center + MAX_REGION_SIZE / 2L;
            }
        }
        return result;
    }

    private void processRegion(long[] box) {
        int minX = (int) box[0], minY = (int) box[1], minZ = (int) box[2];
        int maxX = (int) box[3], maxY = (int) box[4], maxZ = (int) box[5];

        int sx = maxX - minX + 1;
        int sy = maxY - minY + 1;
        int sz = maxZ - minZ + 1;

        int voxelCount = sx * sy * sz;
        if (voxelCount <= 0)
            return;

        byte[] opacity = new byte[voxelCount];
        int[] baseColor = new int[voxelCount];

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int z = 0; z < sz; z++) {
            for (int y = 0; y < sy; y++) {
                for (int x = 0; x < sx; x++) {
                    cursor.set(minX + x, minY + y, minZ + z);
                    opacity[index(x, y, z, sx, sy)] = (byte) getOpacity(cursor);
                }
            }
        }

        for (Map.Entry<Long, Integer> entry : sources.entrySet()) {
            BlockPos p = BlockPos.of(entry.getKey());
            int x = p.getX(), y = p.getY(), z = p.getZ();
            if (x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ)
                continue;

            baseColor[index(x - minX, y - minY, z - minZ, sx, sy)] = entry.getValue();
        }

        ILightComputeBackend.LightComputeResult computed =
                backend.propagateAndSmooth(sx, sy, sz, opacity, baseColor, getMaxRangeBlocks(), getDecayPerOpacityUnit());

        int[] result = computed.propagated();
        int[] smoothed = computed.smoothed();

        for (int z = 0; z < sz; z++) {
            for (int y = 0; y < sy; y++) {
                for (int x = 0; x < sx; x++) {
                    int idx = index(x, y, z, sx, sy);
                    int packed = result[idx];
                    long key = BlockPos.asLong(minX + x, minY + y, minZ + z);

                    if (ColorLightUtil.isEmpty(packed)) {
                        data.remove(key);
                    } else {
                        data.put(key, packed);
                    }

                    int smoothPacked = smoothed[idx];
                    if (ColorLightUtil.isEmpty(smoothPacked)) {
                        smoothData.remove(key);
                    } else {
                        smoothData.put(key, smoothPacked);
                    }
                }
            }
        }

        if (level instanceof ClientLevel clientLevel) {
            ColorLightRenderUtil.setBlocksDirtySafe(clientLevel, minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private static int index(int x, int y, int z, int sx, int sy) {
        return x + y * sx + z * sx * sy;
    }
}
