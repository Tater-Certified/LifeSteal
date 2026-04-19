package com.github.tatercertified.lifesteal.block;

import com.github.tatercertified.lifesteal.Lifesteal;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class SimplePolymerTexturedBlock extends Block implements PolymerTexturedBlock {
    private final BlockState polymerBlockState;

    public SimplePolymerTexturedBlock(Properties settings, String modelId) {
        super(settings);

        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
                BlockModelType.FULL_BLOCK,
                PolymerBlockModel.of(Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, modelId)));

    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return this.polymerBlockState;
    }
}
