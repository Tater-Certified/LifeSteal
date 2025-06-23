package com.github.certifiedtater.lifesteal.utils;

import com.github.certifiedtater.lifesteal.Lifesteal;
import com.github.certifiedtater.lifesteal.data.DeathData;
import com.github.certifiedtater.lifesteal.gamerules.DeathAction;
import com.github.certifiedtater.lifesteal.gamerules.LifeStealGamerules;
import com.github.certifiedtater.lifesteal.items.ModItems;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public final class PlayerUtils {

    /**
     * Handles how the server should process a dead player.
     * Options include banning them, immediately reviving them, or putting them in spectator
     * @param player ServerPlayerEntity that died
     * @param data The player's DeathData
     */
    public static void handleDeadPlayerAction(ServerPlayerEntity player, DeathData data) {
        GameRules gameRules = player.getWorld().getGameRules();
        DeathAction action = gameRules.get(LifeStealGamerules.DEATH_ACTION).get();
        switch (action) {
            case BAN -> {
                if (gameRules.get(LifeStealGamerules.AUTOREVIVAL).get() == 0) {
                    player.networkHandler.disconnect(LifeStealText.DEATH);
                } else {
                    player.networkHandler.disconnect(LifeStealText.deathTime((int) (data.deathTime + gameRules.get(LifeStealGamerules.AUTOREVIVAL).get() - (System.currentTimeMillis() * 0.001))));
                }
            }
            case REVIVE -> {
                setMaxHealth(gameRules.getInt(LifeStealGamerules.MINPLAYERHEALTH), player);
                DeathData.removeFromDeathDataList(player.getUuid()); // I know this is a waste of processing power... but I don't care
                ((PlayerReviveData)player).setNewlyRevived(true); // Prevent heart duplication
            }
            case SPECTATOR -> {
                player.changeGameMode(GameMode.SPECTATOR);
                ((PlayerGameModeInterface)(player.interactionManager)).setPreviousGameMode(GameMode.SPECTATOR);
            }
        }
    }

    /**
     * Handles the player joining the server.
     * It determines if the player should be revived or not
     * @param player ServerPlayerEntity that joined
     */
    public static void handlePlayerJoin(ServerPlayerEntity player) {
        DeathData data = Lifesteal.DEAD_PLAYERS.get(player.getUuid());
        if (data != null) {
            // A reviver takes highest priority
            if (data.reviverPlayerID == null) {
                if (DeathData.shouldAutoRevive(data, player.getWorld().getGameRules().getInt(LifeStealGamerules.AUTOREVIVAL))) {
                    // Autorevival timer up
                    handlePostRevival(data, player, true);
                } else {
                    // Still dead
                    handleDeadPlayerAction(player, data);
                }
            } else {
                // Reviver found
                handlePostRevival(data, player, false);
            }
        }
    }

    /**
     * Handles the revival of a player when they log back in
     * @param data DeathData of the player
     * @param player ServerPlayerEntity that was revived
     * @param autoRevived If the player was revived due to the automatic revival system
     */
    private static void handlePostRevival(DeathData data, ServerPlayerEntity player, boolean autoRevived) {
        GameRules gameRules = player.getWorld().getGameRules();
        if (!autoRevived) {
            player.sendMessage(LifeStealText.onRevivalText(data, player.getServer()));
        } else {
            // Autorevived players shouldn't be exempted from the antiHeartDupe
            ((PlayerReviveData)player).setNewlyRevived(true);
        }
        // Check if invulnerability should be applied
        int invulnerability = gameRules.getInt(LifeStealGamerules.RESPAWN_INVULNERABILITY);
        if (invulnerability != 0) {
            ((PlayerInvulnerabilityInterface)player).setReviveInvulnerability();
        }
        DeathData.removeFromDeathDataList(player.getUuid());
    }

    /**
     * Exchanges max health between the attacker and player being attacked.
     * This function respects the gamerule AntiHeartDupe, meaning that hearts are not exchanged if the player being killed
     * is already at the minimum health after being revived through automated means (not with a heart).
     * Additionally, hearts are given as items if the attacker is already at max health
     * @param killed The ServerPlayerEntity that was killed
     * @param attacker The ServerPlayerEntity that killed the other player
     */
    public static void exchangeHealth(ServerPlayerEntity killed, ServerPlayerEntity attacker) {
        // Killed Player
        EntityAttributeInstance killedMaxHealth = killed.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        GameRules gameRules = killed.getWorld().getGameRules();
        double killedMaxHealthDouble = killedMaxHealth.getBaseValue();

        int minHealth = gameRules.getInt(LifeStealGamerules.MINPLAYERHEALTH);
        if (killedMaxHealthDouble <= minHealth) {
            // Considered dead
            DeathData data = new DeathData(killed.getUuid());
            data.addToDeathDataList();
            handleDeadPlayerAction(killed, data);

            // Check to see if heart should be rewarded
            if (((PlayerReviveData)killed).newlyRevived() && gameRules.getBoolean(LifeStealGamerules.ANTIHEARTDUPE)) {
                return;
            }
        } else {
            changeHealth(killed, killedMaxHealth, -gameRules.getInt(LifeStealGamerules.STEALAMOUNT));
        }

        // Attacker Player
        if (!changeHealth(attacker, gameRules.getInt(LifeStealGamerules.STEALAMOUNT))) {
            // They can't get more health, but they can still get an item to prevent heart deletion
            attacker.sendMessage(LifeStealText.MAX_HEALTH, true);
            givePlayerHeart(attacker, 1);
        }
    }

    /**
     * Determines if the player's health can be changed by the amount without dying or gaining too much health
     * @param currentMaxHealth The current player's max health
     * @param by The amount to change the max health by (can be positive or negative)
     * @param gameRules The server's GameRules instance
     * @return If the max health changing operation can occur
     */
    public static boolean canChangeHealth(double currentMaxHealth, float by, GameRules gameRules) {
        double newMaxHealth = currentMaxHealth + by;
        return newMaxHealth >= gameRules.getInt(LifeStealGamerules.MINPLAYERHEALTH) && newMaxHealth <= gameRules.getInt(LifeStealGamerules.MAXPLAYERHEALTH);
    }

    /**
     * Changes the max health of a player, then sends a message telling how much it was updated by
     * @param player ServerPlayerEntity whose health is being changed
     * @param attribute The Player's Attributes
     * @param by The amount that the max health should be changed by (can be positive or negative)
     */
    public static void changeHealth(ServerPlayerEntity player, EntityAttributeInstance attribute, float by) {
        double currentValue = attribute.getValue();
        attribute.setBaseValue(currentValue + by);
        float health = player.getHealth();
        player.setHealth(health + by);
        player.sendMessage(LifeStealText.updateHealth(by), true);
    }

    /**
     * Changes the max health of the player, ensuring that there is enough health to change
     * @param player ServerPlayerEntity whose health is being changed
     * @param by The amount the max health should be changed by (can be positive or negative)
     * @return If the change succeeded. If the player will "die", then returns false.
     */
    public static boolean changeHealth(ServerPlayerEntity player, float by) {
        EntityAttributeInstance maxHealthAttribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        double maxHealth = maxHealthAttribute.getBaseValue();
        if (canChangeHealth(maxHealth, by, player.getWorld().getGameRules())) {
            changeHealth(player, maxHealthAttribute, by);
            // If they can change health without dying, they aren't newly revived anymore
            ((PlayerReviveData)player).setNewlyRevived(false);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets the max health of a player
     * @param value The new max health
     * @param player The ServerPlayerEntity whose max health is changing
     */
    public static void setMaxHealth(double value, ServerPlayerEntity player) {
        EntityAttributeInstance maxHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        maxHealth.setBaseValue(value);
    }

    /**
     * Turns health into physical heart items
     * @param player ServerPlayerEntity that gets the health converted
     * @param hearts Number of hearts (2HP) to convert
     * @param server MinecraftServer instance
     */
    public static void convertHealthToHeartItems(ServerPlayerEntity player, int hearts, MinecraftServer server, boolean action) {
        // Check for overflowing
        if (hearts < 0) {
            player.sendMessage(LifeStealText.LOW_HEALTH, action);
            return;
        }

        final int health = hearts * server.getGameRules().getInt(LifeStealGamerules.HEARTBONUS);
        if(health == 0) {
            player.sendMessage(LifeStealText.HEART_DISABLED, action);
            return;
        }

        if (!PlayerUtils.changeHealth(player, -health)) {
            player.sendMessage(LifeStealText.LOW_HEALTH, action);
            return;
        }

        givePlayerHeart(player, hearts);
        player.sendMessage(LifeStealText.withdrawnHealth(health, hearts), action);
    }

    /**
     * Gives the player the specified number of hearts
     * @param player ServerPlayerEntity that gets the hearts
     * @param hearts Number of hearts
     */
    private static void givePlayerHeart(ServerPlayerEntity player, int hearts) {
        final ItemStack heartStack = new ItemStack(ModItems.HEART, 1);
        for (int i = 0; i < hearts; i++) {
            if (!player.giveItemStack(heartStack.copy())) {
                // Quick path for dropping the rest of the hearts to avoid unnecessary checks
                for (int j = i; j < hearts; j++) {
                    player.dropItem(heartStack.copy(), false, true);
                }
                break;
            }
        }
    }
}
