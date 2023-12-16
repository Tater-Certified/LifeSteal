package com.github.tatercertified.lifesteal.world.features;

import com.github.tatercertified.lifesteal.Loader;
import com.github.tatercertified.lifesteal.util.Config;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

public class Ores {

    public static final RegistryKey<PlacedFeature> HEART_ORE_PLACED_KEY = RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(Loader.MOD_ID,"heart_ore"));
    public static void initOres() {
        if (Config.generateOres) {
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, HEART_ORE_PLACED_KEY);
        }
    }
}

