package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.Loader;
import com.github.tatercertified.lifesteal.util.ServerPlayerEntityInterface;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.BannedPlayerEntry;
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

import java.util.Objects;

//NOTICE: file was modified to use gamerules instead
//of the configuration implementation and to use attributes, it also fixes the max health attribute
//being lost on death.

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityInterface {

	@Shadow @Final public MinecraftServer server;
	@Unique
	private String reviver = null;

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		if (reviver != null) {
			nbt.putString("reviver", reviver);
		} else if (nbt.contains("reviver") && nbt.get("reviver") == null) {
			nbt.remove("reviver");
		}
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	public void readCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		if (nbt.contains("reviver")) {
			this.reviver = nbt.getString("reviver");
		}
	}

	@Inject(method = "copyFrom", at = @At("TAIL"))
	public void preserveMaxHealth(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo callbackInfo) {
		EntityAttributeInstance oldHealth = oldPlayer.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		assert oldHealth != null;
		EntityAttributeInstance health = ((ServerPlayerEntity) (Object) this).getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		assert health != null;
		health.setBaseValue(oldHealth.getBaseValue());
	}

	@Inject(method = "onDeath", at = @At("TAIL"))
	public void onDeathLowerMaxHealth(DamageSource source, CallbackInfo callbackInfo) {
		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		ServerWorld world = (ServerWorld) player.getWorld();
		Entity entity = source.getAttacker();
		int stealAmount = world.getGameRules().getInt(Loader.STEALAMOUNT);
		if(entity instanceof ServerPlayerEntity && !Objects.equals(player.getIp(), "127.0.0.1")) {
			updateValueOf((ServerPlayerEntity)entity, stealAmount);
			updateValueOf(player, -stealAmount);
		} else if(!world.getGameRules().getBoolean(Loader.PLAYERRELATEDONLY)) {
			updateValueOf(player, -stealAmount);
		}
	}
	
	@Inject(method = "onSpawn", at = @At("TAIL"))
	public void onSpawnCheckToBan(CallbackInfo callbackInfo) {
		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		ServerWorld world = (ServerWorld) player.getWorld();
		int minHealth = player.getWorld().getGameRules().getInt(Loader.MINPLAYERHEALTH);
		if(minHealth < 1) minHealth = 1;
		EntityAttributeInstance health = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		assert health != null;
		if (world.getGameRules().getBoolean(Loader.BANWHENMINHEALTH) && health.getBaseValue() < minHealth) {
			player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.DisconnectS2CPacket(Text.of("You lost your last life. You will be unbanned when someone revives you.")));
			server.getPlayerManager().getUserBanList().add(new BannedPlayerEntry(player.getGameProfile()));
		} else if(health.getBaseValue() < minHealth){
			player.changeGameMode(GameMode.SPECTATOR);
			player.sendMessage(Text.of("You lost your last life. You now must be revived."), true);
		}
	}
	
	@Unique
	private static void updateValueOf(ServerPlayerEntity of, int by) {
		EntityAttributeInstance health = of.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		assert health != null;
		double oldHealth = health.getValue();
		float newHealth = (float) (oldHealth + by);
		int maxHealth = of.getWorld().getGameRules().getInt(Loader.MAXPLAYERHEALTH);
		if(maxHealth > 0 && newHealth > maxHealth) newHealth = maxHealth;
		of.setHealth(of.getHealth()+by);
		health.setBaseValue(newHealth);
	}

	@Override
	public String reviver() {
		String copy = reviver;
		reviver = null;
		return copy;
	}
}
