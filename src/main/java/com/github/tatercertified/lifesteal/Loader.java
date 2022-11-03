package com.github.tatercertified.lifesteal;

import com.github.tatercertified.lifesteal.item.ModItems;
import com.github.tatercertified.lifesteal.world.features.Ores;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import com.github.tatercertified.lifesteal.block.ModBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.GameRules;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

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

	public static int vein_size;
	public static int veins_per_chunk;
	public static int vein_size_deep;
	public static int veins_per_chunk_deep;
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
			if (!(Objects.equals(cfgver, "1.1"))) {
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
		fixOres();
		Ores.initOres();
		PolymerRPUtils.addAssetSource(MOD_ID);
	}

	public void mkfile() throws IOException {
		OutputStream output = new FileOutputStream(String.valueOf(FabricLoader.getInstance().getConfigDir().resolve("lifesteal.properties")));
		if (!properties.contains("config-version")) {properties.setProperty("config-version", "1.1");}
		if (!properties.contains("vein-size")) {properties.setProperty("vein-size", "5");}
		if (!properties.contains("veins-per-chunk")) {properties.setProperty("veins-per-chunk", "1");}
		if (!properties.contains("vein-size-deepslate")) {properties.setProperty("vein-size-deepslate", "5");}
		if (!properties.contains("veins-per-chunk-deepslate")) {properties.setProperty("veins-per-chunk-deepslate", "1");}
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
		vein_size = Integer.parseInt(properties.getProperty("vein-size"));
		veins_per_chunk = Integer.parseInt(properties.getProperty("veins-per-chunk"));
		vein_size_deep = Integer.parseInt(properties.getProperty("vein-size-deepslate"));
		veins_per_chunk_deep = Integer.parseInt(properties.getProperty("veins-per-chunk-deepslate"));
		revival_block = properties.getProperty("revival_block");
	}

	public void fixOres() {
		if (vein_size < 5) {
			vein_size = 5;
		}
		if (vein_size_deep < 5) {
			vein_size_deep = 5;
		}
	}
}
