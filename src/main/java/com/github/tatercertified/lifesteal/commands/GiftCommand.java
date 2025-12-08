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
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.rule.GameRules;

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

    private static int gift(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();
        final MinecraftServer server = source.getServer();
        final GameRules gameRules = source.getWorld().getGameRules();

        // Check if command is enabled

        if (gameRules.getValue(LifeStealGamerules.GIFT_METHOD) == GiftMethod.COMMAND) {
            final int amount = IntegerArgumentType.getInteger(context, "healthPoints");
            final ServerPlayerEntity player = source.getPlayerOrThrow();

            // Check if the source has a large enough max health
            double maxHealth = ((PlayerMaxHealthInterface)player).getBaseMaxHealth();
            if (!PlayerUtils.canChangeHealth(maxHealth, -amount, gameRules)) {
                source.sendError(LifeStealText.LOW_HEALTH);
                return 0;
            }

            // Check if the target is valid
            final Collection<PlayerConfigEntry> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
            if (profiles.isEmpty()) {
                source.sendError(LifeStealText.GIFT_NONE);
                return 0;
            }
            if (profiles.size() > 1) {
                source.sendError(LifeStealText.GIFT_MULTIPLE);
                return 0;
            }

            final PlayerConfigEntry receiver = profiles.iterator().next();

            if (receiver.id() == player.getUuid()) {
                // Can't gift to yourself
                source.sendError(LifeStealText.noSelfGifting(player.getName()));
                return 0;
            }

            if (DeathData.isPlayerDead(receiver.id(), gameRules.getValue(LifeStealGamerules.AUTOREVIVAL))) {
                // Can't gift to a dead guy
                source.sendError(LifeStealText.isDead(Text.of(receiver.name())));
                return 0;
            }

            ServerPlayerEntity receiverPlayer = server.getPlayerManager().getPlayer(receiver.id());

            if (receiverPlayer != null) {
                // Online
                if (PlayerUtils.changeHealth(receiverPlayer, amount)) {
                    PlayerUtils.changeHealthUnchecked(player, -amount);
                    player.sendMessage(LifeStealText.giftSuccess(amount, receiverPlayer.getName()));
                    receiverPlayer.sendMessage(LifeStealText.receiveGift(amount, player.getName()));
                } else {
                    // Receiver has too much health
                    source.sendError(LifeStealText.receiverTooMuchHealth(receiverPlayer.getName()));
                    return 0;
                }
            } else {
                // Offline
                OfflinePlayerData offlinePlayerData = OfflinePlayerData.getOfflinePlayerData(server, receiver);
                double offlineMaxHealth = offlinePlayerData.getMaxHealth();
                Text receiverName = Text.of(receiver.name());
                if (PlayerUtils.canChangeHealth(offlineMaxHealth, amount, gameRules)) {
                    PlayerUtils.changeHealthUnchecked(player, -amount);
                    offlinePlayerData.setMaxHealth(offlineMaxHealth + amount);

                    player.sendMessage(LifeStealText.giftSuccess(amount, receiverName));
                } else {
                    // Receiver has too much health
                    source.sendError(LifeStealText.receiverTooMuchHealth(receiverName));
                    return 0;
                }
            }
            return 1;
        } else {
            source.sendError(LifeStealText.GIFT_COMMAND_DISABLED);
            return 0;
        }
    }
}
