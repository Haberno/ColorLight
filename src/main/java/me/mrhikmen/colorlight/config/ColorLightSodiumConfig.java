package me.mrhikmen.colorlight.config;

import me.mrhikmen.colorlight.ColorLightClient;
import me.mrhikmen.colorlight.core.light.ColorLightBlockRegistry;
import me.mrhikmen.colorlight.core.light.ColorLightChunkScanner;
import me.mrhikmen.colorlight.core.light.ColorLightEngineHolder;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.SteppedValidator;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;

import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
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
                            .addOption(builder.createBooleanOption(Identifier.parse("colorlight:block_enable_" + block.getPath()))
                                    .setName(Component.empty().append(Component.translatable("block." + block.toLanguageKey())).append(Translatable.BLOCK_ENABLE))
                                    .setTooltip(Translatable.BLOCK_ENABLE_Tooltip)
                                    .setStorageHandler(this::save)
                                    .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                                    .setBinding(value -> entry.enable = value, () -> entry.enable)
                                    .setDefaultValue(entry.enable)
                            )
                            .addOption(builder.createIntegerOption(Identifier.parse("colorlight:block_light_range_" + block.getPath()))
                                    .setName(Component.empty().append(Component.translatable("block." + block.toLanguageKey())).append(Translatable.BLOCK_LIGHT_RANGE))
                                    .setTooltip(Translatable.BLOCK_LIGHT_RANGE_Tooltip)
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
                                    .setBinding(value -> entry.light = value, () -> entry.light)
                                    .setDefaultValue(entry.light)
                            )
                            .addOption(builder.createIntegerOption(Identifier.parse("colorlight:block_red_" + block.getPath()))
                                    .setName(Component.empty().append(Component.translatable("block." + block.toLanguageKey())).append(Translatable.BLOCK_RED))
                                    .setTooltip(Translatable.BLOCK_RED_Tooltip)
                                    .setValidator(new SteppedValidator() {
                                        @Override
                                        public int min() {
                                            return 0;
                                        }
                                        @Override
                                        public int max() {
                                            return 255;
                                        }
                                        @Override
                                        public int step() {
                                            return 1;
                                        }
                                    })
                                    .setValueFormatter(value -> Component.literal("" + value))
                                    .setStorageHandler(this::save)
                                    .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                                    .setBinding(value -> entry.r = value, () -> entry.r)
                                    .setDefaultValue(entry.r)
                            )
                            .addOption(builder.createIntegerOption(Identifier.parse("colorlight:block_green_" + block.getPath()))
                                    .setName(Component.empty().append(Component.translatable("block." + block.toLanguageKey())).append(Translatable.BLOCK_GREEN))
                                    .setTooltip(Translatable.BLOCK_GREEN_Tooltip)
                                    .setValidator(new SteppedValidator() {
                                        @Override
                                        public int min() {
                                            return 0;
                                        }
                                        @Override
                                        public int max() {
                                            return 255;
                                        }
                                        @Override
                                        public int step() {
                                            return 1;
                                        }
                                    })
                                    .setValueFormatter(value -> Component.literal("" + value))
                                    .setStorageHandler(this::save)
                                    .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                                    .setBinding(value -> entry.g = value, () -> entry.g)
                                    .setDefaultValue(entry.g)
                            )
                            .addOption(builder.createIntegerOption(Identifier.parse("colorlight:block_blue_" + block.getPath()))
                                    .setName(Component.empty().append(Component.translatable("block." + block.toLanguageKey())).append(Translatable.BLOCK_BLUE))
                                    .setTooltip(Translatable.BLOCK_BLUE_Tooltip)
                                    .setValidator(new SteppedValidator() {
                                        @Override
                                        public int min() {
                                            return 0;
                                        }
                                        @Override
                                        public int max() {
                                            return 255;
                                        }
                                        @Override
                                        public int step() {
                                            return 1;
                                        }
                                    })
                                    .setValueFormatter(value -> Component.literal("" + value))
                                    .setStorageHandler(this::save)
                                    .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                                    .setBinding(value -> entry.b = value, () -> entry.b)
                                    .setDefaultValue(entry.b)
                            )
            );
        }
        return page;
    }
    private void save() {
        config.save();
        ColorLightEngineHolder.configure(config.lightRangeBlocks);
        ColorLightBlockRegistry.load(config);
        var client = net.minecraft.client.Minecraft.getInstance();
        if (client.level != null) {
            ColorLightEngineHolder.set(client.level);
            if (config.ENABLE) {
                ColorLightChunkScanner.rescanAll(client.level);
            } else {
                markWholeRenderDistanceDirty(client);
            }
        }
    }
    private static void markWholeRenderDistanceDirty(net.minecraft.client.Minecraft client) {
        var player = client.player;
        if (player == null) return;
        int renderDistanceBlocks = client.options.renderDistance().get() << 4;
        var pos = player.blockPosition();
        client.levelRenderer.setBlocksDirty(
                pos.getX() - renderDistanceBlocks, client.level.getMinY(), pos.getZ() - renderDistanceBlocks,
                pos.getX() + renderDistanceBlocks, client.level.getMaxY(), pos.getZ() + renderDistanceBlocks
        );
    }
}