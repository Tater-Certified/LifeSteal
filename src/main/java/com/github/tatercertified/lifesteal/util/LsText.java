package com.github.tatercertified.lifesteal.util;

import net.minecraft.text.Text;

/**
 * Translation strings for LifeSteal.
 *
 * @author Ampflower
 * @since ${version}
 **/
public final class LsText {
    public static final Text
            FAILURE_UNKNOWN = Text.translatable("lifesteal.failure.unknown"),
            DEATH = Text.translatable("lifesteal.gameplay.death"),
            MAX_HEALTH = Text.translatable("lifesteal.gameplay.max_health"),
            LOW_HEALTH = Text.translatable("lifesteal.gameplay.low_health"),
            HEART_DISABLED = Text.translatable("lifesteal.heart.disabled"),
            WITHDRAW_ALTAR = Text.translatable("lifesteal.withdraw.altar"),
            GIFT_ALTAR = Text.translatable("lifesteal.gift.altar"),
            GIFT_NONE = Text.translatable("lifesteal.gift.none"),
            GIFT_MULTIPLE = Text.translatable("lifesteal.gift.multiple"),
            GIFT_OVER_LIMIT = Text.translatable("lifesteal.gift.over_limit"),
            GIFT_DISABLED = Text.translatable("lifesteal.gift.heart.disabled"),
            RESET = Text.translatable("lifesteal.admin.ban.reset");

    private static final String
            UPDATE_HEALTH = "lifesteal.gameplay.update_health",
            HEART_WITHDRAWN = "lifesteal.withdraw.heart",
            HEART_WITHDRAWN_SINGLE = "lifesteal.withdraw.heart.single",
            GIFT_RECEIVER_MAX_HEALTH = "lifesteal.gift.receiver.max_health",
            PLAYER_IS_ALIVE = "lifesteal.player.alive",
            PLAYER_IS_DEAD = "lifesteal.player.dead",
            PLAYER_IS_YOU = "lifesteal.gift.self",
            PLAYER_DOES_NOT_EXIST = "lifesteal.player.not_found",
            REVIVEE = "lifesteal.player.revived.receiver",
            REVIVER = "lifesteal.player.revived.sender";

    private static final double HALF = .5d;

    public static Text updateHealth(double health) {
        return Text.translatable(UPDATE_HEALTH, Math.floor(health) * HALF);
    }

    public static Text withdrawnHealth(int health, int hearts) {
        if (hearts == 1) {
            return Text.translatable(HEART_WITHDRAWN_SINGLE, health);
        }
        return Text.translatable(HEART_WITHDRAWN, health, hearts);
    }

    public static Text receiverMaxHealth(Text profile) {
        return Text.translatable(GIFT_RECEIVER_MAX_HEALTH, profile);
    }

    public static Text playerIsAlive(Text profile) {
        return Text.translatable(PLAYER_IS_ALIVE, profile);
    }

    public static Text playerIsDead(Text profile) {
        return Text.translatable(PLAYER_IS_DEAD, profile);
    }

    public static Text playerIsYou(Text profile) {
        return Text.translatable(PLAYER_IS_YOU, profile);
    }

    public static Text notFound(Text profile) {
        return Text.translatable(PLAYER_DOES_NOT_EXIST, profile);
    }

    public static Text revivee(Text profile) {
        return Text.translatable(REVIVEE, profile);
    }

    public static Text revived(Text profile) {
        return Text.translatable(REVIVER, profile);
    }
}
