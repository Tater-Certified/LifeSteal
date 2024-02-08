package com.github.tatercertified.lifesteal;

import com.github.tatercertified.lifesteal.block.ModBlocks;
import com.github.tatercertified.lifesteal.command.AdminCommand;
import com.github.tatercertified.lifesteal.command.GiftCommand;
import com.github.tatercertified.lifesteal.command.WithdrawCommand;
import com.github.tatercertified.lifesteal.item.ModItems;
import com.github.tatercertified.lifesteal.util.Config;
import com.github.tatercertified.lifesteal.util.OfflineUtils;
import com.github.tatercertified.lifesteal.util.PlayerUtils;
import com.github.tatercertified.lifesteal.world.features.Ores;
import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;

import static com.github.tatercertified.lifesteal.item.HeartItem.isAltar;

public class Loader implements ModInitializer {

	public static final String MOD_ID = "lifesteal";



	@Override
	public void onInitialize() {
		Config.init();
		LSGameRules.init();
		ModItems.init();
		ModBlocks.registerBlocks();
		Ores.initOres();
		GiftCommand.register();
		WithdrawCommand.register();
		AdminCommand.register();
		PolymerResourcePackUtils.addModAssets(MOD_ID);

		/*
		  This callback exchanges HP for heart items if right-clicking on an altar
		 */
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (world.getGameRules().getBoolean(LSGameRules.ALTARS) && hand == player.getActiveHand() && player.getStackInHand(hand).isEmpty() && isAltar(world, hitResult.getBlockPos())) {
				PlayerUtils.convertHealthToHeartItems((ServerPlayerEntity) player, 1, player.getServer());
			}
			return ActionResult.PASS;
		});


		/*
		  This callback handles "banning" players if they still need to be revived.
		  All it does is kick a player when they join if they have no more health
		 */
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity connecting = handler.player;

			if (server.getOverworld().getGameRules().getBoolean(LSGameRules.BANWHENMINHEALTH) && PlayerUtils.isPlayerDead(connecting.getUuid(), server)) {
				handler.disconnect(Text.literal(Config.REVIVAL_MESSAGE));
			} else if (PlayerUtils.isPlayerDead(connecting.getUuid(), server)) {
				connecting.changeGameMode(GameMode.SPECTATOR);
			} else {
				NbtCompound compound = OfflineUtils.getPlayerData(connecting.getUuid(), server, connecting.getName().getString());
				String reviver = OfflineUtils.getReviver(compound);
				if (!reviver.isEmpty()) {
					postRevival(compound, connecting, reviver, server);
				}
			}
		});
	}

	private void postRevival(NbtCompound compound, ServerPlayerEntity player, String reviver, MinecraftServer server) {
		player.sendMessage(Text.literal((reviver + Config.REVIVER)));
		OfflineUtils.setReviver(compound, "");
		BlockPos pos = OfflineUtils.getLocation(compound);
		PlayerUtils.setBaseHealth(player, server.getGameRules().getInt(LSGameRules.HEARTBONUS), server);
		FabricDimensions.teleport(player, OfflineUtils.getDimension(compound, server), new TeleportTarget(pos.toCenterPos(), Vec3d.ZERO, 0.0f, 0.0f));
		player.changeGameMode(GameMode.SURVIVAL);
		OfflineUtils.savePlayerData(player.getUuid(), server, player.getName().getString(), compound);
	}
}
