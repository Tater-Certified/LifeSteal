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

    public static void exchangeHealth(ServerPlayerEntity killed, ServerPlayerEntity attacker) {
        // Killed Player
       EntityAttributeInstance killedMaxHealth = killed.getAttributeInstance(EntityAttributes.MAX_HEALTH);
       GameRules gameRules = killed.getServerWorld().getGameRules();
       double killedMaxHealthDouble = killedMaxHealth.getBaseValue();

       int minHealth = gameRules.getInt(LifeStealGamerules.MINPLAYERHEALTH);
       if (killedMaxHealthDouble <= minHealth) {
           // Considered dead
           DeathData data = new DeathData(killed.getUuid());
           data.addToDeathDataList();
           handleDeadPlayerAction(killed, data);

           // Check to see if spawn camping is happening
           if (killedMaxHealthDouble < minHealth && gameRules.getBoolean(LifeStealGamerules.ANTIHEARTDUPE)) {
               return;
           }
       } else {
           changeHealth(killed, killedMaxHealth, -gameRules.getInt(LifeStealGamerules.STEALAMOUNT));
       }

       // Attacker Player
       if (!changeHealth(attacker, gameRules.getInt(LifeStealGamerules.STEALAMOUNT))) {
           attacker.sendMessage(LifeStealText.MAX_HEALTH, true);
       }
    }

    public static void changeHealth(ServerPlayerEntity player, EntityAttributeInstance attribute, float by) {
        double currentValue = attribute.getValue();
        attribute.setBaseValue(currentValue + by);
        float health = player.getHealth();
        player.setHealth(health + by);
        player.sendMessage(LifeStealText.updateHealth(by), true);
    }

    public static boolean changeHealth(ServerPlayerEntity player, float by) {
        EntityAttributeInstance maxHealthAttribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        double maxHealth = maxHealthAttribute.getBaseValue();
        if (canChangeHealth(maxHealth, by, player.getServerWorld().getGameRules())) {
            changeHealth(player, maxHealthAttribute, by);
            return true;
        } else {
            return false;
        }
    }

    public static boolean canChangeHealth(double currentMaxHealth, float by, GameRules gameRules) {
        double newMaxHealth = currentMaxHealth + by;
        return newMaxHealth >= gameRules.getInt(LifeStealGamerules.MINPLAYERHEALTH) && newMaxHealth <= gameRules.getInt(LifeStealGamerules.MAXPLAYERHEALTH);
    }

    public static void handleDeadPlayerAction(ServerPlayerEntity player, DeathData data) {
        GameRules gameRules = player.getServerWorld().getGameRules();
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
                setMaxHealth(gameRules.getInt(LifeStealGamerules.MINPLAYERHEALTH) - 0.01, player); // It's basically Min Health... Right?
                DeathData.removeFromDeathDataList(player.getUuid()); // I know this is a waste of processing power... but I don't care
            }
            case SPECTATOR -> player.changeGameMode(GameMode.SPECTATOR);
        }
    }

    public static void handlePlayerJoin(ServerPlayerEntity player) {
        DeathData data = Lifesteal.DEAD_PLAYERS.get(player.getUuid());
        if (data != null) {
            // Check and see if they can be revived
            if (DeathData.shouldAutoRevive(data, player.getServerWorld().getGameRules().getInt(LifeStealGamerules.AUTOREVIVAL))) {
                handlePostRevival(data, player, true);
                return;
            }
            // Else do traditional checks
            if (data.reviverPlayerID == null) {
                handleDeadPlayerAction(player, data);
            } else {
                handlePostRevival(data, player, false);
            }
        }
    }

    public static void handlePostRevival(DeathData data, ServerPlayerEntity player, boolean autoRevived) {
        GameRules gameRules = player.getServerWorld().getGameRules();
        setMaxHealth(gameRules.getInt(LifeStealGamerules.MINPLAYERHEALTH), player);
        if (!autoRevived) {
            player.sendMessage(LifeStealText.onRevivalText(data, player.server));
        }
        DeathData.removeFromDeathDataList(player.getUuid());
    }

    public static void setMaxHealth(double value, ServerPlayerEntity player) {
        EntityAttributeInstance maxHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        maxHealth.setBaseValue(value);
    }

    /**
     * Turns health into physical heart items
     *
     * @param player ServerPlayerEntity that gets the health converted
     * @param hearts Number of hearts (2HP) to convert
     * @param server MinecraftServer instance
     */
    public static void convertHealthToHeartItems(ServerPlayerEntity player, int hearts, MinecraftServer server, boolean action) {
        final int health = hearts * server.getGameRules().getInt(LifeStealGamerules.HEARTBONUS);
        if(health <= 0) {
            player.sendMessage(LifeStealText.HEART_DISABLED, action);
            return;
        }

        if (!PlayerUtils.changeHealth(player, -health)) {
            player.sendMessage(LifeStealText.LOW_HEALTH, action);
            return;
        }

        final ItemStack heartStack = new ItemStack(ModItems.HEART, hearts);
        if (!player.giveItemStack(heartStack)) {
            player.dropItem(heartStack, false, true);
        }
        player.sendMessage(LifeStealText.withdrawnHealth(health, hearts), action);
    }
}
