package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.util.Config;
import com.github.tatercertified.lifesteal.util.PlayerUtils;
import com.github.tatercertified.lifesteal.util.ServerPlayerEntityInterface;
import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityInterface {

	@Shadow @Final public MinecraftServer server;

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Inject(method = "copyFrom", at = @At("TAIL"))
	public void preserveMaxHealth(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo callbackInfo) {
		EntityAttributeInstance oldHealth = oldPlayer.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		assert oldHealth != null;
		EntityAttributeInstance health = this.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		assert health != null;
		health.setBaseValue(oldHealth.getBaseValue());
	}

	@Inject(method = "onDeath", at = @At("TAIL"))
	public void onDeathLowerMaxHealth(DamageSource source, CallbackInfo callbackInfo) {
		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		ServerWorld world = (ServerWorld) player.getWorld();
		Entity entity = source.getAttacker();
		int stealAmount = world.getGameRules().getInt(LSGameRules.STEALAMOUNT);
		if (entity instanceof ServerPlayerEntity) {
			if (server.getGameRules().getBoolean(LSGameRules.ANTIHEARTDUPE)) {
				if (player.getMaxHealth() > server.getGameRules().getInt(LSGameRules.MINPLAYERHEALTH)) {
					updateValueOf((ServerPlayerEntity)entity, stealAmount);
				}
			} else {
				updateValueOf((ServerPlayerEntity)entity, stealAmount);
			}
			updateValueOf(player, -stealAmount);
		} else if(!world.getGameRules().getBoolean(LSGameRules.PLAYERRELATEDONLY)) {
			updateValueOf(player, -stealAmount);
		}
	}
	
	@Inject(method = "onSpawn", at = @At("TAIL"))
	public void onSpawnCheckToBan(CallbackInfo callbackInfo) {
		checkIfDead();
	}
	
	@Unique
	private static void updateValueOf(ServerPlayerEntity of, int by) {
		EntityAttributeInstance health = of.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		assert health != null;
		double oldHealth = health.getValue();
		float newHealth = (float) (oldHealth + by);
		int maxHealth = of.getWorld().getGameRules().getInt(LSGameRules.MAXPLAYERHEALTH);
		if (maxHealth > 0 && newHealth > maxHealth) newHealth = maxHealth;
		of.setHealth(of.getHealth() + by);
		health.setBaseValue(newHealth);
		// TODO: Figure out a better way to handle this
		//  The current logic doesn't expect for this to raise to the minimum.
		// checkForMinHealth(health, of.getServer());
	}

	@Override
	public void checkIfDead() {
		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		int minHealth = server.getGameRules().getInt(LSGameRules.MINPLAYERHEALTH);
		if (minHealth < 1) minHealth = 1;
		EntityAttributeInstance health = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		assert health != null;

		if (health.getBaseValue() <= 0) {
			if (server.getGameRules().getBoolean(LSGameRules.BANWHENMINHEALTH)) {
				PlayerUtils.addPlayerToDeadList(player.getUuid(), server);
				player.networkHandler.disconnect(Text.literal(Config.REVIVAL_MESSAGE));
			} else if (server.getGameRules().getBoolean(LSGameRules.SPECTATORWHENMINHEALTH)) {
				PlayerUtils.addPlayerToDeadList(player.getUuid(), server);
				player.changeGameMode(GameMode.SPECTATOR);
				player.sendMessage(Text.of(Config.REVIVAL_MESSAGE), true);
			} else {
				player.setHealth(minHealth);
			}
		}
	}

	private static void checkForMinHealth(EntityAttributeInstance health, MinecraftServer server) {
		int minHealth = server.getGameRules().getInt(LSGameRules.MINPLAYERHEALTH);

		if (minHealth > 0 && health.getBaseValue() < minHealth) {
			health.setBaseValue(minHealth);
		}
	}
}
