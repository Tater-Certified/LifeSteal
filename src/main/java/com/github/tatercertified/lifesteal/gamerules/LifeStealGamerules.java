package com.github.tatercertified.lifesteal.gamerules;

import com.github.tatercertified.lifesteal.Lifesteal;
import com.nerjal.unruled_api.UnruledApi;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.*;
import org.jetbrains.annotations.NotNull;

public final class LifeStealGamerules {
    public static MinecraftServer serverInstance;
    public static void init() {}

    /**
     * What criteria must be met in order for hearts to be removed from the player
     */
    public static final GameRule<@NotNull DeathCriteria> DEATH_CRITERIA = GameRuleBuilder.forEnum(DeathCriteria.PLAYER_ONLY)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "death_criteria"));

    /**
     * The action to take when the player goes below the allowed minimum health as defined by {@link #MIN_PLAYER_HEARTS}
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
    public static final GameRule<@NotNull Boolean> ANTI_HEART_DUPE = GameRuleBuilder.forBoolean(true)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "enable_anti_heart_dupe"));

    /**
     * The number of hearts "stolen" from players when other players kill them.
     */
    public static final GameRule<@NotNull Integer> STEAL_AMOUNT = GameRuleBuilder.forInteger(1).minValue(0)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "steal_amount"));

    /**
     * This value determines the threshold for being considered "dead".
     * If a player reaches lower than this value, they will be categorized as dead
     */
    public static final GameRule<@NotNull Integer> MIN_PLAYER_HEARTS = GameRuleBuilder.forInteger(1).minValue(1)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "min_player_hearts"));

    /**
     * The max amount of hearts a player can obtain
     */
    public static final GameRule<@NotNull Integer> MAX_PLAYER_HEARTS = GameRuleBuilder.forInteger(10).minValue(1)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "max_player_hearts"));


    /**
     * The block that is to be used as the altar
     */
    public static final GameRule<Holder.Reference<Block>> ALTAR_BLOCK = UnruledApi.staticRegistryEntryRuleBuilder(GameRuleCategory.MISC, BuiltInRegistries.BLOCK, Blocks.NETHERITE_BLOCK)
            .setChangeCallback((minecraftServer, gameRule, blockReference) -> cachedAltarBlock = blockReference.value())
            .setRequiredFeatures(FeatureFlagSet.of())
            .register(Identifier.tryBuild(Lifesteal.MOD_ID, "altar_block"));

    /**
     * The amount of seconds until the player is automatically revived
     * Setting this to 0 will disable auto-revival
     */
    public static final GameRule<@NotNull Integer> AUTO_REVIVAL = GameRuleBuilder.forInteger(0).minValue(0)
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

    /**
     * If a heart can be crafted in a crafter
     */
    public static final GameRule<@NotNull Boolean> HEART_CRAFT_IN_CRAFTER = GameRuleBuilder.forBoolean(false)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "heart_craft_in_crafter"));

    /**
     * The type of limited crafting of hearts
     */
    public static final GameRule<@NotNull LimitedCraftingType> LIMITED_CRAFTING_TYPE = GameRuleBuilder.forEnum(LimitedCraftingType.NONE)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "limited_heart_crafting_type"));

    /**
     * The amount of hearts that can be crafted before limited
     */
    public static final GameRule<@NotNull Integer> LIMITED_CRAFTING_AMOUNT = GameRuleBuilder.forInteger(0).minValue(0)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "limited_heart_crafting_amount"));

    /**
     * Whether to do basic altar functions or fancy animations
     */
    public static final GameRule<@NotNull Boolean> DO_ALTAR_ANIMATIONS = GameRuleBuilder.forBoolean(true)
            .buildAndRegister(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "altar_animations"));

    private static Block cachedAltarBlock;

    public static Block getAltarBlock(GameRules gameRules) {

        if (cachedAltarBlock == null) {
            cachedAltarBlock = gameRules.get(ALTAR_BLOCK).value();
        }
        return cachedAltarBlock;
    }
}
