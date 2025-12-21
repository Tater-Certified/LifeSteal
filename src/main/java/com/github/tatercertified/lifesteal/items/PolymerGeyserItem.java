package com.github.tatercertified.lifesteal.items;

import de.olivermakesco.polyspring.api.BedrockItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.Properties;

public class PolymerGeyserItem extends SimplePolymerItem implements BedrockItem {

    public PolymerGeyserItem(Properties settings, Item polymerItem, boolean useModel) {
        super(settings, polymerItem, useModel);
    }

    @Override
    public String bedrockName() {
        return Component.translatable(this.getDescriptionId()).tryCollapseToString();
    }
}
