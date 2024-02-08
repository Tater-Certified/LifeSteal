package com.github.tatercertified.lifesteal.command;

import com.github.tatercertified.lifesteal.util.Config;
import com.github.tatercertified.lifesteal.util.PlayerUtils;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class AdminCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> dispatcher.register(literal("resetLifeStealBans")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(AdminCommand::reset)));
    }

    public static int reset(CommandContext<ServerCommandSource> context) {
        PlayerUtils.clearDeadList(context.getSource().getServer());
        context.getSource().sendFeedback(() -> Text.literal(Config.RESET), true);
        return 1;
    }
}
