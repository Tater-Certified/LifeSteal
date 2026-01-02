package com.github.tatercertified.lifesteal.gamerules;

import com.github.tatercertified.lifesteal.Lifesteal;
import com.github.tatercertified.lifesteal.utils.LifeStealText;
import com.nerjal.unruled_api.UnruledApi;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.NotNull;

public final class LifeStealGamerules {
    public static MinecraftServer serverInstance;
    public static void init() {
        // TODO Temp fix until SyncedBoundedIntRule is fixed
        GameRuleEvents.changeCallback(STEALAMOUNT).register((value, server) -> {
            boolean minPlayerEven = server.overworld().getGameRules().get(MINPLAYERHEALTH) % 2 == 0;
            boolean stealEven = value % 2 == 0;
            if (stealEven != minPlayerEven) {
                server.overworld().getGameRules().set(MINPLAYERHEALTH, value + 1, server);
                server.sendSystemMessage(LifeStealText.MIN_PLAYER_HEALTH_ADJUST);
            }
        });

        GameRuleEvents.changeCallback(MINPLAYERHEALTH).register((value, server) -> {
            boolean stealEven = server.overworld().getGameRules().get(STEALAMOUNT) % 2 == 0;
            boolean minPlayerEven = value % 2 == 0;
            if (stealEven != minPlayerEven) {
                server.overworld().getGameRules().set(MINPLAYERHEALTH, value + 1, server);
                server.sendSystemMessage(LifeStealText.MIN_PLAYER_HEALTH_ADJUST);
            }
        });
    }

    /**
     * What criteria must be met in order for hearts to be removed from the player
     */
    public static final GameRule<@NotNull DeathCriteria> DEATH_CRITERIA = GameRuleBuilder.forEnum(DeathCriteria.PLAYER_ONLY)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "death_criteria"));

    /**
     * The action to take when the player goes below the allowed minimum health as defined by {@link #MINPLAYERHEALTH}
     */
    public static final GameRule<@NotNull DeathAction> DEATH_ACTION = GameRuleBuilder.forEnum(DeathAction.BAN)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "death_action"));

    /**
     * The method required to revive a player
     */
    public static final GameRule<@NotNull ReviveMethod> REVIVE_METHOD = GameRuleBuilder.forEnum(ReviveMethod.ALTAR)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "revive_method"));

    /**
     * The method required to gift a heart
     */
    public static final GameRule<@NotNull GiftMethod> GIFT_METHOD = GameRuleBuilder.forEnum(GiftMethod.MANUAL)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "gift_method"));

    /**
     * The method required to withdraw a heart to a heart item
     */
    public static final GameRule<@NotNull WithdrawMethod> WITHDRAW_METHOD = GameRuleBuilder.forEnum(WithdrawMethod.ALTAR)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "withdraw_method"));

    /**
     * Whether to disable getting "free" hearts from killing people with the minimum HP.
     * This can prevent spawn camping and harvesting tons of hearts from teammates
     */
    public static final GameRule<@NotNull Boolean> ANTIHEARTDUPE = GameRuleBuilder.forBoolean(true)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "enable_anti_heart_dupe"));

    /**
     * The amount of health "stolen" from players when other players kill them.
     */
    public static final GameRule<@NotNull Integer> STEALAMOUNT = GameRuleBuilder.forInteger(2).minValue(0)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "steal_amount"));

    /**
     * This value determines the threshold for being considered "dead".
     * If a player reaches lower than this value, they will be categorized as dead unless BanWhenMaxHealth is disabled
     * If StealAmount is a multiple of 2, so should this value
     */
    // TODO Enforce multiple of 2 using events
    // TODO Fix SyncedBoundedIntRule
    public static final GameRule<@NotNull Integer> MINPLAYERHEALTH = GameRuleBuilder.forInteger(2).minValue(1)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "min_player_health"));



    /**
     * The max amount of health a player can obtain
     */
    public static final GameRule<@NotNull Integer> MAXPLAYERHEALTH = GameRuleBuilder.forInteger(40).minValue(1)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "max_player_health"));

    /**
     * The amount of health received from heart crystals
     */
    public static final GameRule<@NotNull Integer> HEARTBONUS = GameRuleBuilder.forInteger(2).minValue(0)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "health_from_heart"));


    /**
     * The block that is to be used as the altar
     */
    public static final GameRule<Holder.Reference<Block>> ALTAR_BLOCK = UnruledApi.dynamicRegistryEntryRuleBuilder(GameRuleCategory.MISC, Registries.BLOCK, BuiltInRegistries.BLOCK.getKey(Blocks.NETHERITE_BLOCK))
            .setChangeCallback((minecraftServer, gameRule, blockReference) -> cachedAltarBlock = blockReference.value())
            .setRequiredFeatures(FeatureFlagSet.of())
            .register(Identifier.tryBuild(Lifesteal.MOD_ID, "altar_block"));


    /**
     * The amount of seconds until the player is automatically revived
     * Setting this to 0 will disable auto-revival
     */
    public static final GameRule<@NotNull Integer> AUTOREVIVAL = GameRuleBuilder.forInteger(0).minValue(0)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "auto_revival_seconds"));

    /**
     * The amount of time a player is invulnerable after being revived in seconds
     * The default value is 0 seconds, which disables the feature
     */
    public static final GameRule<@NotNull Integer> RESPAWN_INVULNERABILITY = GameRuleBuilder.forInteger(0).minValue(0)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "revival_invulnerability_seconds"));

    /**
     * The maximum stack size of the heart item
     */
    public static final GameRule<@NotNull Integer> HEART_STACK_SIZE = GameRuleBuilder.forInteger(1).range(1, 64)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "heart_stack_size"));

    private static Block cachedAltarBlock;

    public static Block getAltarBlock(GameRules gameRules) {
        if (cachedAltarBlock == null) {
            cachedAltarBlock = gameRules.get(ALTAR_BLOCK).value();
        }
        return cachedAltarBlock;
    }
}
