package com.github.certifiedtater.lifesteal.commands;

import com.github.certifiedtater.lifesteal.data.DeathData;
import com.github.certifiedtater.lifesteal.gamerules.LifeStealGamerules;
import com.github.certifiedtater.lifesteal.utils.LifeStealText;
import com.github.certifiedtater.lifesteal.utils.OfflinePlayerData;
import com.github.certifiedtater.lifesteal.utils.PlayerUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GiftCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            dispatcher.register(literal("gift")
                    .requires(ServerCommandSource::isExecutedByPlayer)
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                            .then(argument("healthPoints", IntegerArgumentType.integer(1)).executes(GiftCommand::gift))));
        });
    }

    public static int gift(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();
        final ServerPlayerEntity player = source.getPlayerOrThrow();
        final MinecraftServer server = source.getServer();
        final GameRules gameRules = server.getGameRules();

        if (gameRules.getBoolean(LifeStealGamerules.ALTARS)) {
            source.sendError(LifeStealText.GIFT_ALTAR);
            return 0;
        }

        if (!gameRules.getBoolean(LifeStealGamerules.GIFTHEARTS)) {
            source.sendError(LifeStealText.GIFT_DISABLED);
            return 0;
        }

        final int amount = IntegerArgumentType.getInteger(context, "healthPoints");
        if (amount > gameRules.getInt(LifeStealGamerules.MAXPLAYERHEALTH) - gameRules.getInt(LifeStealGamerules.MINPLAYERHEALTH)) {
            source.sendError(LifeStealText.GIFT_OVER_LIMIT);
            return 0;
        }

        final Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
        if (profiles.isEmpty()) {
            source.sendError(LifeStealText.GIFT_NONE);
            return 0;
        }
        if (profiles.size() > 1) {
            source.sendError(LifeStealText.GIFT_MULTIPLE);
            return 0;
        }

        final GameProfile receiver = profiles.iterator().next();

        if (receiver.getId() == player.getUuid()) {
            // Can't gift to yourself
            source.sendError(LifeStealText.noSelfGifting(player.getName()));
            return 0;
        }

        if (DeathData.isPlayerDead(receiver.getId())) {
            // Can't gift to a dead guy
            source.sendError(LifeStealText.isDead(Text.of(receiver.getName())));
            return 0;
        }

        ServerPlayerEntity receiverPlayer = server.getPlayerManager().getPlayer(receiver.getId());

        EntityAttributeInstance maxHealthAttribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        double maxHealth = maxHealthAttribute.getBaseValue();

        if (!PlayerUtils.canChangeHealth(maxHealth, -amount, gameRules)) {
            // Player has too little health
            source.sendError(LifeStealText.LOW_HEALTH);
        }

        if (receiverPlayer != null) {
            // Online
            EntityAttributeInstance maxHealthAttributeReceiver = receiverPlayer.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            double maxHealthReceiver = maxHealthAttributeReceiver.getBaseValue();
            if (!PlayerUtils.canChangeHealth(maxHealthReceiver, amount, gameRules)) {
                // Receiver has too much health
                source.sendError(LifeStealText.receiverTooMuchHealth(receiverPlayer.getName()));
            }

            PlayerUtils.changeHealth(player, -amount);
            PlayerUtils.changeHealth(receiverPlayer, amount);

            player.sendMessage(LifeStealText.giftSuccess(amount, receiverPlayer.getName()));
            receiverPlayer.sendMessage(LifeStealText.receiveGift(amount, player.getName()));
        } else {
            // Offline
            OfflinePlayerData offlinePlayerData = OfflinePlayerData.getOfflinePlayerData(server, receiver);
            double offlineMaxHealth = offlinePlayerData.getMaxHealth();
            Text receiverName = Text.of(receiver.getName());
            if (!PlayerUtils.canChangeHealth(offlineMaxHealth, amount, gameRules)) {
                // Receiver has too much health
                source.sendError(LifeStealText.receiverTooMuchHealth(receiverName));
            }

            PlayerUtils.changeHealth(player, -amount);
            offlinePlayerData.setMaxHealth(offlineMaxHealth + amount);

            player.sendMessage(LifeStealText.giftSuccess(amount, receiverName));
        }
        return 1;
    }
}
