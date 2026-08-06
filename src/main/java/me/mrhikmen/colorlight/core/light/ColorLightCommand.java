package me.mrhikmen.colorlight.core.light;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import me.mrhikmen.colorlight.config.Translatable;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class ColorLightCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            dispatcher.register(ClientCommandManager.literal("colorlight")
                    .then(ClientCommandManager.literal("inspect")
                            .executes(ColorLightCommand::debugDaylight))

                    .then(ClientCommandManager.literal("target")

                            .then(ClientCommandManager.literal("set")
                                    .then(ClientCommandManager.argument("r", IntegerArgumentType.integer(0, ColorLightUtil.MAX))
                                            .then(ClientCommandManager.argument("g", IntegerArgumentType.integer(0, ColorLightUtil.MAX))
                                                    .then(ClientCommandManager.argument("b", IntegerArgumentType.integer(0, ColorLightUtil.MAX))
                                                            .then(ClientCommandManager.argument("strength", IntegerArgumentType.integer(1, 30))
                                                                    .executes(ColorLightCommand::addAtTarget))))))

                            .then(ClientCommandManager.literal("reset")
                                    .executes(ColorLightCommand::removeAtTarget))
                    )

                    .then(ClientCommandManager.literal("clear")
                            .executes(ColorLightCommand::clearAll))
            );
        });
    }

    private static int debugDaylight(CommandContext<FabricClientCommandSource> ctx) {

        BlockPos pos = targetPos();
        if (pos == null) {
            var player = Minecraft.getInstance().player;
            if (player == null) return 0;
            pos = player.blockPosition();
        }

        ColorLightEngine engine = ColorLightEngineHolder.get();
        if (engine == null) {
            ctx.getSource().sendError(Translatable.ENGINE_OFF);
            return 0;
        }

        ctx.getSource().sendFeedback(Component.literal(engine.debugDaylight(pos)));
        return 1;
    }

    private static int clearAll(CommandContext<FabricClientCommandSource> ctx) {

        ColorLightEngine engine = ColorLightEngineHolder.get();
        if (engine == null)
            return 0;

        engine.clearAll();

        var player = Minecraft.getInstance().player;
        if (player != null) {
            BlockPos pos = player.blockPosition();
            int radius = 64;

            Minecraft.getInstance().levelRenderer.setBlocksDirty(
                    pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                    pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius
            );
        }

        ctx.getSource().sendFeedback(Translatable.CLEAN_ALL);
        return 1;
    }

    private static int addAtTarget(CommandContext<FabricClientCommandSource> ctx) {

        BlockPos pos = targetPos();
        if (pos == null) {
            ctx.getSource().sendError(Translatable.LOOK_AT_BLOCK);
            return 0;
        }

        ColorLightEngine engine = ColorLightEngineHolder.get();
        if (engine == null) {
            ctx.getSource().sendError(Translatable.ENGINE_OFF);
            return 0;
        }

        int r = IntegerArgumentType.getInteger(ctx, "r");
        int g = IntegerArgumentType.getInteger(ctx, "g");
        int b = IntegerArgumentType.getInteger(ctx, "b");
        int strength = IntegerArgumentType.getInteger(ctx, "strength");

        engine.addSource(pos, r, g, b, strength);
        markDirtyAround(pos);

        ctx.getSource().sendFeedback(Translatable.LIGHT_ADD);
        return 1;
    }

    private static int removeAtTarget(CommandContext<FabricClientCommandSource> ctx) {

        BlockPos pos = targetPos();
        if (pos == null) {
            ctx.getSource().sendError(Translatable.LOOK_AT_BLOCK);
            return 0;
        }

        ColorLightEngine engine = ColorLightEngineHolder.get();
        if (engine == null)
            return 0;

        engine.removeSource(pos);
        markDirtyAround(pos);

        ctx.getSource().sendFeedback(Translatable.LIGHT_DEL);
        return 1;
    }

    private static BlockPos targetPos() {
        HitResult hit = Minecraft.getInstance().hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            return blockHit.getBlockPos();
        }
        return null;
    }

    private static void markDirtyAround(BlockPos pos) {
        ColorLightEngine engine = ColorLightEngineHolder.get();
        int radius = (engine != null ? engine.getMaxRangeBlocks() : 15) + 1;

        Minecraft.getInstance().levelRenderer.setBlocksDirty(
                pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius
        );
    }

    private ColorLightCommand() {
    }
}