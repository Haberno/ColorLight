package me.mrhikmen.colorlight.core.render;

import me.mrhikmen.colorlight.ColorLightClient;
import me.mrhikmen.colorlight.core.light.ColorLightEngine;
import me.mrhikmen.colorlight.core.light.ColorLightEngineHolder;
import me.mrhikmen.colorlight.core.light.ColorLightUtil;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;


import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

public class TintedBakedModel implements BlockStateModel {

    private final BlockStateModel wrapped;

    public TintedBakedModel(BlockStateModel original) {
        this.wrapped = original;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> output) {
        wrapped.collectParts(random, output);
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return wrapped.particleIcon();
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {

        ColorLightEngine engine = ColorLightEngineHolder.get();

        if (engine == null) {
            wrapped.emitQuads(emitter, blockView, pos, state, random, cullTest);
            return;
        }

        emitter.pushTransform(quad -> {
            Direction face = quad.lightFace();
            BlockPos daylightPos = (face != null) ? pos.relative(face) : pos;
            float daylight = engine.getDaylightFactor(daylightPos);

            boolean smooth = ColorLightClient.config.SMOOTH_LIGHTING;
            int flat = smooth ? 0 : engine.sampleFlatColor(pos, face);

            for (int i = 0; i < 4; i++) {
                int packed = smooth
                        ? engine.sampleSmoothColor(pos, face, quad.x(i), quad.y(i), quad.z(i))
                        : flat;
                quad.color(i, ColorLightUtil.toArgb(packed, daylight));
            }

            return true;
        });

        wrapped.emitQuads(emitter, blockView, pos, state, random, cullTest);

        emitter.popTransform();
    }
}