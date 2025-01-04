package com.github.certifiedtater.lifesteal.items;

import com.github.certifiedtater.lifesteal.Lifesteal;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {

    private static final RegistryKey<Item> key_heart = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Lifesteal.MOD_ID, "heart"));

    public static final Item HEART = register(
            new HeartItem(new Item.Settings().maxCount(1).registryKey(key_heart)),
            key_heart
    );

    private static final RegistryKey<Item> key_heart_crystal = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Lifesteal.MOD_ID, "heart_crystal"));
    public static final Item HEART_CRYSTAL = register(
            new SimplePolymerItem(new Item.Settings().registryKey(key_heart_crystal), Items.ECHO_SHARD, true),
            key_heart_crystal
    );

    private static final RegistryKey<Item> key_heart_dust = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Lifesteal.MOD_ID, "heart_dust"));
    public static final Item HEART_DUST = register(
            new SimplePolymerItem(new Item.Settings().registryKey(key_heart_dust), Items.REDSTONE, true),
           key_heart_dust
    );

    public static Item register(Item item, RegistryKey<Item> key) {
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void initialize() {
    }
}
