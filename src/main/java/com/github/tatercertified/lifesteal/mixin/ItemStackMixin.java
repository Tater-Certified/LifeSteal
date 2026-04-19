package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.items.HeartItem;
import com.github.tatercertified.lifesteal.items.ModItems;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInstance.class)
public interface ItemStackMixin extends TypedInstance<Item>, DataComponentGetter {

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void lifesteal$redirectMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        if (this.is(ModItems.HEART)) {
            cir.setReturnValue(((HeartItem)this).getDefaultMaxStackSize());
        }
    }
}
