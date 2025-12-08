package com.github.tatercertified.lifesteal.gamerules;

import com.github.tatercertified.lifesteal.Lifesteal;
import com.github.tatercertified.lifesteal.utils.LifeStealText;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;

public final class LifeStealGamerules {
    public static MinecraftServer serverInstance;
    public static void init() {
        // TODO Wait for Unruled API to update
        /*
        GameRuleEvents.changeCallback(GameRules.FIRE_DAMAGE).register((value, server) -> {
            // Your code here
        });

         */

        // TODO Temp fix until SyncedBoundedIntRule is fixed
        GameRuleEvents.changeCallback(STEALAMOUNT).register((value, server) -> {
            boolean minPlayerEven = server.getOverworld().getGameRules().getValue(MINPLAYERHEALTH) % 2 == 0;
            boolean stealEven = value % 2 == 0;
            if (stealEven != minPlayerEven) {
                server.getOverworld().getGameRules().setValue(MINPLAYERHEALTH, value + 1, server);
                server.sendMessage(LifeStealText.MIN_PLAYER_HEALTH_ADJUST);
            }
        });

        GameRuleEvents.changeCallback(MINPLAYERHEALTH).register((value, server) -> {
            boolean stealEven = server.getOverworld().getGameRules().getValue(STEALAMOUNT) % 2 == 0;
            boolean minPlayerEven = value % 2 == 0;
            if (stealEven != minPlayerEven) {
                server.getOverworld().getGameRules().setValue(MINPLAYERHEALTH, value + 1, server);
                server.sendMessage(LifeStealText.MIN_PLAYER_HEALTH_ADJUST);
            }
        });
    }

    /**
     * What criteria must be met in order for hearts to be removed from the player
     */
    public static final GameRule<DeathCriteria> DEATH_CRITERIA = GameRuleBuilder.forEnum(DeathCriteria.PLAYER_ONLY)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "deathCriteria"));

    /**
     * The action to take when the player goes below the allowed minimum health as defined by {@link #MINPLAYERHEALTH}
     */
    public static final GameRule<DeathAction> DEATH_ACTION = GameRuleBuilder.forEnum(DeathAction.BAN)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "deathAction"));

    /**
     * The method required to revive a player
     */
    public static final GameRule<ReviveMethod> REVIVE_METHOD = GameRuleBuilder.forEnum(ReviveMethod.ALTAR)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "reviveMethod"));

    /**
     * The method required to gift a heart
     */
    public static final GameRule<GiftMethod> GIFT_METHOD = GameRuleBuilder.forEnum(GiftMethod.ALTAR)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "giftMethod"));

    /**
     * The method required to withdraw a heart to a heart item
     */
    public static final GameRule<WithdrawMethod> WITHDRAW_METHOD = GameRuleBuilder.forEnum(WithdrawMethod.ALTAR)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "withdrawMethod"));

    /**
     * Whether to disable getting "free" hearts from killing people with the minimum HP.
     * This can prevent spawn camping and harvesting tons of hearts from teammates
     */
    public static final GameRule<Boolean> ANTIHEARTDUPE = GameRuleBuilder.forBoolean(true)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "enableAntiHeartDupe"));

    /**
     * The amount of health "stolen" from players when other players kill them.
     */
    public static final GameRule<Integer> STEALAMOUNT = GameRuleBuilder.forInteger(2).minValue(0)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "stealAmount"));

    /**
     * This value determines the threshold for being considered "dead".
     * If a player reaches lower than this value, they will be categorized as dead unless BanWhenMaxHealth is disabled
     * If StealAmount is a multiple of 2, so should this value
     */
    // TODO Enforce multiple of 2 using events
            // TODO Fix SyncedBoundedIntRule
    public static final GameRule<Integer> MINPLAYERHEALTH = GameRuleBuilder.forInteger(2).minValue(1)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "minPlayerHealth"));



    /**
     * The max amount of health a player can obtain
     */
    public static final GameRule<Integer> MAXPLAYERHEALTH = GameRuleBuilder.forInteger(40).minValue(1)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "maxPlayerHealth"));

    /**
     * The amount of health received from heart crystals
     */
    public static final GameRule<Integer> HEARTBONUS = GameRuleBuilder.forInteger(2).minValue(0)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "healthFromHeart"));


    /**
     * The block that is to be used as the altar
     */
    // TODO Wait for Unruled API to update
            /*
    public static final GameRules.Key<RegistryEntryRule<Block>> ALTAR_BLOCK = UnruledApi.registryEntryRuleBuilder(Registries.BLOCK, Blocks.NETHERITE_BLOCK)
            .setChangeCallback((server, blockRegistryEntryRule) -> altarGameRuleModified = true)
            .setFeatureSet(FeatureSet.empty())
            .register(Lifesteal.MOD_ID + ":altarBlock", GameRules.Category.MISC);

             */


    /**
     * The amount of seconds until the player is automatically revived
     * Setting this to 0 will disable auto-revival
     */
    public static final GameRule<Integer> AUTOREVIVAL = GameRuleBuilder.forInteger(0).minValue(0)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "autoRevivalSeconds"));

    /**
     * The amount of time a player is invulnerable after being revived in seconds
     * The default value is 0 seconds, which disables the feature
     */
    public static final GameRule<Integer> RESPAWN_INVULNERABILITY = GameRuleBuilder.forInteger(0).minValue(0)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "revivalInvulnerabilitySeconds"));

    /**
     * The maximum stack size of the heart item
     */
    public static final GameRule<Integer> HEART_STACK_SIZE = GameRuleBuilder.forInteger(1).range(1, 64)
            .buildAndRegister(Identifier.of(Lifesteal.MOD_ID, "heartStackSize"));

    public static boolean altarGameRuleModified = true;
    private static Block cachedAltarBlock;

    // TODO Wait for Unruled API to update
    /*
    public static Block getBlockFromGameRule(GameRules gameRules) {
        if (altarGameRuleModified) {
            cachedAltarBlock = ((IGameRulesProvider)gameRules).unruled_getRegistryEntry(ALTAR_BLOCK);
            altarGameRuleModified = !altarGameRuleModified;
        }
        return cachedAltarBlock;
    }

     */
}
