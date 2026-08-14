package me.mrhikmen.colorlight.mixin.minecraft;

import me.mrhikmen.colorlight.config.BlockSettings;
import me.mrhikmen.colorlight.core.light.ColorLightBlockRegistry;
import me.mrhikmen.colorlight.core.light.ColorLightEngine;
import me.mrhikmen.colorlight.core.light.ColorLightEngineHolder;
import me.mrhikmen.colorlight.core.light.ColorLightUtil;
import me.mrhikmen.colorlight.core.util.ColorLightRenderUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    @Shadow
    private Level level;

    @Inject(
            method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN")
    )
    private void colorlight$onSetBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() == null)
            return;

        if (!(this.level instanceof ClientLevel clientLevel))
            return;

        ColorLightEngine engine = ColorLightEngineHolder.get();
        if (engine == null)
            return;

        Block newBlock = state.getBlock();
        BlockSettings entry = ColorLightBlockRegistry.get(newBlock);

        boolean mayAffectLighting;

        if (entry != null) {

            int emission = state.getLightEmission();

            if (emission > 0) {
                int strength = Math.min(entry.light, emission);
                engine.addSource(pos, entry.r, entry.g, entry.b, strength);
                mayAffectLighting = true;
            } else if (engine.hasSource(pos)) {
                engine.removeSource(pos);
                mayAffectLighting = true;
            } else if (colorlight$mayAffectLighting(engine, pos)) {
                engine.onBlockChanged(pos);
                mayAffectLighting = true;
            } else {
                mayAffectLighting = false;
            }
        } else if (engine.hasSource(pos)) {
            engine.removeSource(pos);
            mayAffectLighting = true;
        } else if (colorlight$mayAffectLighting(engine, pos)) {
            engine.onBlockChanged(pos);
            mayAffectLighting = true;
        } else {
            mayAffectLighting = false;
        }

        if (!mayAffectLighting)
            return;

        int radius = engine.getMaxRangeBlocks() + 1;

        ColorLightRenderUtil.setBlocksDirtySafe(clientLevel,
                pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius
        );
    }

    private static boolean colorlight$mayAffectLighting(ColorLightEngine engine, BlockPos pos) {
        if (!ColorLightUtil.isEmpty(engine.getColor(pos)))
            return true;

        for (Direction dir : Direction.values()) {
            if (!ColorLightUtil.isEmpty(engine.getColor(pos.relative(dir))))
                return true;
        }
        return false;
    }
}