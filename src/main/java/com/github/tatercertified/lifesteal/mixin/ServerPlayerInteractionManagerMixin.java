package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.utils.PlayerGameModeInterface;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin implements PlayerGameModeInterface {
    @Shadow private @Nullable GameMode previousGameMode;

    @Override
    public void setPreviousGameMode(GameMode gameMode) {
        this.previousGameMode = gameMode;
    }
}
