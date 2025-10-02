package com.github.tatercertified.lifesteal.mixin.gametest;

import com.github.tatercertified.lifesteal.gametest.LifestealGameTest;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.server.GameProfileResolver;
import net.minecraft.util.ApiServices;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(ApiServices.class)
public class GameTestUserCacheMixin {
    @Inject(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/UserCache;<init>(Lcom/mojang/authlib/GameProfileRepository;Ljava/io/File;)V"), cancellable = true)
    private static void lifesteal$replace(YggdrasilAuthenticationService authenticationService, File rootDirectory, CallbackInfoReturnable<ApiServices> cir, @Local(ordinal = 0) MinecraftSessionService minecraftSessionService, @Local(ordinal = 0) GameProfileRepository gameProfileRepository) {
        GameProfileResolver gameProfileResolver = new GameProfileResolver.CachedSessionProfileResolver(minecraftSessionService, LifestealGameTest.gameTestUserCache);
        cir.setReturnValue(new ApiServices(minecraftSessionService, authenticationService.getServicesKeySet(), gameProfileRepository, LifestealGameTest.gameTestUserCache, gameProfileResolver));
    }
}
