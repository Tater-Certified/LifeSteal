package com.github.tatercertified.lifesteal.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AltarRitualAnimation {

    private static final int RING_TICKS = 60;
    private static final int RING_HOLD_TICKS = 20;
    private static final int SPIRAL_TICKS = 80;
    private static final int HEART_HOLD_TICKS = 100;

    private static final int HEART_PARTICLE_COUNT = 36;
    private static final double RING_RADIUS = 0.75;
    private static final double HEART_SCALE = 1.2;


    private static final DustParticleOptions BLOOD =
            new DustParticleOptions(ARGB.color(new Vec3(0.6f, 0.0f, 0.0f)), 1.2f);

    private enum Phase {
        RING,
        SPIRAL,
        HEART_FORM,
        HEART_HOLD,
        DONE
    }

    private Phase phase = Phase.RING;

    private Vec3 ringCenter;
    private Vec3 heartCenter;
    private Vec3 spiralGoal;
    private BlockPos altarPos;

    private int age = 0;
    private int phaseAge = 0;

    private final List<TrackedParticle> heartParticles = new ArrayList<>();

    public static AltarRitualAnimation create(BlockPos altarPos, ServerLevel level) {
        level.playSound(null, altarPos, SoundEvents.WITHER_AMBIENT, SoundSource.BLOCKS, 1.0f, 0.3f);
        AltarRitualAnimation anim = new AltarRitualAnimation();
        anim.altarPos = altarPos;
        anim.ringCenter = altarPos.getCenter();
        anim.heartCenter = anim.ringCenter.add(0, 3.0, 0);
        anim.spiralGoal = anim.heartCenter.subtract(0, 1.2, 0);
        return anim;
    }

    public void tick(ServerLevel level) {
        switch (phase) {
            case RING -> ringPhase(level);
            case SPIRAL -> spiralPhase(level);
            case HEART_FORM -> heartFormPhase(level);
            case HEART_HOLD -> heartHoldPhase(level);
        }

        age++;
        phaseAge++;
    }

    public boolean isDone() {
        return phase == Phase.DONE;
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
            spawn(level, pos);
        }

        if (phaseAge >= RING_TICKS) {
            if (shouldSpawnHeld()) {
                spawnRing(level);
            }

            if (phaseAge >= RING_TICKS + RING_HOLD_TICKS) {
                initSpiralParticles();
                transition(Phase.SPIRAL);
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
            heartParticles.add(p);
        }
    }

    private void spiralPhase(ServerLevel level) {
        for (TrackedParticle p : heartParticles) {
            if (p.delay-- > 0) continue;

            double t = p.t;
            double angle = t * 6 * Mth.TWO_PI + p.angleOffset;
            double radius = lerp(RING_RADIUS, 0.0, t);

            Vec3 pos = spiralGoal.add(
                    Math.cos(angle) * radius,
                    lerp(-2, 0, t),
                    Math.sin(angle) * radius
            );

            spawn(level, pos);
            p.t += 1.0 / SPIRAL_TICKS;
            p.pos = pos;
        }

        if (phaseAge >= SPIRAL_TICKS) {
            transition(Phase.HEART_FORM);
        }
    }

    // STEP 3: HEART
    private void heartFormPhase(ServerLevel level) {
        int i = 0;
        for (TrackedParticle p : heartParticles) {
            double targetT = (i++ / (double) HEART_PARTICLE_COUNT) * Mth.TWO_PI;
            Vec3 target = heartPoint(targetT)
                    .scale(HEART_SCALE)
                    .add(heartCenter);

            if (p.pos == null) p.pos = heartCenter;
            p.pos = p.pos.lerp(target, 0.1);
            spawn(level, p.pos);
        }

        if (phaseAge > 30) {
            // TODO Figure out why this sound doesn't play
            level.playSound(null, altarPos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
            transition(Phase.HEART_HOLD);
        }
    }

    private void heartHoldPhase(ServerLevel level) {
        if (shouldSpawnHeld()) {
            for (TrackedParticle p : heartParticles) {
                spawn(level, p.pos);
            }
        }

        if (phaseAge >= HEART_HOLD_TICKS) {
            transition(Phase.DONE);
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
            spawn(level, pos);
        }
    }

    private void spawn(ServerLevel level, Vec3 pos) {
        level.sendParticles(BLOOD, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
    }

    private boolean shouldSpawnHeld() {
        return (age % 3) == 0;
    }

    private void transition(Phase next) {
        phase = next;
        phaseAge = 0;
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
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

    private static class TrackedParticle {
        Vec3 pos;
        double t;
        int delay;
        double angleOffset;
    }
}
