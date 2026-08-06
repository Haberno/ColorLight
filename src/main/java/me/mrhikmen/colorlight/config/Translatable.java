package me.mrhikmen.colorlight.config;

import net.minecraft.network.chat.Component;

public class Translatable {
    public static Component CLEAN_ALL = Component.translatable("me.colorlight.command.clean_all");
    public static Component ENGINE_OFF = Component.translatable("me.colorlight.command.engine_off");
    public static Component LOOK_AT_BLOCK = Component.translatable("me.colorlight.command.look_at_block");
    public static Component LIGHT_ADD = Component.translatable("me.colorlight.command.light_add");
    public static Component LIGHT_DEL = Component.translatable("me.colorlight.command.light_del");

    public static Component GENERAL = Component.translatable("me.colorlight.general");
    public static Component BLOCK = Component.translatable("me.colorlight.block");

    public static Component ENABLE = Component.translatable("me.colorlight.general.enable");
    public static Component ENABLE_Tooltip = Component.translatable("me.colorlight.general.enable.tooltip");

    public static Component LIGHT_RANGE = Component.translatable("me.colorlight.general.light_range");
    public static Component LIGHT_RANGE_Tooltip = Component.translatable("me.colorlight.general.light_range.tooltip");
    public static Component LIGHT_RANGE_Value(int value) {
        if(value > 10 && value < 15) return Component.translatable("me.colorlight.general.light_range.value", value);
        if(value == 1) return Component.translatable("me.colorlight.general.light_range.value.1", value);
        if(value%10 > 1 && value%10 < 5) return Component.translatable("me.colorlight.general.light_range.value.1-5", value);
        return Component.translatable("me.colorlight.general.light_range.value", value);
    }

    public static Component WEIGHT = Component.translatable("me.colorlight.general.weight");
    public static Component BRIGHTNESS_WEIGHT = Component.translatable("me.colorlight.general.brightness_weight");
    public static Component LOCAL_WEIGHT = Component.translatable("me.colorlight.general.local_weight");
    public static Component REGION_WEIGHT = Component.translatable("me.colorlight.general.region_weight");
    public static Component ALPHA_WEIGHT = Component.translatable("me.colorlight.general.alpha_weight");
    public static Component ANOMALY_WEIGHT = Component.translatable("me.colorlight.general.anomaly_weight");
    public static Component SATURATION_WEIGHT = Component.translatable("me.colorlight.general.saturation_weight");
    public static Component GLOWCOLORSCORE_WEIGHT = Component.translatable("me.colorlight.general.glowcolorscore_weight");
    public static Component WHITEPENALTY_WEIGHT = Component.translatable("me.colorlight.general.whitepenalty_weight");
    public static Component WEIGHT_Tooltip = Component.translatable("me.colorlight.general.weight.tooltip");
    public static Component WEIGHT_Value(int value) {
        if(value == 0) return Component.translatable("me.colorlight.general.weight.value.0", value);
        return Component.translatable("me.colorlight.general.weight.value", value);
    }

    public static Component BLOCK_ENABLE = Component.translatable("me.colorlight.block.enable_block");
    public static Component BLOCK_ENABLE_Tooltip = Component.translatable("me.colorlight.block.enable_block.tooltip");
    public static Component BLOCK_LIGHT_RANGE = Component.translatable("me.colorlight.block.light_range_block");
    public static Component BLOCK_LIGHT_RANGE_Tooltip = Component.translatable("me.colorlight.block.light_range_block.tooltip");
    public static Component BLOCK_RED = Component.translatable("me.colorlight.block.red");
    public static Component BLOCK_RED_Tooltip = Component.translatable("me.colorlight.block.red.tooltip");
    public static Component BLOCK_GREEN = Component.translatable("me.colorlight.block.green");
    public static Component BLOCK_GREEN_Tooltip = Component.translatable("me.colorlight.block.green.tooltip");
    public static Component BLOCK_BLUE = Component.translatable("me.colorlight.block.blue");
    public static Component BLOCK_BLUE_Tooltip = Component.translatable("me.colorlight.block.blue.tooltip");
}

