package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.utils.EffectEndEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "onEffectsRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;removeAttributeModifiers(Lnet/minecraft/world/entity/ai/attributes/AttributeMap;)V"))
    private void lifesteal$callEndEvent(Collection<MobEffectInstance> collection, CallbackInfo ci, @Local MobEffectInstance mobEffectInstance) {
        if ((LivingEntity) (Object) this instanceof ServerPlayer serverPlayer){
            ((EffectEndEvent)(mobEffectInstance.getEffect().value())).onEffectFinished(serverPlayer);
        }
    }

    @Inject(method = "onEffectUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;removeAttributeModifiers(Lnet/minecraft/world/entity/ai/attributes/AttributeMap;)V"))
    private void lifesteal$callEndEvent(MobEffectInstance mobEffectInstance, boolean bl, Entity entity, CallbackInfo ci){
        if ((LivingEntity) (Object) this instanceof ServerPlayer serverPlayer){
            ((EffectEndEvent)(mobEffectInstance.getEffect().value())).onEffectFinished(serverPlayer);
        }
    }
}
