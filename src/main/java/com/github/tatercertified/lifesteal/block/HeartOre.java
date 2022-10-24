package com.github.tatercertified.lifesteal.block;

import com.github.tatercertified.lifesteal.Loader;
import eu.pb4.polymer.ext.blocks.api.BlockModelType;
import eu.pb4.polymer.ext.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.ext.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.ext.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

public class HeartOre extends Block implements PolymerTexturedBlock {

    private final BlockState polymerBlockState;
    public HeartOre(Settings settings, BlockModelType type, String modelId) {
        super(settings);

        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
                type,
                PolymerBlockModel.of(new Identifier(Loader.MOD_ID, modelId)));

    }


    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.polymerBlockState.getBlock();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.polymerBlockState;
    }
}
