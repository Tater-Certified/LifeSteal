package com.github.certifiedtater.lifesteal.block;

import com.github.certifiedtater.lifesteal.Lifesteal;
import com.github.certifiedtater.lifesteal.items.DeepslateHeartOreItem;
import com.github.certifiedtater.lifesteal.items.HeartOreItem;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static void registerBlocks() {
        registerBlocksAndItems("deepslate_heart_ore", AbstractBlock.Settings.copy(Blocks.DEEPSLATE_DIAMOND_ORE).requiresTool()
                .strength(6.0f, 6.0f).sounds(BlockSoundGroup.DEEPSLATE), Blocks.DEEPSLATE_REDSTONE_ORE);

        registerBlocksAndItems("heart_ore", AbstractBlock.Settings.copy(Blocks.DIAMOND_ORE).requiresTool()
                .strength(6.0f, 6.0f).sounds(BlockSoundGroup.STONE), Blocks.REDSTONE_ORE);
    }

    public static void registerBlocksAndItems(String id, AbstractBlock.Settings settings, Block visibleBlock) {
        Identifier identifier = Identifier.of(Lifesteal.MOD_ID, id);
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, identifier);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, identifier);

        settings.registryKey(blockKey);
        Block block = new SimplePolymerBlock(settings, visibleBlock);
        BlockItem item = new PolymerBlockItem(block, new Item.Settings().useBlockPrefixedTranslationKey().registryKey(itemKey), visibleBlock.asItem());

        Registry.register(Registries.BLOCK, blockKey, block);
        Registry.register(Registries.ITEM, itemKey, item);
    }
}
