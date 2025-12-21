package com.github.tatercertified.lifesteal.utils;

import com.github.tatercertified.lifesteal.Lifesteal;
import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.gamerules.DeathAction;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.items.ModItems;
import com.github.tatercertified.lifesteal.mixin.ServerPlayerServerAccessor;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;

public final class PlayerUtils {

    /**
     * Handles how the server should process a dead player.
     * Options include banning them, immediately reviving them, or putting them in spectator
     * @param player ServerPlayerEntity that died
     * @param data The player's DeathData
     */
    public static void handleDeadPlayerAction(ServerPlayer player, DeathData data) {
        GameRules gameRules = player.level().getGameRules();
        DeathAction action = gameRules.get(LifeStealGamerules.DEATH_ACTION);
        switch (action) {
            case BAN -> {
                if (gameRules.get(LifeStealGamerules.AUTOREVIVAL) == 0) {
                    player.connection.disconnect(LifeStealText.DEATH);
                } else {
                    player.connection.disconnect(LifeStealText.deathTime((int) (data.deathTime + gameRules.get(LifeStealGamerules.AUTOREVIVAL) - (System.currentTimeMillis() * 0.001))));
                }
            }
            case REVIVE -> {
                setMaxHealth(gameRules.get(LifeStealGamerules.MINPLAYERHEALTH), player);
                DeathData.removeFromDeathDataList(player.getUUID()); // I know this is a waste of processing power... but I don't care
                ((PlayerReviveData)player).setNewlyRevived(true); // Prevent heart duplication
            }
            case SPECTATOR -> {
                player.setGameMode(GameType.SPECTATOR);
                ((PlayerGameModeInterface)(player.gameMode)).setPreviousGameMode(GameType.SPECTATOR);
            }
        }
    }

