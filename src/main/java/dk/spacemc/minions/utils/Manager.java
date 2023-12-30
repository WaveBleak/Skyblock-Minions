package dk.spacemc.minions.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dk.spacemc.minions.Minions;
import dk.spacemc.minions.classes.Minion;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static dk.spacemc.minions.Minions.getInstance;


import com.google.gson.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Manager {
    private final File dbFile;
    private JsonArray dataArray;
    private final Gson gson = new Gson();
    private final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    private final Gson advancedGson = new GsonBuilder()
            .registerTypeAdapter(Minion.class, (JsonSerializer<Minion>) (minion, typeOfSrc, context) -> {
                JsonObject jsonObject = new JsonObject();
                int type;
                switch(minion.getType()) {
                    case DIG:
                        type = 1;
                        break;
                    case PICKUP:
                        type = 2;
                        break;
                    case ATTACK:
                        type = 3;
                        break;
                    default:
                        type = 4;
                        break;
                }
                jsonObject.addProperty("level", minion.getLevel());
                jsonObject.addProperty("type", type);
                jsonObject.addProperty("uuidOfOwner", minion.getUuidOfOwner());
                jsonObject.addProperty("x", minion.getX());
                jsonObject.addProperty("y", minion.getY());
                jsonObject.addProperty("z", minion.getZ());
                jsonObject.addProperty("chestX", minion.getChestX());
                jsonObject.addProperty("chestY", minion.getChestY());
                jsonObject.addProperty("chestZ", minion.getChestZ());
                jsonObject.addProperty("yaw", minion.getYaw());
                jsonObject.addProperty("world", minion.getWorld().getName());
                jsonObject.addProperty("blocksBroken", minion.getBlocksBroken());
                jsonObject.addProperty("entitiesKilled", minion.getEntitiesKilled());
                jsonObject.addProperty("itemsSold", minion.getItemsSold());
                jsonObject.addProperty("itemsPickedUp", minion.getItemsPickedUp());
                jsonObject.addProperty("secondsAlive", minion.getSecondsAlive());
                jsonObject.addProperty("isRunning", minion.isRunning());
                return jsonObject;
            }).create();

    public Manager() {
        dbFile = new File("test.json"); // simplified for this example
        if(!dbFile.exists()) {
            List<Minion> datas = new ArrayList<>();
            datas.add(new Minion(1, 2, "390f6268-c72e-4206-a8dc-4980cd655845", 5, 5, 5, 6, 6, 6, 0, "world"));
            datas.add(new Minion(1, 1, "67b6c55c-948d-4967-99f2-5ff1c254b27f", 10, 10, 10, 11, 11, 11, 0, "world"));
            saveData(datas);
        }
        dataArray = loadJsonFromFile();
    }

    public synchronized List<Minion> loadData() {
        Type type = new TypeToken<List<Minion>>() {}.getType();
        return gson.fromJson(dataArray, type);
    }

    public synchronized void saveData(List<Minion> data) {
        dataArray = advancedGson.toJsonTree(data).getAsJsonArray();
        saveJsonToFile(dataArray);
    }

    public synchronized JsonArray loadJsonFromFile() {
        try (FileReader reader = new FileReader(dbFile)) {
            return gson.fromJson(reader, JsonArray.class);
        }catch (Exception e) {
            Minions.getInstance().getLogger().log(Level.SEVERE, "An error occurred while loading json from file", e);
        }
        return new JsonArray();
    }

    public synchronized <T> void saveJsonToFile(T object) {
        try (FileWriter writer = new FileWriter(dbFile)) {
            String jsonString = prettyGson.toJson(object);
            writer.write(jsonString);
        }catch (Exception e) {
            Minions.getInstance().getLogger().log(Level.SEVERE, "An error occurred while saving json to file", e);
        }
    }
}