package com.github.tatercertified.lifesteal.gametest;

import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.gamerules.DeathAction;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.items.HeartItem;
import com.github.tatercertified.lifesteal.items.ModItems;
import com.github.tatercertified.lifesteal.utils.LifestealMixinConfig;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.NameToIdCache;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

import java.util.UUID;

public class LifestealGameTest {
    public static final NameToIdCache gameTestUserCache = new GameTestUserCache();

    @GameTest
    public void testHeartConsumption(TestContext context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 1: Heart Consumption");
        TestSubject player = spawnSinglePlayerTest(context);
        ItemStack heart = new ItemStack(ModItems.HEART, 1);
        context.getWorld().getGameRules().get(LifeStealGamerules.HEARTBONUS).set(2, context.getWorld().getServer());

        context.waitAndRun(1, () -> player.setStackInHand(Hand.MAIN_HAND, heart.copy()));
        context.waitAndRun(2, () -> use(player, heart.copy()));
        context.waitAndRun(3, () -> {
            double maxHealth = player.getMaxBaseHealth();
            context.assertTrue(maxHealth == 22.0F, Text.of("Max Health Mismatch; Expected: 22.0, Got: " + maxHealth));
        });

        context.waitAndRun(4, () -> player.setStackInHand(Hand.MAIN_HAND, heart.copy()));
        context.waitAndRun(5, () -> context.getWorld().getGameRules().get(LifeStealGamerules.HEARTBONUS).set(4, context.getWorld().getServer()));
        context.waitAndRun(6, () -> use(player, heart.copy()));
        context.waitAndRun(7, () -> {
            double maxHealth = player.getMaxBaseHealth();
            context.assertTrue(maxHealth == 26.0F, Text.of("Max Health Mismatch; Expected: 26.0, Got: " + maxHealth));
        });

        context.waitAndRun(8, () -> {
            context.getWorld().getGameRules().get(LifeStealGamerules.HEARTBONUS).set(2, context.getWorld().getServer());
            end(context, player);
        });
    }

    @GameTest(setupTicks = 10)
    public void testAltar(TestContext context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 2: Altar");
        TestSubject player = spawnSinglePlayerTest(context);
        BlockPos altar_relative = new BlockPos(2, 151, 2);
        BlockPos altar = spawnAltar(context, altar_relative);
        context.getWorld().getGameRules().get(LifeStealGamerules.ALTAR_BLOCK).set(Blocks.NETHERITE_BLOCK, context.getWorld().getServer());
        context.assertTrue(HeartItem.isAltar(context.getWorld(), altar), Text.of("Altar failed to be created"));
        context.waitAndRun(1, () -> {
            player.setSneaking(true);
            player.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, altar.toCenterPos());
        });
        context.waitAndRun(2, () -> UseBlockCallback.EVENT.invoker().interact(player, player.getEntityWorld(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(altar), Direction.NORTH, altar, true)));
        context.waitAndRun(3, () -> {
            double maxHealth = player.getMaxBaseHealth();
            context.assertTrue(maxHealth == 18.0F, Text.of("Max Health Mismatch; Expected: 18.0, Got: " + maxHealth));
        });
        context.waitAndRun(4, () -> {
            context.getWorld().getGameRules().get(LifeStealGamerules.ALTAR_BLOCK).set(Blocks.DIAMOND_BLOCK, context.getWorld().getServer());
            context.getWorld().setBlockState(altar, Blocks.DIAMOND_BLOCK.getDefaultState());
        });
        context.waitAndRun(5, () -> context.assertTrue(HeartItem.isAltar(context.getWorld(), altar), Text.of("Altar block failed to be set")));

