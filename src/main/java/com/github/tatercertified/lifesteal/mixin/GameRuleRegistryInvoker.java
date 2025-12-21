package com.github.tatercertified.lifesteal.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;
import net.minecraft.world.rule.GameRuleType;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.ToIntFunction;

@Mixin(GameRules.class)
public interface GameRuleRegistryInvoker {
    @Invoker("register")
    public static <T> GameRule<T> register(
            String name,
            GameRuleCategory category,
            GameRuleType type,
            ArgumentType<T> argumentType,
            Codec<T> codec,
            T defaultValue,
            FeatureSet requiredFeatures,
            GameRules.Acceptor<T> acceptor,
            ToIntFunction<T> commandResultSupplier
    ) {
        throw new AssertionError();
    }
}
