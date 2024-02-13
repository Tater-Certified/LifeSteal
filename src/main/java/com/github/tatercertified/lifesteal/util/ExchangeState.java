package com.github.tatercertified.lifesteal.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Supplier;

/**
 * The return state of the exchange.
 *
 * @author Ampflower
 * @see PlayerUtils#exchangeHealth(ServerPlayerEntity, GameProfile, int)
 * @since 1.4.0
 **/
public enum ExchangeState {
    FAIL_GENERIC(() -> Text.of("Unknown fault. If you're the server admin, check logs.")),
    FAIL_DEAD(() -> Text.of(Config.PLAYER_IS_DEAD)),
    FAIL_SELF(() -> Text.of(Config.PLAYER_IS_YOU)),
    FAIL_MISSING(() -> Text.of(Config.PLAYER_DOES_NOT_EXIST)),
    FAIL_RECEIVER_TOO_MUCH_HEALTH(() -> Text.of(Config.RECEIVER_TOO_MUCH_HEALTH)),
    FAIL_GIVER_TOO_LITTLE_HEALTH(() -> Text.of(Config.GIVER_TOO_LITTLE_HEALTH)),
    SUCCESS(Text::empty);

    public final Supplier<Text> message;

    ExchangeState(Supplier<Text> message) {
        this.message = message;
    }
}
