package com.github.certifiedtater.lifesteal.block;

import com.github.certifiedtater.lifesteal.Lifesteal;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class SimplePolymerTexturedBlockItem extends BlockItem implements PolymerItem {

    private final PolymerModelData polymerModel;

    public SimplePolymerTexturedBlockItem(Block block, Settings settings, String modelId, Block modelBlock) {
        super(block, settings);
        this.polymerModel = PolymerResourcePackUtils.requestModel(modelBlock.asItem(), Identifier.of(Lifesteal.MOD_ID, modelId));

    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.polymerModel.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.polymerModel.value();
    }
}
