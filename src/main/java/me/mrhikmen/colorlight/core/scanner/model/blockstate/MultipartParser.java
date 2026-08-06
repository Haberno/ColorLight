package me.mrhikmen.colorlight.core.scanner.model.blockstate;

import me.mrhikmen.colorlight.core.scanner.model.ModelTextureResolver;

import net.minecraft.resources.Identifier;

import java.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MultipartParser {

    public MultipartParser(JsonObject json, int i) {

        JsonArray multipart = json.getAsJsonArray("multipart");

        List<Identifier> models = new ArrayList<>();

        for (JsonElement partElement : multipart) {

            JsonObject part = partElement.getAsJsonObject();

            if (!part.has("apply"))
                continue;

            JsonElement apply = part.get("apply");

            if (apply.isJsonObject()) {
                addModel(apply.getAsJsonObject(), models);

            } else if (apply.isJsonArray()) {

                for (JsonElement element : apply.getAsJsonArray()) {
                    addModel(element.getAsJsonObject(), models);
                }

            }
        }

        ModelTextureResolver.resolve(models, i);

    }

    private static void addModel(JsonObject apply, List<Identifier> models) {

        if (!apply.has("model"))
            return;

        models.add(Identifier.parse(apply.get("model").getAsString()));
    }
}