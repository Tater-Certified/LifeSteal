package com.github.tatercertified.lifesteal.block;

import com.github.tatercertified.lifesteal.Lifesteal;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.resources.Identifier;

public class ModBlocks {
    public static void registerBlocks() {
        register("deepslate_heart_ore", Blocks.DEEPSLATE_REDSTONE_ORE, SoundType.DEEPSLATE);
        register("heart_ore", Blocks.REDSTONE_ORE, SoundType.STONE);
    }

    public static void register(String modelId, Block modelBlock, SoundType soundGroup) {
        var id = Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, modelId);
        var block = Registry.register(BuiltInRegistries.BLOCK, id,
                new SimplePolymerTexturedBlock(Block.Properties.ofFullCopy(Blocks.DIAMOND_ORE).requiresCorrectToolForDrops()
                        .strength(6.0f, 6.0f).sound(soundGroup).setId(ResourceKey.create(Registries.BLOCK, id)), "block/" + modelId));

        Registry.register(BuiltInRegistries.ITEM, id, new PolymerBlockItem(block, new net.minecraft.world.item.Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .useBlockDescriptionPrefix(),
                modelBlock.asItem(),
                true));
    }
}
