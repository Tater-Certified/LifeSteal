package com.github.tatercertified.lifesteal.items;

import com.github.tatercertified.lifesteal.Lifesteal;
import net.minecraft.component.DataComponentTypes;
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
            new HeartItem(new Item.Settings().maxCount(1).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).registryKey(key_heart)),
            key_heart
    );

    private static final RegistryKey<Item> key_totem = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Lifesteal.MOD_ID, "totem_of_rebirth"));
    public static final Item TOTEM = register(
            new TotemOfRebirth(new Item.Settings().maxCount(1).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).registryKey(key_totem)),
            key_totem
    );

    public static Item register(Item item, RegistryKey<Item> key) {
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void initialize() {
        RegistryKey<Item> key_heart_dust = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Lifesteal.MOD_ID, "heart_dust"));
        register(
                new PolymerGeyserItem(new Item.Settings().registryKey(key_heart_dust), Items.REDSTONE, true),
                key_heart_dust
        );
        RegistryKey<Item> key_heart_crystal = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Lifesteal.MOD_ID, "heart_crystal"));
        register(
                new PolymerGeyserItem(new Item.Settings().registryKey(key_heart_crystal), Items.ECHO_SHARD, true),
                key_heart_crystal
        );
    }
}
