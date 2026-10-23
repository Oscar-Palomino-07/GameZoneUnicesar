package com.gamezone.persistence;

import com.gamezone.model.Cable;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;

/**
 * Rebuilds a {@link Product} from its JSON representation, choosing the
 * concrete subtype ({@link VideoGame}, {@link Console}, {@link Controller},
 * {@link Cable} or {@link Memory}) based on the attributes present in the
 * serialized object.
 *
 * <p>It is shared by the repositories that store products inside other
 * objects, so the rule that tells the subtypes apart is defined in a single
 * place.</p>
 */
class ProductDeserializer implements JsonDeserializer<Product> {

    private final Gson typeGson = new Gson();

    @Override
    public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject object = json.getAsJsonObject();
        if (object.has("connectionType")) {
            return typeGson.fromJson(json, Controller.class);
        }
        if (object.has("lengthInMeters") || object.has("connectorType")) {
            return typeGson.fromJson(json, Cable.class);
        }
        if (object.has("capacityInGb") || object.has("memoryType")) {
            return typeGson.fromJson(json, Memory.class);
        }
        if (object.has("platform") || object.has("genre") || object.has("ageRating")) {
            return typeGson.fromJson(json, VideoGame.class);
        }
        return typeGson.fromJson(json, Console.class);
    }
}
