package com.github.tatercertified.lifesteal.util;

import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class OfflineUtils {
    private static final Logger logger = LogUtils.getLogger();

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
     * Obtains a detached attribute instance for a given player.
     *
     * @param playerData The player data to obtain attributes from
     * @param attribute  The attribute of interest
     * @return The attribute instance if it exists, null otherwise.
     */
    @Nullable
    public static EntityAttributeInstance getAttribute(NbtCompound playerData, EntityAttribute attribute) {
        final var attributes = getAttributes(playerData);
        if (attributes == null) {
            return null;
        }
        return attributes.get(attribute);
    }

    /**
     * Obtains a map of attributes to detached instances for a given player.
     *
     * @param playerData The player data to obtain attributes from.
     * @return The attribute map if it exists, null otherwise.
     * @throws AssertionError if the {@code Attributes} list doesn't exist and {@code -ea} was passed.
     */
    // Null is possible in production environments, albeit unexpected.
    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static Map<EntityAttribute, EntityAttributeInstance> getAttributes(NbtCompound playerData) {
        if (!playerData.contains("Attributes", NbtElement.LIST_TYPE)) {
            assert false : "Attributes";
            return null;
        }
        final var attributes = playerData.getList("Attributes", NbtElement.COMPOUND_TYPE);
        final var ret = new HashMap<EntityAttribute, EntityAttributeInstance>(attributes.size());

        for (int i = 0; i < attributes.size(); i++) {
            final var compound = attributes.getCompound(i);
            final var attribute = getAttributeByName(compound.get("Name"));
            if (attribute == null) {
                continue;
            }

            final var inst = new EntityAttributeInstance(attribute, ignored -> {
            });
            inst.readNbt(compound);
            ret.put(attribute, inst);
        }

        return ret;
    }

    /**
     * Serialises a list of attributes into an NBT list and stores it in the given playerData,
     * overwriting the existing {@code Attributes} list.
     *
     * @param playerData The player data to add attributes to.
     * @param attributes The attributes to save.
     */
    public static void putAttributes(NbtCompound playerData, Iterable<EntityAttributeInstance> attributes) {
        final var list = new NbtList();

        for (final var attribute : attributes) {
            list.add(attribute.toNbt());
        }

        playerData.put("Attributes", list);
    }

    /**
     * Fetches the entity attribute by the given ID if it is one
     */
    private static EntityAttribute getAttributeByName(NbtElement element) {
        if (!(element instanceof NbtString string)) {
            return null;
        }
        final var id = Identifier.tryParse(string.asString());
        if (id == null) {
            return null;
        }
        return Registries.ATTRIBUTE.get(id);
    }

    /**
     * Legacy death check for existing players of the mod.
     *
     * @param playerData The player data to check for legacy death data of.
     * @param server     The Minecraft Server the player originates from.
     * @return true if the player is considered dead by legacy rules, false otherwise.
     */
    public static boolean isDead(NbtCompound playerData, MinecraftServer server) {
        if (playerData.contains("lifeStealData")) {
            // Not actually dead; pending revival.
            return false;
        }

        final var health = getAttribute(playerData, EntityAttributes.GENERIC_MAX_HEALTH);
        if (health != null) {
            return health.getBaseValue() < server.getGameRules().getInt(LSGameRules.MINPLAYERHEALTH);
        }
        return false;
    }
}
