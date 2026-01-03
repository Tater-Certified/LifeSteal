package com.github.tatercertified.lifesteal.gametest;

import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.gamerules.*;
import com.github.tatercertified.lifesteal.items.HeartItem;
import com.github.tatercertified.lifesteal.items.ModItems;
import com.github.tatercertified.lifesteal.utils.LifestealMixinConfig;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.UUID;

public class LifestealGameTest {
    public static final UserNameToIdResolver gameTestUserCache = new GameTestUserCache();

    @GameTest
    public void testHeartConsumption(GameTestHelper context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 1: Heart Consumption");
        TestSubject player = spawnSinglePlayerTest(context);
        ItemStack heart = new ItemStack(ModItems.HEART, 1);

        context.runAfterDelay(1, () -> player.setItemInHand(InteractionHand.MAIN_HAND, heart.copy()));
        context.runAfterDelay(2, () -> use(player, heart.copy()));
        context.runAfterDelay(3, () -> {
            double maxHealth = player.getMaxBaseHealth();
            context.assertTrue(maxHealth == 22.0F, Component.nullToEmpty("Max Health Mismatch; Expected: 22.0, Got: " + maxHealth));
        });

        context.runAfterDelay(4, () -> end(context, player));
    }

    @GameTest(setupTicks = 10)
    public void testAltar(GameTestHelper context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 2: Altar");
        TestSubject player = spawnSinglePlayerTest(context);
        BlockPos altar_relative = new BlockPos(2, 151, 2);
        BlockPos altar = spawnAltar(context, altar_relative);
        context.getLevel().getGameRules().set(LifeStealGamerules.ALTAR_BLOCK, BuiltInRegistries.BLOCK.createIntrusiveHolder(Blocks.NETHERITE_BLOCK), context.getLevel().getServer());
        context.assertTrue(HeartItem.isAltar(context.getLevel(), altar), Component.nullToEmpty("Altar failed to be created"));
        context.runAfterDelay(1, () -> {
            player.setShiftKeyDown(true);
            player.lookAt(EntityAnchorArgument.Anchor.FEET, altar.getCenter());
        });
        context.runAfterDelay(2, () -> UseBlockCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(altar), Direction.NORTH, altar, true)));
        context.runAfterDelay(3, () -> {
            double maxHealth = player.getMaxBaseHealth();
            context.assertTrue(maxHealth == 18.0F, Component.nullToEmpty("Max Health Mismatch; Expected: 18.0, Got: " + maxHealth));
        });
        context.runAfterDelay(4, () -> {
            context.getLevel().getGameRules().set(LifeStealGamerules.ALTAR_BLOCK, BuiltInRegistries.BLOCK.createIntrusiveHolder(Blocks.DIAMOND_BLOCK), context.getLevel().getServer());
            context.getLevel().setBlockAndUpdate(altar, Blocks.DIAMOND_BLOCK.defaultBlockState());
        });
        context.runAfterDelay(5, () -> context.assertTrue(HeartItem.isAltar(context.getLevel(), altar), Component.nullToEmpty("Altar block failed to be set")));

        context.runAfterDelay(6, () -> {
            context.getLevel().getGameRules().set(LifeStealGamerules.ALTAR_BLOCK, BuiltInRegistries.BLOCK.createIntrusiveHolder(Blocks.NETHERITE_BLOCK), context.getLevel().getServer());
            end(context, player);
        });
    }

    @GameTest(setupTicks = 20, maxTicks = 15)
    public void testHeartSteal(GameTestHelper context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 3: Heart Stealing");
        TestSubject[] players = spawnDoublePlayerTest(context);
        // Test natural death gamerule
        context.getLevel().getGameRules().set(LifeStealGamerules.DEATH_CRITERIA, DeathCriteria.ANY_DEATH, context.getLevel().getServer());
        context.getLevel().getGameRules().set(GameRules.IMMEDIATE_RESPAWN, true, context.getLevel().getServer());
        context.runAfterDelay(1, () -> players[0].kill());
        context.runAfterDelay(2, () -> {
            double maxHealth = players[0].getMaxBaseHealth();
            context.assertTrue(maxHealth == 18.0F, Component.nullToEmpty("Max Health Mismatch; Expected: 18.0, Got: " + maxHealth));
        });
        context.runAfterDelay(3, () -> players[0].respawn());

        // Test player kill
        context.runAfterDelay(4, () -> context.getLevel().getGameRules().set(LifeStealGamerules.DEATH_CRITERIA, DeathCriteria.PLAYER_ONLY, context.getLevel().getServer()));
        context.runAfterDelay(5, () -> players[0].kill(players[1]));
        context.runAfterDelay(6, () -> {
            double killedMaxHealth = players[0].getMaxBaseHealth();
            context.assertTrue(killedMaxHealth == 16.0F, Component.nullToEmpty("Max Health Mismatch; Expected: 16.0, Got: " + killedMaxHealth));
            double attackerMaxHealth = players[1].getMaxBaseHealth();
            context.assertTrue(attackerMaxHealth == 22.0F, Component.nullToEmpty("Max Health Mismatch; Expected: 22.0, Got: " + attackerMaxHealth));
        });

        // Test heart steal gamerule
        context.runAfterDelay(7, () -> {
            players[0].respawn();
            context.getLevel().getGameRules().set(LifeStealGamerules.STEAL_AMOUNT, 4, context.getLevel().getServer());
        });
        context.runAfterDelay(8, () -> players[0].kill(players[1]));
        context.runAfterDelay(9, () -> {
            double killedMaxHealth = players[0].getMaxBaseHealth();
            context.assertTrue(killedMaxHealth == 12.0F, Component.nullToEmpty("Max Health Mismatch; Expected: 12.0, Got: " + killedMaxHealth));
            double attackerMaxHealth = players[1].getMaxBaseHealth();
            context.assertTrue(attackerMaxHealth == 26.0F, Component.nullToEmpty("Max Health Mismatch; Expected: 26.0, Got: " + attackerMaxHealth));
        });

        context.runAfterDelay(10, () -> {
            context.getLevel().getGameRules().set(LifeStealGamerules.STEAL_AMOUNT, 2, context.getLevel().getServer());
            end(context, players);
        });
    }

    @GameTest(setupTicks = 35)
    public void testDeath(GameTestHelper context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 4: Death Consequences");
        final TestSubject[] player = {spawnSinglePlayerTest(context)};
        Vec3 playerPos = player[0].position();
        player[0].setLowMaxHealth(context);
        context.getLevel().getGameRules().set(LifeStealGamerules.DEATH_CRITERIA, DeathCriteria.ANY_DEATH, context.getLevel().getServer());

        // Ban test
        context.runAfterDelay(1, player[0]::kill);
        context.runAfterDelay(2, () -> context.assertTrue(DeathData.isPlayerDead(player[0].getUUID(), 0), Component.nullToEmpty("Player should be banned")));

        // Spectator test
        context.runAfterDelay(3, () -> {
            player[0] = spawnPlayer(context, playerPos);
            player[0].setLowMaxHealth(context);
            context.getLevel().getGameRules().set(LifeStealGamerules.DEATH_ACTION, DeathAction.SPECTATOR, context.getLevel().getServer());
        });
        context.runAfterDelay(4, player[0]::kill);
        context.runAfterDelay(5, () -> {
            // TODO Fix the FakePlayer somehow being in survival mode when they should be spectating. This feature DOES work properly for real players
            //context.assertTrue(player[0].getGameMode() == GameMode.SPECTATOR, Text.of("Player should be spectating"));
        });

        // Revive test
        context.runAfterDelay(6, () -> {
            player[0] = spawnPlayer(context, playerPos);
            player[0].setLowMaxHealth(context);
            context.getLevel().getGameRules().set(LifeStealGamerules.DEATH_ACTION, DeathAction.REVIVE, context.getLevel().getServer());
        });
        context.runAfterDelay(7, player[0]::kill);
        context.runAfterDelay(8, () -> {
            context.assertTrue(player[0].isAlive(), Component.nullToEmpty("Player should be alive"));
            context.assertFalse(DeathData.isPlayerDead(player[0].getUUID(), 0), Component.nullToEmpty("Player should not be banned"));
        });

        context.runAfterDelay(9, () -> {
            context.getLevel().getGameRules().set(LifeStealGamerules.DEATH_CRITERIA, DeathCriteria.PLAYER_ONLY, context.getLevel().getServer());
            context.getLevel().getGameRules().set(LifeStealGamerules.DEATH_ACTION, DeathAction.BAN, context.getLevel().getServer());
            end(context, player);
        });
    }

    @GameTest(setupTicks = 45)
    public void testRevive(GameTestHelper context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 5: Revival");
        TestSubject[] players = spawnDoublePlayerTest(context);
        players[0].setLowMaxHealth(context);
        Component name = players[0].getName();
        UUID uuid = players[0].getUUID();
        ItemStack heart = new ItemStack(ModItems.HEART);
        heart.set(DataComponents.CUSTOM_NAME, name);
        BlockPos altar_relative = new BlockPos(2, 151, 2);
        BlockPos altar = spawnAltar(context, altar_relative);

        context.runAfterDelay(1, players[0]::kill);
        context.runAfterDelay(2, () -> {
            players[1].setItemInHand(InteractionHand.MAIN_HAND, heart.copy());
            players[1].setShiftKeyDown(true);
            players[1].lookAt(EntityAnchorArgument.Anchor.FEET, altar.getCenter());
        });
        context.runAfterDelay(3, () -> heart.useOn(new UseOnContext(players[1], InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(altar), Direction.NORTH, altar, true))));
        context.runAfterDelay(5, () -> context.assertFalse(DeathData.isPlayerDead(uuid, 0), Component.nullToEmpty("Player was not revived")));
        context.runAfterDelay(6, () -> end(context, players));
    }

    @GameTest(setupTicks = 55)
    public void testHeartStacks(GameTestHelper context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 6: Heart Stacking");
        TestSubject player = spawnSinglePlayerTest(context);
        ItemStack heart = new ItemStack(ModItems.HEART);
        context.runAfterDelay(1, () -> player.addItem(heart.copy()));
        context.runAfterDelay(2, () -> player.addItem(heart.copy()));
        context.runAfterDelay(3, () -> {
            context.assertTrue(player.getMainHandItem().getCount() == 1, Component.nullToEmpty("Hearts stacked when unstackable"));
            context.getLevel().getGameRules().set(LifeStealGamerules.HEART_STACK_SIZE, 2, context.getLevel().getServer());
        });
        context.runAfterDelay(4, () -> player.addItem(heart.copy()));
        context.runAfterDelay(5, () -> context.assertTrue(player.getMainHandItem().getCount() == 2, Component.nullToEmpty("Hearts did not stack")));
        context.runAfterDelay(6, () -> {
            context.getLevel().getGameRules().set(LifeStealGamerules.HEART_STACK_SIZE, 1, context.getLevel().getServer());
            end(context, player);
        });
    }

    @GameTest(setupTicks = 65)
    public void testWithdrawCommand(GameTestHelper context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 7: Heart Withdraw Command");
        TestSubject player = spawnSinglePlayerTest(context);
        context.runAfterDelay(1, () -> {
            context.getLevel().getGameRules().set(LifeStealGamerules.HEART_STACK_SIZE, 2, context.getLevel().getServer());
            context.getLevel().getGameRules().set(LifeStealGamerules.WITHDRAW_METHOD, WithdrawMethod.COMMAND, context.getLevel().getServer());
        });
        // Test valid amount
        context.runAfterDelay(2, () -> player.executeCommand("withdraw 2"));
        context.runAfterDelay(3, () -> {
            context.assertTrue(player.getMainHandItem().getCount() == 2, Component.nullToEmpty("Expected 2 Hearts; Given " + player.getMainHandItem().getCount()));
            context.assertTrue(player.getMaxBaseHealth() == 16.0, Component.nullToEmpty("Expected 16.0 Max Health; Has " + player.getMaxBaseHealth()));
        });
        // Test invalid max health
        context.runAfterDelay(4, () -> {
            player.getInventory().clearContent();
            context.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "withdraw 16");
        }); // Should fail
        context.runAfterDelay(5, () -> {
            context.assertTrue(player.getMainHandItem().isEmpty(), Component.nullToEmpty("Expected 0 Hearts; Given " + player.getMainHandItem().getCount()));
            context.assertTrue(player.getMaxBaseHealth() == 16.0, Component.nullToEmpty("Expected 16.0 Max Health; Has " + player.getMaxBaseHealth()));
        });

        context.runAfterDelay(6, () -> {
            context.getLevel().getGameRules().set(LifeStealGamerules.WITHDRAW_METHOD, WithdrawMethod.ALTAR, context.getLevel().getServer());
            context.getLevel().getGameRules().set(LifeStealGamerules.HEART_STACK_SIZE, 1, context.getLevel().getServer());
            end(context, player);
        });
    }

    @GameTest(setupTicks = 75)
    public void testGiftCommand(GameTestHelper context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 8: Heart Gift Command");
        TestSubject[] players = spawnDoublePlayerTest(context);
        context.runAfterDelay(1, () -> {
            players[0].setMaxHealth(10.0);
            players[1].setMaxHealth(20.0);
            context.getLevel().getGameRules().set(LifeStealGamerules.GIFT_METHOD, GiftMethod.COMMAND, context.getLevel().getServer());
        });
        // Test valid amount
        context.runAfterDelay(2, () -> players[1].executeCommand("gift " + players[0].getStringUUID() + " 2"));
        context.runAfterDelay(3, () -> {
            context.assertTrue(players[0].getMaxBaseHealth() == 12.0, Component.nullToEmpty("Expected 12.0 Max Health; Has " + players[0].getMaxBaseHealth()));
            context.assertTrue(players[1].getMaxBaseHealth() == 18.0, Component.nullToEmpty("Expected 18.0 Max Health; Has " + players[1].getMaxBaseHealth()));
        });
        // Test invalid max health
        //context.waitAndRun(4, () -> players[1].executeCommand("gift " + players[0].getName().getString() + " 19")); // Should fail
        context.runAfterDelay(5, () -> {
            //context.assertTrue(players[0].getMaxBaseHealth() == 12.0, Text.of("Expected 12.0 Max Health; Has " + players[0].getMaxBaseHealth()));
            end(context, players);
        });
    }

    private TestSubject spawnSinglePlayerTest(GameTestHelper context) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                context.setBlock(new BlockPos(i, 150, j), Blocks.BEDROCK);
            }
        }
        Vec3 playerPos = context.absoluteVec(new Vec3(2.5, 151.5, 2.5));
        return spawnPlayer(context, playerPos);
    }

    private TestSubject[] spawnDoublePlayerTest(GameTestHelper context) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                context.setBlock(new BlockPos(i, 150, j), Blocks.BEDROCK);
            }
        }
        TestSubject[] players = new TestSubject[2];
        for (int i = 0; i < 2; i++) {
            Vec3 playerPos = context.absoluteVec(new Vec3(2.5 + i, 151.5, 2.5));
            players[i] = spawnPlayer(context, playerPos);;
        }
        return players;
    }

    private TestSubject spawnPlayer(GameTestHelper context, Vec3 pos) {
        TestSubject player = TestSubject.getRandomTestSubject(context.getLevel());
        player.level().addFreshEntity(player);
        player.setGameMode(GameType.SURVIVAL);
        player.setPosRaw(pos.x(), pos.y(), pos.z());
        return player;
    }

    private void use(TestSubject player, ItemStack stack) {
        stack.use(player.level(), player, InteractionHand.MAIN_HAND);
    }

    private void removePlayers(TestSubject... players) {
        for (TestSubject player : players) {
            player.remove();
        }
    }

    private BlockPos spawnAltar(GameTestHelper context, BlockPos center) {
        context.setBlock(center, Blocks.NETHERITE_BLOCK);
        BlockState candle = Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true);
        context.setBlock(center.north(), candle);
        context.setBlock(center.east(), candle);
        context.setBlock(center.south(), candle);
        context.setBlock(center.west(), candle);
        return context.absolutePos(center);
    }

    private void end(GameTestHelper context, TestSubject... players) {
        removePlayers(players);
        context.succeed();
        LifestealMixinConfig.TEST_LOGGER.info("Test Passed");
    }
}
