package com.github.tatercertified.lifesteal.data;

import com.github.tatercertified.lifesteal.Lifesteal;
import com.github.tatercertified.lifesteal.utils.DeadPlayerIdentification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class DeathData {
    private final UUID deadPlayerID;
    public UUID reviverPlayerID;
    public final long deathTime;

    /**
     * Dead player's Lifesteal Data
     * @param deadPlayerID The UUID of the dead player
     */
    public DeathData(UUID deadPlayerID) {
        this.deadPlayerID = deadPlayerID;
        this.deathTime = System.currentTimeMillis() / 1000;
    }

    /**
     * Adds the player to the global death list
     */
    public void addToDeathDataList() {
        Lifesteal.DEAD_PLAYERS.put(this.deadPlayerID, this);
        saveDeathDataToFile();
    }

    /**
     * Removes the player from the global death list
     * @param playerID UUID of that player
     */
    public static void removeFromDeathDataList(UUID playerID) {
        Lifesteal.DEAD_PLAYERS.remove(playerID);
        saveDeathDataToFile();
    }

    /**
     * Sets a reviver player for the dead player
     * @param deadPlayer The UUID of the dead player
     * @param reviver The UUID of the reviver
     */
    public static void setReviver(UUID deadPlayer, UUID reviver) {
        Lifesteal.DEAD_PLAYERS.get(deadPlayer).reviverPlayerID = reviver;
    }

    /**
     * Saves the DeathData to file
     */
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

    /**
     * Loads the DeathData from file
     */
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

    /**
     * Determines if the player is dead
     * @param player The ServerPlayerEntity that is being queried
     * @param waitTime The auto revival time from the GameRule
     * @return If the player is considered dead
     */
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

    /**
     * Gets a list of all dead players' UUIDs and names
     * @param server MinecraftServer instance
     * @return List of all dead players' UUIDs and names
     */
    public static List<DeadPlayerIdentification> getDeadPlayers(MinecraftServer server) {
        List<DeadPlayerIdentification> dead = new ArrayList<>();
        for (Map.Entry<UUID, DeathData> entry : Lifesteal.DEAD_PLAYERS.entrySet()) {
            if (entry.getValue().reviverPlayerID == null) {
                Optional<NameAndId> playerName = server.services().nameToIdCache().get(entry.getKey());
                playerName.ifPresent(playerConfigEntry -> dead.add(new DeadPlayerIdentification(entry.getKey(), playerConfigEntry.name())));
            }
        }
        return dead;
    }

    /**
     * Determines if the player should be automatically revived
     * @param data The player's DeathData
     * @param waitTime The auto revival time from the GameRule
     * @return If a player should be automatically revived
     */
    public static boolean shouldAutoRevive(DeathData data, int waitTime) {
        if (waitTime == 0) {
            return false;
        }

        return waitTime <= (System.currentTimeMillis() * 0.001) - data.deathTime;
    }
}
