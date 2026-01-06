package com.github.tatercertified.lifesteal.gametest.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class GameTestUtils {

    public static TestSubject spawnSinglePlayer(GameTestHelper helper) {
        Vec3 pos = helper.absoluteVec(new Vec3(2.5, 151.5, 2.5));
        TestSubject player = TestSubject.getRandomTestSubject(helper.getLevel());
        helper.getLevel().addFreshEntity(player);
        player.setPos(pos);
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }

    public static TestSubject[] spawnDoublePlayerTest(GameTestHelper helper) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                helper.setBlock(new BlockPos(i, 150, j), Blocks.BEDROCK);
            }
        }
        TestSubject[] players = new TestSubject[2];
        for (int i = 0; i < 2; i++) {
            Vec3 playerPos = helper.absoluteVec(new Vec3(2.5 + i, 151.5, 2.5));
            TestSubject player = TestSubject.getRandomTestSubject(helper.getLevel());
            helper.getLevel().addFreshEntity(player);
            player.setPos(playerPos);
            player.setGameMode(GameType.SURVIVAL);
            players[i] = player;
        }
        return players;
    }

    public static void cleanup(Entity... entities) {
        for (Entity e : entities) {
            if (e != null) e.discard();
        }
    }
}
