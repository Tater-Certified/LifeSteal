package com.github.tatercertified.lifesteal.gametest.structure;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public abstract class GameTest {

    protected GameTestHelper helper;
    protected TestContext ctx;

    /**
     * Called once when test begins
     */
    public void start(GameTestHelper helper, TestContext ctx) {
        this.helper = helper;
        this.ctx = ctx;
        run();
    }

    /**
     * Implement test logic here
     */
    protected abstract void run();

    /**
     * Call when test is done
     */
    protected final void pass() {
        ctx.nextTest();
    }

    /**
     * Call on failure
     */
    protected final void fail(String message) {
        helper.fail(Component.literal(message));
    }

    /**
     * Execute with delay
     * @param ticks ticks to wait
     * @param r The task to run
     */
    protected void after(int ticks, Runnable r) {
        helper.runAfterDelay(ticks, r);
    }
}