package dk.spacemc.minions.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import dk.spacemc.minions.classes.Minion;

import java.lang.reflect.Type;

public class MinionSerializer implements JsonSerializer<Minion> {
    @Override
    public JsonElement serialize(Minion minion, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("level", minion.getLevel());
        jsonObject.addProperty("type", Minion.getType(minion.getType()));
        jsonObject.addProperty("uuidOfOwner", minion.getUuidOfOwner());
        jsonObject.addProperty("x", minion.getX());
        jsonObject.addProperty("y", minion.getY());
        jsonObject.addProperty("z", minion.getZ());
        jsonObject.addProperty("chestX", minion.getChestX());
        jsonObject.addProperty("chestY", minion.getChestY());
        jsonObject.addProperty("chestZ", minion.getChestZ());
        jsonObject.addProperty("world", minion.getWorld().getName());
        jsonObject.addProperty("isDisabled", minion.isDisabled());
        return jsonObject;
    }
}