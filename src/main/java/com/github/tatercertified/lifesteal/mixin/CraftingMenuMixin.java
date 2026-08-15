package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.items.ModItems;
import com.github.tatercertified.lifesteal.utils.CraftedHeartsInterface;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {
    @WrapOperation(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;assemble(Lnet/minecraft/world/item/crafting/RecipeInput;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack lifesteal$checkIfHeartCrafted(CraftingRecipe instance, RecipeInput recipeInput, Operation<ItemStack> original, @Local(argsOnly = true) Player player) {
        ItemStack result = original.call(instance, recipeInput);
        if (result.is(ModItems.HEART)) {
            if (player instanceof ServerPlayer serverPlayer && ((CraftedHeartsInterface) serverPlayer).canCraftHeart()) {
                ((CraftedHeartsInterface) serverPlayer).incrementHeartsCrafted();
                return result;
            } else {
                return ItemStack.EMPTY;
            }
        } else {
            return result;
        }
    }
}
