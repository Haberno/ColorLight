package me.mrhikmen.colorlight.core.scanner.model;

import me.mrhikmen.colorlight.ColorLightClient;
import me.mrhikmen.colorlight.config.BlockSettings;
import me.mrhikmen.colorlight.core.scanner.texture.PixelData;
import me.mrhikmen.colorlight.core.scanner.texture.SearchBestPixel;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ModelTextureResolver {

    public static void resolve(List<Identifier> models, int i) {

        List<PixelData> bestPixels = new ArrayList<>();

        for (Identifier modelId : models) {

            Map<String, String> textures = resolveTextures(modelId, new HashSet<>());

            for (Map.Entry<String, String> entry : textures.entrySet()) {

                String textureName = entry.getValue();
                int guard = 0;

                while (textureName.startsWith("#") && guard++ < 16) {

                    String key = textureName.substring(1);

                    if (textures.containsKey(key)) {
                        textureName = textures.get(key);
                    } else {
                        break;
                    }
                }

                if (textureName.startsWith("#"))
                    continue;

                Identifier texture = Identifier.parse(textureName);
                PixelData result = SearchBestPixel.search(texture);

                if (result != null)
                    bestPixels.add(result);
            }
        }

        PixelData best = null;

        for (PixelData pixel : bestPixels) {

            if (best == null || pixel.score > best.score)
                best = pixel;
        }

        if (best != null) {
            BlockSettings data = ColorLightClient.config.blocks.get(i);

            data.r = best.r;
            data.g = best.g;
            data.b = best.b;
        }
    }

    private static Map<String, String> resolveTextures(Identifier modelId, Set<Identifier> visited) {

        Map<String, String> textures = new HashMap<>();

        if (modelId == null || !visited.add(modelId))
            return textures;

        Identifier modelFile = Identifier.fromNamespaceAndPath(modelId.getNamespace(), "models/" + modelId.getPath() + ".json");
        Optional<Resource> modelResource = Minecraft.getInstance().getResourceManager().getResource(modelFile);

        if (modelResource.isEmpty())
            return textures;

        JsonObject modelJson;

        try (InputStream stream = modelResource.get().open()) {

            modelJson = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (modelJson.has("parent")) {

            Identifier parentId = Identifier.parse(modelJson.get("parent").getAsString());
            textures.putAll(resolveTextures(parentId, visited));

        }
        if (modelJson.has("textures")) {

            JsonObject localTextures = modelJson.getAsJsonObject("textures");

            for (Map.Entry<String, JsonElement> entry : localTextures.entrySet()) {
                textures.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return textures;
    }
}