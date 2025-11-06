package com.github.tatercertified.lifesteal.commands;

import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.gamerules.WithdrawMethod;
import com.github.tatercertified.lifesteal.utils.LifeStealText;
import com.github.tatercertified.lifesteal.utils.PlayerUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameRules;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class WithdrawCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((
                dispatcher,
                registryAccess,
                dedicated) -> dispatcher.register(literal("withdraw")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .then(argument("amount", IntegerArgumentType.integer(1))
                        .executes(WithdrawCommand::withdraw))));
    }

    private static int withdraw(CommandContext<ServerCommandSource> context) {
        final ServerCommandSource source = context.getSource();
        final MinecraftServer server = source.getServer();
        final GameRules gameRules = server.getGameRules();

        if (gameRules.get(LifeStealGamerules.WITHDRAW_METHOD).get() == WithdrawMethod.COMMAND) {
            final int amount = IntegerArgumentType.getInteger(context, "amount");

            PlayerUtils.convertHealthToHeartItems(source.getPlayer(), amount, server, false);
            return 1;
        } else {
            source.sendError(LifeStealText.WITHDRAW_COMMAND_DISABLED);
            return 0;
        }
    }
}
