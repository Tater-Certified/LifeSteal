package com.github.tatercertified.lifesteal.items;

import com.github.tatercertified.lifesteal.Lifesteal;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public class ModItems {

    private static final ResourceKey<Item> key_heart = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "heart"));
    public static final Item HEART = register(
            new HeartItem(new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).setId(key_heart)),
            key_heart
    );

    private static final ResourceKey<Item> key_totem = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "totem_of_rebirth"));
    public static final Item TOTEM = register(
            new TotemOfRebirth(new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).setId(key_totem)),
            key_totem
    );

    public static Item register(Item item, ResourceKey<Item> key) {
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void initialize() {
        ResourceKey<Item> key_heart_dust = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "heart_dust"));
        register(
                new PolymerGeyserItem(new Item.Properties().setId(key_heart_dust), Items.REDSTONE, true),
                key_heart_dust
        );
        ResourceKey<Item> key_heart_crystal = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "heart_crystal"));
        register(
                new PolymerGeyserItem(new Item.Properties().setId(key_heart_crystal), Items.ECHO_SHARD, true),
                key_heart_crystal
        );
    }
}
