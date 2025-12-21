package com.github.tatercertified.lifesteal.gametest;

import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class GameTestUserCache implements UserNameToIdResolver {
    private final HashMap<Object, NameAndId> cache = new HashMap<>();

    @Override
    public void add(NameAndId profile) {
        cache.put(profile.id(), profile);
        cache.put(profile.name(), profile);
    }

    @Override
    public Optional<NameAndId> get(String name) {
        return Optional.ofNullable(cache.get(name));
    }

    @Override
    public void save() {
    }

    @Override
    public Optional<NameAndId> get(UUID uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }

    @Override
    public void resolveOfflineUsers(boolean offlineMode) {
    }
}
