package com.github.tatercertified.lifesteal.gametest.tests;

import com.github.tatercertified.lifesteal.gametest.structure.GameTest;
import com.github.tatercertified.lifesteal.gametest.structure.GameTestUtils;
import com.github.tatercertified.lifesteal.gametest.structure.TestSubject;
import com.github.tatercertified.lifesteal.items.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class HeartConsumptionTest extends GameTest {

    @Override
    protected void run() {
        TestSubject player = GameTestUtils.spawnSinglePlayer(helper);
        ItemStack heart = new ItemStack(ModItems.HEART);

        after(1, () -> player.setItemInHand(InteractionHand.MAIN_HAND, heart));
        after(2, () -> heart.use(player.level(), player, InteractionHand.MAIN_HAND));

        after(3, () -> {
            if (player.getMaxBaseHealth() != 22.0F) {
                fail("Expected 22 max health");
                return;
            }
            GameTestUtils.cleanup(player);
            pass();
        });
    }
}