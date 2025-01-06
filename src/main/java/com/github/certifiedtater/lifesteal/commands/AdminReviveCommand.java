package com.github.certifiedtater.lifesteal.commands;

import com.github.certifiedtater.lifesteal.data.DeathData;
import com.github.certifiedtater.lifesteal.gamerules.LifeStealGamerules;
import com.github.certifiedtater.lifesteal.items.HeartItem;
import com.github.certifiedtater.lifesteal.utils.LifeStealText;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AdminReviveCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            dispatcher.register(literal("revive")
                    .requires(ServerCommandSource::isExecutedByPlayer)
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                          .executes(AdminReviveCommand::reset)));
        });
    }

    public static int reset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();
        final Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
        if (profiles.size() != 1) {
            // Error
            return 0;
        }
        final GameProfile receiver = profiles.iterator().next();
        if (!DeathData.isPlayerDead(receiver.getId(), source.getServer().getGameRules().getInt(LifeStealGamerules.AUTOREVIVAL))) {
            // Error
            source.sendError(LifeStealText.playerIsAlive(Text.of(receiver.getName())));
            return 0;
        }
        HeartItem.revive(receiver.getName(), source.getServer(), source.getWorld(), source.getPlayer().getBlockPos(), source.getPlayer(), Optional.empty());
        DeathData.removeFromDeathDataList(receiver.getId());
        context.getSource().sendFeedback(() -> LifeStealText.adminRevive(receiver.getName()), true);
        return 1;
    }
}
