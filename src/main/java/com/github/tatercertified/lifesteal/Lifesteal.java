package com.github.tatercertified.lifesteal;

import com.github.tatercertified.lifesteal.block.ModBlocks;
import com.github.tatercertified.lifesteal.commands.*;
import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.effect.InvulnerableStatusEffect;
import com.github.tatercertified.lifesteal.effect.ParticleAnimation;
import com.github.tatercertified.lifesteal.effect.WithdrawRitualAnimation;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.gamerules.WithdrawMethod;
import com.github.tatercertified.lifesteal.items.HeartItem;
import com.github.tatercertified.lifesteal.items.ModItems;
import com.github.tatercertified.lifesteal.mixin.ServerPlayerServerAccessor;
import com.github.tatercertified.lifesteal.utils.PlayerInvulnerabilityInterface;
import com.github.tatercertified.lifesteal.utils.PlayerUtils;
import com.github.tatercertified.lifesteal.world.Ores;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;

import java.nio.file.Path;
import java.util.*;

public class Lifesteal implements ModInitializer {

    public static final String MOD_ID = "lifesteal";
    public static final Map<UUID, DeathData> DEAD_PLAYERS = new HashMap<>();
    public static final Path DEAD_PLAYERS_FILE_PATH = Path.of(FabricLoader.getInstance().getConfigDir().resolve("lifesteal-deaths.json").toString());
    public static final List<ParticleAnimation> ANIMATIONS = new ArrayList<>();
    public static PlayerTeam invulnerableTeam;

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(Lifesteal.MOD_ID);
        DeathData.loadDeathDataFromFile(); // Load DeathData
        ModItems.initialize();
        ModBlocks.registerBlocks();
        Ores.initOres();
        AdminReviveCommand.register();
        ReviveCommand.register();
        GiftCommand.register();
        WithdrawCommand.register();
        AdminCancelInvulnerabilityCommand.register();
        LifeStealGamerules.init();
        InvulnerableStatusEffect.register();

        // You can't remove the effect through suicide either... sorry
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, b) -> {
            if (((PlayerInvulnerabilityInterface)oldPlayer).isReviveInvulnerable()) {
                newPlayer.addEffect(new MobEffectInstance(InvulnerableStatusEffect.INVULNERABLE, ((PlayerInvulnerabilityInterface)oldPlayer).getRemaining(), 0, false, false, true));
                ((ServerPlayerServerAccessor) newPlayer).getServer().getScoreboard().addPlayerToTeam(newPlayer.getScoreboardName(), invulnerableTeam);
            }
        });

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> LifeStealGamerules.serverInstance = minecraftServer);

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            boolean containsTeam = minecraftServer.getScoreboard().getPlayerTeams().stream()
                    .anyMatch(team -> team.getName().equals("invulnerable"));

            if (containsTeam) {
                invulnerableTeam = minecraftServer.getScoreboard().getPlayerTeam("invulnerable");
            } else {
                invulnerableTeam = minecraftServer.getScoreboard().addPlayerTeam("invulnerable");
                invulnerableTeam.setColor(ChatFormatting.DARK_RED);
                invulnerableTeam.setNameTagVisibility(Team.Visibility.ALWAYS);
            }
        });

        ServerTickEvents.START_WORLD_TICK.register(level -> {
            Iterator<ParticleAnimation> iterator = ANIMATIONS.iterator();
            while (iterator.hasNext()) {
                ParticleAnimation anim = iterator.next();
                anim.tick(level);
                if (anim.isDone()) {
                    anim.onDone(level);
                    iterator.remove();
                }
            }
        });

        /*
         This callback checks if a player is considered dead
         */
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PlayerUtils.handlePlayerJoin(handler.getPlayer()));

        /*
		 This callback exchanges HP for heart items if right-clicking on an altar
		 */
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                GameRules gameRules = ((ServerLevel) world).getGameRules();
                if (gameRules.get(LifeStealGamerules.WITHDRAW_METHOD) == WithdrawMethod.ALTAR
                        && serverPlayer.isShiftKeyDown()
                        && hand == serverPlayer.getUsedItemHand()
                        && serverPlayer.getItemInHand(hand).isEmpty()
                        && HeartItem.isAltar((ServerLevel) world, hitResult.getBlockPos())) {
                    if (gameRules.get(LifeStealGamerules.DO_ALTAR_ANIMATIONS)) {
                        if (PlayerUtils.handleWithdraw(serverPlayer, 1, false)) {
                            Lifesteal.ANIMATIONS.add(new WithdrawRitualAnimation(serverPlayer, hitResult.getBlockPos()));
                        }
                    } else {
                        PlayerUtils.handleWithdraw(serverPlayer, 1, true);
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }
}
