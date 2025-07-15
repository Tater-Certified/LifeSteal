package com.github.tatercertified.lifesteal.mixin;

import net.fabricmc.fabric.api.entity.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = FakePlayer.class, remap = false)
public interface FakePlayerAccessor {
    @Accessor("FAKE_PLAYER_MAP")
    static Map<Object, FakePlayer> getPlayerMap() {
        throw new AssertionError();
    }
}
