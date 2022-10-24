package com.github.tatercertified.lifesteal.item;

import com.github.tatercertified.lifesteal.util.PolyLustUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;

public class ModItems {

    public static final Item HEARTDUST = dust();
    public static final Item HEARTCRYSTAL = crystal();
    public static final Item HEART = heart();

    private static Item dust() {
        return PolyLustUtils.ofModelled("heart_dust", Items.REDSTONE, ItemGroup.MISC);
    }

    private static Item crystal() {
        return PolyLustUtils.ofModelled("heart_crystal", Items.ECHO_SHARD, ItemGroup.MISC);
    }

    private static Item heart() {
        return PolyLustUtils.ofModelled("heart", Items.POTION, ItemGroup.MISC,
                (settings, modelData) -> new HeartItem(settings.maxCount(1), modelData));
    }

    public static void init() {
    }

    private ModItems() {
        throw new UnsupportedOperationException();
    }
}
