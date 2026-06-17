package com.github.tatercertified.lifesteal.effect;

import com.github.tatercertified.lifesteal.items.ModItems;
import com.github.tatercertified.lifesteal.mixin.DisplayEntityAccessor;
import com.github.tatercertified.lifesteal.mixin.ItemDisplayInvoker;
import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ReviveRitualAnimation extends ParticleAnimation {

    private static final int RISE_TICKS = 120;
    private static final int SPHERE_HOLD_TICKS = 80;
    private static final int SUPERNOVA_TICKS = 28;

    private static final double MAX_HEIGHT = 3.0;
    private static final double SPHERE_RADIUS = 1.2;

    private static final DustParticleOptions ENERGY =
            new DustParticleOptions(ARGB.color(new Vec3(.9f, 0.2f, 0.2f)), 1.1f);

    private Vec3 altarCenter;
    private Display.ItemDisplay heart;
    private Vec3 supernovaCenter;
    private float heartYaw = 0.0f;
    private float heartSpinSpeed = 2.5f;

    public ReviveRitualAnimation(BlockPos pos, ServerLevel level) {
        super(pos, level);
    }

    @Override
    void create(BlockPos referencePos, ServerLevel level) {
        this.altarCenter = Vec3.atCenterOf(referencePos);
        level.playSound(null, referencePos, SoundEvents.ENDER_DRAGON_AMBIENT, SoundSource.BLOCKS, 1.0f, 0.3f);
        spawnHeart(level);
    }

    @Override
    void create(Entity reference, BlockPos referencePos) {
        // not used
    }

    @Override
    public void tick(ServerLevel level) {
        switch (phase) {
            case 0 -> heartRise(level);
            case 1 -> stableSphere(level);
            case 2 -> supernova(level);
        }

        super.tick(level);
    }

    // PHASE 0: HEART RISE
    private void spawnHeart(ServerLevel level) {
        heart = new Display.ItemDisplay(EntityTypes.ITEM_DISPLAY, level);

        heart.snapTo(
                altarCenter.x,
                altarCenter.y + 0.9,
                altarCenter.z,
                0,
                90
        );

        heart.setNoGravity(true);

        ((ItemDisplayInvoker) heart)
                .invokeSetItemStack(new ItemStack(ModItems.HEART));

        ((ItemDisplayInvoker) heart)
                .invokeSetTransformationMode(ItemDisplayContext.GUI);

        ((DisplayEntityAccessor) heart)
                .invokeSetBillboardConstraints(Display.BillboardConstraints.FIXED);

        heart.setTransformationInterpolationDuration(0);
        level.addFreshEntity(heart);
    }

    private void heartRise(ServerLevel level) {
        double t = phaseAge / (double) RISE_TICKS;
        t = Mth.clamp(t, 0, 1);

        Vec3 heartPos = altarCenter.add(0, 0.9 + t * MAX_HEIGHT, 0);

        float pitch = (float) Mth.lerp(t, 90, 0);
        float yaw = age * 3.0f;

        heart.snapTo(heartPos.x, heartPos.y, heartPos.z, yaw, pitch);

        spawnCandleStreams(level, heartPos, t);

        if (phaseAge >= RISE_TICKS) {
            nextPhase();
        }
    }

    // CANDLE -> HEART STREAM
    private void spawnCandleStreams(ServerLevel level, Vec3 heartPos, double progress) {
        int rate = 1 + (int) (progress * 6);

        for (int i = 0; i < rate; i++) {
            Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(level.getRandom());

            Vec3 start = altarCenter.add(
                    d.getStepX(),
                    0,
                    d.getStepZ()
            );

            Vec3 pos = start.lerp(heartPos, level.getRandom().nextDouble() * progress);
            spawn(level, pos, ENERGY);
        }
    }

    // PHASE 1: STABLE SPHERE
    private void stableSphere(ServerLevel level) {
        if (heart == null) return;

        Vec3 center = heart.position();
        double time = age * 0.15;

        heartYaw += heartSpinSpeed;
        heartSpinSpeed = Math.min(heartSpinSpeed + 0.2f, 8.0f);

        heart.setYRot(heartYaw);
        heart.setXRot(0);

        for (int i = 0; i < 10; i++) {
            double phi = time + i * (Mth.TWO_PI / 10);
            double theta = i * 0.6;

            Vec3 offset = new Vec3(
                    Math.cos(phi) * Math.sin(theta),
                    Math.cos(theta),
                    Math.sin(phi) * Math.sin(theta)
            ).scale(SPHERE_RADIUS);

            spawn(level, center.add(offset), ENERGY);
        }

        if (level.getRandom().nextFloat() < 0.35f) {
            Vec3 randomOffset = randomPointInSphere(level.getRandom(), SPHERE_RADIUS * 0.9);
            spawn(level, center.add(randomOffset), ParticleTypes.ELECTRIC_SPARK);
        }

        if (phaseAge >= SPHERE_HOLD_TICKS) {
            nextPhase();
        }
    }

    private static Vec3 randomPointInSphere(RandomSource random, double radius) {
        Vec3 v;
        do {
            v = new Vec3(
                    random.nextDouble() * 2 - 1,
                    random.nextDouble() * 2 - 1,
                    random.nextDouble() * 2 - 1
            );
        } while (v.lengthSqr() > 1.0);

        return v.scale(radius);
    }

    // PHASE 2: SUPERNOVA
    private void supernova(ServerLevel level) {
        if (heart == null) return;

        if (phaseAge == 1) {
            supernovaCenter = heart.position();
        }

        double collapse = phaseAge / (double) SUPERNOVA_TICKS;
        collapse = Mth.clamp(collapse, 0.0, 1.0);

        float scaleValue = (float) Math.pow(1.0 - collapse, 2.5);
        scaleValue = Math.max(scaleValue, 0.001f);

        heartYaw -= 12.0f;

        Quaternionf rotation = new Quaternionf()
                .rotateY((heartYaw % 360) * Mth.DEG_TO_RAD);

        Vector3f scale = new Vector3f(scaleValue, scaleValue, scaleValue);

        Transformation transform = new Transformation(
                new Vector3f(0, 0, 0),
                rotation,
                scale,
                new Quaternionf()
        );

        ((DisplayEntityAccessor) heart).invokeSetTransformation(transform);

        double radius = collapse < 0.3
                ? SPHERE_RADIUS * (1.0 + collapse)
                : SPHERE_RADIUS * (1.6 - collapse * 2.0);

        int particleCount = Mth.clamp(
                (int) (24 * (1.0 - collapse) * (1.0 - collapse)),
                2,
                24
        );

        if (collapse < 0.9 || (age & 1) == 0) {
            for (int i = 0; i < particleCount; i++) {
                double a = level.getRandom().nextDouble() * Mth.TWO_PI;
                double b = level.getRandom().nextDouble() * Math.PI;

                Vec3 offset = new Vec3(
                        Math.cos(a) * Math.sin(b),
                        Math.cos(b),
                        Math.sin(a) * Math.sin(b)
                ).scale(radius);

                spawn(level, supernovaCenter.add(offset), ENERGY);
            }
        }

        if (phaseAge >= SUPERNOVA_TICKS) {
            heart.discard();
            heart = null;
            endPhase();
        }
    }

    private void fireSonicBoom(ServerLevel level) {
        Vec3 start = supernovaCenter.add(0, 0.2, 0);
        Vec3 end = altarCenter.add(0, 0.1, 0);

        Vec3 delta = end.subtract(start);
        Vec3 direction = delta.normalize();

        int steps = Mth.floor(delta.length()) + 7;

        for (int i = 1; i < steps; i++) {
            Vec3 pos = start.add(direction.scale(i));

            level.sendParticles(
                    ParticleTypes.SONIC_BOOM,
                    pos.x,
                    pos.y,
                    pos.z,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
            );
        }
    }

    @Override
    public void onDone(ServerLevel level) {
        level.playSound(null, BlockPos.containing(altarCenter), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 1.0f, 0.3f);
        fireSonicBoom(level);
    }
}