    /**
     * Handles the player joining the server.
     * It determines if the player should be revived or not
     * @param player ServerPlayerEntity that joined
     */
    public static void handlePlayerJoin(ServerPlayer player) {
        DeathData data = Lifesteal.DEAD_PLAYERS.get(player.getUUID());
        if (data != null) {
            // A reviver takes highest priority
            if (data.reviverPlayerID == null) {
                if (DeathData.shouldAutoRevive(data, player.level().getGameRules().get(LifeStealGamerules.AUTOREVIVAL))) {
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
    private static void handlePostRevival(DeathData data, ServerPlayer player, boolean autoRevived) {
        GameRules gameRules = player.level().getGameRules();
        if (!autoRevived) {
            player.sendSystemMessage(LifeStealText.onRevivalText(data, ((ServerPlayerServerAccessor) player).getServer()));
        } else {
            // Autorevived players shouldn't be exempted from the antiHeartDupe
            ((PlayerReviveData)player).setNewlyRevived(true);
        }
        // Check if invulnerability should be applied
        int invulnerability = gameRules.get(LifeStealGamerules.RESPAWN_INVULNERABILITY);
        if (invulnerability != 0) {
            ((PlayerInvulnerabilityInterface)player).setReviveInvulnerability();
        }
        DeathData.removeFromDeathDataList(player.getUUID());
    }

    /**
     * Exchanges max health between the attacker and player being attacked.
     * This function respects the gamerule AntiHeartDupe, meaning that hearts are not exchanged if the player being killed
     * is already at the minimum health after being revived through automated means (not with a heart).
     * Additionally, hearts are given as items if the attacker is already at max health
     * @param killed The ServerPlayerEntity that was killed
     * @param attacker The ServerPlayerEntity that killed the other player
     */
    public static void exchangeHealth(ServerPlayer killed, ServerPlayer attacker) {
        // Killed Player
        AttributeInstance killedMaxHealth = killed.getAttribute(Attributes.MAX_HEALTH);
        GameRules gameRules = killed.level().getGameRules();
        double killedMaxHealthDouble = killedMaxHealth.getBaseValue();

        int minHealth = gameRules.get(LifeStealGamerules.MINPLAYERHEALTH);
        if (killedMaxHealthDouble <= minHealth) {
            // Considered dead
            DeathData data = new DeathData(killed.getUUID());
            data.addToDeathDataList();
            handleDeadPlayerAction(killed, data);

            // Check to see if heart should be rewarded
            if (((PlayerReviveData)killed).newlyRevived() && gameRules.get(LifeStealGamerules.ANTIHEARTDUPE)) {
                return;
            }
        } else {
            changeHealthUnchecked(killed, -gameRules.get(LifeStealGamerules.STEALAMOUNT));
        }

        // Attacker Player
        if (!changeHealth(attacker, gameRules.get(LifeStealGamerules.STEALAMOUNT))) {
            // They can't get more health, but they can still get an item to prevent heart deletion
            attacker.displayClientMessage(LifeStealText.MAX_HEALTH, true);
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
        return newMaxHealth >= gameRules.get(LifeStealGamerules.MINPLAYERHEALTH) && newMaxHealth <= gameRules.get(LifeStealGamerules.MAXPLAYERHEALTH);
    }

    /**
     * Changes the max health of a player, then sends a message telling how much it was updated by
     * @param player ServerPlayerEntity whose health is being changed
     * @param by The amount that the max health should be changed by (can be positive or negative)
     */
    public static void changeHealthUnchecked(ServerPlayer player, float by) {
        double currentValue = ((PlayerMaxHealthInterface)player).getBaseMaxHealth();
        ((PlayerMaxHealthInterface)player).setBaseMaxHealth(currentValue + by);
        float health = player.getHealth();
        player.setHealth(health + by);
        // If they can change health without dying, they aren't newly revived anymore
        ((PlayerReviveData)player).setNewlyRevived(false);
        player.displayClientMessage(LifeStealText.updateHealth(by), true);
    }

    /**
     * Changes the max health of the player, ensuring that there is enough health to change
     * @param player ServerPlayerEntity whose health is being changed
     * @param by The amount the max health should be changed by (can be positive or negative)
     * @return If the change succeeded. If the player will "die", then returns false.
     */
    public static boolean changeHealth(ServerPlayer player, float by) {
        double maxHealth = ((PlayerMaxHealthInterface)player).getBaseMaxHealth();
        if (canChangeHealth(maxHealth, by, player.level().getGameRules())) {
            changeHealthUnchecked(player, by);
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
    public static void setMaxHealth(double value, ServerPlayer player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        maxHealth.setBaseValue(value);
    }

    /**
     * Turns health into physical heart items
     * @param player ServerPlayerEntity that gets the health converted
     * @param hearts Number of hearts (2HP) to convert
     */
    public static void convertHealthToHeartItems(ServerPlayer player, int hearts, boolean action) {

        final int health = hearts * player.level().getGameRules().get(LifeStealGamerules.HEARTBONUS);
        // Detect overflow
        if (health < 0) {
            player.displayClientMessage(LifeStealText.LOW_HEALTH, action);
            return;
        }
        if(health == 0) {
            player.displayClientMessage(LifeStealText.HEART_DISABLED, action);
            return;
        }

        if (!PlayerUtils.changeHealth(player, -health)) {
            player.displayClientMessage(LifeStealText.LOW_HEALTH, action);
            return;
        }

        givePlayerHeart(player, hearts);
        player.displayClientMessage(LifeStealText.withdrawnHealth(health, hearts), action);
    }

    /**
     * Gives the player the specified number of hearts
     * @param player ServerPlayerEntity that gets the hearts
     * @param hearts Number of hearts
     */
    private static void givePlayerHeart(ServerPlayer player, int hearts) {
        final ItemStack heartStack = new ItemStack(ModItems.HEART, 1);
        for (int i = 0; i < hearts; i++) {
            if (!player.addItem(heartStack.copy())) {
                // Quick path for dropping the rest of the hearts to avoid unnecessary checks
                for (int j = i; j < hearts; j++) {
                    player.drop(heartStack.copy(), false, true);
                }
                break;
            }
        }
    }
}
