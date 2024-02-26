package com.github.tatercertified.lifesteal.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Function;

/**
 * The return state of the exchange.
 *
 * @author Ampflower
 * @see PlayerUtils#exchangeHealth(ServerPlayerEntity, GameProfile, int)
 * @since 1.4.0
 **/
public enum ExchangeState {
    FAIL_GENERIC(player -> LsText.FAILURE_UNKNOWN),
    FAIL_DEAD(LsText::playerIsDead),
    FAIL_SELF(LsText::playerIsYou),
    FAIL_MISSING(LsText::notFound),
    FAIL_RECEIVER_TOO_MUCH_HEALTH(LsText::receiverMaxHealth),
    FAIL_GIVER_TOO_LITTLE_HEALTH(player -> LsText.LOW_HEALTH),
    SUCCESS(player -> Text.of(null));

    public final Function<Text, Text> message;

    ExchangeState(Function<Text, Text> message) {
        this.message = message;
    }
}
