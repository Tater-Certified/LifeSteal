package com.github.tatercertified.lifesteal.commands;

import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.utils.LifeStealText;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.NameAndId;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.Optional;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class AdminReviveCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            dispatcher.register(literal("admin-revive")
                    .requires(CommandSourceStack::isPlayer)
                    .requires(Commands.hasPermission(GameModeCommand.PERMISSION_CHECK))
                    .then(argument("player", GameProfileArgument.gameProfile())
                          .executes(AdminReviveCommand::reset)));
        });
    }

    public static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(context, "player");
        if (profiles.size() != 1) {
            // Error
            return 0;
        }
        final NameAndId receiver = profiles.iterator().next();
        if (!DeathData.isPlayerDead(receiver.id(), source.getLevel().getGameRules().get(LifeStealGamerules.AUTOREVIVAL))) {
            // Error
            source.sendFailure(LifeStealText.playerIsAlive(Component.nullToEmpty(receiver.name())));
            return 0;
        }
        DeathData.revive(receiver.name(), source.getServer(), source.getLevel(), source.getPlayer().blockPosition(), source.getPlayer(), Optional.empty());
        DeathData.removeFromDeathDataList(receiver.id());
        context.getSource().sendSuccess(() -> LifeStealText.adminRevive(receiver.name()), true);
        return 1;
    }
}
