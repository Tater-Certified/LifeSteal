package com.github.tatercertified.lifesteal.util;

import com.github.tatercertified.lifesteal.item.ModItems;
import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import com.github.tatercertified.lifesteal.world.nbt.NBTStorage;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

public final class PlayerUtils {

    /**
     * Checks to see if the UUID is on the dead players list
     * @param uuid ServerPlayerEntity's UUID
     * @param server MinecraftServer instance
     * @return boolean of whether the UUID is present in the list
     */
    public static boolean isPlayerDead(UUID uuid, MinecraftServer server) {
        NBTStorage storage = NBTStorage.getServerState(server);

        for (int i = 0; i < storage.deadPlayers.size(); i++) {
            if (storage.deadPlayers.get(i).asString().equals(uuid.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a player's UUID to the list of dead players
     * @param uuid ServerPlayerEntity's UUID
     * @param server MinecraftServer instance
     */
    public static void addPlayerToDeadList(UUID uuid, MinecraftServer server) {
        NBTStorage storage = NBTStorage.getServerState(server);
        NbtString idString = NbtString.of(uuid.toString());
        storage.deadPlayers.add(idString);
    }

    /**
     * Iterates through the list of dead players and remove the specified UUID from it
     * @param uuid ServerPlayerEntity's UUID
     * @param server MinecraftServer instance
     */
    public static void removePlayerFromDeadList(UUID uuid, MinecraftServer server) {
        NBTStorage storage = NBTStorage.getServerState(server);
        NbtString idString = NbtString.of(uuid.toString());

        for (int i = 0; i < storage.deadPlayers.size(); i++) {
            if (storage.deadPlayers.get(i).equals(idString)) {
                storage.deadPlayers.remove(storage.deadPlayers.get(i));
                return;
            }
        }
    }

    /**
     * Exchanges 'amount' of health points between two players
     * @param giver The player who is giving health
     * @param uuid The player who is receiving health
     * @param amount The amount of health points that is being exchanged
     * @param server MinecraftServer instance
     */
    public static void exchangeHealth(ServerPlayerEntity giver, UUID uuid, int amount, MinecraftServer server) {
        Optional<ServerPlayerEntity> receiverOp = OfflineUtils.getPlayer(server, uuid);

        if (receiverOp.isPresent()) {
            ServerPlayerEntity receiver = receiverOp.get();

            if (!OfflineUtils.isPlayerOnline(receiver, server)) {
                return;
            }

            float giverHealth = giver.getMaxHealth();

            // Check if the transaction can even occur:
            if (giverHealth - amount < server.getGameRules().getInt(LSGameRules.MINPLAYERHEALTH)) {
                giver.sendMessage(Text.literal(Config.GIVER_TOO_LITTLE_HEALTH));
                return;
            }

            // TODO Fix offline health
            /*
            NbtCompound receiverData = OfflineUtils.getPlayerData(server, receiver);
            if (OfflineUtils.getHealth(receiverData) + amount > server.getOverworld().getGameRules().getInt(LSGameRules.MAXPLAYERHEALTH)) {
                giver.sendMessage(Text.literal("This player cannot receive this much health"));
                return;
            }
             */

            if (receiver.getMaxHealth() + amount > server.getGameRules().getInt(LSGameRules.MAXPLAYERHEALTH)) {
                giver.sendMessage(Text.literal(Config.RECEIVER_TOO_MUCH_HEALTH));
                return;
            }


            // Passed checks, let's exchange health!
            setBaseHealth(receiver, amount, server);
            setBaseHealth(giver, -amount, server);
        }
    }

    /**
     * Sets a new base health amount
     * @param player ServerPlayerEntity instance
     * @param increaseBy The amount that base health should increase by (can be negative)
     * @param server MinecraftServer instance
     * @return Returns if the new base health succeeded
     */
    public static boolean setBaseHealth(ServerPlayerEntity player, double increaseBy, MinecraftServer server) {
        boolean online = OfflineUtils.isPlayerOnline(player, server);

        EntityAttributeInstance health = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        double current = health.getBaseValue();
        double finalHealth = current + increaseBy;

        if (finalHealth > server.getGameRules().getInt(LSGameRules.MAXPLAYERHEALTH)) {
            if (online) {
                player.sendMessage(Text.of(Config.MAX_HEALTH_REACHED), true);
            }
            return false;
        } else if (finalHealth < server.getGameRules().getInt(LSGameRules.MINPLAYERHEALTH)) {
            return false;
        } else {

            if (player.getHealth() > finalHealth) {
                player.setHealth((float) finalHealth);
            }
            health.setBaseValue(finalHealth);

            if (online) {
                player.sendMessage(Text.of(Config.HEALTH_INFO_MESSAGE + finalHealth), true);
            }
        }

        return true;
    }

    /**
     * Turns health into physical heart items
     *
     * @param player ServerPlayerEntity that gets the health converted
     * @param hearts Number of hearts (2HP) to convert
     * @param server MinecraftServer instance
     */
    public static void convertHealthToHeartItems(ServerPlayerEntity player, int hearts, MinecraftServer server) {
        if (PlayerUtils.setBaseHealth(player, -(hearts * server.getGameRules().getInt(LSGameRules.HEARTBONUS)), server)) {
            player.giveItemStack(new ItemStack(ModItems.HEART, hearts));
            player.getInventory().updateItems();
            player.sendMessage(Text.of(Config.HEART_TRADED));
        } else {
            player.sendMessage(Text.of(Config.GIVER_TOO_LITTLE_HEALTH), true);
        }
    }
}
