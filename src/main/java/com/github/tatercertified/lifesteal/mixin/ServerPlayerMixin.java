package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.effect.InvulnerableStatusEffect;
import com.github.tatercertified.lifesteal.gamerules.DeathCriteria;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.items.ModItems;
import com.github.tatercertified.lifesteal.utils.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements PlayerReviveData, PlayerInvulnerabilityInterface, PlayerMaxHealthInterface {

    private boolean newlyRevived;
    private int invulnerableTicks = 0;

    public ServerPlayerMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow @Final
    private MinecraftServer server;

    @Shadow
    public abstract ServerLevel level();

    @Inject(method = "die", at = @At("TAIL"))
    private void lifesteal$onDeath(DamageSource damageSource, CallbackInfo ci) {
        Entity attacker = damageSource.getEntity();
        if (attacker instanceof ServerPlayer playerAttacker) {
            PlayerUtils.exchangeHealth(((ServerPlayer) (Object) this), playerAttacker);
        } else if (level().getGameRules().get(LifeStealGamerules.DEATH_CRITERIA) == DeathCriteria.ANY_DEATH ||
                level().getGameRules().get(LifeStealGamerules.DEATH_CRITERIA) == DeathCriteria.ANY_DEATH_DROP_HEART
        ) {
            AttributeInstance killedMaxHealth = this.getAttribute(Attributes.MAX_HEALTH);
            PlayerUtils.changeHealthUnchecked(((ServerPlayer) (Object) this), -level().getGameRules().get(LifeStealGamerules.STEALAMOUNT));
            // Drop heart in the world
            if (level().getGameRules().get(LifeStealGamerules.DEATH_CRITERIA) == DeathCriteria.ANY_DEATH_DROP_HEART) {
                this.drop(new ItemStack(ModItems.HEART, 1), true, false);
            }
            // Check to see if the player is dead
            int minHealth = this.level().getGameRules().get(LifeStealGamerules.MINPLAYERHEALTH);
            if (killedMaxHealth.getBaseValue() <= minHealth) {
                // Considered dead
                DeathData data = new DeathData(this.getUUID());
                data.addToDeathDataList();
                PlayerUtils.handleDeadPlayerAction((ServerPlayer)(Object)this, data);
            }
        }
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void lifesteal$copyNewlyRevived(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        this.setNewlyRevived(((PlayerReviveData)oldPlayer).newlyRevived());
        this.invulnerableTicks = ((PlayerInvulnerabilityInterface)oldPlayer).getRemaining();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void lifesteal$readRevivedData(ValueInput view, CallbackInfo ci) {
        this.setNewlyRevived(view.getBooleanOr("newly_revived", false));
        invulnerableTicks = view.getIntOr("invulnerability_ticks", 0);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void lifesteal$writeRevivedData(ValueOutput view, CallbackInfo ci) {
        view.putBoolean("newly_revived", this.newlyRevived);
        view.putInt("invulnerability_ticks", this.invulnerableTicks);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lifesteal$tickInvulnerability(CallbackInfo ci) {
        if (isReviveInvulnerable()) {
            invulnerableTicks--;
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
}
