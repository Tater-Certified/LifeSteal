package com.github.tatercertified.lifesteal.block;

import com.github.tatercertified.lifesteal.Lifesteal;
import de.olivermakesco.polyspring.api.BedrockBlock;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public class SimplePolymerTexturedBlock extends Block implements PolymerTexturedBlock, BedrockBlock {
    private final BlockState polymerBlockState;

    public SimplePolymerTexturedBlock(Properties settings, String modelId) {
        super(settings);

        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
                BlockModelType.FULL_BLOCK,
                PolymerBlockModel.of(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, modelId)));

    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.polymerBlockState;
    }

    @Override
    public String bedrockName() {
        return polymerBlockState.getBlock().getName().tryCollapseToString();
    }
}
