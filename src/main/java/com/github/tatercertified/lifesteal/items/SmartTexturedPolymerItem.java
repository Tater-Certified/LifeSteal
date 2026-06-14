package com.github.tatercertified.lifesteal.items;

import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class SmartTexturedPolymerItem extends SimplePolymerItem {

    public SmartTexturedPolymerItem(Properties settings, Item polymerItem, boolean useModel) {
        super(settings, polymerItem, useModel);
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        if (PolymerResourcePackUtils.hasPack(context, context.get(PacketContext.GAME_PROFILE).id())) {
            return super.getPolymerItemModel(stack, context, lookup);
        } else if (context.get(PacketContext.SERVER_INSTANCE).getGameRules().get(LifeStealGamerules.FALLBACK_TEXTURES)) {
            return Items.NETHER_STAR.getDefaultInstance().get(DataComponents.ITEM_MODEL);
        } else {
            return null;
        }
    }
}
