package com.github.tatercertified.lifesteal.util;

import com.github.tatercertified.lifesteal.item.ModItems;
import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import com.github.tatercertified.lifesteal.world.nbt.NBTStorage;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;

import static com.github.tatercertified.lifesteal.util.ExchangeState.*;

public final class PlayerUtils {
    private static final Logger logger = LogUtils.getLogger();

    /**
     * Checks to see if the UUID is on the dead players list
     * @param uuid ServerPlayerEntity's UUID
     * @param server MinecraftServer instance
     * @return boolean of whether the UUID is present in the list
     */
    public static boolean isPlayerDead(UUID uuid, MinecraftServer server) {
        NBTStorage storage = NBTStorage.getServerState(server);
        NbtIntArray uuidAsArray = NbtHelper.fromUuid(uuid);

        return storage.deadPlayers.contains(uuidAsArray);
    }

    /**
     * Adds a player's UUID to the list of dead players
     * @param uuid ServerPlayerEntity's UUID
     * @param server MinecraftServer instance
     */
    public static void addPlayerToDeadList(UUID uuid, MinecraftServer server) {
        NBTStorage storage = NBTStorage.getServerState(server);
        NbtIntArray uuidAsArray = NbtHelper.fromUuid(uuid);

        if (storage.deadPlayers.add(uuidAsArray)) {
            storage.markDirty();
        }
    }

    /**
     * Iterates through the list of dead players and remove the specified UUID from it
     * @param uuid ServerPlayerEntity's UUID
     * @param server MinecraftServer instance
     */
    public static void removePlayerFromDeadList(UUID uuid, MinecraftServer server) {
        NBTStorage storage = NBTStorage.getServerState(server);
        NbtIntArray uuidAsArray = NbtHelper.fromUuid(uuid);

        if (storage.deadPlayers.remove(uuidAsArray)) {
            storage.markDirty();
        }
    }

    /**
     * Clears the deadPlayers list
     *
     * @param server MinecraftServer instance
     */
    public static void clearDeadList(MinecraftServer server) {
        NBTStorage storage = NBTStorage.getServerState(server);
        storage.deadPlayers.clear();
        storage.markDirty();
    }

    /**
     * Removes the ban for a legacy player,
     * if they were banned and {@link LSGameRules#UNBAN_ON_REVIVAL} is {@code true}.
     *
     * @param server  The Minecraft Server
     * @param profile The profile of the player to unban
     * @param reviver The player reviving the player currently being unbanned.
     * @return true if unbanned or was never banned, false otherwise.
     */
    public static boolean unbanLegacyPlayer(MinecraftServer server, GameProfile profile, PlayerEntity reviver) {
        BannedPlayerList bans = server.getPlayerManager().getUserBanList();
        BannedPlayerEntry banEntry = bans.get(profile);
        if (banEntry == null) {
            // Was never banned to begin with.
            return true;
        }
        if (server.getGameRules().getBoolean(LSGameRules.UNBAN_ON_REVIVAL)
                && "(Unknown)".equals(banEntry.getSource())
                && "Banned by an operator.".equals(banEntry.getReason())) {
            // This is an administrative action and may not be intended
            logger.info("[LifeSteal] Unbanning {} as part of revival by {}", profile, reviver);
            bans.remove(banEntry);
            return true;
        }
        return false;
    }

    /**
     * Exchanges 'amount' of health points between two players
     *
     * @param giver   The player who is giving health
     * @param profile The player who is receiving health
     * @param amount  The amount of health being exchanged
     * @return The state of the exchange.
     */
    public static ExchangeState exchangeHealth(ServerPlayerEntity giver, GameProfile profile, int amount) {
        final MinecraftServer server = giver.getServer();
        final UUID uuid = profile.getId();
        if (giver.getUuid().equals(uuid)) {
            return FAIL_SELF;
        }
        if (isPlayerDead(uuid, server)) {
            return FAIL_DEAD;
        }
        // Make sure the player can even give before executing a potentially expensive task
        if (failsMinCheck(giver, amount)) {
            return FAIL_RECEIVER_TOO_MUCH_HEALTH;
        }
        ServerPlayerEntity receiver = server.getPlayerManager().getPlayer(profile.getId());
        if (receiver == null) {
            return exchangeHealthOffline(giver, profile, amount);
        }

        // Check if the receiver can take more health
        if (failsMaxCheck(receiver, amount)) {
            return FAIL_RECEIVER_TOO_MUCH_HEALTH;
        }

        // Passed checks, let's exchange health!
        adjustBaseHealthInternal(receiver, amount);
        adjustBaseHealthInternal(giver, -amount);

        return SUCCESS;
    }

    /**
     * Exchanges 'amount' of health between an online and offline player.
     *
     * @param giver   The player who is giving health
     * @param profile The player who is receiving health
     * @param amount  The amount of health being exchanged
     * @return The state of the exchange.
     */
    private static ExchangeState exchangeHealthOffline(ServerPlayerEntity giver, GameProfile profile, int amount) {
        final MinecraftServer server = giver.getServer();
        final OfflinePlayerData playerData = OfflineUtils.getOfflinePlayerData(server, profile);
        if (playerData == null) {
            // The player somehow doesn't exist yet.
            // Probably was pulled up via another command at one point.
            return FAIL_MISSING;
        }

        final Map<EntityAttribute, EntityAttributeInstance> attributes = OfflineUtils.getAttributes(playerData.root);
        if (attributes == null) {
            // I actually don't know if this is possible,
            // but we'll assume missing for practical purposes.
            return FAIL_MISSING;
        }

        final EntityAttributeInstance maxHealth = attributes.get(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealth == null) {
            // Also shouldn't be possible, but again,
            // we'll assume missing for practical purposes.
            return FAIL_MISSING;
        }

        // We at this point have all the preconditions met.
        // Double-check the giver here in case this is called directly in the future.
        if (failsMinCheck(giver, amount)) {
            return FAIL_GIVER_TOO_LITTLE_HEALTH;
        }

        if (failsMaxCheck(maxHealth, amount, giver.getWorld().getGameRules())) {
            return FAIL_RECEIVER_TOO_MUCH_HEALTH;
        }

        adjustBaseHealthInternal(giver, -amount);
        adjustBaseHealthInternal(playerData.root, maxHealth, amount);

        OfflineUtils.putAttributes(playerData.root, attributes.values());
        if (!playerData.save()) {
            // Rollback, player data not saved.
            adjustBaseHealthInternal(giver, amount);
            return FAIL_GENERIC;
        }

        return SUCCESS;
    }

