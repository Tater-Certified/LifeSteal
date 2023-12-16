package com.github.tatercertified.lifesteal;

import com.github.tatercertified.lifesteal.block.ModBlocks;
import com.github.tatercertified.lifesteal.command.GiftCommand;
import com.github.tatercertified.lifesteal.command.WithdrawCommand;
import com.github.tatercertified.lifesteal.item.ModItems;
import com.github.tatercertified.lifesteal.util.Config;
import com.github.tatercertified.lifesteal.util.PlayerUtils;
import com.github.tatercertified.lifesteal.util.ServerPlayerEntityInterface;
import com.github.tatercertified.lifesteal.world.features.Ores;
import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

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
			} else {
				String reviver = ((ServerPlayerEntityInterface) connecting).reviver();
				if (reviver != null) {
					connecting.sendMessage(Text.literal(((ServerPlayerEntityInterface) connecting).reviver() + Config.REVIVER));
				}
			}
		});
	}
}
