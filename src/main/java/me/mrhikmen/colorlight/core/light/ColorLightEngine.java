package me.mrhikmen.colorlight.core.light;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ColorLightEngine {

    protected static final int VANILLA_MAX_OPACITY = 15;

    private final int maxRangeBlocks;

    protected final float decayPerOpacityUnit;

    protected final ConcurrentHashMap<Long, Integer> data = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<Long, Integer> sources = new ConcurrentHashMap<>();

    protected final LevelAccessor level;

    public ColorLightEngine(LevelAccessor level, int maxRangeBlocks) {
        this.level = level;
        this.maxRangeBlocks = Math.max(1, maxRangeBlocks); // защита от 0/отрицательных значений из конфига
        this.decayPerOpacityUnit = ColorLightUtil.MAX / (float) this.maxRangeBlocks;
    }

    public int getMaxRangeBlocks() {
        return maxRangeBlocks;
    }

    public int getColor(BlockPos pos) {
        return getRaw(pos.asLong());
    }

    public boolean hasSource(BlockPos pos) {
        return sources.containsKey(pos.asLong());
    }

    public boolean isOpaque(BlockPos pos) {
        return getOpacity(pos) >= VANILLA_MAX_OPACITY;
    }

    public List<BlockPos> getSourcePositions() {
        List<BlockPos> result = new ArrayList<>();
        for (Long key : sources.keySet()) {
            result.add(BlockPos.of(key));
        }
        return result;
    }

    public void clearAll() {
        data.clear();
        sources.clear();
    }

    protected int getRaw(long key) {
        Integer v = data.get(key);
        return v != null ? v : ColorLightUtil.EMPTY;
    }

    public float getDecayPerOpacityUnit() {
        return decayPerOpacityUnit;
    }

    public void addSource(BlockPos pos, int r, int g, int b, int strength) {
        float scale = ColorLightUtil.clamp01(strength / 15f);
        int packed = ColorLightUtil.pack(Math.round(r * scale), Math.round(g * scale), Math.round(b * scale));

        long key = pos.asLong();

        sources.put(key, packed);
        data.put(key, packed);

        ArrayDeque<Long> queue = new ArrayDeque<>();
        queue.add(key);
        propagateAdd(queue);
    }

    private void propagateAdd(ArrayDeque<Long> queue) {
        while (!queue.isEmpty()) {
            long key = queue.poll();
            BlockPos pos = BlockPos.of(key);
            int current = getRaw(key);

            int r = ColorLightUtil.r(current);
            int g = ColorLightUtil.g(current);
            int b = ColorLightUtil.b(current);

            if (r == 0 && g == 0 && b == 0)
                continue;

            for (Direction dir : Direction.values()) {

                BlockPos neighborPos = pos.relative(dir);
                long neighborKey = neighborPos.asLong();

                int opacity = getOpacity(neighborPos);
                if (opacity >= VANILLA_MAX_OPACITY)
                    continue;

                int decay = Math.round((1 + opacity) * decayPerOpacityUnit);

                int nr = Math.max(0, r - decay);
                int ng = Math.max(0, g - decay);
                int nb = Math.max(0, b - decay);

                if (nr == 0 && ng == 0 && nb == 0)
                    continue;

                int neighborCurrent = getRaw(neighborKey);
                int cr = ColorLightUtil.r(neighborCurrent);
                int cg = ColorLightUtil.g(neighborCurrent);
                int cb = ColorLightUtil.b(neighborCurrent);

                boolean changed = false;
                int fr = cr, fg = cg, fb = cb;

                if (nr > cr) { fr = nr; changed = true; }
                if (ng > cg) { fg = ng; changed = true; }
                if (nb > cb) { fb = nb; changed = true; }

                if (changed) {
                    data.put(neighborKey, ColorLightUtil.pack(fr, fg, fb));
                    queue.add(neighborKey);
                }
            }
        }
    }

    public void removeSource(BlockPos pos) {
        long key = pos.asLong();
        Integer sourceColor = sources.remove(key);
        if (sourceColor == null)
            return;

        int old = getRaw(key);
        data.put(key, ColorLightUtil.EMPTY);

        ArrayDeque<Long> relightSeeds = darkenAndCollectSeeds(key, old);
        propagateAdd(relightSeeds);
    }

    private ArrayDeque<Long> darkenAndCollectSeeds(long startKey, int oldColorAtStart) {

        ArrayDeque<long[]> darkenQueue = new ArrayDeque<>();
        ArrayDeque<Long> relightSeeds = new ArrayDeque<>();

        if (!ColorLightUtil.isEmpty(oldColorAtStart)) {
            darkenQueue.add(new long[]{
                    startKey,
                    ColorLightUtil.r(oldColorAtStart),
                    ColorLightUtil.g(oldColorAtStart),
                    ColorLightUtil.b(oldColorAtStart)
            });
        }

        while (!darkenQueue.isEmpty()) {

            long[] entry = darkenQueue.poll();
            long curKey = entry[0];
            int r = (int) entry[1];
            int g = (int) entry[2];
            int b = (int) entry[3];

            BlockPos curPos = BlockPos.of(curKey);

            for (Direction dir : Direction.values()) {

                BlockPos neighborPos = curPos.relative(dir);
                long neighborKey = neighborPos.asLong();

                if (sources.containsKey(neighborKey)) {
                    relightSeeds.add(neighborKey);
                    continue;
                }

                int neighborPacked = getRaw(neighborKey);
                int nr = ColorLightUtil.r(neighborPacked);
                int ng = ColorLightUtil.g(neighborPacked);
                int nb = ColorLightUtil.b(neighborPacked);

                if (nr == 0 && ng == 0 && nb == 0)
                    continue;

                boolean darkened = false;
                int fr = nr, fg = ng, fb = nb;

                if (nr != 0 && nr < r) { fr = 0; darkened = true; }
                if (ng != 0 && ng < g) { fg = 0; darkened = true; }
                if (nb != 0 && nb < b) { fb = 0; darkened = true; }

                if (darkened) {
                    data.put(neighborKey, ColorLightUtil.pack(fr, fg, fb));
                    darkenQueue.add(new long[]{neighborKey, nr, ng, nb});
                }

                if (nr >= r || ng >= g || nb >= b) {
                    relightSeeds.add(neighborKey);
                }
            }
        }
        return relightSeeds;
    }

    public void onBlockChanged(BlockPos pos) {
        long key = pos.asLong();

        if (sources.containsKey(key))
            return;

        int old = getRaw(key);
        data.put(key, ColorLightUtil.EMPTY);

        ArrayDeque<Long> relightSeeds = darkenAndCollectSeeds(key, old);

        relightSeeds.add(key);
        for (Direction dir : Direction.values()) {
            relightSeeds.add(pos.relative(dir).asLong());
        }

        propagateAdd(relightSeeds);
    }

    public float getDaylightFactor(BlockPos pos) {
        if (!(level instanceof Level realLevel))
            return 0f;

        float skyExposure = realLevel.getBrightness(LightLayer.SKY, pos) / 15f;

        float timeOfDayFactor = computeTimeOfDayFactor(realLevel);

        return ColorLightUtil.clamp01(skyExposure * timeOfDayFactor);
    }

    private static float computeTimeOfDayFactor(Level level) {
        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < 0)
            dayTime += 24000L;

        if (dayTime < 12000) return 1f;
        if (dayTime < 12100) return 0.9f;
        if (dayTime < 12200) return 0.8f;
        if (dayTime < 12300) return 0.7f;
        if (dayTime < 12400) return 0.6f;
        if (dayTime < 12500) return 0.5f;
        if (dayTime < 12600) return 0.4f;
        if (dayTime < 12700) return 0.3f;
        if (dayTime < 12800) return 0.2f;
        if (dayTime < 12900) return 0.1f;
        if (dayTime < 23000) return 0f;
        if (dayTime < 23100) return 0.1f;
        if (dayTime < 23200) return 0.2f;
        if (dayTime < 23300) return 0.3f;
        if (dayTime < 23400) return 0.4f;
        if (dayTime < 23500) return 0.5f;
        if (dayTime < 23600) return 0.6f;
        if (dayTime < 23700) return 0.7f;
        if (dayTime < 23800) return 0.8f;
        if (dayTime < 23900) return 0.9f;
        return 1f;
    }

    public String debugDaylight(BlockPos pos) {
        if (!(level instanceof Level realLevel))
            return "[ColorLight] Level is not a real Level (" + level.getClass() + ")";

        long dayTimeRaw = realLevel.getBrightness(LightLayer.SKY, pos);
        long dayTime = dayTimeRaw % 24000L;
        if (dayTime < 0) dayTime += 24000L;

        int rawSky = realLevel.getBrightness(LightLayer.SKY, pos);
        float skyExposure = rawSky / 15f;
        float timeFactor = computeTimeOfDayFactor(realLevel);
        float finalFactor = getDaylightFactor(pos);

        return "dayTimeRaw=" + dayTimeRaw + " dayTime%24000=" + dayTime + " rawSky=" + rawSky + " skyExposure=" + skyExposure + " timeFactor=" + timeFactor + " finalDaylightFactor=" + finalFactor;
    }

    protected int getOpacity(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return Math.max(0, Math.min(VANILLA_MAX_OPACITY, state.getLightBlock()));
    }

    public int sampleSmoothColor(BlockPos pos, Direction face, float vx, float vy, float vz) {

        BlockPos facePos = (face != null) ? pos.relative(face) : pos;

        if (face == null) {
            return ColorLightUtil.max(getColor(pos), getColor(facePos));
        }

        Direction.Axis axisA;
        Direction.Axis axisB;

        switch (face.getAxis()) {
            case X -> { axisA = Direction.Axis.Y; axisB = Direction.Axis.Z; }
            case Y -> { axisA = Direction.Axis.X; axisB = Direction.Axis.Z; }
            default -> { axisA = Direction.Axis.X; axisB = Direction.Axis.Y; }
        }

        float coordA = axisCoord(axisA, vx, vy, vz);
        float coordB = axisCoord(axisB, vx, vy, vz);

        int[] offsetsA = (coordA < 0.5f) ? new int[]{-1, 0} : new int[]{0, 1};
        int[] offsetsB = (coordB < 0.5f) ? new int[]{-1, 0} : new int[]{0, 1};

        int sumR = 0, sumG = 0, sumB = 0;
        int validSamples = 0;

        for (int oa : offsetsA) {
            for (int ob : offsetsB) {
                BlockPos samplePos = offsetAxis(offsetAxis(facePos, axisA, oa), axisB, ob);

                if (isOpaque(samplePos))
                    continue;

                int color = getColor(samplePos);
                sumR += ColorLightUtil.r(color);
                sumG += ColorLightUtil.g(color);
                sumB += ColorLightUtil.b(color);
                validSamples++;
            }
        }

        int avg = (validSamples > 0)
                ? ColorLightUtil.pack(Math.round(sumR / (float) validSamples), Math.round(sumG / (float) validSamples), Math.round(sumB / (float) validSamples))
                : ColorLightUtil.EMPTY;

        return ColorLightUtil.max(avg, getColor(pos));
    }

    public int sampleFlatColor(BlockPos pos, Direction face) {
        BlockPos facePos = (face != null) ? pos.relative(face) : pos;
        return ColorLightUtil.max(getColor(pos), getColor(facePos));
    }

    private static float axisCoord(Direction.Axis axis, float x, float y, float z) {
        return switch (axis) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }

    private static BlockPos offsetAxis(BlockPos pos, Direction.Axis axis, int amount) {
        if (amount == 0) return pos;
        return switch (axis) {
            case X -> pos.offset(amount, 0, 0);
            case Y -> pos.offset(0, amount, 0);
            case Z -> pos.offset(0, 0, amount);
        };
    }
}