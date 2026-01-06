package com.github.tatercertified.lifesteal.utils;

import com.github.tatercertified.lifesteal.data.DeathData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;

public final class LifeStealText {
    public static final Component
            FAILURE_UNKNOWN = Component.translatable("lifesteal.failure.unknown"), // For debug purposes
            DEATH = Component.translatable("lifesteal.gameplay.death"),
            MAX_HEALTH = Component.translatable("lifesteal.gameplay.max_health"),
            LOW_HEALTH = Component.translatable("lifesteal.gameplay.low_health"),
            INVALID_HEART_AMOUNT = Component.translatable("lifesteal.gameplay.invalid_heart_amount"),
            GIFT_COMMAND_DISABLED = Component.translatable("lifesteal.command.gift.disabled"),
            REVIVE_COMMAND_DISABLED = Component.translatable("lifesteal.command.revive.disabled"),
            WITHDRAW_COMMAND_DISABLED = Component.translatable("lifesteal.command.withdraw.disabled"),
            GIFT_NONE = Component.translatable("lifesteal.gift.none"),
            GIFT_MULTIPLE = Component.translatable("lifesteal.gift.multiple"),
            PREVENT_ATTACK = Component.translatable("lifesteal.gameplay.prevent_attack"),
            REVIVE_HOLD = Component.translatable("lifesteal.revive.holding"),
            BACK = Component.translatable("lifesteal.gui.back"),
            NEXT = Component.translatable("lifesteal.gui.next"),
            TITLE = Component.translatable("lifesteal.gui.title");

    private static final String
            HEART_WITHDRAWN = "lifesteal.withdraw.heart",
            HEART_WITHDRAWN_SINGLE = "lifesteal.withdraw.heart.single",
            GIFT_RECEIVER_MAX_HEALTH = "lifesteal.gift.receiver.max_health",
            GIFT_SUCCESS = "lifesteal.gift.success",
            RECEIVE_SUCCESS = "lifesteal.gift.received",
            PLAYER_IS_ALIVE = "lifesteal.player.alive",
            PLAYER_IS_DEAD = "lifesteal.player.dead",
            PLAYER_IS_YOU = "lifesteal.gift.self",
            PLAYER_DOES_NOT_EXIST = "lifesteal.player.not_found",
            REVIVEE = "lifesteal.player.revived.receiver",
            REVIVER = "lifesteal.player.revived.sender",
            SELF_REVIVE = "lifesteal.revive.self",
            DEATH_TIME = "lifesteal.gameplay.death_time",
            PREVENT_DAMAGE = "lifesteal.gameplay.prevent_damage",
            ADMIN_REVIVE = "lifesteal.admin.revive";

    public static Component onRevivalText(DeathData data, MinecraftServer server) {
        return Component.translatable(REVIVEE, server.services().nameToIdCache().get(data.reviverPlayerID).get().name());
    }

    public static Component notFound(String playerName) {
        return Component.translatable(PLAYER_DOES_NOT_EXIST, playerName);
    }

    public static Component onRevivalText(Component reviver) {
        return Component.translatable(REVIVEE, reviver);
    }

    public static Component revived(Component revived) {
        return Component.translatable(REVIVER, revived);
    }

    public static Component playerIsAlive(Component player) {
        return Component.translatable(PLAYER_IS_ALIVE, player);
    }

    public static Component withdrawnHealth(int hearts) {
        if (hearts == 1) {
            return Component.translatable(HEART_WITHDRAWN_SINGLE);
        }
        return Component.translatable(HEART_WITHDRAWN, hearts);
    }

    public static Component receiverTooMuchHealth(Component receiver) {
        return Component.translatable(GIFT_RECEIVER_MAX_HEALTH, receiver);
    }

    public static Component giftSuccess(double health, Component receiver) {
        return Component.translatable(GIFT_SUCCESS, health, receiver);
    }

    public static Component receiveGift(double health, Component sender) {
        return Component.translatable(RECEIVE_SUCCESS, health, sender);
    }

    public static Component noSelfGifting(Component name) {
        return Component.translatable(PLAYER_IS_YOU, name);
    }

    public static Component noSelfReviving(Component name) {
        return Component.translatable(SELF_REVIVE, name);
    }

    public static Component isDead(Component name) {
        return Component.translatable(PLAYER_IS_DEAD, name);
    }

    public static Component adminRevive(String player) {
        return Component.translatable(ADMIN_REVIVE, player);
    }

    public static Component deathTime(int seconds) {
        return Component.translatable(DEATH_TIME, seconds);
    }

    public static Component preventDamage(Component player) {
        return Component.translatable(PREVENT_DAMAGE, player);
    }
}
