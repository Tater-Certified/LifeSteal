package com.github.tatercertified.lifesteal.world;

import com.github.tatercertified.lifesteal.Lifesteal;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;

public class Ores {

    public static final ResourceKey<@NotNull PlacedFeature> HEART_ORE_PLACED_KEY = ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID,"heart_ore"));
    public static void initOres() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, HEART_ORE_PLACED_KEY);
    }
}