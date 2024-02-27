package com.github.tatercertified.lifesteal;

import com.github.tatercertified.lifesteal.block.ModBlocks;
import com.github.tatercertified.lifesteal.command.AdminCommand;
import com.github.tatercertified.lifesteal.command.GiftCommand;
import com.github.tatercertified.lifesteal.command.WithdrawCommand;
import com.github.tatercertified.lifesteal.item.ModItems;
import com.github.tatercertified.lifesteal.util.*;
import com.github.tatercertified.lifesteal.world.features.Ores;
import com.github.tatercertified.lifesteal.world.gamerules.DeathAction;
import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import com.mojang.logging.LogUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;
import org.slf4j.Logger;

import static com.github.tatercertified.lifesteal.item.HeartItem.isAltar;

public class Loader implements ModInitializer {
	private static final Logger logger = LogUtils.getLogger();

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

		// Migrates gamerules to a new value automagically.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			final GameRules gameRules = server.getGameRules();

			final var actionOnDeath = gameRules.get(LSGameRules.DEATH_ACTION);

			if (actionOnDeath.get() != DeathAction.UNSET) {
				return;
			}

			final var banOnDeath = gameRules.get(LSGameRules.BANWHENMINHEALTH);
			final var spectateOnDeath = gameRules.get(LSGameRules.SPECTATORWHENMINHEALTH);

			DeathAction action = DeathAction.REVIVE;

			if (banOnDeath.get()) {
				action = DeathAction.BAN;
			} else if (spectateOnDeath.get()) {
				action = DeathAction.SPECTATOR;
			}

			actionOnDeath.set(action, server);
			// Would ideally want a dedicated deprecated state but unfortunately, not really possible.
			// So for now, reset to default, so it falls back in case someone sets it to uninit.
			banOnDeath.set(true, server);
			spectateOnDeath.set(false, server);

			logger.info("Migrated LifeSteal gamerules 'banWhenMinHealth' and 'whitelistWhenMinHealth'; 'actionOnDeath' now set to '{}'", action);
		});

		/*
		  This callback exchanges HP for heart items if right-clicking on an altar
		 */
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (world.getGameRules().getBoolean(LSGameRules.ALTARS) && hand == player.getActiveHand() && player.getStackInHand(hand).isEmpty() && isAltar(world, hitResult.getBlockPos())) {
				PlayerUtils.convertHealthToHeartItems((ServerPlayerEntity) player, 1, player.getServer(), true);
			}
			return ActionResult.PASS;
		});


		/*
		  This callback handles "banning" players if they still need to be revived.
		  All it does is kick a player when they join if they have no more health
		 */
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity connecting = handler.player;
			final GameRules gameRules = server.getGameRules();
			final DeathAction action = gameRules.get(LSGameRules.DEATH_ACTION).get();

			if (!PlayerUtils.isPlayerDead(connecting.getUuid(), server)) {
				OfflinePlayerData data = OfflineUtils.getOfflinePlayerData(server, connecting.getGameProfile());
				LifeStealPlayerData compound = data.getLifeStealData();
				if (compound != null) {
					postRevival(compound, connecting, server);
					data.setLifeStealData(null);
					data.save();
				}

				return;
			}

			switch (action) {
				case BAN -> handler.disconnect(LsText.DEATH);
				case SPECTATOR -> {
					connecting.changeGameMode(GameMode.SPECTATOR);
					connecting.sendMessage(LsText.DEATH, true);
				}
				case REVIVE -> {
					// Deader than a wedge they say... is -∞ what they mean?
					connecting.setHealth(Float.NEGATIVE_INFINITY);
					connecting.changeGameMode(server.getDefaultGameMode());
					PlayerUtils.setExactBaseHealth(connecting, gameRules.getInt(LSGameRules.MINPLAYERHEALTH));
					PlayerUtils.removePlayerFromDeadList(connecting.getUuid(), server);

					// Tell the client it just won the game... just to have it respawn itself.
					// No there isn't really any better way that is just as easy, as it breaks everything.
					handler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_WON, 0F));
				}
			}
		});
	}

	private static void postRevival(LifeStealPlayerData data, ServerPlayerEntity player, MinecraftServer server) {
		player.sendMessage(LsText.revivee(Text.of(data.reviver)));
		BlockPos pos = data.teleport;
		PlayerUtils.setExactBaseHealth(player, server.getGameRules().getInt(LSGameRules.MINPLAYERHEALTH));
		FabricDimensions.teleport(player, data.resolveDimension(server), new TeleportTarget(pos.toCenterPos(), Vec3d.ZERO, 0.0f, 0.0f));
		player.changeGameMode(GameMode.SURVIVAL);
	}
}
