package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.effect.InvulnerableStatusEffect;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.utils.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements PlayerReviveData, PlayerInvulnerabilityInterface, PlayerMaxHealthInterface, CraftedHeartsInterface, AnimationCooldownInterface {

    private boolean newlyRevived;
    private int invulnerableTicks = 0;
    private int heartsCrafted = 0;
    private int withdrawCooldown = 0;

    public ServerPlayerMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    public abstract ServerLevel level();

    @Inject(method = "die", at = @At("TAIL"))
    private void lifesteal$onDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer attacker = damageSource.getEntity() instanceof ServerPlayer ? (ServerPlayer) damageSource.getEntity() : null;
        PlayerUtils.handleDeath((ServerPlayer) (Object) this, attacker);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void lifesteal$copyNewlyRevived(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        this.setNewlyRevived(((PlayerReviveData)oldPlayer).newlyRevived());
        this.invulnerableTicks = ((PlayerInvulnerabilityInterface)oldPlayer).getRemaining();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void lifesteal$readRevivedData(ValueInput view, CallbackInfo ci) {
        this.setNewlyRevived(view.getBooleanOr("newly_revived", false));
        this.invulnerableTicks = view.getIntOr("invulnerability_ticks", 0);
        this.heartsCrafted = view.getIntOr("hearts_crafted", 0);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void lifesteal$writeRevivedData(ValueOutput view, CallbackInfo ci) {
        view.putBoolean("newly_revived", this.newlyRevived);
        view.putInt("invulnerability_ticks", this.invulnerableTicks);
        view.putInt("hearts_crafted", this.heartsCrafted);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lifesteal$tickInvulnerability(CallbackInfo ci) {
        if (isReviveInvulnerable()) {
            invulnerableTicks--;
        }
        if (withdrawCooldown > 0) {
            withdrawCooldown--;
        }
    }
    // You cannot be killed by players if invulnerable
    @Inject(method = "canHarmPlayer", at = @At("HEAD"), cancellable = true)
    private void lifesteal$checkInvulnerability(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (isReviveInvulnerable()) {
            player.displayClientMessage(LifeStealText.preventDamage(this.getName()), true);
            cir.setReturnValue(false);
        }
    }
    // You cannot kill players if invulnerable either
    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;attack(Lnet/minecraft/world/entity/Entity;)V"), cancellable = true)
    private void lifesteal$preventAttackingPlayers(Entity target, CallbackInfo ci) {
        if (isReviveInvulnerable() && target instanceof ServerPlayer) {
            this.displayClientMessage(LifeStealText.PREVENT_ATTACK, true);
            ci.cancel();
        }
    }

    @Override
    public boolean newlyRevived() {
        return newlyRevived;
    }

    @Override
    public void setNewlyRevived(boolean set) {
        this.newlyRevived = set;
    }

    @Override
    public void setReviveInvulnerability() {
        invulnerableTicks = this.level().getGameRules().get(LifeStealGamerules.RESPAWN_INVULNERABILITY) * 20;
        this.addEffect(new MobEffectInstance(InvulnerableStatusEffect.INVULNERABLE, this.getRemaining(), 0, false, false, true));
    }

    @Override
    public boolean isReviveInvulnerable() {
        return invulnerableTicks != 0;
    }

    @Override
    public int getRemaining() {
        return invulnerableTicks;
    }

    @Override
    public double getBaseMaxHealth() {
        return this.getAttributeBaseValue(Attributes.MAX_HEALTH);
    }

    @Override
    public void setBaseMaxHealth(double value) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(value);
    }

    @Override
    public int getHeartsCrafted() {
        return this.heartsCrafted;
    }

    @Override
    public void resetHeartsCrafted() {
        this.heartsCrafted = 0;
    }

    @Override
    public ServerPlayer getInstance() {
        return (ServerPlayer) (Object) this;
    }

    @Override
    public void incrementHeartsCrafted() {
        this.heartsCrafted++;
    }

    @Override
    public boolean canWithdraw() {
        boolean canWithdraw = this.withdrawCooldown == 0;
        if (canWithdraw) {
            this.withdrawCooldown = 30;
        }
        return canWithdraw;
    }
}
