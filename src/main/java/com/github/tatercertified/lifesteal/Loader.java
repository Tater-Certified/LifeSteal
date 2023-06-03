package com.github.tatercertified.lifesteal;

import com.github.tatercertified.lifesteal.block.ModBlocks;
import com.github.tatercertified.lifesteal.item.ModItems;
import com.github.tatercertified.lifesteal.world.features.Ores;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

import static com.github.tatercertified.lifesteal.item.HeartItem.isAltar;
import static com.github.tatercertified.lifesteal.item.ModItems.HEART;

//NOTICE: This file was modified to remove all configuration setup and instead establish gamerules.

public class Loader implements ModInitializer {

	public static final String MOD_ID = "lifesteal";

	public static final GameRules.Key<GameRules.BooleanRule> PLAYERRELATEDONLY =
			GameRuleRegistry.register(MOD_ID + ":playerKillOnly", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.BooleanRule> BANWHENMINHEALTH =
			GameRuleRegistry.register(MOD_ID + ":banWhenMinHealth", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.BooleanRule> GIFTHEARTS =
			GameRuleRegistry.register(MOD_ID + ":giftHearts", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.IntRule> STEALAMOUNT =
			GameRuleRegistry.register(MOD_ID + ":stealAmount", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(2));

	public static final GameRules.Key<GameRules.IntRule> MINPLAYERHEALTH =
			GameRuleRegistry.register(MOD_ID + ":minPlayerHealth", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(1));

	public static final GameRules.Key<GameRules.IntRule> MAXPLAYERHEALTH =
			GameRuleRegistry.register(MOD_ID + ":maxPlayerHealth", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(40));

	public static final GameRules.Key<GameRules.IntRule> HEARTBONUS =
			GameRuleRegistry.register(MOD_ID + ":healthPerUse", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(2));

	public static String revival_block;
	public static String cfgver;
	public static Properties properties = new Properties();

	@Override
	public void onInitialize() {
		//Config
		var path = FabricLoader.getInstance().getConfigDir().resolve("lifesteal.properties");

		if (Files.notExists(path)) {
			try {
				mkfile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				loadcfg();
			} catch (IOException e) {
				e.printStackTrace();
			}
			cfgver = properties.getProperty("config-version");
			if (!(Objects.equals(cfgver, "1.3"))) {
				try {
					mkfile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				parse();
			}
		}
		ModItems.init();
		ModBlocks.registerBlocks();
		Ores.initOres();
		PolymerResourcePackUtils.addModAssets(MOD_ID);

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (hand == player.getActiveHand() && player.getStackInHand(hand).isEmpty() && isAltar(world, hitResult.getBlockPos())) {
				convertHealth(2, player, hand);
			}
			return ActionResult.PASS;
		});
	}

	public void convertHealth(double amount, PlayerEntity player, Hand hand) {
		EntityAttributeInstance health = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		assert health != null;
		float current = player.getHealth();
		double newhealth = health.getValue() - amount;
		if (current > amount) {
			health.setBaseValue(newhealth);
			player.setHealth((float) (current - amount));
			player.setStackInHand(hand, new ItemStack(HEART));
			player.sendMessage(Text.of("You converted "+ amount +" health"), true);
		} else {
			player.sendMessage(Text.of("You don't have enough health!"));
		}
	}

	public void mkfile() throws IOException {
		OutputStream output = new FileOutputStream(String.valueOf(FabricLoader.getInstance().getConfigDir().resolve("lifesteal.properties")));
		if (!properties.contains("config-version")) {properties.setProperty("config-version", "1.3");}
		if (!properties.contains("revival_block")) {properties.setProperty("revival_block", "minecraft:netherite_block");}
		properties.store(output, null);
		parse();
		output.close();
	}

	public void loadcfg() throws IOException {
		InputStream input = new FileInputStream(String.valueOf(FabricLoader.getInstance().getConfigDir().resolve("lifesteal.properties")));
		properties.load(input);
		input.close();
	}

	public void parse() {
		cfgver = properties.getProperty("config-version");
		revival_block = properties.getProperty("revival_block");
	}
}
