package com.github.tatercertified.lifesteal.commands;

import com.github.tatercertified.lifesteal.Lifesteal;
import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.items.HeartItem;
import com.github.tatercertified.lifesteal.utils.LifeStealText;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
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
            Optional<PlayerConfigEntry> optionalGameProfile = server.getApiServices().nameToIdCache().getByUuid(playerId);
            optionalGameProfile.ifPresent(profile -> {
                if (DeathData.isPlayerDead(profile.id(), server.getGameRules().getInt(LifeStealGamerules.AUTOREVIVAL))) {
                    builder.suggest(profile.name());
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
        Optional<PlayerConfigEntry> optionalGameProfile = server.getApiServices().nameToIdCache().findByName(name);
        if (optionalGameProfile.isPresent()) {
            PlayerConfigEntry profile = optionalGameProfile.get();
            if (DeathData.isPlayerDead(profile.id(), 0)) {
                ItemUsageContext usageContext = new ItemUsageContext(source.getPlayer(), Hand.MAIN_HAND, new BlockHitResult(source.getPlayer().getEntityPos(), Direction.DOWN, source.getPlayer().getBlockPos(), true));
                HeartItem.revive(profile.id(), server, source.getWorld(), source.getPlayer().getBlockPos(), source.getPlayer(), Optional.of(usageContext));
                //DeathData.removeFromDeathDataList(profile.getId());
            } else {
                source.sendError(LifeStealText.playerIsAlive(Text.of(profile.name())));
                return 0;
            }
        } else {
            source.sendError(LifeStealText.notFound(name));
            return 0;
        }
        return 1;
    }
}
