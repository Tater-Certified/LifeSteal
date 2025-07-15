package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.items.HeartItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void lifesteal$redirectMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        if (this.getItem() instanceof HeartItem heart) {
            cir.setReturnValue(heart.getMaxCount());
        }
    }
}
