package com.github.tatercertified.lifesteal.command;

import com.github.tatercertified.lifesteal.util.Config;
import com.github.tatercertified.lifesteal.util.ExchangeState;
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

        if (!source.getServer().getGameRules().getBoolean(LSGameRules.GIFTHEARTS)) {
            source.sendError(Text.of(Config.HEART_GIFTING_DISABLED));
            return 0;
        }

        final int amount = IntegerArgumentType.getInteger(context, "healthPoints");
        if (amount > gameRules.getInt(LSGameRules.MAXPLAYERHEALTH) - gameRules.getInt(LSGameRules.MINPLAYERHEALTH)) {
            source.sendError(Text.of("Cannot give more than legal amount"));
            return 0;
        }

        final Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
        if (profiles.isEmpty()) {
            source.sendError(Text.of("Cannot give nobody hearts"));
            return 0;
        }
        if (profiles.size() > 1) {
            source.sendError(Text.of("Cannot give multiple folks hearts"));
            return 0;
        }

        final ExchangeState result = PlayerUtils.exchangeHealth(player, profiles.iterator().next(), amount);
        if (result == ExchangeState.SUCCESS) {
            return 1;
        }

        source.sendError(result.message.get());
        return 0;
    }
}
