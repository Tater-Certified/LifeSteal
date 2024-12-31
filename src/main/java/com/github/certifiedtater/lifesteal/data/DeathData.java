package com.github.certifiedtater.lifesteal.data;

import com.github.certifiedtater.lifesteal.Lifesteal;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class DeathData {
    public final UUID deadPlayerID;
    public UUID reviverPlayerID;

    public DeathData(UUID deadPlayerID) {
        this.deadPlayerID = deadPlayerID;
    }

    public DeathData(UUID deadPlayerID, UUID reviverPlayerID) {
        this.deadPlayerID = deadPlayerID;
        this.reviverPlayerID = reviverPlayerID;
    }

    public void addToDeathDataList() {
        Lifesteal.DEAD_PLAYERS.put(this.deadPlayerID, this);
        saveDeathDataToFile();
    }

    public static void removeFromDeathDataList(UUID playerID) {
        Lifesteal.DEAD_PLAYERS.remove(playerID);
        saveDeathDataToFile();
    }

    public static void setReviver(UUID deadPlayer, UUID reviver) {
        Lifesteal.DEAD_PLAYERS.get(deadPlayer).reviverPlayerID = reviver;
    }

    public static void saveDeathDataToFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<DeathData> deathData = new ArrayList<>(Lifesteal.DEAD_PLAYERS.values());
        String data = gson.toJson(deathData);
        if (Files.notExists(Lifesteal.DEAD_PLAYERS_FILE_PATH)) {
            try {
                Files.createFile(Lifesteal.DEAD_PLAYERS_FILE_PATH);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Files.writeString(Lifesteal.DEAD_PLAYERS_FILE_PATH, data, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write data to file: " + Lifesteal.DEAD_PLAYERS_FILE_PATH, e);
        }
    }

    public static void loadDeathDataFromFile() {
        Gson gson = new Gson();
        try {
            if (Files.notExists(Lifesteal.DEAD_PLAYERS_FILE_PATH)) {
                Files.createFile(Lifesteal.DEAD_PLAYERS_FILE_PATH);
            }
            String jsonData = Files.readString(Lifesteal.DEAD_PLAYERS_FILE_PATH);
            List<DeathData> data = gson.fromJson(jsonData, new TypeToken<List<DeathData>>(){}.getType());
            if (data != null) {
                for (DeathData deathData : data) {
                    Lifesteal.DEAD_PLAYERS.put(deathData.deadPlayerID, deathData);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data from file: " + Lifesteal.DEAD_PLAYERS_FILE_PATH, e);
        }
    }

    public static boolean isPlayerDead(UUID player) {
        DeathData data = Lifesteal.DEAD_PLAYERS.get(player);
        if (data != null) {
            return data.reviverPlayerID == null;
        } else {
            return false;
        }
    }
}
