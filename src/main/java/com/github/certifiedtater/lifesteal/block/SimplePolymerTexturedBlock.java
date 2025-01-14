package com.github.certifiedtater.lifesteal.block;

import com.github.certifiedtater.lifesteal.Lifesteal;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

public class SimplePolymerTexturedBlock extends Block implements PolymerTexturedBlock {
    private final BlockState polymerBlockState;

    public SimplePolymerTexturedBlock(Settings settings, String modelId) {
        super(settings);

        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
                BlockModelType.FULL_BLOCK,
                PolymerBlockModel.of(Identifier.of(Lifesteal.MOD_ID, modelId)));

    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState) {
        return this.polymerBlockState;
    }
}
