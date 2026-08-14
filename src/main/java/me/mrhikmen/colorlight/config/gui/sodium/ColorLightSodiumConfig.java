package me.mrhikmen.colorlight.config.gui.sodium;

import me.mrhikmen.colorlight.ColorLightClient;
import me.mrhikmen.colorlight.config.BlockSettings;
import me.mrhikmen.colorlight.config.ColorLightConfig;
import me.mrhikmen.colorlight.config.Translatable;
import me.mrhikmen.colorlight.config.gui.screen.ColorLightBlockConfigScreen;
import me.mrhikmen.colorlight.core.light.ColorLightBlockRegistry;
import me.mrhikmen.colorlight.core.light.ColorLightChunkScanner;
import me.mrhikmen.colorlight.core.light.ColorLightEngineHolder;
import me.mrhikmen.colorlight.core.util.ColorLightRenderUtil;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.SteppedValidator;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;

import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@ConfigEntryPointForge("colorlight")
public class ColorLightSodiumConfig implements ConfigEntryPoint {

    private final ColorLightConfig config = ColorLightClient.config;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        config.load();
        builder.registerOwnModOptions()
                .setNonTintedIcon(Identifier.parse("colorlight:icon.png"))
                .setColorTheme(builder.createColorTheme().setBaseThemeRGB(0x73efff))
                .addPage(builder.createOptionPage()
                        .setName(Translatable.GENERAL)
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createBooleanOption(Identifier.parse("colorlight:enable"))
                                        .setName(Translatable.ENABLE)
                                        .setTooltip(Translatable.ENABLE_Tooltip)
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                                        .setBinding(value -> config.ENABLE = value, () -> config.ENABLE)
                                        .setDefaultValue(config.ENABLE)
                                )
                                .addOption(builder.createIntegerOption(Identifier.parse("colorlight:light_range"))
                                        .setName(Translatable.LIGHT_RANGE)
                                        .setTooltip(Translatable.LIGHT_RANGE_Tooltip)
                                        .setValidator(new SteppedValidator() {
                                            @Override
                                            public int min() {
                                                return 1;
                                            }
                                            @Override
                                            public int max() {
                                                return 30;
                                            }
                                            @Override
                                            public int step() {
                                                return 1;
                                            }
                                        })
                                        .setValueFormatter(value -> Translatable.LIGHT_RANGE_Value(value))
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                                        .setBinding(value -> config.lightRangeBlocks = value, () -> config.lightRangeBlocks)
                                        .setDefaultValue(config.lightRangeBlocks)
                                )
                                .addOption(builder.createBooleanOption(Identifier.parse("colorlight:use_gpu_lighting"))
                                        .setName(Translatable.USE_GPU_LIGHTING)
                                        .setTooltip(Translatable.USE_GPU_LIGHTING_Tooltip)
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                                        .setBinding(value -> config.USE_GPU_LIGHTING = value, () -> config.USE_GPU_LIGHTING)
                                        .setDefaultValue(config.USE_GPU_LIGHTING)
                                )
                                .addOption(builder.createBooleanOption(Identifier.parse("colorlight:smooth_lighting"))
                                        .setName(Translatable.SMOOTH_LIGHTING)
                                        .setTooltip(Translatable.SMOOTH_LIGHTING_Tooltip)
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                        .setBinding(value -> config.SMOOTH_LIGHTING = value, () -> config.SMOOTH_LIGHTING)
                                        .setDefaultValue(config.SMOOTH_LIGHTING)
                                )
                        )
                        .addOptionGroup(builder.createOptionGroup()
                                .setName(Translatable.WEIGHT)
                                .addOption(builder.createIntegerOption(Identifier.parse("colorlight:brightness_weight"))
                                        .setName(Translatable.BRIGHTNESS_WEIGHT)
                                        .setTooltip(Translatable.WEIGHT_Tooltip)
                                        .setValidator(new SteppedValidator() {
                                            @Override
                                            public int min() {
                                                return 0;
                                            }
                                            @Override
                                            public int max() {
                                                return 100;
                                            }
                                            @Override
                                            public int step() {
                                                return 1;
                                            }
                                        })
                                        .setValueFormatter(value -> Translatable.WEIGHT_Value(value))
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                        .setBinding(value -> config.BRIGHTNESS_WEIGHT = value, () -> config.BRIGHTNESS_WEIGHT)
                                        .setDefaultValue(config.BRIGHTNESS_WEIGHT)
                                )
                                .addOption(builder.createIntegerOption(Identifier.parse("colorlight:local_weight"))
                                        .setName(Translatable.LOCAL_WEIGHT)
                                        .setTooltip(Translatable.WEIGHT_Tooltip)
                                        .setValidator(new SteppedValidator() {
                                            @Override
                                            public int min() {
                                                return 0;
                                            }
                                            @Override
                                            public int max() {
                                                return 100;
                                            }
                                            @Override
                                            public int step() {
                                                return 1;
                                            }
                                        })
                                        .setValueFormatter(value -> Translatable.WEIGHT_Value(value))
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                        .setBinding(value -> config.LOCAL_WEIGHT = value, () -> config.LOCAL_WEIGHT)
                                        .setDefaultValue(config.LOCAL_WEIGHT)
                                )
                                .addOption(builder.createIntegerOption(Identifier.parse("colorlight:region_weight"))
                                        .setName(Translatable.REGION_WEIGHT)
                                        .setTooltip(Translatable.WEIGHT_Tooltip)
                                        .setValidator(new SteppedValidator() {
                                            @Override
                                            public int min() {
                                                return 0;
                                            }
                                            @Override
                                            public int max() {
                                                return 100;
                                            }
                                            @Override
                                            public int step() {
                                                return 1;
                                            }
                                        })
                                        .setValueFormatter(value -> Translatable.WEIGHT_Value(value))
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                        .setBinding(value -> config.REGION_WEIGHT = value, () -> config.REGION_WEIGHT)
                                        .setDefaultValue(config.REGION_WEIGHT)
                                )
                                .addOption(builder.createIntegerOption(Identifier.parse("colorlight:alpha_weight"))
                                        .setName(Translatable.ALPHA_WEIGHT)
                                        .setTooltip(Translatable.WEIGHT_Tooltip)
                                        .setValidator(new SteppedValidator() {
                                            @Override
                                            public int min() {
                                                return 0;
                                            }
                                            @Override
                                            public int max() {
                                                return 100;
                                            }
                                            @Override
                                            public int step() {
                                                return 1;
                                            }
                                        })
                                        .setValueFormatter(value -> Translatable.WEIGHT_Value(value))
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                        .setBinding(value -> config.ALPHA_WEIGHT = value, () -> config.ALPHA_WEIGHT)
                                        .setDefaultValue(config.ALPHA_WEIGHT)
                                )
                                .addOption(builder.createIntegerOption(Identifier.parse("colorlight:anomaly_weight"))
                                        .setName(Translatable.ANOMALY_WEIGHT)
                                        .setTooltip(Translatable.WEIGHT_Tooltip)
                                        .setValidator(new SteppedValidator() {
                                            @Override
                                            public int min() {
                                                return 0;
                                            }
                                            @Override
                                            public int max() {
                                                return 100;
                                            }
                                            @Override
                                            public int step() {
                                                return 1;
                                            }
                                        })
                                        .setValueFormatter(value -> Translatable.WEIGHT_Value(value))
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                        .setBinding(value -> config.ANOMALY_WEIGHT = value, () -> config.ANOMALY_WEIGHT)
                                        .setDefaultValue(config.ANOMALY_WEIGHT)
                                )
                                .addOption(builder.createIntegerOption(Identifier.parse("colorlight:saturation_weight"))
                                        .setName(Translatable.SATURATION_WEIGHT)
                                        .setTooltip(Translatable.WEIGHT_Tooltip)
                                        .setValidator(new SteppedValidator() {
                                            @Override
                                            public int min() {
                                                return 0;
                                            }
                                            @Override
                                            public int max() {
                                                return 100;
                                            }
                                            @Override
                                            public int step() {
                                                return 1;
                                            }
                                        })
                                        .setValueFormatter(value -> Translatable.WEIGHT_Value(value))
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                        .setBinding(value -> config.SATURATION_WEIGHT = value, () -> config.SATURATION_WEIGHT)
                                        .setDefaultValue(config.SATURATION_WEIGHT)
                                )
                                .addOption(builder.createIntegerOption(Identifier.parse("colorlight:glowcolorscore_weight"))
                                        .setName(Translatable.GLOWCOLORSCORE_WEIGHT)
                                        .setTooltip(Translatable.WEIGHT_Tooltip)
                                        .setValidator(new SteppedValidator() {
                                            @Override
                                            public int min() {
                                                return 0;
                                            }
                                            @Override
                                            public int max() {
                                                return 100;
                                            }
                                            @Override
                                            public int step() {
                                                return 1;
                                            }
                                        })
                                        .setValueFormatter(value -> Translatable.WEIGHT_Value(value))
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                        .setBinding(value -> config.GLOWCOLORSCORE_WEIGHT = value, () -> config.GLOWCOLORSCORE_WEIGHT)
                                        .setDefaultValue(config.GLOWCOLORSCORE_WEIGHT)
                                )
                                .addOption(builder.createIntegerOption(Identifier.parse("colorlight:whitepenalty_weight"))
                                        .setName(Translatable.WHITEPENALTY_WEIGHT)
                                        .setTooltip(Translatable.WEIGHT_Tooltip)
                                        .setValidator(new SteppedValidator() {
                                            @Override
                                            public int min() {
                                                return 0;
                                            }
                                            @Override
                                            public int max() {
                                                return 100;
                                            }
                                            @Override
                                            public int step() {
                                                return 1;
                                            }
                                        })
                                        .setValueFormatter(value -> Translatable.WEIGHT_Value(value))
                                        .setStorageHandler(this::save)
                                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                        .setBinding(value -> config.WHITEPENALTY_WEIGHT = value, () -> config.WHITEPENALTY_WEIGHT)
                                        .setDefaultValue(config.WHITEPENALTY_WEIGHT)
                                )
                        )
                )
                .addPage(this.BlockPage(builder));
    }
    private OptionPageBuilder BlockPage(ConfigBuilder builder) {
        OptionPageBuilder page = builder.createOptionPage().setName(Translatable.BLOCK);
        for (BlockSettings entry : ColorLightClient.config.blocks) {
            Identifier block = entry.getBlock();
            page.addOptionGroup(
                    builder.createOptionGroup()
                            .addOption(builder.createExternalButtonOption(Identifier.parse("colorlight:block_" + block.getPath()))
                                    .setName(Component.translatable("block." + block.toLanguageKey()))
                                    .setTooltip(Translatable.BLOCK_Tooltip)
                                    .setScreenConsumer(parentScreen -> Minecraft.getInstance().setScreenAndShow(new ColorLightBlockConfigScreen(parentScreen, entry, this::save)))
                            )
            );
        }
        return page;
    }
    private static final long SAVE_DEBOUNCE_MS = 150L;
    private static final java.util.concurrent.ScheduledExecutorService SAVE_DEBOUNCER = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(
            runnable -> {
                Thread thread = new Thread(runnable, "ColorLight Config Save Debouncer");
                thread.setDaemon(true);
                return thread;
            });
    private java.util.concurrent.ScheduledFuture<?> pendingSave;
    private void save() {
        if (pendingSave != null) {
            pendingSave.cancel(false);
        }
        pendingSave = SAVE_DEBOUNCER.schedule(() -> Minecraft.getInstance().execute(this::applySaveNow), SAVE_DEBOUNCE_MS, java.util.concurrent.TimeUnit.MILLISECONDS
        );
    }

    private void applySaveNow() {
        config.save();
        ColorLightEngineHolder.configure(config.lightRangeBlocks, config.USE_GPU_LIGHTING);
        ColorLightBlockRegistry.load(config);
        var client = net.minecraft.client.Minecraft.getInstance();
        if (client.level != null) {
            ColorLightEngineHolder.set(client.level);
            if (config.ENABLE) {
                ColorLightChunkScanner.rescanAll(client.level);
            }
            markWholeRenderDistanceDirty(client);
        }
    }
    private static void markWholeRenderDistanceDirty(net.minecraft.client.Minecraft client) {
        var player = client.player;
        if (player == null || client.level == null) return;
        int renderDistanceBlocks = client.options.renderDistance().get() << 4;
        var pos = player.blockPosition();
        ColorLightRenderUtil.setBlocksDirtySafe(client.level,
                pos.getX() - renderDistanceBlocks, client.level.getMinY(), pos.getZ() - renderDistanceBlocks,
                pos.getX() + renderDistanceBlocks, client.level.getMaxY(), pos.getZ() + renderDistanceBlocks
        );
    }
}