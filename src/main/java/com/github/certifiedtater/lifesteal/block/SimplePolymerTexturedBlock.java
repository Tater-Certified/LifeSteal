package com.github.certifiedtater.lifesteal.block;

import com.github.certifiedtater.lifesteal.Lifesteal;
import de.olivermakesco.polyspring.api.BedrockBlock;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public class SimplePolymerTexturedBlock extends Block implements PolymerTexturedBlock, BedrockBlock {
    private final BlockState polymerBlockState;

    public SimplePolymerTexturedBlock(Settings settings, String modelId) {
        super(settings);

        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
                BlockModelType.FULL_BLOCK,
                PolymerBlockModel.of(Identifier.of(Lifesteal.MOD_ID, modelId)));

    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.polymerBlockState;
    }

    @Override
    public String bedrockName() {
        return polymerBlockState.getBlock().getName().getLiteralString();
    }
}
