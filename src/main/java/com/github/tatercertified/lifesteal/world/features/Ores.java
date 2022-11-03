package com.github.tatercertified.lifesteal.world.features;

import com.github.tatercertified.lifesteal.block.ModBlocks;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placementmodifier.*;

import java.util.List;

import static com.github.tatercertified.lifesteal.Loader.vein_size;
import static com.github.tatercertified.lifesteal.Loader.veins_per_chunk;

public class Ores {

    //Stone
    public static RegistryEntry<ConfiguredFeature<OreFeatureConfig, ?>> HEART_ORE_CONFIGURED_FEATURE = ConfiguredFeatures.register("heart_ore", Feature.ORE,
            new OreFeatureConfig(
                    OreConfiguredFeatures.STONE_ORE_REPLACEABLES, ModBlocks.NORMAL_BLOCK.getDefaultState(),
                    vein_size));

    public static final RegistryEntry<PlacedFeature> HEART_ORE_PLACED_FEATURE = PlacedFeatures.register("heart_ore", HEART_ORE_CONFIGURED_FEATURE,
            modifiersWithCount(veins_per_chunk,
                    HeightRangePlacementModifier.uniform(YOffset.fixed(0),YOffset.fixed(60))));


    //Deepslate
    public static RegistryEntry<ConfiguredFeature<OreFeatureConfig, ?>> DEEPSLATE_HEART_ORE_CONFIGURED_FEATURE = ConfiguredFeatures.register("deepslate_heart_ore", Feature.ORE,
            new OreFeatureConfig(
                    OreConfiguredFeatures.DEEPSLATE_ORE_REPLACEABLES, ModBlocks.NORMAL_BLOCK.getDefaultState(),
                    vein_size));

    public static final RegistryEntry<PlacedFeature> DEEPSLATE_HEART_ORE_PLACED_FEATURE = PlacedFeatures.register("deepslate_heart_ore", DEEPSLATE_HEART_ORE_CONFIGURED_FEATURE,
            modifiersWithCount(veins_per_chunk,
                    HeightRangePlacementModifier.uniform(YOffset.fixed(-64),YOffset.fixed(0))));


    public static void initOres() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,  RegistryKey.of(Registry.PLACED_FEATURE_KEY, new Identifier("heart_ore")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,  RegistryKey.of(Registry.PLACED_FEATURE_KEY, new Identifier("deepslate_heart_ore")));
    }


    private static List<PlacementModifier> modifiers(PlacementModifier countModifier, PlacementModifier heightModifier) {
        return List.of(countModifier, SquarePlacementModifier.of(), heightModifier, BiomePlacementModifier.of());
    }
    private static List<PlacementModifier> modifiersWithCount(int count, PlacementModifier heightModfier) {
        return modifiers(CountPlacementModifier.of(count), heightModfier);
    }
}

