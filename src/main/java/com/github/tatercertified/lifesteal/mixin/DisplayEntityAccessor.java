package com.github.tatercertified.lifesteal.mixin;

import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface DisplayEntityAccessor {
    @Invoker
    void invokeSetBillboardConstraints(Display.BillboardConstraints mode);

    @Invoker
    void invokeSetTransformation(Transformation transformation);
}
