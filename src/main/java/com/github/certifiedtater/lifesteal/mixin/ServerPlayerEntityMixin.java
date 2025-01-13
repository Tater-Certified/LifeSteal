package com.github.certifiedtater.lifesteal.mixin;

import com.github.certifiedtater.lifesteal.data.DeathData;
import com.github.certifiedtater.lifesteal.effect.InvulnerableStatusEffect;
import com.github.certifiedtater.lifesteal.gamerules.LifeStealGamerules;
import com.github.certifiedtater.lifesteal.utils.LifeStealText;
import com.github.certifiedtater.lifesteal.utils.PlayerInvulnerabilityInterface;
import com.github.certifiedtater.lifesteal.utils.PlayerUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlayerInvulnerabilityInterface {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    private int invulnerableTicks = 0;

    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow @Final public MinecraftServer server;

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void lifesteal$onDeath(DamageSource damageSource, CallbackInfo ci) {
        Entity attacker = damageSource.getAttacker();
        if (attacker instanceof ServerPlayerEntity playerAttacker) {
            PlayerUtils.exchangeHealth(((ServerPlayerEntity) (Object) this), playerAttacker);
        } else if (!getServerWorld().getGameRules().getBoolean(LifeStealGamerules.PLAYERRELATEDONLY)) {
            EntityAttributeInstance killedMaxHealth = this.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            PlayerUtils.changeHealth(((ServerPlayerEntity) (Object) this), killedMaxHealth, -getServerWorld().getGameRules().getInt(LifeStealGamerules.STEALAMOUNT));
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

    @Inject(method = "tick", at = @At("TAIL"))
    private void lifesteal$tickInvulnerability(CallbackInfo ci) {
        if (isReviveInvulnerable()) {
            invulnerableTicks--;
        }
    }

    // You cannot be killed by players if invulnerable
    @Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
    private void lifesteal$checkInvulnerability(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        System.out.println(player.getName() + " Being Attacked");
        if (isReviveInvulnerable()) {
            cir.setReturnValue(false);
        }
    }

    // You cannot kill players if invulnerable either
    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;attack(Lnet/minecraft/entity/Entity;)V"), cancellable = true)
    private void lifesteal$preventAttackingPlayers(Entity target, CallbackInfo ci) {
        System.out.println("ATTACKING PLAYER");
        if (isReviveInvulnerable() && target instanceof ServerPlayerEntity) {
            this.sendMessage(LifeStealText.PREVENT_ATTACK, true);
            ci.cancel();
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void lifesteal$readInvulnerabilityFromNBT(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("invulnerability_ticks")) {
            invulnerableTicks = nbt.getInt("invulnerability_ticks");
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void lifesteal$writeInvulnerabilityToNBT(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("invulnerability_ticks", invulnerableTicks);
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void lifesteal$copyInvulnerability(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.invulnerableTicks = ((PlayerInvulnerabilityInterface)oldPlayer).getRemaining();
    }

    @Override
    public void setReviveInvulnerability() {
        invulnerableTicks = this.server.getGameRules().getInt(LifeStealGamerules.RESPAWN_INVULNERABILITY) * 20;
        this.addStatusEffect(new StatusEffectInstance(RegistryEntry.of(new InvulnerableStatusEffect()), this.getRemaining()));
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
