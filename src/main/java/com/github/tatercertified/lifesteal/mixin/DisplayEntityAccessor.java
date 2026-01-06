package com.github.tatercertified.lifesteal.mixin;

import com.mojang.math.Transformation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface DisplayEntityAccessor {
    @Accessor("DATA_SCALE_ID")
    static EntityDataAccessor<Vector3f> getScale() {
        throw new AssertionError();
    }

    @Accessor("DATA_BRIGHTNESS_OVERRIDE_ID")
    static EntityDataAccessor<Integer> getBrightness() {
        throw new AssertionError();
    }

    @Accessor("DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID")
    static EntityDataAccessor<Integer> getTransInterpolationDuration() {
        throw new AssertionError();
    }

    @Accessor("DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID")
    static EntityDataAccessor<Integer> getInterpolationDelta() {
        throw new AssertionError();
    }

    @Accessor("DATA_TRANSLATION_ID")
    static EntityDataAccessor<Vector3f> getTranslation() {
        throw new AssertionError();
    }

    @Invoker
    void invokeSetBillboardConstraints(Display.BillboardConstraints mode);

    @Invoker
    void invokeSetTransformation(Transformation transformation);
}
