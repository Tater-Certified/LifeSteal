package com.github.tatercertified.lifesteal.world.gamerules;

import com.github.tatercertified.lifesteal.Loader;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public final class LSGameRules {
    public static void init() {}

    /**
     * If true: Players only get base health removed by player kills
     * If false: Players get health removed from any death
     */
    public static final GameRules.Key<GameRules.BooleanRule> PLAYERRELATEDONLY = GameRuleRegistry.register(Loader.MOD_ID + ":playerKillOnly", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

    /**
     * Whether to "ban" players when they receive a base health of 0.
     * This overrides 'spectateWhenMinHealth'
     */
    public static final GameRules.Key<GameRules.BooleanRule> BANWHENMINHEALTH = GameRuleRegistry.register(Loader.MOD_ID + ":banWhenMinHealth", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

    /**
     * Whether to put players into spectator if they receive a base health of 0
     */
    public static final GameRules.Key<GameRules.BooleanRule> SPECTATORWHENMINHEALTH = GameRuleRegistry.register(Loader.MOD_ID + ":spectateWhenMinHealth", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    /**
     * Whether to allow gifting hearts to other players, via the command or altar.
     */
    public static final GameRules.Key<GameRules.BooleanRule> GIFTHEARTS = GameRuleRegistry.register(Loader.MOD_ID + ":giftHearts", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

    /**
     * Whether to allow creating an altar to revive and exchange player hearts for heart crystal items.
     * Disabling this effectively disables trading and revival, with the exception of the /gift command
     */
    public static final GameRules.Key<GameRules.BooleanRule> ALTARS = GameRuleRegistry.register(Loader.MOD_ID + ":enableAltars", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

    /**
     * Whether to disable getting getting "free" hearts from killing people with 1 HP.
     * This can prevent spawn camping and harvesting tons of hearts from teammates
     */
    public static final GameRules.Key<GameRules.BooleanRule> ANTIHEARTDUPE = GameRuleRegistry.register(Loader.MOD_ID + ":enableAntiHeartDupe", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

    /**
     * The amount of health "stolen" from players when other players kill them
     */
    public static final GameRules.Key<GameRules.IntRule> STEALAMOUNT = GameRuleRegistry.register(Loader.MOD_ID + ":stealAmount", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(2));

    /**
     * If this value is not set to 0, the players that die with the minimum amount of health will respawn with the minimum amount of health
     */
    public static final GameRules.Key<GameRules.IntRule> MINPLAYERHEALTH = GameRuleRegistry.register(Loader.MOD_ID + ":minPlayerHealth", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(0));

    /**
     * The max amount of health a player can obtain
     */
    public static final GameRules.Key<GameRules.IntRule> MAXPLAYERHEALTH = GameRuleRegistry.register(Loader.MOD_ID + ":maxPlayerHealth", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(40));

    /**
     * The amount of health received from heart crystals
     */
    public static final GameRules.Key<GameRules.IntRule> HEARTBONUS = GameRuleRegistry.register(Loader.MOD_ID + ":healthPerUse", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(2));
}
