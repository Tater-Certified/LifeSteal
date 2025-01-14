package com.github.certifiedtater.lifesteal.items;

import com.github.certifiedtater.lifesteal.utils.PolyUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class ModItems {

    public static final Item HEARTDUST = dust();
    public static final Item HEARTCRYSTAL = crystal();
    public static final Item HEART = heart();

    private static Item dust() {
        return PolyUtils.ofModelled("heart_dust", Items.REDSTONE);
    }

    private static Item crystal() {
        return PolyUtils.ofModelled("heart_crystal", Items.ECHO_SHARD);
    }

    private static Item heart() {
        return PolyUtils.ofModelled("heart", Items.HONEY_BOTTLE,
                (settings, modelData) -> new HeartItem(settings.maxCount(1), modelData));
    }

    public static void initialize() {
    }

    private ModItems() {
        throw new UnsupportedOperationException();
    }
}
