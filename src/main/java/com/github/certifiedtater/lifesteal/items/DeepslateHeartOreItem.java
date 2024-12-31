package com.github.certifiedtater.lifesteal.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import xyz.nucleoid.packettweaker.PacketContext;

public class DeepslateHeartOreItem extends BlockItem implements PolymerItem {

    public DeepslateHeartOreItem(Settings settings, Block block) {
        super(block, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.DEEPSLATE_REDSTONE_ORE;
    }
}