package com.github.tatercertified.lifesteal.utils;

import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;

public interface CraftedHeartsInterface {
    int getHeartsCrafted();
    void resetHeartsCrafted();
    void incrementHeartsCrafted();

    ServerPlayer getInstance();

    /**
     * If the player can craft a new heart item
     * @return If the player can crate a heart
     */
    default boolean canCraftHeart() {
        ServerPlayer player = getInstance();
        GameRules gameRules = player.level().getGameRules();
        int amount = gameRules.get(LifeStealGamerules.LIMITED_CRAFTING_AMOUNT);
        switch (gameRules.get(LifeStealGamerules.LIMITED_CRAFTING_TYPE)) {
            case FOREVER, UNTIL_BANNED -> {
                return getHeartsCrafted() < amount;
            }
            case HEART_BASED -> {
                return amount > PlayerUtils.getMaxHearts(player);
            }
            default -> {
                return true;
            }
        }
    }
}
