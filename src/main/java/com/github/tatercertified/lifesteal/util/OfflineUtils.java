package com.github.tatercertified.lifesteal.util;

import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

public final class OfflineUtils {
    private static final Logger logger = LogUtils.getLogger();

    /**
     * Gets a ServerPlayerEntity regardless if it is online or not
     *
     * @param uuid   UUID of the player
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
     *
     * @param server  The Minecraft Server
     * @param profile The profile of the player being fetched
     * @return The offline player's data if it exists and can be read, null otherwise
     */
    public static OfflinePlayerData getOfflinePlayerData(MinecraftServer server, GameProfile profile) {
        final Path dir = server.getSavePath(WorldSavePath.PLAYERDATA);
        final Path dat = dir.resolve(profile.getId() + ".dat");
        if (Files.exists(dat) && Files.isRegularFile(dat)) {
            try (final InputStream stream = Files.newInputStream(dat)) {
                final NbtCompound compound = NbtIo.readCompressed(stream);
                return new OfflinePlayerData(profile, compound, dir);
            } catch (IOException ioe) {
                logger.warn("Unable to read NBT for {}", profile, ioe);
            }
        }
        return null;
    }

    /**
     * Checks if a UUID is present in the PlayerManager
     *
     * @param uuid   UUID of player
     * @param server MinecraftServer
     * @return Returns true if the UUID is present
     */
    public static boolean isPlayerOnline(UUID uuid, MinecraftServer server) {
        return server.getPlayerManager().getPlayer(uuid) != null;
    }

    /**
     * Obtains a detached attribute instance for a given player.
     *
     * @param playerData The player data to obtain attributes from
     * @param attribute  The attribute of interest
     * @return The attribute instance if it exists, null otherwise.
     */
    @Nullable
    public static EntityAttributeInstance getAttribute(NbtCompound playerData, EntityAttribute attribute) {
        if (!playerData.contains("Attributes", NbtElement.LIST_TYPE)) {
            return null;
        }
        final var attributes = playerData.getList("Attributes", NbtElement.COMPOUND_TYPE);
        final var id = Registries.ATTRIBUTE.getId(attribute).toString();

        for (int i = 0; i < attributes.size(); i++) {
            final var compound = attributes.getCompound(i);
            if (!compound.contains("Name", NbtElement.STRING_TYPE)) {
                continue;
            }

            if (!id.equals(compound.getString("Name"))) {
                continue;
            }

            final var inst = new EntityAttributeInstance(attribute, ignored -> {
            });
            inst.readNbt(compound);
            return inst;
        }

        return null;
    }

    /**
     * Legacy death check for existing players of the mod.
     *
     * @param playerData The player data to check for legacy death data of.
     * @param server     The Minecraft Server the player originates from.
     * @return true if the player is considered dead by legacy rules, false otherwise.
     */
    public static boolean isDead(NbtCompound playerData, MinecraftServer server) {
        final var health = getAttribute(playerData, EntityAttributes.GENERIC_MAX_HEALTH);
        if (health != null) {
            return health.getBaseValue() < server.getGameRules().getInt(LSGameRules.MINPLAYERHEALTH);
        }
        return false;
    }
}
