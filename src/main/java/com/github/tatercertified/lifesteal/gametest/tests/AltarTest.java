package com.github.tatercertified.lifesteal.gametest.tests;

import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.gametest.structure.GameTest;
import com.github.tatercertified.lifesteal.gametest.structure.GameTestUtils;
import com.github.tatercertified.lifesteal.gametest.structure.TestSubject;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AltarTest extends GameTest {
    @Override
    protected void run() {
        TestSubject player = GameTestUtils.spawnSinglePlayer(this.helper);
        GameRules gameRules = this.helper.getLevel().getGameRules();
        MinecraftServer server = this.helper.getLevel().getServer();
        BlockPos altar = spawnAltar(this.helper, new BlockPos(2, 151, 2));
        after(1, () -> {
            player.lookAt(EntityAnchorArgument.Anchor.FEET, Vec3.atCenterOf(altar));
            player.setShiftKeyDown(true);
        });
        after(2, () -> UseBlockCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(altar), Direction.NORTH, altar, true)));
        after(3, () -> {
            double maxHealth = player.getMaxBaseHealth();
            if (maxHealth != 18.0) {
                fail("Max Health Mismatch; Expected: 18.0, Got: " + maxHealth);
            }
        });
        after(4, () -> gameRules.set(LifeStealGamerules.ALTAR_BLOCK, BuiltInRegistries.BLOCK.createIntrusiveHolder(Blocks.DIAMOND_BLOCK), server));
        after(5, () -> UseBlockCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(altar), Direction.NORTH, altar, true)));
        after(6, () -> {
            double maxHealth = player.getMaxBaseHealth();
            if (maxHealth != 18.0) {
                fail("Max Health Mismatch; Expected: 18.0, Got: " + maxHealth);
            }
        });
        after(7, () -> {
            GameTestUtils.cleanup(player);
            pass();
        });
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
}
