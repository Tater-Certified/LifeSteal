package com.github.tatercertified.lifesteal.command;

import com.github.tatercertified.lifesteal.util.PlayerUtils;
import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GiftCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            dispatcher.register(literal("gift")
                    .then(argument("player", StringArgumentType.string())
                            .then(argument("healthPoints", IntegerArgumentType.integer()).executes(GiftCommand::gift))));
        });
    }

    public static int gift(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer() || !context.getSource().getServer().getGameRules().getBoolean(LSGameRules.GIFTHEARTS)) {
            return 0;
        }

        Optional<GameProfile> profile = context.getSource().getServer().getUserCache().findByName(StringArgumentType.getString(context, "player"));
        if (profile.isPresent()) {
            UUID id = profile.get().getId();

            PlayerUtils.exchangeHealth(context.getSource().getPlayer(), id, IntegerArgumentType.getInteger(context, "healthPoints"), context.getSource().getServer());
        }
        return 1;
    }
}
