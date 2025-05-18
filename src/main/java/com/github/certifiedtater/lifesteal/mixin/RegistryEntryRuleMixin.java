package com.github.certifiedtater.lifesteal.mixin;

import com.github.certifiedtater.lifesteal.gamerules.RegistryEntryRuleInterface;
import mc.recraftors.unruled_api.rules.RegistryEntryRule;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RegistryEntryRule.class, remap = false)
public class RegistryEntryRuleMixin implements RegistryEntryRuleInterface {
    Runnable callback;

    @Inject(method = "set(Ljava/lang/Object;)V", at = @At(value = "TAIL"))
    private <T> void addCallBack(T t, CallbackInfo ci) {
        if (this.callback != null) {
            this.callback.run();
        }
    }

    @Inject(method = "bump", at = @At("TAIL"))
    private <T> void addCallback(T value, MinecraftServer server, CallbackInfo ci) {
        if (this.callback != null) {
            this.callback.run();
        }
    }

    @Override
    public void setCallback(Runnable callback) {
        this.callback = callback;
    }
}
