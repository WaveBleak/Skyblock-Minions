package dk.spacemc.minions.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import dk.spacemc.minions.classes.Minion;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static dk.spacemc.minions.Minions.getInstance;


public class Manager {
    private final File dbFile;
    private JsonArray dataArray;

    public Manager() {
        dbFile = new File(getInstance().getDataFolder().toString() + "/Minions.json");
        if(!dbFile.exists()) {
            try {
                if(dbFile.createNewFile()) {
                    getInstance().getLogger().info("Created new Minions file");
                    List<Minion> datas = new ArrayList<>();
                    datas.add(new Minion(1, 2, "390f6268-c72e-4206-a8dc-4980cd655845", 5, 5, 5, 6, 6, 6, 0, "world"));
                    datas.add(new Minion(1, 1, "67b6c55c-948d-4967-99f2-5ff1c254b27f", 10, 10, 10, 11, 11, 11, 0, "world"));
                    saveJsonToFile(datas);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        dataArray = loadJsonFromFile();
    }

    public List<Minion> loadData() {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Minion>>() {}.getType();
        return gson.fromJson(dataArray, type);
    }

    public void saveData(List<Minion> data) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Minion.class, new MinionSerializer()).create();
        dataArray = gson.toJsonTree(data).getAsJsonArray();
        saveJsonToFile(dataArray);
    }

    public JsonArray loadJsonFromFile() {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(dbFile)) {
            return gson.fromJson(reader, JsonArray.class);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonArray();
    }

    public <T> void saveJsonToFile(T object) {
        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter(dbFile)) {
            String jsonString = gson.toJson(object);
            writer.write(jsonString);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
