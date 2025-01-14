package com.github.certifiedtater.lifesteal.utils;// Created 2022-13-07T01:12:20

import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

import static com.github.certifiedtater.lifesteal.Lifesteal.MOD_ID;


/**
 * @author Ampflower
 * @since ${version}
 **/
public class PolyUtils {

    /**
     * Creates &amp; registers a Polymer item with a model bound for the given masking item.
     *
     * @param path  The path of the item in the registry.
     * @param mask  The vanilla item to act as the mask.
     * @return A registered Polymer item ready for use.
     */
    public static Item ofModelled(@NotNull String path, @NotNull Item mask) {
        Objects.requireNonNull(path, "Invalid registry path.");
        check(mask);
        var item = new ModelledPolymerItem(new Item.Settings(), getModelData(path, mask));
        registerItem(path, item);
        return item;
    }

    /**
     * Creates &amp; registers a customised Polymer item with a model bound for the given masking item.
     *
     * @param path        The path of the item in the registry.
     * @param mask        The vanilla item to act as the mask.
     * @param constructor The constructor reference or lambda for a custom Item instance.
     * @return A registered custom Polymer item ready for use.
     */
    public static <T extends Item> T ofModelled(@NotNull String path, @NotNull Item mask,
                                                BiFunction<Item.Settings, PolymerModelData, T> constructor) {
        Objects.requireNonNull(path, "Invalid registry path.");
        check(mask);
        T item = constructor.apply(new Item.Settings(), getModelData(path, mask));
        registerItem(path, item);
        return item;
    }

    /**
     * Helper method to get custom model data for the item.
     *
     * @param path The path of the item's model.
     * @param mask The vanilla item acting as the mask.
     * @return The PolymerModelData referencing the item and model.
     */
    public static PolymerModelData getModelData(String path, Item mask) {
        return PolymerResourcePackUtils.requestModel(mask, Identifier.of(MOD_ID, "item/" + path));
    }

    /**
     * Helper method to register an item for Lustrousness.
     *
     * @param path The path of the item in the registry.
     * @param item The item to register.
     */
    public static void registerItem(String path, Item item) {
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, path), item);
    }

    /**
     * Checks item to ensure not-null & vanilla.
     */
    private static void check(Item item) {
        Objects.requireNonNull(item, "Invalid item for mask.");
        var identifier = Registries.ITEM.getId(item);
        if (!"minecraft".equals(identifier.getNamespace())) {
            throw new IllegalArgumentException("Non-vanilla item " + item + " (" + identifier + ")");
        }
    }
}
