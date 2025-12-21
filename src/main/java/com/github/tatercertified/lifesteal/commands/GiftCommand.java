package com.github.tatercertified.lifesteal.commands;

import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.gamerules.GiftMethod;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.utils.LifeStealText;
import com.github.tatercertified.lifesteal.utils.OfflinePlayerData;
import com.github.tatercertified.lifesteal.utils.PlayerMaxHealthInterface;
import com.github.tatercertified.lifesteal.utils.PlayerUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.Collection;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class GiftCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            dispatcher.register(literal("gift")
                    .requires(CommandSourceStack::isPlayer)
                    .then(argument("player", GameProfileArgument.gameProfile())
                            .then(argument("healthPoints", IntegerArgumentType.integer(1)).executes(GiftCommand::gift))));
        });
    }

    private static int gift(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final MinecraftServer server = source.getServer();
        final GameRules gameRules = source.getLevel().getGameRules();

        // Check if command is enabled

        if (gameRules.get(LifeStealGamerules.GIFT_METHOD) == GiftMethod.COMMAND) {
            final int amount = IntegerArgumentType.getInteger(context, "healthPoints");
            final ServerPlayer player = source.getPlayerOrException();

            // Check if the source has a large enough max health
            double maxHealth = ((PlayerMaxHealthInterface)player).getBaseMaxHealth();
            if (!PlayerUtils.canChangeHealth(maxHealth, -amount, gameRules)) {
                source.sendFailure(LifeStealText.LOW_HEALTH);
                return 0;
            }

            // Check if the target is valid
            final Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(context, "player");
            if (profiles.isEmpty()) {
                source.sendFailure(LifeStealText.GIFT_NONE);
                return 0;
            }
            if (profiles.size() > 1) {
                source.sendFailure(LifeStealText.GIFT_MULTIPLE);
                return 0;
            }

            final NameAndId receiver = profiles.iterator().next();

            if (receiver.id() == player.getUUID()) {
                // Can't gift to yourself
                source.sendFailure(LifeStealText.noSelfGifting(player.getName()));
                return 0;
            }

            if (DeathData.isPlayerDead(receiver.id(), gameRules.get(LifeStealGamerules.AUTOREVIVAL))) {
                // Can't gift to a dead guy
                source.sendFailure(LifeStealText.isDead(Component.nullToEmpty(receiver.name())));
                return 0;
            }

            ServerPlayer receiverPlayer = server.getPlayerList().getPlayer(receiver.id());

            if (receiverPlayer != null) {
                // Online
                if (PlayerUtils.changeHealth(receiverPlayer, amount)) {
                    PlayerUtils.changeHealthUnchecked(player, -amount);
                    player.sendSystemMessage(LifeStealText.giftSuccess(amount, receiverPlayer.getName()));
                    receiverPlayer.sendSystemMessage(LifeStealText.receiveGift(amount, player.getName()));
                } else {
                    // Receiver has too much health
                    source.sendFailure(LifeStealText.receiverTooMuchHealth(receiverPlayer.getName()));
                    return 0;
                }
            } else {
                // Offline
                OfflinePlayerData offlinePlayerData = OfflinePlayerData.getOfflinePlayerData(server, receiver);
                double offlineMaxHealth = offlinePlayerData.getMaxHealth();
                Component receiverName = Component.nullToEmpty(receiver.name());
                if (PlayerUtils.canChangeHealth(offlineMaxHealth, amount, gameRules)) {
                    PlayerUtils.changeHealthUnchecked(player, -amount);
                    offlinePlayerData.setMaxHealth(offlineMaxHealth + amount);

                    player.sendSystemMessage(LifeStealText.giftSuccess(amount, receiverName));
                } else {
                    // Receiver has too much health
                    source.sendFailure(LifeStealText.receiverTooMuchHealth(receiverName));
                    return 0;
                }
            }
            return 1;
        } else {
            source.sendFailure(LifeStealText.GIFT_COMMAND_DISABLED);
            return 0;
        }
    }
}
