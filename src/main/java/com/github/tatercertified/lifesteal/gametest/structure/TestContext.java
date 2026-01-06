package com.github.tatercertified.lifesteal.gametest.structure;

import com.github.tatercertified.lifesteal.utils.LifestealMixinConfig;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.List;

public class TestContext {

    private final List<GameTest> tests;
    private int index = 0;
    private final GameTestHelper helper;

    public TestContext(GameTestHelper helper, List<GameTest> tests) {
        this.helper = helper;
        this.tests = tests;
    }

    public void start() {
        runCurrent();
    }

    public void nextTest() {
        index++;
        if (index >= tests.size()) {
            helper.succeed();
        } else {
            runCurrent();
        }
    }

    private void runCurrent() {
        GameTest test = tests.get(index);
        LifestealMixinConfig.TEST_LOGGER.info("Running test {}/{}: {}", index + 1, tests.size(), test.getClass().getSimpleName());
        test.start(helper, this);
    }
}
