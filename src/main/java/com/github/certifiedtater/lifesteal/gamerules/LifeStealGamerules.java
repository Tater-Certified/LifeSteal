package com.github.certifiedtater.lifesteal.gamerules;

import com.github.certifiedtater.lifesteal.Lifesteal;
import com.github.certifiedtater.lifesteal.mixin.GameRulesTypeInvoker;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import mc.recraftors.unruled_api.rules.RegistryEntryRule;
import mc.recraftors.unruled_api.utils.IGameRulesProvider;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public final class LifeStealGamerules {
    public static MinecraftServer serverInstance;
    public static void init() {
    }

    /**
     * If true: Players only get base health removed by player kills
     * If false: Players get health removed from any death
     */
    public static final GameRules.Key<GameRules.BooleanRule> PLAYERRELATEDONLY = GameRuleRegistry.register(Lifesteal.MOD_ID + ":playerKillOnly", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

    /**
     * The action to take when the player goes below the allowed minimum health as defined by {@link #MINPLAYERHEALTH}
     */
    public static final GameRules.Key<EnumRule<DeathAction>> DEATH_ACTION =
            registerPlayerRule("deathAction", GameRuleFactory.createEnumRule(DeathAction.BAN));

    /**
     * Whether to allow gifting hearts to other players, via the command or altar.
     */
    public static final GameRules.Key<GameRules.BooleanRule> GIFTHEARTS = GameRuleRegistry.register(Lifesteal.MOD_ID + ":giftHearts", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

    /**
     * Whether to allow creating an altar to revive and exchange player hearts for heart crystal items.
     * Disabling this effectively disables trading and revival, except the /gift command
     */
    public static final GameRules.Key<GameRules.BooleanRule> ALTARS = GameRuleRegistry.register(Lifesteal.MOD_ID + ":enableAltars", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

    /**
     * Whether to disable getting "free" hearts from killing people with the minimum HP.
     * This can prevent spawn camping and harvesting tons of hearts from teammates
     */
    public static final GameRules.Key<GameRules.BooleanRule> ANTIHEARTDUPE = GameRuleRegistry.register(Lifesteal.MOD_ID + ":enableAntiHeartDupe", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

    /**
     * The amount of health "stolen" from players when other players kill them.
     */
    public static final GameRules.Key<GameRules.IntRule> STEALAMOUNT = GameRuleRegistry.register(Lifesteal.MOD_ID + ":stealAmount", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(2, 0));

    /**
     * This value determines the threshold for being considered "dead".
     * If a player reaches lower than this value, they will be categorized as dead unless BanWhenMaxHealth is disabled
     * If StealAmount is a multiple of 2, so should this value
     */
    public static GameRules.Key<GameRules.IntRule>  MINPLAYERHEALTH = GameRuleRegistry.register(Lifesteal.MOD_ID + ":minPlayerHealth", GameRules.Category.PLAYER, createIntRule(2, 1));

    /**
     * The max amount of health a player can obtain
     */
    public static final GameRules.Key<GameRules.IntRule> MAXPLAYERHEALTH = GameRuleRegistry.register(Lifesteal.MOD_ID + ":maxPlayerHealth", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(40, 1));

    /**
     * The amount of health received from heart crystals
     */
    public static final GameRules.Key<GameRules.IntRule> HEARTBONUS = GameRuleRegistry.register(Lifesteal.MOD_ID + ":healthFromHeart", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(2, 0));

    /**
     * The block that is to be used as the altar
     */
    public static final GameRules.Key<RegistryEntryRule<Block>> ALTAR_BLOCK = GameRuleRegistry.register(
            Lifesteal.MOD_ID + ":altarBlock", GameRules.Category.MISC,
            createRegistryEntryRule(Registries.BLOCK, Blocks.NETHERITE_BLOCK,
                    (server, blockRegistryEntryRule) -> altarGameRuleModified = true));

    public static boolean altarGameRuleModified = true;
    private static Block cachedAltarBlock;

    public static Block getBlockFromGameRule(GameRules gameRules) {
        if (altarGameRuleModified) {
            cachedAltarBlock = ((IGameRulesProvider)gameRules).unruled_getRegistryEntry(ALTAR_BLOCK);
        }
        return cachedAltarBlock;
    }

    private static <R extends GameRules.Rule<R>, T extends GameRules.Type<R>> GameRules.Key<R> registerPlayerRule(String name, T rule) {
        return GameRuleRegistry.register(Lifesteal.MOD_ID + ':' + name, GameRules.Category.PLAYER, rule);
    }

    private static GameRules.Type<GameRules.IntRule> createIntRule(int defaultValue, int minimumValue) {
        return createIntRule(defaultValue, minimumValue, Integer.MAX_VALUE, (server, rule) -> {
        });
    }

    private static GameRules.Type<GameRules.IntRule> createIntRule(int defaultValue, int minimumValue, int maximumValue, @Nullable BiConsumer<MinecraftServer, GameRules.IntRule> changedCallback) {
        return GameRulesTypeInvoker.invokeInit(() -> IntegerArgumentType.integer(minimumValue, maximumValue), (type) -> new SyncedBoundedIntRule(type, defaultValue, minimumValue, maximumValue), changedCallback, GameRules.Visitor::visitInt, FeatureSet.empty());
    }

    @Contract(
            value = "_, _, _ -> new",
            pure = true
    )
    @NotNull
    private static <T> GameRules.@NotNull Type<RegistryEntryRule<T>> createRegistryEntryRule(Registry<T> registry, T initialValue, BiConsumer<MinecraftServer, RegistryEntryRule<T>> changeCallback) {
        return RegistryEntryRule.create(registry, initialValue, changeCallback);
    }
}
