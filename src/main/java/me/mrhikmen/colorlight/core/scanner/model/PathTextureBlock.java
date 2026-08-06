package me.mrhikmen.colorlight.core.scanner.model;

import me.mrhikmen.colorlight.ColorLightClient;
import me.mrhikmen.colorlight.config.BlockSettings;
import me.mrhikmen.colorlight.core.scanner.model.blockstate.*;
import me.mrhikmen.colorlight.core.scanner.texture.PixelData;
import me.mrhikmen.colorlight.core.scanner.texture.SearchBestPixel;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PathTextureBlock {

    public PathTextureBlock() {

        for (int i = 0; i < ColorLightClient.config.blocks.size();) {

            Identifier block = ColorLightClient.config.blocks.get(i).getBlock();;

            Identifier modelId = Identifier.fromNamespaceAndPath(block.getNamespace(), "blockstates/" + block.getPath() + ".json");
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(modelId);

            if (resource.isPresent()) {

                try (InputStream stream = resource.get().open()) {

                    JsonObject json = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();

                    if (json.has("variants")) {
                        new VariantParser(json, i);
                    } else if (json.has("multipart")) {
                        new MultipartParser(json, i);
                    } else {
                        ColorLightClient.LOGGER.info("Model not found");
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {

                Identifier textureId = Identifier.parse(block.getNamespace() + ":block/" + block.getPath());
                Identifier texture = Identifier.fromNamespaceAndPath(textureId.getNamespace(), "textures/" + textureId.getPath() + ".png");

                PixelData best = SearchBestPixel.search(texture);


                if (best != null) {
                    BlockSettings data = ColorLightClient.config.blocks.get(i);
                    data.r = best.r;
                    data.g = best.g;
                    data.b = best.b;
                }
            }

            i++;
        }
    }
}