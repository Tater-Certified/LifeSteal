package com.github.tatercertified.lifesteal.gametest.tests;

import com.github.tatercertified.lifesteal.gametest.structure.GameTest;
import com.github.tatercertified.lifesteal.gametest.structure.GameTestUtils;
import com.github.tatercertified.lifesteal.gametest.structure.TestSubject;

public class HeartStealTest extends GameTest {
    @Override
    protected void run() {
        TestSubject[] subjects = GameTestUtils.spawnDoublePlayerTest(this.helper);
        // TODO Implement
    }
}
