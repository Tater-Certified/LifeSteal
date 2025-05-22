package com.github.certifiedtater.lifesteal.gametest;

import com.github.certifiedtater.lifesteal.gamerules.LifeStealGamerules;
import com.github.certifiedtater.lifesteal.items.HeartItem;
import com.github.certifiedtater.lifesteal.items.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifestealGameTest {

    private final Logger testLogger = LoggerFactory.getLogger("Lifesteal Test");

    @GameTest
    public void testHeartConsumption(TestContext context) {
        testLogger.info("Test 1: Heart Consumption");
        TestSubject player = spawnSinglePlayerTest(context);
        ItemStack heart = new ItemStack(ModItems.HEART, 1);
        context.getWorld().getGameRules().get(LifeStealGamerules.HEARTBONUS).set(2, context.getWorld().getServer());

        context.waitAndRun(1, () -> player.setStackInHand(Hand.MAIN_HAND, heart.copy()));
        context.waitAndRun(2, () -> use(player, heart.copy()));
        context.waitAndRun(3, () -> {
            double maxHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH).getBaseValue();
            context.assertTrue(maxHealth == 22.0F, Text.of("Max Health Mismatch; Expected: 22.0, Got: " + maxHealth));
        });

        context.waitAndRun(4, () -> player.setStackInHand(Hand.MAIN_HAND, heart.copy()));
        context.waitAndRun(5, () -> context.getWorld().getGameRules().get(LifeStealGamerules.HEARTBONUS).set(4, context.getWorld().getServer()));
        context.waitAndRun(6, () -> use(player, heart.copy()));
        context.waitAndRun(7, () -> {
            double maxHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH).getBaseValue();
            context.assertTrue(maxHealth == 26.0F, Text.of("Max Health Mismatch; Expected: 26.0, Got: " + maxHealth));
        });

        context.waitAndRun(8, () -> {
            context.getWorld().getGameRules().get(LifeStealGamerules.HEARTBONUS).set(2, context.getWorld().getServer());
            end(context, player);
        });
    }

    @GameTest(setupTicks = 10)
    public void testAltar(TestContext context) {
        testLogger.info("Test 2: Altar");
        TestSubject player = spawnSinglePlayerTest(context);
        BlockPos altar_relative = new BlockPos(2, 151, 2);
        BlockPos altar = spawnAltar(context, altar_relative);
        context.getWorld().getGameRules().get(LifeStealGamerules.ALTAR_BLOCK).set(Blocks.NETHERITE_BLOCK, context.getWorld().getServer());
        context.assertTrue(HeartItem.isAltar(context.getWorld(), altar), Text.of("Altar failed to be created"));
        context.waitAndRun(1, () -> {
            player.setSneaking(true);
            player.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, altar.toCenterPos());
        });
        context.waitAndRun(2, () -> UseBlockCallback.EVENT.invoker().interact(player, player.getWorld(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(altar), Direction.NORTH, altar, true)));
        context.waitAndRun(3, () -> {
            double maxHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH).getBaseValue();
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
        testLogger.info("Test 3: Heart Stealing");
        TestSubject[] players = spawnDoublePlayerTest(context);
        // Test natural death gamerule
        context.getWorld().getGameRules().get(LifeStealGamerules.PLAYERRELATEDONLY).set(false, context.getWorld().getServer());
        context.getWorld().getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, context.getWorld().getServer());
        context.waitAndRun(1, () -> players[0].kill());
        context.waitAndRun(2, () -> {
            double maxHealth = players[0].getAttributeInstance(EntityAttributes.MAX_HEALTH).getBaseValue();
            context.assertTrue(maxHealth == 18.0F, Text.of("Max Health Mismatch; Expected: 18.0, Got: " + maxHealth));
        });
        context.waitAndRun(3, () -> players[0].respawn());

        // Test player kill
        context.waitAndRun(4, () -> context.getWorld().getGameRules().get(LifeStealGamerules.PLAYERRELATEDONLY).set(true, context.getWorld().getServer()));
        context.waitAndRun(5, () -> players[0].kill(players[1]));
        context.waitAndRun(6, () -> {
            double killedMaxHealth = players[0].getAttributeInstance(EntityAttributes.MAX_HEALTH).getBaseValue();
            context.assertTrue(killedMaxHealth == 16.0F, Text.of("Max Health Mismatch; Expected: 16.0, Got: " + killedMaxHealth));
            double attackerMaxHealth = players[1].getAttributeInstance(EntityAttributes.MAX_HEALTH).getBaseValue();
            context.assertTrue(attackerMaxHealth == 22.0F, Text.of("Max Health Mismatch; Expected: 22.0, Got: " + attackerMaxHealth));
        });

        // Test heart steal command
        context.waitAndRun(7, () -> {
            players[0].respawn();
            context.getWorld().getGameRules().get(LifeStealGamerules.STEALAMOUNT).set(4, context.getWorld().getServer());
        });
        context.waitAndRun(8, () -> players[0].kill(players[1]));
        context.waitAndRun(9, () -> {
            double killedMaxHealth = players[0].getAttributeInstance(EntityAttributes.MAX_HEALTH).getBaseValue();
            context.assertTrue(killedMaxHealth == 12.0F, Text.of("Max Health Mismatch; Expected: 12.0, Got: " + killedMaxHealth));
            double attackerMaxHealth = players[1].getAttributeInstance(EntityAttributes.MAX_HEALTH).getBaseValue();
            context.assertTrue(attackerMaxHealth == 26.0F, Text.of("Max Health Mismatch; Expected: 26.0, Got: " + attackerMaxHealth));
        });

        context.waitAndRun(10, () -> {
            context.getWorld().getGameRules().get(LifeStealGamerules.STEALAMOUNT).set(2, context.getWorld().getServer());
            end(context, players);
        });
    }

    private TestSubject spawnSinglePlayerTest(TestContext context) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                context.setBlockState(new BlockPos(i, 150, j), Blocks.BEDROCK);
            }
        }
        TestSubject player = TestSubject.getRandomTestSubject(context.getWorld());
        player.changeGameMode(GameMode.SURVIVAL);
        Vec3d playerPos = context.getAbsolute(new Vec3d(2.5, 151.5, 2.5));
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());
        return player;
    }

    private TestSubject[] spawnDoublePlayerTest(TestContext context) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                context.setBlockState(new BlockPos(i, 150, j), Blocks.BEDROCK);
            }
        }
        TestSubject[] players = new TestSubject[2];
        for (int i = 0; i < 2; i++) {
            TestSubject player = TestSubject.getRandomTestSubject(context.getWorld());
            player.getWorld().spawnEntity(player);
            player.changeGameMode(GameMode.SURVIVAL);
            Vec3d playerPos = context.getAbsolute(new Vec3d(2.5 + i, 151.5, 2.5));
            player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());
            players[i] = player;
        }
        return players;
    }

    private void use(TestSubject player, ItemStack stack) {
        stack.use(player.getWorld(), player, Hand.MAIN_HAND);
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
        testLogger.info("Test Passed");
    }
}
