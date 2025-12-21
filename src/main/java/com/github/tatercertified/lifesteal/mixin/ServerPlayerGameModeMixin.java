package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.utils.PlayerGameModeInterface;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin implements PlayerGameModeInterface {
    @Shadow private @Nullable GameType previousGameModeForPlayer;

    @Override
    public void setPreviousGameMode(GameType gameMode) {
        this.previousGameModeForPlayer = gameMode;
    }
}
