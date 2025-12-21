package com.github.tatercertified.lifesteal.data;

import com.github.tatercertified.lifesteal.Lifesteal;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.utils.LifeStealText;
import com.github.tatercertified.lifesteal.utils.OfflinePlayerData;
import com.github.tatercertified.lifesteal.utils.PlayerReviveData;
import com.github.tatercertified.lifesteal.utils.PlayerUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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
    public static List<Tuple<UUID, String>> getDeadPlayers(MinecraftServer server) {
        List<Tuple<UUID, String>> dead = new ArrayList<>();
        for (Map.Entry<UUID, DeathData> entry : Lifesteal.DEAD_PLAYERS.entrySet()) {
            if (entry.getValue().reviverPlayerID == null) {
                Optional<NameAndId> playerName = server.services().nameToIdCache().get(entry.getKey());
                playerName.ifPresent(playerConfigEntry -> dead.add(new Tuple<>(entry.getKey(), playerConfigEntry.name())));
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

    /**
     * Revives a player at the location of the reviver
     * @param reviveeId The UUID of the player being revived
     * @param reviver The player doing the reviving
     * @param contextOptional If an item was used
     * @return 0 if success, 1 if error, and 2 if the player was not found
     */
    public static byte revive(UUID reviveeId, ServerPlayer reviver, Optional<UseOnContext> contextOptional) {
        return revive(reviveeId, reviver.level().getServer(), reviver.level(), reviver.blockPosition(), reviver, contextOptional);
    }

    /**
     * Revives a player at a specified location
     * @param playerName The player's name that is being revived
     * @param server Instance of MinecraftServer
     * @param world World that the revived player should spawn in
     * @param pos The position the revived player should spawn at
     * @param reviver The player doing the reviving
     * @param contextOptional If an item was used
     * @return 0 if success, 1 if error, and 2 if the player was not found
     */
    public static byte revive(String playerName, MinecraftServer server, ServerLevel world, BlockPos pos, ServerPlayer reviver, Optional<UseOnContext> contextOptional) {
        return revive(server.getPlayerList().getPlayerByName(playerName), null, playerName, server, world, pos, reviver, contextOptional);
    }

    /**
     * Revives a player at a specified location
     * @param reviveeId The player's UUID that is being revived
     * @param server Instance of MinecraftServer
     * @param world World that the revived player should spawn in
     * @param pos The position the revived player should spawn at
     * @param reviver The player doing the reviving
     * @param contextOptional If an item was used
     * @return 0 if success, 1 if error, and 2 if the player was not found
     */
    public static byte revive(UUID reviveeId, MinecraftServer server, ServerLevel world, BlockPos pos, ServerPlayer reviver, Optional<UseOnContext> contextOptional) {
        return revive(server.getPlayerList().getPlayer(reviveeId), reviveeId, null, server, world, pos, reviver, contextOptional);
    }

    private static byte revive(ServerPlayer revivee, @Nullable UUID reviveeId, @Nullable String reviveeName, MinecraftServer server, ServerLevel world, BlockPos pos, ServerPlayer reviver, Optional<UseOnContext> contextOptional) {
        boolean fromHeart = contextOptional.isPresent();
        if (revivee != null) {
            if (reviveOnline(revivee, world, pos, reviver, fromHeart)) {
                contextOptional.ifPresent(itemUsageContext -> revived(reviver, itemUsageContext, revivee.getDisplayName()));
                return 0;
            }
            failed(reviver, pos, revivee.getDisplayName());
            return 1;
        }

        Optional<NameAndId> profile;
        if (reviveeId != null) {
            profile = server.services().nameToIdCache().get(reviveeId);
        } else if (reviveeName != null) {
            profile = server.services().nameToIdCache().get(reviveeName);
        } else {
            profile = Optional.empty();
        }

        if (profile.isPresent()) {
            if (reviveOffline(profile.get(), world, pos, reviver, fromHeart)) {
                contextOptional.ifPresent(itemUsageContext -> revived(reviver, itemUsageContext, Component.nullToEmpty(profile.get().name())));
                return 0;
            }
            failed(reviver, pos, Component.nullToEmpty(profile.get().name()));
            return 1;
        }
        return 2;
    }

    private static boolean reviveOnline(ServerPlayer player, ServerLevel world, BlockPos alter, Player reviver, boolean fromHeart) {
        if (!DeathData.isPlayerDead(player.getUUID(), world.getGameRules().get(LifeStealGamerules.AUTOREVIVAL))) {
            return false;
        }
        teleport(player, world, alter);
        player.setGameMode(GameType.SURVIVAL);

        player.sendSystemMessage(LifeStealText.onRevivalText(reviver.getDisplayName()));
        PlayerUtils.setMaxHealth(world.getGameRules().get(LifeStealGamerules.MINPLAYERHEALTH), player);
        DeathData.removeFromDeathDataList(player.getUUID());
        // These players are not newly revived if a heart was consumed to revive them
        ((PlayerReviveData)player).setNewlyRevived(!fromHeart);
        return true;
    }

    private static boolean reviveOffline(NameAndId profile, ServerLevel world, BlockPos alter, Player reviver, boolean fromHeart) {
        if (!DeathData.isPlayerDead(profile.id(), world.getGameRules().get(LifeStealGamerules.AUTOREVIVAL))) {
            return false;
        }

        MinecraftServer server = world.getServer();
        OfflinePlayerData playerData = OfflinePlayerData.getOfflinePlayerData(server, profile);
        if (playerData == null) {
            return false;
        }
        playerData.setPosition(world, alter.above().getCenter());
        playerData.setGamemode(GameType.SURVIVAL);
        playerData.setMaxHealth(world.getGameRules().get(LifeStealGamerules.MINPLAYERHEALTH));
        // These players are not newly revived if a heart was consumed to revive them
        playerData.setNewlyRevived(!fromHeart);
        playerData.save();

        DeathData.setReviver(profile.id(), reviver.getUUID());
        return true;
    }

    private static void revived(ServerPlayer reviver, UseOnContext context, Component revived) {
        successSound(context.getLevel(), context.getClickedPos());
        context.getItemInHand().shrink(1);
        reviver.displayClientMessage(LifeStealText.revived(revived), true);
    }

    private static void successSound(Level world, BlockPos alter) {
        world.playSound(null, alter, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 16.f, 1);
    }

    private static void failed(ServerPlayer reviver, BlockPos alter, Component revived) {
        failedSound(reviver.level(), alter);
        reviver.displayClientMessage(LifeStealText.playerIsAlive(revived), true);
    }

    /**
     * The sound that should play if a revive fails
     * @param world The world to play the sound in
     * @param pos The position to play it at
     */
    public static void failedSound(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.PLAYERS, 16.f, 1);
    }

    private static void teleport(Player player, ServerLevel target, BlockPos alterPos) {
        Vec3 pos = alterPos.above().getCenter();
        player.teleport(new TeleportTransition(target, pos, Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
    }
}
