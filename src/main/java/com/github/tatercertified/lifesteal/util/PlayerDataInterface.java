package com.github.tatercertified.lifesteal.util;

import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public interface PlayerDataInterface {
    NbtCompound getLifeStealInfo(UUID uuid, String playerName);
    void saveLifeStealInfo(UUID uuid, String playerName, NbtCompound lifeStealData);
}
