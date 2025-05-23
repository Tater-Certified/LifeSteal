package com.github.certifiedtater.lifesteal.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.UserCache;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GameTestUserCache extends UserCache {
    private final HashMap<Object, GameProfile> cache = new HashMap<>();

    public GameTestUserCache() {
        super(null, null);
    }

    @Override
    public void add(GameProfile profile) {
        cache.put(profile.getId(), profile);
        cache.put(profile.getName(), profile);
    }

    @Override
    public List<UserCache.Entry> load() {
        return List.of();
    }

    @Override
    public Optional<GameProfile> findByName(String name) {
        return Optional.ofNullable(cache.get(name));
    }

    @Override
    public void save() {
    }

    @Override
    public Optional<GameProfile> getByUuid(UUID uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }
}
