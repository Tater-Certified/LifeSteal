package com.github.certifiedtater.lifesteal.block;

import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import com.github.certifiedtater.lifesteal.Lifesteal;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class SimplePolymerTexturedBlockItem extends BlockItem implements PolymerItem {
    private final Identifier polymerModel;
    private final Item polymerItem;
    public SimplePolymerTexturedBlockItem(Settings settings, Block block, String modelId, Item polymerItem) {
        super(block, settings);
        this.polymerModel = ResourcePackExtras.bridgeModel(Identifier.of(Lifesteal.MOD_ID, modelId));
        this.polymerItem = polymerItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext player) {
        return this.polymerItem;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.polymerModel;
    }
}
