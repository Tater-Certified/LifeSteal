package com.github.certifiedtater.lifesteal.commands;

import com.github.certifiedtater.lifesteal.Lifesteal;
import com.github.certifiedtater.lifesteal.data.DeathData;
import com.github.certifiedtater.lifesteal.gamerules.LifeStealGamerules;
import com.github.certifiedtater.lifesteal.items.HeartItem;
import com.github.certifiedtater.lifesteal.utils.LifeStealText;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ReviveCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            dispatcher.register(literal("revive")
                    .requires(ServerCommandSource::isExecutedByPlayer)
                    .then(argument("player", StringArgumentType.string())
                            .suggests(ReviveCommand::suggestPlayers)
                            .executes(ReviveCommand::revive)));
        });
    }

    private static CompletableFuture<Suggestions> suggestPlayers(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        MinecraftServer server = context.getSource().getServer();

        for (UUID playerId : Lifesteal.DEAD_PLAYERS.keySet()) {
            Optional<GameProfile> optionalGameProfile = server.getUserCache().getByUuid(playerId);
            optionalGameProfile.ifPresent(profile -> {
                if (DeathData.isPlayerDead(profile.getId(), server.getGameRules().getInt(LifeStealGamerules.AUTOREVIVAL))) {
                    builder.suggest(profile.getName());
                }
            });
        }

        return builder.buildFuture();
    }

    private static int revive(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        ServerCommandSource source = context.getSource();

        if (server.getGameRules().get(LifeStealGamerules.ALTARS).get()) {
            source.sendError(LifeStealText.REVIVE_ALTAR);
            return 0;
        }

        ItemStack holding = source.getPlayer().getMainHandStack();
        if (!(holding.getItem() instanceof HeartItem)) {
            source.sendError(LifeStealText.REVIVE_HOLD);
            return 0;
        }

        String name = StringArgumentType.getString(context, "player");
        Optional<GameProfile> optionalGameProfile = server.getUserCache().findByName(name);
        if (optionalGameProfile.isPresent()) {
            GameProfile profile = optionalGameProfile.get();
            if (DeathData.isPlayerDead(profile.getId(), 0)) {
                ItemUsageContext usageContext = new ItemUsageContext(source.getPlayer(), Hand.MAIN_HAND, new BlockHitResult(source.getPlayer().getPos(), Direction.DOWN, source.getPlayer().getBlockPos(), true));
                HeartItem.revive(profile.getId(), server, source.getWorld(), source.getPlayer().getBlockPos(), source.getPlayer(), Optional.of(usageContext));
                //DeathData.removeFromDeathDataList(profile.getId());
            } else {
                source.sendError(LifeStealText.playerIsAlive(Text.of(profile.getName())));
                return 0;
            }
        } else {
            source.sendError(LifeStealText.notFound(name));
            return 0;
        }
        return 1;
    }
}
