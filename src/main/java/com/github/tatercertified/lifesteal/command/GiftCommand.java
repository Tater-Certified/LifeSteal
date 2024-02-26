package com.github.tatercertified.lifesteal.command;

import com.github.tatercertified.lifesteal.util.ExchangeState;
import com.github.tatercertified.lifesteal.util.LsText;
import com.github.tatercertified.lifesteal.util.PlayerUtils;
import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.GameProfileArgumentType;
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

        if (gameRules.getBoolean(LSGameRules.ALTARS)) {
            source.sendError(LsText.GIFT_ALTAR);
            return 0;
        }

        if (!gameRules.getBoolean(LSGameRules.GIFTHEARTS)) {
            source.sendError(LsText.GIFT_DISABLED);
            return 0;
        }

        final int amount = IntegerArgumentType.getInteger(context, "healthPoints");
        if (amount > gameRules.getInt(LSGameRules.MAXPLAYERHEALTH) - gameRules.getInt(LSGameRules.MINPLAYERHEALTH)) {
            source.sendError(LsText.GIFT_OVER_LIMIT);
            return 0;
        }

        final Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
        if (profiles.isEmpty()) {
            source.sendError(LsText.GIFT_NONE);
            return 0;
        }
        if (profiles.size() > 1) {
            source.sendError(LsText.GIFT_MULTIPLE);
            return 0;
        }

        final GameProfile receiver = profiles.iterator().next();

        final ExchangeState result = PlayerUtils.exchangeHealth(player, receiver, amount);
        if (result == ExchangeState.SUCCESS) {
            return 1;
        }

        final ServerPlayerEntity receiverPlayer = server.getPlayerManager().getPlayer(receiver.getId());
        final Text receiverText = receiverPlayer == null ? Text.of(receiver.getName()) : receiverPlayer.getDisplayName();

        source.sendError(result.message.apply(receiverText));
        return 0;
    }
}
