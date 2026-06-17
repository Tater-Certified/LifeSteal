package com.github.tatercertified.lifesteal.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class AltarRitualAnimation extends ParticleAnimation {

    private static final int RING_TICKS = 60;
    private static final int RING_HOLD_TICKS = 20;
    private static final int SPIRAL_TICKS = 80;
    private static final int HEART_HOLD_TICKS = 100;
    private static final int HEART_FORM_TICKS = 30;

    private static final int HEART_PARTICLE_COUNT = 36;
    private static final double RING_RADIUS = 1.0;
    private static final double HEART_SCALE = 1.2;


    private static final DustParticleOptions BLOOD =
            new DustParticleOptions(ARGB.color(new Vec3(0.6f, 0.0f, 0.0f)), 1.2f);

    private Vec3 ringCenter;
    private Vec3 heartCenter;
    private Vec3 spiralGoal;
    private BlockPos altarPos;

    public AltarRitualAnimation(BlockPos altarPos, ServerLevel level) {
        super(altarPos, level);
    }

    @Override
    public void create(BlockPos altarPos, ServerLevel level) {
        level.playSound(null, altarPos, SoundEvents.WITHER_AMBIENT, SoundSource.BLOCKS, 1.0f, 0.3f);
        this.altarPos = altarPos;
        ringCenter = Vec3.atCenterOf(altarPos);
        heartCenter = ringCenter.add(0, 3.0, 0);
        spiralGoal = heartCenter.subtract(0, 1.2, 0);
    }

    @Override
    void create(Entity reference, BlockPos referencePos) {}

    @Override
    public void tick(ServerLevel level) {
        switch (phase) {
            case 0 -> ringPhase(level);
            case 1 -> spiralPhase(level);
            case 2 -> heartFormPhase(level);
            case 3 -> heartHoldPhase(level);
        }
        super.tick(level);
    }

    @Override
    public void onDone(ServerLevel level) {
        level.playSound(null, altarPos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    // STEP 1: RING
    private void ringPhase(ServerLevel level) {
        double progress = phaseAge / (double) RING_TICKS;
        progress = Mth.clamp(progress, 0.0, 1.0);

        double maxAngle = progress * Mth.TWO_PI;
        int segments = 12;

        int visible = Math.max(1, (int) (segments * progress));
        for (int i = 0; i < visible; i++) {
            double angle = (i / (double) segments) * Mth.TWO_PI;
            if (angle > maxAngle) break;

            Vec3 pos = ringCenter.add(
                    Math.cos(angle) * RING_RADIUS,
                    0.05,
                    Math.sin(angle) * RING_RADIUS
            );
            spawn(level, pos, BLOOD);
        }

        if (phaseAge >= RING_TICKS) {
            if (shouldSpawnHeld()) {
                spawnRing(level);
            }

            if (phaseAge >= RING_TICKS + RING_HOLD_TICKS) {
                initSpiralParticles();
                nextPhase();
            }
        }
    }

    // STEP 2: SPIRAL
    private void initSpiralParticles() {
        for (int i = 0; i < HEART_PARTICLE_COUNT; i++) {
            TrackedParticle p = new TrackedParticle();
            p.t = 0;
            p.delay = (i % 4) * 4;
            p.angleOffset = (i / (double) HEART_PARTICLE_COUNT) * Mth.TWO_PI;
            trackedParticles.add(p);
        }
    }

    private void spiralPhase(ServerLevel level) {
        for (TrackedParticle p : trackedParticles) {
            if (p.delay-- > 0) {
                continue;
            }

            double t = p.t;
            double angle = t * 6 * Mth.TWO_PI + p.angleOffset;
            double radius = lerp(RING_RADIUS, 0.0, t);

            Vec3 pos = spiralGoal.add(
                    Math.cos(angle) * radius,
                    lerp(-2, 0, t),
                    Math.sin(angle) * radius
            );

            spawn(level, pos, BLOOD);
            p.t += 1.0 / SPIRAL_TICKS;
            p.pos = pos;
        }

        if (phaseAge >= SPIRAL_TICKS) {
            nextPhase();
        }
    }

    // STEP 3: HEART
    private void heartFormPhase(ServerLevel level) {
        int i = 0;
        for (TrackedParticle p : trackedParticles) {
            double targetT = (i++ / (double) HEART_PARTICLE_COUNT) * Mth.TWO_PI;
            Vec3 target = heartPoint(targetT)
                    .scale(HEART_SCALE)
                    .add(heartCenter);

            if (p.pos == null) p.pos = heartCenter;
            p.pos = p.pos.lerp(target, 0.1);
            spawn(level, p.pos, BLOOD);
        }

        if (phaseAge > HEART_FORM_TICKS) {
            nextPhase();
        }
    }

    private void heartHoldPhase(ServerLevel level) {
        if (shouldSpawnHeld()) {
            for (TrackedParticle p : trackedParticles) {
                spawn(level, p.pos, BLOOD);
            }
        }

        if (phaseAge >= HEART_HOLD_TICKS) {
            endPhase();
        }
    }

    // Helper functions

    private void spawnRing(ServerLevel level) {
        for (int i = 0; i < 12; i++) {
            double angle = (i / 12.0 + age * 0.02) * Mth.TWO_PI;
            Vec3 pos = ringCenter.add(
                    Math.cos(angle) * RING_RADIUS,
                    0.05,
                    Math.sin(angle) * RING_RADIUS
            );
            spawn(level, pos, BLOOD);
        }
    }

    private boolean shouldSpawnHeld() {
        return (age % 3) == 0;
    }

    // Heart Shape
    private static Vec3 heartPoint(double t) {
        double x = Math.pow(Math.sin(t), 3);
        double y =
                0.8125 * Math.cos(t)
                        - 0.3125 * Math.cos(2 * t)
                        - 0.125 * Math.cos(3 * t)
                        - 0.0625 * Math.cos(4 * t);
        return new Vec3(x, y, 0);
    }
}