    /**
     * Checks if the health would dip below the minimum allowed.
     */
    private static boolean failsMinCheck(ServerPlayerEntity giver, int amount) {
        return giver.getMaxHealth() - amount < giver.getWorld().getGameRules().getInt(LSGameRules.MINPLAYERHEALTH);
    }

    /**
     * Checks if the health would exceed the maximum allowed.
     */
    private static boolean failsMaxCheck(ServerPlayerEntity receiver, int amount) {
        return receiver.getMaxHealth() + amount > receiver.getWorld().getGameRules().getInt(LSGameRules.MAXPLAYERHEALTH);
    }

    /**
     * Check if the health would exceed the maximum allowed.
     */
    private static boolean failsMaxCheck(EntityAttributeInstance attribute, int amount, GameRules gameRules) {
        return attribute.getBaseValue() + amount > gameRules.getInt(LSGameRules.MAXPLAYERHEALTH);
    }

    /**
     * Sets a new base health amount
     *
     * @param player     ServerPlayerEntity instance
     * @param increaseBy The amount that base health should increase by (can be negative)
     * @param server     MinecraftServer instance
     * @return Returns if the new base health succeeded
     */
    public static boolean setBaseHealth(ServerPlayerEntity player, double increaseBy, MinecraftServer server) {
        EntityAttributeInstance health = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        double current = health.getBaseValue();
        double finalHealth = current + increaseBy;

        if (finalHealth > server.getGameRules().getInt(LSGameRules.MAXPLAYERHEALTH)) {
            player.sendMessage(LsText.MAX_HEALTH, true);
            return false;
        } else if (finalHealth < server.getGameRules().getInt(LSGameRules.MINPLAYERHEALTH)) {
            return false;
        } else {

            health.setBaseValue(finalHealth);
            clampHealth(player, finalHealth);

            player.sendMessage(LsText.updateHealth(finalHealth), true);
        }

        return true;
    }

    /**
     * Adjusts and sets the new max health of a given player, reducing health if necessary.
     *
     * @param player The player having their max health adjusted.
     * @param amount The amount the player is having their health adjusted.
     * @implNote This method does not do any checks as to whether the new value is valid.
     */
    private static void adjustBaseHealthInternal(ServerPlayerEntity player, double amount) {
        EntityAttributeInstance health = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        double finalHealth = health.getBaseValue() + amount;
        health.setBaseValue(finalHealth);

        clampHealth(player, finalHealth);

        player.sendMessage(LsText.updateHealth(finalHealth), true);
    }

    /**
     * Adjusts and sets the new max health of a given player, reducing health if necessary.
     *
     * @param playerData The data of the player having their max health adjusted.
     * @param health     The attribute instance of health.
     * @param amount     The amount the player is having their health adjusted.
     * @implNote This method does not do any checks as to whether the new value is valid.
     */
    private static void adjustBaseHealthInternal(NbtCompound playerData, EntityAttributeInstance health, double amount) {
        double finalHealth = health.getBaseValue() + amount;
        health.setBaseValue(finalHealth);

        // Clamps Health.
        playerData.putFloat("Health", Math.min(playerData.getFloat("Health"), (float) finalHealth));
    }

    /**
     * Sets the new max health of a given player, reducing health if necessary.
     *
     * @param player The player having their max health adjusted.
     * @param value  The new value for health.
     * @implNote This method does not do any checks as to whether the new value is valid.
     */
    public static void setExactBaseHealth(ServerPlayerEntity player, double value) {
        EntityAttributeInstance health = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        health.setBaseValue(value);

        clampHealth(player, value);
    }

    /**
     * Clamps the health of the player to max or maxHealth.
     *
     * @param player The player having their health clamped.
     * @param max    The maximum health allowed to be set.
     */
    private static void clampHealth(ServerPlayerEntity player, double max) {
        float maxHealth = Math.min((float) max, player.getMaxHealth());
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    /**
     * Turns health into physical heart items
     *
     * @param player ServerPlayerEntity that gets the health converted
     * @param hearts Number of hearts (2HP) to convert
     * @param server MinecraftServer instance
     */
    public static void convertHealthToHeartItems(ServerPlayerEntity player, int hearts, MinecraftServer server) {
        final int health = hearts * server.getGameRules().getInt(LSGameRules.HEARTBONUS);
        if (PlayerUtils.setBaseHealth(player, -health, server)) {

            if (player.giveItemStack(new ItemStack(ModItems.HEART, hearts))) {
                player.sendMessage(LsText.withdrawnHealth(health, hearts));
            } else {
                player.getWorld().spawnEntity(new ItemEntity(player.getWorld(), player.getX(), player.getY(), player.getZ(), new ItemStack(ModItems.HEART, hearts)));
            }
        } else {
            player.sendMessage(LsText.LOW_HEALTH, true);
        }
    }
}
