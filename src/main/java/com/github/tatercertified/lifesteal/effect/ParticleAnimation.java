package com.github.tatercertified.lifesteal.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public abstract class ParticleAnimation {

    protected int age = 0;
    protected int phaseAge = 0;
    protected int phase = 0;

    protected final List<TrackedParticle> trackedParticles = new ArrayList<>();

    public ParticleAnimation(BlockPos pos, ServerLevel level) {
        create(pos, level);
    }

    public ParticleAnimation(Entity entity, BlockPos pos) {
        create(entity, pos);
    }

    abstract void create(BlockPos referencePos, ServerLevel level);
    abstract void create(Entity reference, BlockPos referencePos);
    public void tick(ServerLevel level) {
        age++;
        phaseAge++;
    }
    public boolean isDone() {
        return phase == -1;
    }
    public void onDone(ServerLevel level) {}

    protected static class TrackedParticle {
        Vec3 pos;
        double t;
        int delay;
        double angleOffset;
    }

    protected void spawn(ServerLevel level, Vec3 pos, ParticleOptions particleOptions) {
        level.sendParticles(particleOptions, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
    }

    protected void nextPhase() {
        phase++;
        phaseAge = 0;
    }

    protected void endPhase() {
        phase = -1;
    }

    protected static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
