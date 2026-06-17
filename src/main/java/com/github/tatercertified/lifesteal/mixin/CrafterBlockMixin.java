package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.items.ModItems;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.CrafterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrafterBlock.class)
public abstract class CrafterBlockMixin extends BaseEntityBlock {
    protected CrafterBlockMixin(Properties properties) {
        super(properties);
    }

    @Redirect(method = "dispenseFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0))
    private boolean lifesteal$cancelHeartCraft(ItemStack itemStack, @Local(argsOnly = true, name = "level") ServerLevel level) {
        return itemStack.isEmpty() || (itemStack.is(ModItems.HEART) && !level.getGameRules().get(LifeStealGamerules.HEART_CRAFT_IN_CRAFTER));
    }
}