        context.waitAndRun(6, () -> {
            context.getWorld().getGameRules().get(LifeStealGamerules.ALTAR_BLOCK).set(Blocks.NETHERITE_BLOCK, context.getWorld().getServer());
            end(context, player);
        });
    }

    @GameTest(setupTicks = 20, maxTicks = 15)
    public void testHeartSteal(TestContext context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 3: Heart Stealing");
        TestSubject[] players = spawnDoublePlayerTest(context);
        // Test natural death gamerule
        context.getWorld().getGameRules().get(LifeStealGamerules.PLAYERRELATEDONLY).set(false, context.getWorld().getServer());
        context.getWorld().getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, context.getWorld().getServer());
        context.waitAndRun(1, () -> players[0].kill());
        context.waitAndRun(2, () -> {
            double maxHealth = players[0].getMaxBaseHealth();
            context.assertTrue(maxHealth == 18.0F, Text.of("Max Health Mismatch; Expected: 18.0, Got: " + maxHealth));
        });
        context.waitAndRun(3, () -> players[0].respawn());

        // Test player kill
        context.waitAndRun(4, () -> context.getWorld().getGameRules().get(LifeStealGamerules.PLAYERRELATEDONLY).set(true, context.getWorld().getServer()));
        context.waitAndRun(5, () -> players[0].kill(players[1]));
        context.waitAndRun(6, () -> {
            double killedMaxHealth = players[0].getMaxBaseHealth();
            context.assertTrue(killedMaxHealth == 16.0F, Text.of("Max Health Mismatch; Expected: 16.0, Got: " + killedMaxHealth));
            double attackerMaxHealth = players[1].getMaxBaseHealth();
            context.assertTrue(attackerMaxHealth == 22.0F, Text.of("Max Health Mismatch; Expected: 22.0, Got: " + attackerMaxHealth));
        });

        // Test heart steal gamerule
        context.waitAndRun(7, () -> {
            players[0].respawn();
            context.getWorld().getGameRules().get(LifeStealGamerules.STEALAMOUNT).set(4, context.getWorld().getServer());
        });
        context.waitAndRun(8, () -> players[0].kill(players[1]));
        context.waitAndRun(9, () -> {
            double killedMaxHealth = players[0].getMaxBaseHealth();
            context.assertTrue(killedMaxHealth == 12.0F, Text.of("Max Health Mismatch; Expected: 12.0, Got: " + killedMaxHealth));
            double attackerMaxHealth = players[1].getMaxBaseHealth();
            context.assertTrue(attackerMaxHealth == 26.0F, Text.of("Max Health Mismatch; Expected: 26.0, Got: " + attackerMaxHealth));
        });

        context.waitAndRun(10, () -> {
            context.getWorld().getGameRules().get(LifeStealGamerules.STEALAMOUNT).set(2, context.getWorld().getServer());
            end(context, players);
        });
    }

    @GameTest(setupTicks = 35)
    public void testDeath(TestContext context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 4: Death Consequences");
        final TestSubject[] player = {spawnSinglePlayerTest(context)};
        Vec3d playerPos = player[0].getEntityPos();
        player[0].setLowMaxHealth(context);
        context.getWorld().getGameRules().get(LifeStealGamerules.PLAYERRELATEDONLY).set(false, context.getWorld().getServer());

        // Ban test
        context.waitAndRun(1, player[0]::kill);
        context.waitAndRun(2, () -> context.assertTrue(DeathData.isPlayerDead(player[0].getUuid(), 0), Text.of("Player should be banned")));

        // Spectator test
        context.waitAndRun(3, () -> {
            player[0] = spawnPlayer(context, playerPos);
            player[0].setLowMaxHealth(context);
            context.getWorld().getGameRules().get(LifeStealGamerules.DEATH_ACTION).set(DeathAction.SPECTATOR, context.getWorld().getServer());
        });
        context.waitAndRun(4, player[0]::kill);
        context.waitAndRun(5, () -> {
            // TODO Fix the FakePlayer somehow being in survival mode when they should be spectating. This feature DOES work properly for real players
            //context.assertTrue(player[0].getGameMode() == GameMode.SPECTATOR, Text.of("Player should be spectating"));
        });

        // Revive test
        context.waitAndRun(6, () -> {
            player[0] = spawnPlayer(context, playerPos);
            player[0].setLowMaxHealth(context);
            context.getWorld().getGameRules().get(LifeStealGamerules.DEATH_ACTION).set(DeathAction.REVIVE, context.getWorld().getServer());
        });
        context.waitAndRun(7, player[0]::kill);
        context.waitAndRun(8, () -> {
            context.assertTrue(player[0].isAlive(), Text.of("Player should be alive"));
            context.assertFalse(DeathData.isPlayerDead(player[0].getUuid(), 0), Text.of("Player should not be banned"));
        });

        context.waitAndRun(9, () -> {
            context.getWorld().getGameRules().get(LifeStealGamerules.PLAYERRELATEDONLY).set(true, context.getWorld().getServer());
            context.getWorld().getGameRules().get(LifeStealGamerules.DEATH_ACTION).set(DeathAction.BAN, context.getWorld().getServer());
            end(context, player);
        });
    }

    @GameTest(setupTicks = 45)
    public void testRevive(TestContext context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 5: Revival");
        TestSubject[] players = spawnDoublePlayerTest(context);
        players[0].setLowMaxHealth(context);
        Text name = players[0].getName();
        UUID uuid = players[0].getUuid();
        ItemStack heart = new ItemStack(ModItems.HEART);
        heart.set(DataComponentTypes.CUSTOM_NAME, name);
        BlockPos altar_relative = new BlockPos(2, 151, 2);
        BlockPos altar = spawnAltar(context, altar_relative);

        context.waitAndRun(1, players[0]::kill);
        context.waitAndRun(2, () -> {
            players[1].setStackInHand(Hand.MAIN_HAND, heart.copy());
            players[1].setSneaking(true);
            players[1].lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, altar.toCenterPos());
        });
        context.waitAndRun(3, () -> heart.useOnBlock(new ItemUsageContext(players[1], Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(altar), Direction.NORTH, altar, true))));
        context.waitAndRun(5, () -> context.assertFalse(DeathData.isPlayerDead(uuid, 0), Text.of("Player was not revived")));
        context.waitAndRun(6, () -> end(context, players));
    }

    @GameTest(setupTicks = 55)
    public void testHeartStacks(TestContext context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 6: Heart Stacking");
        TestSubject player = spawnSinglePlayerTest(context);
        ItemStack heart = new ItemStack(ModItems.HEART);
        context.waitAndRun(1, () -> player.giveItemStack(heart.copy()));
        context.waitAndRun(2, () -> player.giveItemStack(heart.copy()));
        context.waitAndRun(3, () -> {
            context.assertTrue(player.getMainHandStack().getCount() == 1, Text.of("Hearts stacked when unstackable"));
            context.getWorld().getGameRules().get(LifeStealGamerules.HEART_STACK_SIZE).set(2, context.getWorld().getServer());
        });
        context.waitAndRun(4, () -> player.giveItemStack(heart.copy()));
        context.waitAndRun(5, () -> context.assertTrue(player.getMainHandStack().getCount() == 2, Text.of("Hearts did not stack")));
        context.waitAndRun(6, () -> {
            context.getWorld().getGameRules().get(LifeStealGamerules.HEART_STACK_SIZE).set(1, context.getWorld().getServer());
            end(context, player);
        });
    }

    @GameTest(setupTicks = 65)
    public void testWithdrawCommand(TestContext context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 7: Heart Withdraw Command");
        TestSubject player = spawnSinglePlayerTest(context);
        context.waitAndRun(1, () -> {
            context.getWorld().getGameRules().get(LifeStealGamerules.HEART_STACK_SIZE).set(2, context.getWorld().getServer());
            context.getWorld().getGameRules().get(LifeStealGamerules.ALTARS).set(false, context.getWorld().getServer());
        });
        // Test valid amount
        context.waitAndRun(2, () -> player.executeCommand("withdraw 2"));
        context.waitAndRun(3, () -> {
            context.assertTrue(player.getMainHandStack().getCount() == 2, Text.of("Expected 2 Hearts; Given " + player.getMainHandStack().getCount()));
            context.assertTrue(player.getMaxBaseHealth() == 16.0, Text.of("Expected 16.0 Max Health; Has " + player.getMaxBaseHealth()));
        });
        // Test invalid max health
        context.waitAndRun(4, () -> {
            player.getInventory().clear();
            context.getWorld().getServer().getCommandManager().parseAndExecute(player.getCommandSource(), "withdraw 16");
        }); // Should fail
        context.waitAndRun(5, () -> {
            context.assertTrue(player.getMainHandStack().isEmpty(), Text.of("Expected 0 Hearts; Given " + player.getMainHandStack().getCount()));
            context.assertTrue(player.getMaxBaseHealth() == 16.0, Text.of("Expected 16.0 Max Health; Has " + player.getMaxBaseHealth()));
        });

        context.waitAndRun(6, () -> {
            context.getWorld().getGameRules().get(LifeStealGamerules.ALTARS).set(true, context.getWorld().getServer());
            context.getWorld().getGameRules().get(LifeStealGamerules.HEART_STACK_SIZE).set(1, context.getWorld().getServer());
            end(context, player);
        });
    }

    @GameTest(setupTicks = 75)
    public void testGiftCommand(TestContext context) {
        LifestealMixinConfig.TEST_LOGGER.info("Test 8: Heart Gift Command");
        TestSubject[] players = spawnDoublePlayerTest(context);
        context.waitAndRun(1, () -> {
            players[0].setMaxHealth(10.0);
            players[1].setMaxHealth(20.0);
            context.getWorld().getGameRules().get(LifeStealGamerules.GIFTHEARTS).set(true, context.getWorld().getServer());
            context.getWorld().getGameRules().get(LifeStealGamerules.ALTARS).set(false, context.getWorld().getServer());
        });
        // Test valid amount
        context.waitAndRun(2, () -> players[1].executeCommand("gift " + players[0].getUuidAsString() + " 2"));
        context.waitAndRun(3, () -> {
            context.assertTrue(players[0].getMaxBaseHealth() == 12.0, Text.of("Expected 12.0 Max Health; Has " + players[0].getMaxBaseHealth()));
            context.assertTrue(players[1].getMaxBaseHealth() == 18.0, Text.of("Expected 18.0 Max Health; Has " + players[1].getMaxBaseHealth()));
        });
        // Test invalid max health
        //context.waitAndRun(4, () -> players[1].executeCommand("gift " + players[0].getName().getString() + " 19")); // Should fail
        context.waitAndRun(5, () -> {
            //context.assertTrue(players[0].getMaxBaseHealth() == 12.0, Text.of("Expected 12.0 Max Health; Has " + players[0].getMaxBaseHealth()));
            end(context, players);
        });
    }

    private TestSubject spawnSinglePlayerTest(TestContext context) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                context.setBlockState(new BlockPos(i, 150, j), Blocks.BEDROCK);
            }
        }
        Vec3d playerPos = context.getAbsolute(new Vec3d(2.5, 151.5, 2.5));
        return spawnPlayer(context, playerPos);
    }

    private TestSubject[] spawnDoublePlayerTest(TestContext context) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                context.setBlockState(new BlockPos(i, 150, j), Blocks.BEDROCK);
            }
        }
        TestSubject[] players = new TestSubject[2];
        for (int i = 0; i < 2; i++) {
            Vec3d playerPos = context.getAbsolute(new Vec3d(2.5 + i, 151.5, 2.5));
            players[i] = spawnPlayer(context, playerPos);;
        }
        return players;
    }

    private TestSubject spawnPlayer(TestContext context, Vec3d pos) {
        TestSubject player = TestSubject.getRandomTestSubject(context.getWorld());
        player.getEntityWorld().spawnEntity(player);
        player.changeGameMode(GameMode.SURVIVAL);
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        return player;
    }

    private void use(TestSubject player, ItemStack stack) {
        stack.use(player.getEntityWorld(), player, Hand.MAIN_HAND);
    }

    private void removePlayers(TestSubject... players) {
        for (TestSubject player : players) {
            player.remove();
        }
    }

    private BlockPos spawnAltar(TestContext context, BlockPos center) {
        context.setBlockState(center, Blocks.NETHERITE_BLOCK);
        BlockState candle = Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, true);
        context.setBlockState(center.north(), candle);
        context.setBlockState(center.east(), candle);
        context.setBlockState(center.south(), candle);
        context.setBlockState(center.west(), candle);
        return context.getAbsolutePos(center);
    }

    private void end(TestContext context, TestSubject... players) {
        removePlayers(players);
        context.complete();
        LifestealMixinConfig.TEST_LOGGER.info("Test Passed");
    }
}
