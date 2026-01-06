package com.github.tatercertified.lifesteal.commands;

import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.gamerules.WithdrawMethod;
import com.github.tatercertified.lifesteal.utils.LifeStealText;
import com.github.tatercertified.lifesteal.utils.PlayerUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class WithdrawCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((
                dispatcher,
                registryAccess,
                dedicated) -> dispatcher.register(literal("withdraw")
                .requires(CommandSourceStack::isPlayer)
                .then(argument("hearts", IntegerArgumentType.integer(1))
                        .executes(WithdrawCommand::withdraw))));
    }

    private static int withdraw(CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        final GameRules gameRules = source.getLevel().getGameRules();

        if (source.isPlayer() && gameRules.get(LifeStealGamerules.WITHDRAW_METHOD) == WithdrawMethod.COMMAND) {
            ServerPlayer player = source.getPlayer();
            PlayerUtils.handleWithdraw(player, IntegerArgumentType.getInteger(context, "hearts"), true);
            return 1;
        } else {
            source.sendFailure(LifeStealText.WITHDRAW_COMMAND_DISABLED);
            return 0;
        }
    }
}
