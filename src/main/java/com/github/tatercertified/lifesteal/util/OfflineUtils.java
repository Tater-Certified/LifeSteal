package com.github.tatercertified.lifesteal.util;

import com.github.tatercertified.lifesteal.mixin.SaveHandlerAccessor;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.UUID;

public final class OfflineUtils {
    /**
     * Gets a ServerPlayerEntity regardless if it is online or not
     * @param uuid UUID of the player
     * @param server MinecraftServer
     * @return Returns Optional of a ServerPlayerEntity if it exists
     */
    public static Optional<ServerPlayerEntity> getPlayer(MinecraftServer server, UUID uuid) {
        Optional<ServerPlayerEntity> player = Optional.ofNullable(server.getPlayerManager().getPlayer(uuid));
        if (player.isPresent()) {
            return player;
        } else {
            return getOfflinePlayer(server, uuid);
        }
    }

    /**
     * Creates a ServerPlayerEntity from the UserCache
     * @param server MinecraftServer
     * @param uuid UUID of the player
     * @return Returns Optional of a ServerPlayerEntity if the user exists in the UserCache
     */
    public static Optional<ServerPlayerEntity> getOfflinePlayer(MinecraftServer server, UUID uuid) {
        Optional<GameProfile> profile = server.getUserCache().getByUuid(uuid);
        return profile.map(gameProfile -> {
            ServerPlayerEntity player = server.getPlayerManager().createPlayer(gameProfile);
            server.getPlayerManager().loadPlayerData(player);
            return player;
        });
    }

    /**
     * Gets the player data from the SaveHandler
     * @param uuid Player UUID
     * @param server MinecraftServer
     * @param playerName String player name
     * @return LifeSteal NbtCompound data
     */
    public static NbtCompound getPlayerData(UUID uuid, MinecraftServer server, String playerName) {
        return ((PlayerDataInterface)((SaveHandlerAccessor)server.getPlayerManager()).getSaveHandler()).getLifeStealInfo(uuid, playerName);
    }

    /**
     * Saves the player data
     * @param uuid Player UUID
     * @param server MinecraftServer
     * @param playerName String player name
     * @param lifeStealData LifeSteal NbtCompound data
     */
    public static void savePlayerData(UUID uuid, MinecraftServer server, String playerName, NbtCompound lifeStealData) {
        ((PlayerDataInterface)((SaveHandlerAccessor)server.getPlayerManager()).getSaveHandler()).saveLifeStealInfo(uuid, playerName, lifeStealData);
    }

    /**
     * Sets the location for teleportation when the player logs in
     * @param compound NbtCompound from getPlayerData
     * @param pos BlockPos for teleporting
     * @return NbtCompound so you can reuse it
     */
    public static NbtCompound setLocation(NbtCompound compound, BlockPos pos) {
        int[] array = new int[3];
        array[0] = pos.getX();
        array[1] = pos.getY();
        array[2] = pos.getZ();
        compound.putIntArray("teleport", array);
        return compound;
    }

    /**
     * Gets the BlockPos for teleporting
     * @param compound NbtCompound from getPlayerData
     * @return BlockPos for the teleportation position
     */
    public static BlockPos getLocation(NbtCompound compound) {
        int[] array = compound.getIntArray("teleport");
        return  new BlockPos(array[0], array[1], array[2]);
    }

    /**
     * Sets the dimension for reviving
     * @param compound NbtCompound from getPlayerData
     * @param dimension Identifier for the Dimension
     * @return NbtCompound for reuse
     */
    public static NbtCompound setDimension(NbtCompound compound, Identifier dimension) {
        compound.putString("dimension", dimension.toString());
        return compound;
    }

    /**
     * Gets the World for teleportation
     *
     * @param compound NbtCompound from getPlayerData
     * @param server   MinecraftServer
     * @return World from the Identifier in NbtCompound
     */
    public static ServerWorld getDimension(NbtCompound compound, MinecraftServer server) {
        Identifier identifier = new Identifier(compound.getString("dimension"));
        for (ServerWorld world : server.getWorlds()) {
            if (identifier.equals(world.getRegistryKey().getValue())) {
                return world;
            }
        }
        return null;
    }

    /**
     * Sets the reviver
     * @param compound NbtCompound from getPlayerData
     * @param name String of the reviver username
     * @return NbtCompound for reuse
     */
    public static NbtCompound setReviver(NbtCompound compound, String name) {
        compound.putString("reviver", name);
        return compound;
    }

    /**
     * Gets the reviver
     * @param compound NbtCompound from getPlayerData
     * @return String of the reviver's username
     */
    public static String getReviver(NbtCompound compound) {
        return compound.getString("reviver");
    }

    /**
     * Checks if a UUID is present in the PlayerManager
     * @param uuid UUID of player
     * @param server MinecraftServer
     * @return Returns true if the UUID is present
     */
    public static boolean isPlayerOnline(UUID uuid, MinecraftServer server) {
        return server.getPlayerManager().getPlayer(uuid) != null;
    }
}
