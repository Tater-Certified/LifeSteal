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
    public final long deathTime;

    public DeathData(UUID deadPlayerID) {
        this.deadPlayerID = deadPlayerID;
        this.deathTime = System.currentTimeMillis() / 1000;
    }

    public DeathData(UUID deadPlayerID, UUID reviverPlayerID) {
        this.deadPlayerID = deadPlayerID;
        this.reviverPlayerID = reviverPlayerID;
        this.deathTime = System.currentTimeMillis() / 1000;
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

    public static boolean isPlayerDead(UUID player, int waitTime) {
        DeathData data = Lifesteal.DEAD_PLAYERS.get(player);
        if (data != null) {
            if (shouldAutoRevive(data, waitTime)) {
                // Revive
                removeFromDeathDataList(player);
                return false;
            }

            return data.reviverPlayerID == null;
        } else {
            return false;
        }
    }

    public static boolean shouldAutoRevive(DeathData data, int waitTime) {
        if (waitTime == 0) {
            return false;
        }

        return waitTime <= (System.currentTimeMillis() * 0.001) - data.deathTime;
    }
}
