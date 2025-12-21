package com.github.tatercertified.lifesteal.commands;

import com.github.tatercertified.lifesteal.Lifesteal;
import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.gamerules.ReviveMethod;
import com.github.tatercertified.lifesteal.items.HeartItem;
import com.github.tatercertified.lifesteal.utils.LifeStealText;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ReviveCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            dispatcher.register(literal("revive")
                    .requires(CommandSourceStack::isPlayer)
                    .then(argument("player", StringArgumentType.string())
                            .suggests(ReviveCommand::suggestPlayers)
                            .executes(ReviveCommand::revive)));
        });
    }

    private static CompletableFuture<Suggestions> suggestPlayers(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        MinecraftServer server = context.getSource().getServer();

        for (UUID playerId : Lifesteal.DEAD_PLAYERS.keySet()) {
            Optional<NameAndId> optionalGameProfile = server.services().nameToIdCache().get(playerId);
            optionalGameProfile.ifPresent(profile -> {
                if (DeathData.isPlayerDead(profile.id(), context.getSource().getLevel().getGameRules().get(LifeStealGamerules.AUTOREVIVAL))) {
                    builder.suggest(profile.name());
                }
            });
        }

        return builder.buildFuture();
    }

    private static int revive(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        CommandSourceStack source = context.getSource();

        if (source.getLevel().getGameRules().get(LifeStealGamerules.REVIVE_METHOD) == ReviveMethod.COMMAND) {
            ItemStack holding = source.getPlayer().getMainHandItem();
            if (!(holding.getItem() instanceof HeartItem)) {
                source.sendFailure(LifeStealText.REVIVE_HOLD);
                return 0;
            }

            String name = StringArgumentType.getString(context, "player");
            Optional<NameAndId> optionalGameProfile = server.services().nameToIdCache().get(name);
            if (optionalGameProfile.isPresent()) {
                NameAndId profile = optionalGameProfile.get();
                if (DeathData.isPlayerDead(profile.id(), 0)) {
                    UseOnContext usageContext = new UseOnContext(source.getPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(source.getPlayer().position(), Direction.DOWN, source.getPlayer().blockPosition(), true));
                    DeathData.revive(profile.id(), server, source.getLevel(), source.getPlayer().blockPosition(), source.getPlayer(), Optional.of(usageContext));
                    //DeathData.removeFromDeathDataList(profile.getId());
                } else {
                    source.sendFailure(LifeStealText.playerIsAlive(Component.nullToEmpty(profile.name())));
                    return 0;
                }
            } else {
                source.sendFailure(LifeStealText.notFound(name));
                return 0;
            }
            return 1;
        } else {
            source.sendFailure(LifeStealText.REVIVE_COMMAND_DISABLED);
            return 0;
        }
    }
}
