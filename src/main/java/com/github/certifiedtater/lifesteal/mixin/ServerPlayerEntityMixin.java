package com.github.certifiedtater.lifesteal.mixin;

import com.github.certifiedtater.lifesteal.data.DeathData;
import com.github.certifiedtater.lifesteal.gamerules.LifeStealGamerules;
import com.github.certifiedtater.lifesteal.utils.PlayerUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void lifsteal$onDeath(DamageSource damageSource, CallbackInfo ci) {
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
}
