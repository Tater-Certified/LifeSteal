package com.github.tatercertified.lifesteal.utils;

import net.minecraft.server.level.ServerPlayer;

public interface EffectEndEvent {
    void onEffectFinished(ServerPlayer effectedPlayer);
}
