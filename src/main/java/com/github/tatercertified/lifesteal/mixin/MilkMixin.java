package com.github.tatercertified.lifesteal.mixin;
import com.github.tatercertified.lifesteal.effect.InvulnerableStatusEffect;
import com.github.tatercertified.lifesteal.utils.PlayerInvulnerabilityInterface;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.consume.ClearAllEffectsConsumeEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(ClearAllEffectsConsumeEffect.class)
public class MilkMixin {
    @Redirect(method = "onConsume", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z"))
    private boolean lifesteal$preventInvulnerabilityRemoval(LivingEntity instance) {
        instance.clearStatusEffects();
        if (instance instanceof ServerPlayerEntity player && ((PlayerInvulnerabilityInterface)player).isReviveInvulnerable()) {
            instance.addStatusEffect(new StatusEffectInstance(InvulnerableStatusEffect.INVULNERABLE, ((PlayerInvulnerabilityInterface)player).getRemaining(), 0, false, false, true));
        }
        return false;
    }
}