package com.github.certifiedtater.lifesteal.block;

import com.github.certifiedtater.lifesteal.Lifesteal;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static void registerBlocks() {
        register("deepslate_heart_ore", Blocks.DEEPSLATE_REDSTONE_ORE, BlockSoundGroup.DEEPSLATE);
        register("heart_ore", Blocks.REDSTONE_ORE, BlockSoundGroup.STONE);
    }

    public static void register(String modelId, Block modelBlock, BlockSoundGroup soundGroup) {
        var id = Identifier.of(Lifesteal.MOD_ID, modelId);
        var block = Registry.register(Registries.BLOCK, id,
                new SimplePolymerTexturedBlock(Block.Settings.copy(Blocks.DIAMOND_ORE).requiresTool()
                        .strength(6.0f, 6.0f).sounds(soundGroup), "block/" + modelId));

        Registry.register(Registries.ITEM, id, new SimplePolymerTexturedBlockItem(block, new Item.Settings(), "block/" + modelId, modelBlock));
    }
}
