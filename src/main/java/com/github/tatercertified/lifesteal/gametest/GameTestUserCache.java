package com.github.tatercertified.lifesteal.gametest;

import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.util.NameToIdCache;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class GameTestUserCache implements NameToIdCache {
    private final HashMap<Object, PlayerConfigEntry> cache = new HashMap<>();

    @Override
    public void add(PlayerConfigEntry profile) {
        cache.put(profile.id(), profile);
        cache.put(profile.name(), profile);
    }

    @Override
    public Optional<PlayerConfigEntry> findByName(String name) {
        return Optional.ofNullable(cache.get(name));
    }

    @Override
    public void save() {
    }

    @Override
    public Optional<PlayerConfigEntry> getByUuid(UUID uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }

    @Override
    public void setOfflineMode(boolean offlineMode) {
    }
}
