package com.github.tatercertified.lifesteal.command;

import com.github.tatercertified.lifesteal.util.PlayerUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class WithdrawCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            dispatcher.register(literal("withdraw")
                    .then(argument("amount", IntegerArgumentType.integer())
                            .executes(WithdrawCommand::withdraw)));
        });
    }

    private static int withdraw(CommandContext<ServerCommandSource> context) {
        if (context.getSource().isExecutedByPlayer()) {
            PlayerUtils.convertHealthToHeartItems(context.getSource().getPlayer(), IntegerArgumentType.getInteger(context, "amount"), context.getSource().getServer());
        }
        return 1;
    }
}
