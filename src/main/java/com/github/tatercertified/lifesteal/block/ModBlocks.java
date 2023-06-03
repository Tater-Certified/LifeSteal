package com.github.tatercertified.lifesteal.block;

import com.github.tatercertified.lifesteal.Loader;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static Block DEEP_BLOCK;
    public static Block NORMAL_BLOCK;

    public static void registerBlocks() {
        PolymerResourcePackUtils.markAsRequired();
        PolymerResourcePackUtils.addModAssets(Loader.MOD_ID);

        register(BlockModelType.FULL_BLOCK, "heart_ore");
        registerDeep(BlockModelType.FULL_BLOCK, "deepslate_heart_ore");
    }

    public static void registerDeep(BlockModelType type, String modelId) {
        var DEEP = new Identifier(Loader.MOD_ID, modelId);
        DEEP_BLOCK = Registry.register(Registries.BLOCK, DEEP,
                new DeepslateHeartOre(FabricBlockSettings.of(Material.STONE).requiresTool().strength(6.0f, 6.0f).sounds(BlockSoundGroup.DEEPSLATE), type, modelId));

        Registry.register(Registries.ITEM, DEEP, new DeepslateHeartOreItem(new Item.Settings(), DEEP_BLOCK, modelId));
    }

    public static void register(BlockModelType type, String modelId) {
        var NORMAL = new Identifier(Loader.MOD_ID, modelId);
        NORMAL_BLOCK = Registry.register(Registries.BLOCK, NORMAL,
                new HeartOre(FabricBlockSettings.of(Material.STONE).requiresTool().strength(6.0f, 6.0f).sounds(BlockSoundGroup.STONE), type, modelId));

        Registry.register(Registries.ITEM, NORMAL, new HeartOreItem(new Item.Settings(), NORMAL_BLOCK, modelId));
    }
}
