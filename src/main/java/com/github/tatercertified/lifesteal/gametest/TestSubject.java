package com.github.tatercertified.lifesteal.gametest;

import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.mixin.FakePlayerAccessor;
import com.github.tatercertified.lifesteal.utils.LifestealMixinConfig;
import com.github.tatercertified.lifesteal.utils.PlayerMaxHealthInterface;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.gametest.framework.GameTestHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestSubject extends FakePlayer {
    protected TestSubject(ServerLevel world, GameProfile profile) {
        super(world, profile);
    }

    /**
     * Gets a random TestSubject instance
     * @param world ServerWorld instance
     * @return Random TestSubject instance
     */
    public static TestSubject getRandomTestSubject(ServerLevel world) {
        String name;
        do {
            name = "TEST" + ThreadLocalRandom.current().nextInt(0, 10000);
        } while (Arrays.asList(world.getServer().getPlayerList().getPlayerNamesArray()).contains(name));

        return TestSubject.getNew(world, new GameProfile(UUID.randomUUID(), name));
    }

    /**
     * Creates a new instance of TestSubject
     * @param world ServerWorld instance
     * @param profile GameProfile for the player
     * @return Instance of TestSubject
     */
    public static TestSubject getNew(ServerLevel world, GameProfile profile) {
        Objects.requireNonNull(world, "World may not be null.");
        Objects.requireNonNull(profile, "Game profile may not be null.");

        Object playerKey;
        try {
            playerKey = createFakePlayerKey(world, profile);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        ServerLevel playerWorld = null;
        GameProfile playerProfile = null;
        try {
            Field worldField = playerKey.getClass().getDeclaredField("world");
            Field profileField = playerKey.getClass().getDeclaredField("profile");

            worldField.setAccessible(true);
            profileField.setAccessible(true);

            playerWorld = (ServerLevel) worldField.get(playerKey);
            playerProfile = (GameProfile) profileField.get(playerKey);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            LifestealMixinConfig.TEST_LOGGER.error("Failed to create TestSubject instance", e);
        }

        ServerLevel finalPlayerWorld = playerWorld;
        GameProfile finalPlayerProfile = playerProfile;
        return (TestSubject) FakePlayerAccessor.getPlayerMap().computeIfAbsent(playerKey, key -> new TestSubject(finalPlayerWorld, finalPlayerProfile));
    }

    // Thank you ThePotatoKing :)
    private static Object createFakePlayerKey(ServerLevel world, GameProfile profile) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName("net.fabricmc.fabric.api.entity.FakePlayer$FakePlayerKey");
        Constructor<?> constructor = clazz.getDeclaredConstructor(ServerLevel.class, GameProfile.class);
        constructor.setAccessible(true);
        return constructor.newInstance(world, profile);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel world, DamageSource damageSource) {
        return false;
    }

    /**
     * Simulates killing the TestSubject
     */
    public void kill() {
        this.setHealth(0.0F);
        this.die(this.damageSources().genericKill());
    }

    /**
     * Simulates being killed by another TestSubject
     * @param attacker Other TestSubject
     */
    public void kill(TestSubject attacker) {
        this.setHealth(0.0F);
        this.setLastHurtByMob(attacker);
        this.die(this.damageSources().playerAttack(attacker));
    }

    /**
     * Simulates respawning the TestSubject
     */
    public void respawn() {
        this.setHealth((float) this.getAttribute(Attributes.MAX_HEALTH).getBaseValue());
    }

    /**
     * Removes the TestSubject
     */
    public void remove() {
        this.getInventory().clearContent();
        this.removeAllEffects();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0);
        this.setHealth(20.0F);
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    /**
     * Gets the max health of the TestSubject
     * @return Max health of the TestSubject
     */
    public double getMaxBaseHealth() {
        return this.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
    }

    /**
     * Sets the TestSubject's health to the lowest possible health before a DeathAction occurs
     * @param context TestContext instance
     */
    public void setLowMaxHealth(GameTestHelper context) {
        this.setMaxHealth(context.getLevel().getGameRules().get(LifeStealGamerules.MINPLAYERHEALTH));
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
        this.level().getServer().getCommands().performPrefixedCommand(this.createCommandSourceStack(), command);
    }
}
