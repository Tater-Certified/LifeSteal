package com.github.certifiedtater.lifesteal.mixin.gametest;

import com.github.certifiedtater.lifesteal.gametest.LifestealGameTest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MinecraftServer.class)
public class GameTestUserCacheMixin {
    /**
     * @author QPCrummer
     * @reason Replace with custom implementation to prevent NPE in GameTest environment
     */
    @Overwrite
    public UserCache getUserCache() {
        return LifestealGameTest.gameTestUserCache;
    }
}
