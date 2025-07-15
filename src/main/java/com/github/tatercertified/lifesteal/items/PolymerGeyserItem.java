package com.github.tatercertified.lifesteal.items;

import de.olivermakesco.polyspring.api.BedrockItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

public class PolymerGeyserItem extends SimplePolymerItem implements BedrockItem {

    public PolymerGeyserItem(Settings settings, Item polymerItem, boolean useModel) {
        super(settings, polymerItem, useModel);
    }

    @Override
    public String bedrockName() {
        return Text.translatable(this.getTranslationKey()).getLiteralString();
    }
}
