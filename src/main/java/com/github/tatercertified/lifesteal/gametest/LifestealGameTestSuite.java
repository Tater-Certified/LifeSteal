package com.github.tatercertified.lifesteal.gametest;

import com.github.tatercertified.lifesteal.gametest.structure.TestContext;
import com.github.tatercertified.lifesteal.gametest.tests.AltarTest;
import com.github.tatercertified.lifesteal.gametest.tests.HeartConsumptionTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.List;

public class LifestealGameTestSuite {

    @GameTest()
    public void runAll(GameTestHelper helper) {

        List<com.github.tatercertified.lifesteal.gametest.structure.GameTest> tests = List.of(
                new HeartConsumptionTest(),
                new AltarTest(),
                new HeartStealTest(),
                new DeathTest(),
                new ReviveTest(),
                new HeartStackTest(),
                new WithdrawCommandTest(),
                new GiftCommandTest()
        );

        new TestContext(helper, tests).start();
    }
}