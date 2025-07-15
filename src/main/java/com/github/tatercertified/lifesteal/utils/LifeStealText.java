package com.github.tatercertified.lifesteal.utils;

import com.github.tatercertified.lifesteal.data.DeathData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public final class LifeStealText {
    public static final Text
            FAILURE_UNKNOWN = Text.translatable("lifesteal.failure.unknown"), // For debug purposes
            DEATH = Text.translatable("lifesteal.gameplay.death"),
            MAX_HEALTH = Text.translatable("lifesteal.gameplay.max_health"),
            LOW_HEALTH = Text.translatable("lifesteal.gameplay.low_health"),
            HEART_DISABLED = Text.translatable("lifesteal.heart.disabled"),
            WITHDRAW_ALTAR = Text.translatable("lifesteal.withdraw.altar"),
            GIFT_ALTAR = Text.translatable("lifesteal.gift.altar"),
            GIFT_NONE = Text.translatable("lifesteal.gift.none"),
            GIFT_MULTIPLE = Text.translatable("lifesteal.gift.multiple"),
            PREVENT_ATTACK = Text.translatable("lifesteal.gameplay.prevent_attack"),
            REVIVE_ALTAR = Text.translatable("lifesteal.revive.altar"),
            REVIVE_HOLD = Text.translatable("lifesteal.revive.holding"),
            GIFT_DISABLED = Text.translatable("lifesteal.gift.heart.disabled");

    private static final String
            UPDATE_HEALTH = "lifesteal.gameplay.update_health",
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

    public static Text onRevivalText(DeathData data, MinecraftServer server) {
        return Text.translatable(REVIVEE, server.getUserCache().getByUuid(data.reviverPlayerID).get().getName());
    }

    public static Text notFound(String playerName) {
        return Text.translatable(PLAYER_DOES_NOT_EXIST, playerName);
    }

    public static Text onRevivalText(Text reviver) {
        return Text.translatable(REVIVEE, reviver);
    }

    public static Text revived(Text revived) {
        return Text.translatable(REVIVER, revived);
    }

    public static Text playerIsAlive(Text player) {
        return Text.translatable(PLAYER_IS_ALIVE, player);
    }

    public static Text withdrawnHealth(int health, int hearts) {
        if (hearts == 1) {
            return Text.translatable(HEART_WITHDRAWN_SINGLE, health);
        }
        return Text.translatable(HEART_WITHDRAWN, health, hearts);
    }

    public static Text receiverTooMuchHealth(Text receiver) {
        return Text.translatable(GIFT_RECEIVER_MAX_HEALTH, receiver);
    }

    public static Text giftSuccess(double health, Text receiver) {
        return Text.translatable(GIFT_SUCCESS, health, receiver);
    }

    public static Text receiveGift(double health, Text sender) {
        return Text.translatable(RECEIVE_SUCCESS, health, sender);
    }

    public static Text noSelfGifting(Text name) {
        return Text.translatable(PLAYER_IS_YOU, name);
    }

    public static Text noSelfReviving(Text name) {
        return Text.translatable(SELF_REVIVE, name);
    }

    public static Text isDead(Text name) {
        return Text.translatable(PLAYER_IS_DEAD, name);
    }

    public static Text updateHealth(double changeAmount) { // +X Health
        String changeStr = String.valueOf(changeAmount);
        if (changeAmount > 0) {
            changeStr = "+" + changeStr;
        }
        return Text.translatable(UPDATE_HEALTH, changeStr).withColor(TextColor.fromFormatting(Formatting.RED).getRgb());
    }

    public static Text adminRevive(String player) {
        return Text.translatable(ADMIN_REVIVE, player);
    }

    public static Text deathTime(int seconds) {
        return Text.translatable(DEATH_TIME, seconds);
    }

    public static Text preventDamage(Text player) {
        return Text.translatable(PREVENT_DAMAGE, player);
    }
}
