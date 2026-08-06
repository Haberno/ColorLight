package me.mrhikmen.colorlight.core.scanner.model.blockstate;

import me.mrhikmen.colorlight.core.scanner.model.ModelTextureResolver;

import net.minecraft.resources.Identifier;

import java.util.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VariantParser {

    public VariantParser(JsonObject json, int i) {

        JsonObject variants = json.getAsJsonObject("variants");
        List<Identifier> models = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : variants.entrySet()) {

            JsonElement variant = entry.getValue();

            if (variant.isJsonObject()) {

                JsonObject model = variant.getAsJsonObject();
                models.add(Identifier.parse(model.get("model").getAsString()));

            } else if (variant.isJsonArray()) {

                for (JsonElement element : variant.getAsJsonArray()) {

                    JsonObject model = element.getAsJsonObject();
                    models.add(Identifier.parse(model.get("model").getAsString()));

                }
            }
        }

        ModelTextureResolver.resolve(models, i);

    }
}