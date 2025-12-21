package com.github.tatercertified.lifesteal.mixin;
import com.github.tatercertified.lifesteal.effect.InvulnerableStatusEffect;
import com.github.tatercertified.lifesteal.utils.PlayerInvulnerabilityInterface;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(ClearAllStatusEffectsConsumeEffect.class)
public class MilkMixin {
    @Redirect(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"))
    private boolean lifesteal$preventInvulnerabilityRemoval(LivingEntity instance) {
        instance.removeAllEffects();
        if (instance instanceof ServerPlayer player && ((PlayerInvulnerabilityInterface)player).isReviveInvulnerable()) {
            instance.addEffect(new MobEffectInstance(InvulnerableStatusEffect.INVULNERABLE, ((PlayerInvulnerabilityInterface)player).getRemaining(), 0, false, false, true));
        }
        return false;
    }
}