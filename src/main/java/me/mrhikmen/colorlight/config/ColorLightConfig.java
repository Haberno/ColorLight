package me.mrhikmen.colorlight.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

public class ColorLightConfig {

    public boolean ENABLE = true;

    public int lightRangeBlocks = 15;

    public boolean USE_GPU_LIGHTING = true;

    public boolean SMOOTH_LIGHTING = true;

    public int BRIGHTNESS_WEIGHT     = 100;
    public int LOCAL_WEIGHT          = 100;
    public int REGION_WEIGHT         = 0;
    public int ALPHA_WEIGHT          = 0;
    public int ANOMALY_WEIGHT        = 100;
    public int SATURATION_WEIGHT     = 100;
    public int GLOWCOLORSCORE_WEIGHT = 100;
    public int WHITEPENALTY_WEIGHT   = 100;

    public LinkedList<BlockSettings> blocks = new LinkedList<>();

    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("colorlight.json");

    public void load() {
        Gson gson = new Gson();

        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                ColorLightConfig loaded = gson.fromJson(reader, ColorLightConfig.class);

                this.ENABLE = loaded.ENABLE;
                this.blocks = loaded.blocks;
                this.lightRangeBlocks = loaded.lightRangeBlocks > 0 ? loaded.lightRangeBlocks : this.lightRangeBlocks;
                this.USE_GPU_LIGHTING = loaded.USE_GPU_LIGHTING;
                this.SMOOTH_LIGHTING = loaded.SMOOTH_LIGHTING;

                this.BRIGHTNESS_WEIGHT = loaded.BRIGHTNESS_WEIGHT > -1 ? loaded.BRIGHTNESS_WEIGHT : this.BRIGHTNESS_WEIGHT;
                this.LOCAL_WEIGHT = loaded.LOCAL_WEIGHT > -1 ? loaded.LOCAL_WEIGHT : this.LOCAL_WEIGHT;
                this.REGION_WEIGHT = loaded.REGION_WEIGHT > -1 ? loaded.REGION_WEIGHT : this.REGION_WEIGHT;
                this.ALPHA_WEIGHT = loaded.ALPHA_WEIGHT > -1 ? loaded.ALPHA_WEIGHT : this.ALPHA_WEIGHT;
                this.ANOMALY_WEIGHT = loaded.ANOMALY_WEIGHT > -1 ? loaded.ANOMALY_WEIGHT : this.ANOMALY_WEIGHT;
                this.SATURATION_WEIGHT = loaded.SATURATION_WEIGHT > -1 ? loaded.SATURATION_WEIGHT : this.SATURATION_WEIGHT;
                this.GLOWCOLORSCORE_WEIGHT = loaded.GLOWCOLORSCORE_WEIGHT > -1 ? loaded.GLOWCOLORSCORE_WEIGHT : this.GLOWCOLORSCORE_WEIGHT;
                this.WHITEPENALTY_WEIGHT = loaded.WHITEPENALTY_WEIGHT > -1 ? loaded.WHITEPENALTY_WEIGHT : this.WHITEPENALTY_WEIGHT;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (Writer writer = Files.newBufferedWriter(PATH)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}