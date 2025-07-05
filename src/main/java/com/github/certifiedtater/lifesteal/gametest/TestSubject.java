package com.github.certifiedtater.lifesteal.gametest;

import com.github.certifiedtater.lifesteal.gamerules.LifeStealGamerules;
import com.github.certifiedtater.lifesteal.mixin.FakePlayerAccessor;
import com.github.certifiedtater.lifesteal.utils.LifestealMixinConfig;
import com.github.certifiedtater.lifesteal.utils.PlayerMaxHealthInterface;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestSubject extends FakePlayer {
    protected TestSubject(ServerWorld world, GameProfile profile) {
        super(world, profile);
    }

    /**
     * Gets a random TestSubject instance
     * @param world ServerWorld instance
     * @return Random TestSubject instance
     */
    public static TestSubject getRandomTestSubject(ServerWorld world) {
        String name;
        do {
            name = "TEST" + ThreadLocalRandom.current().nextInt(0, 10000);
        } while (Arrays.asList(world.getServer().getPlayerManager().getPlayerNames()).contains(name));

        return TestSubject.getNew(world, new GameProfile(UUID.randomUUID(), name));
    }

    /**
     * Creates a new instance of TestSubject
     * @param world ServerWorld instance
     * @param profile GameProfile for the player
     * @return Instance of TestSubject
     */
    public static TestSubject getNew(ServerWorld world, GameProfile profile) {
        Objects.requireNonNull(world, "World may not be null.");
        Objects.requireNonNull(profile, "Game profile may not be null.");

        Object playerKey;
        try {
            playerKey = createFakePlayerKey(world, profile);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        ServerWorld playerWorld = null;
        GameProfile playerProfile = null;
        try {
            Field worldField = playerKey.getClass().getDeclaredField("world");
            Field profileField = playerKey.getClass().getDeclaredField("profile");

            worldField.setAccessible(true);
            profileField.setAccessible(true);

            playerWorld = (ServerWorld) worldField.get(playerKey);
            playerProfile = (GameProfile) profileField.get(playerKey);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            LifestealMixinConfig.TEST_LOGGER.error("Failed to create TestSubject instance", e);
        }

        ServerWorld finalPlayerWorld = playerWorld;
        GameProfile finalPlayerProfile = playerProfile;
        return (TestSubject) FakePlayerAccessor.getPlayerMap().computeIfAbsent(playerKey, key -> new TestSubject(finalPlayerWorld, finalPlayerProfile));
    }

    // Thank you ThePotatoKing :)
    private static Object createFakePlayerKey(ServerWorld world, GameProfile profile) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName("net.fabricmc.fabric.api.entity.FakePlayer$FakePlayerKey");
        Constructor<?> constructor = clazz.getDeclaredConstructor(ServerWorld.class, GameProfile.class);
        constructor.setAccessible(true);
        return constructor.newInstance(world, profile);
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource damageSource) {
        return false;
    }

    /**
     * Simulates killing the TestSubject
     */
    public void kill() {
        this.setHealth(0.0F);
        this.onDeath(this.getDamageSources().genericKill());
    }

    /**
     * Simulates being killed by another TestSubject
     * @param attacker Other TestSubject
     */
    public void kill(TestSubject attacker) {
        this.setHealth(0.0F);
        this.setAttacker(attacker);
        this.onDeath(this.getDamageSources().playerAttack(attacker));
    }

    /**
     * Simulates respawning the TestSubject
     */
    public void respawn() {
        this.setHealth((float) this.getAttributeInstance(EntityAttributes.MAX_HEALTH).getBaseValue());
    }

    /**
     * Removes the TestSubject
     */
    public void remove() {
        this.getInventory().clear();
        this.clearStatusEffects();
        this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(20.0);
        this.setHealth(20.0F);
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    /**
     * Gets the max health of the TestSubject
     * @return Max health of the TestSubject
     */
    public double getMaxBaseHealth() {
        return this.getAttributeInstance(EntityAttributes.MAX_HEALTH).getBaseValue();
    }

    /**
     * Sets the TestSubject's health to the lowest possible health before a DeathAction occurs
     * @param context TestContext instance
     */
    public void setLowMaxHealth(TestContext context) {
        this.setMaxHealth(context.getWorld().getGameRules().get(LifeStealGamerules.MINPLAYERHEALTH).get());
    }

    /**
     * Sets the TestSubject's max health
     * @param value Health value
     */
    public void setMaxHealth(double value) {
        ((PlayerMaxHealthInterface)this).setBaseMaxHealth(value);
    }

    /**
     * Runs a command as if it were executed by the TestSubject
     * @param command Command string
     */
    public void executeCommand(String command) {
        this.getServer().getCommandManager().executeWithPrefix(this.getCommandSource(), command);
    }
}
