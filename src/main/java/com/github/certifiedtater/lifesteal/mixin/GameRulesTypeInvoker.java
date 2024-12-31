package com.github.certifiedtater.lifesteal.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(GameRules.Type.class)
public interface GameRulesTypeInvoker {
    @Invoker("<init>")
    public static <T extends GameRules.Rule<T>> GameRules.Type<T> invokeInit(Supplier<ArgumentType<?>> argumentType, Function<GameRules.Type<T>, T> ruleFactory, BiConsumer<MinecraftServer, T> changeCallback, GameRules.Acceptor<T> ruleAcceptor, FeatureSet requiredFeatures) {
        throw new AssertionError();
    }
}
