package com.github.certifiedtater.lifesteal.block;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public class SimplePolymerTexturedBlock extends SimplePolymerBlock implements PolymerTexturedBlock {
    private final Identifier id;
    public SimplePolymerTexturedBlock(Settings settings, Block polymerBlock, Identifier identifier) {
        super(settings, polymerBlock);
        this.id = identifier;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        if (PolymerResourcePackUtils.hasMainPack(context)) {
            return PolymerBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK, PolymerBlockModel.of(Identifier.of(id.getNamespace(), "block/" + id.getPath())));
        } else {
            return super.getPolymerBlockState(state, context);
        }
    }
}
