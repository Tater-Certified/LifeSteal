package com.github.certifiedtater.lifesteal.mixin;

import com.github.certifiedtater.lifesteal.data.DeathData;
import com.github.certifiedtater.lifesteal.effect.InvulnerableStatusEffect;
import com.github.certifiedtater.lifesteal.gamerules.LifeStealGamerules;
import com.github.certifiedtater.lifesteal.utils.LifeStealText;
import com.github.certifiedtater.lifesteal.utils.PlayerInvulnerabilityInterface;
import com.github.certifiedtater.lifesteal.utils.PlayerReviveData;
import com.github.certifiedtater.lifesteal.utils.PlayerUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlayerReviveData, PlayerInvulnerabilityInterface  {

    private boolean newlyRevived;
    private int invulnerableTicks = 0;

    public ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow public abstract ServerWorld getWorld();
    @Shadow @Final
    public MinecraftServer server;

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void lifesteal$onDeath(DamageSource damageSource, CallbackInfo ci) {
        Entity attacker = damageSource.getAttacker();
        if (attacker instanceof ServerPlayerEntity playerAttacker) {
            PlayerUtils.exchangeHealth(((ServerPlayerEntity) (Object) this), playerAttacker);
        } else if (!getWorld().getGameRules().getBoolean(LifeStealGamerules.PLAYERRELATEDONLY)) {
            EntityAttributeInstance killedMaxHealth = this.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            PlayerUtils.changeHealth(((ServerPlayerEntity) (Object) this), killedMaxHealth, -getWorld().getGameRules().getInt(LifeStealGamerules.STEALAMOUNT));
            // Check to see if the player is dead
            int minHealth = this.getServer().getGameRules().getInt(LifeStealGamerules.MINPLAYERHEALTH);
            if (killedMaxHealth.getBaseValue() <= minHealth) {
                // Considered dead
                DeathData data = new DeathData(this.getUuid());
                data.addToDeathDataList();
                PlayerUtils.handleDeadPlayerAction((ServerPlayerEntity)(Object)this, data);
            }
        }
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void lifesteal$copyNewlyRevived(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.setNewlyRevived(((PlayerReviveData)oldPlayer).newlyRevived());
        this.invulnerableTicks = ((PlayerInvulnerabilityInterface)oldPlayer).getRemaining();
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void lifesteal$readRevivedData(ReadView view, CallbackInfo ci) {
        this.setNewlyRevived(view.getBoolean("newly_revived", false));
        invulnerableTicks = view.getInt("invulnerability_ticks", 0);
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void lifesteal$writeRevivedData(WriteView view, CallbackInfo ci) {
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
    @Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
    private void lifesteal$checkInvulnerability(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (isReviveInvulnerable()) {
            player.sendMessage(LifeStealText.preventDamage(this.getName()), true);
            cir.setReturnValue(false);
        }
    }
    // You cannot kill players if invulnerable either
    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;attack(Lnet/minecraft/entity/Entity;)V"), cancellable = true)
    private void lifesteal$preventAttackingPlayers(Entity target, CallbackInfo ci) {
        if (isReviveInvulnerable() && target instanceof ServerPlayerEntity) {
            this.sendMessage(LifeStealText.PREVENT_ATTACK, true);
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
        invulnerableTicks = this.server.getGameRules().getInt(LifeStealGamerules.RESPAWN_INVULNERABILITY) * 20;
        this.addStatusEffect(new StatusEffectInstance(InvulnerableStatusEffect.INVULNERABLE, this.getRemaining(), 0, false, false, true));
    }
    @Override
    public boolean isReviveInvulnerable() {
        return invulnerableTicks != 0;
    }
    @Override
    public int getRemaining() {
        return invulnerableTicks;
    }
}
