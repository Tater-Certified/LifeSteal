package com.github.tatercertified.lifesteal.effect;

import com.github.tatercertified.lifesteal.items.ModItems;
import com.github.tatercertified.lifesteal.mixin.DisplayEntityAccessor;
import com.github.tatercertified.lifesteal.mixin.ItemDisplayInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class WithdrawRitualAnimation extends ParticleAnimation {

    private static final DustParticleOptions HAND =
            new DustParticleOptions(ARGB.color(new Vec3(0.02f, 0.02f, 0.02f)), 1.1f);

    private static final int ARM_TICKS = 30;
    private static final int REACH_TICKS = 20;
    private static final int HOLD_TICKS = 10;
    private static final int RETURN_TICKS = 30;

    private Entity reference;

    private Vec3 altarCenter;
    private Vec3 grabPoint;

    private Display.ItemDisplay heartDisplay;

    public WithdrawRitualAnimation(Entity entity, BlockPos pos) {
        super(entity, pos);
    }

    @Override
    void create(BlockPos referencePos, ServerLevel level) {
        // not used
    }

    @Override
    void create(Entity reference, BlockPos referencePos) {
        this.reference = reference;
        this.altarCenter = referencePos.getCenter().add(0, 0.2, 0);
        if (reference.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, referencePos, SoundEvents.PHANTOM_SWOOP, SoundSource.BLOCKS, 1.0f, 0.3f);
        }
    }

    @Override
    public void tick(ServerLevel level) {
        if (reference == null || !reference.isAlive()) {
            endPhase();
            return;
        }

        switch (phase) {
            case 0 -> fingersReach(level);
            case 1 -> grab(level);
            case 2 -> retract(level);
        }

        super.tick(level);
    }

    // Phases

    // Phase 0: Fingers arc toward player
    private void fingersReach(ServerLevel level) {
        double t = phaseAge / (double) REACH_TICKS;
        t = Mth.clamp(t, 0, 1);

        grabPoint = reference.position().add(0, 1.2, 0);

        Vec3 wrist = altarCenter.add(0, 1.6, 0);

        for (int i = 0; i < 4; i++) {
            double angle = i * Mth.HALF_PI;
            Vec3 offset = new Vec3(
                    Math.cos(angle) * 0.25,
                    0,
                    Math.sin(angle) * 0.25
            );

            Vec3 fingerEnd = wrist
                    .add(offset)
                    .lerp(grabPoint, t);

            spawnSegment(level, wrist.add(offset), fingerEnd, 4);
        }

        if (phaseAge >= REACH_TICKS) {
            nextPhase();
        }
    }

    // Phase 1: Spawn & grab heart
    private void grab(ServerLevel level) {
        if (phaseAge == 1) {
            heartDisplay = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);

            heartDisplay.snapTo(
                    grabPoint.x,
                    grabPoint.y,
                    grabPoint.z,
                    0.0f,
                    0.0f
            );

            heartDisplay.setNoGravity(true);

            ((ItemDisplayInvoker) heartDisplay)
                    .invokeSetItemStack(new ItemStack(ModItems.HEART));

            ((ItemDisplayInvoker) heartDisplay)
                    .invokeSetTransformationMode(ItemDisplayContext.GUI);

            ((DisplayEntityAccessor) heartDisplay)
                    .invokeSetBillboardConstraints(Display.BillboardConstraints.FIXED);

            heartDisplay.setTransformationInterpolationDuration(0);

            heartDisplay.setTransformationInterpolationDuration(3);
            heartDisplay.setPosRotInterpolationDuration(3);

            level.addFreshEntity(heartDisplay);

            level.playSound(null, BlockPos.containing(grabPoint), SoundEvents.PHANTOM_BITE, SoundSource.BLOCKS, 1.0f, 0.3f);
        }

        if (phaseAge >= HOLD_TICKS) {
            nextPhase();
        }
    }

    // Phase 2: Pull heart back into altar
    private void retract(ServerLevel level) {
        double t = phaseAge / (double) RETURN_TICKS;
        t = Mth.clamp(t, 0, 1);

        Vec3 wrist = altarCenter.add(0, 1.6 * (1 - t), 0);

        spawnSegment(level, wrist, altarCenter, 6);

        if (heartDisplay != null) {
            Vec3 pos = heartDisplay.position().lerp(wrist, 0.3);
            heartDisplay.setPos(pos.x, pos.y, pos.z);
        }

        if (phaseAge >= RETURN_TICKS) {
            if (heartDisplay != null) {
                heartDisplay.discard();
            }
            endPhase();
        }
    }

    private void spawnSegment(ServerLevel level, Vec3 from, Vec3 to, int points) {
        if ((age & 1) != 0) {
            return;
        }

        for (int i = 0; i <= points; i++) {
            double t = i / (double) points;
            Vec3 p = from.lerp(to, t);
            spawn(level, p, HAND);
        }
    }

    @Override
    public void onDone(ServerLevel level) {
        if (heartDisplay != null) {
            heartDisplay.discard();
        }
        ItemEntity itemEntity = new ItemEntity(level, altarCenter.x, altarCenter.y + 0.3, altarCenter.z, new ItemStack(ModItems.HEART, 1), 0, 0, 0);
        level.addFreshEntity(itemEntity);
    }
}