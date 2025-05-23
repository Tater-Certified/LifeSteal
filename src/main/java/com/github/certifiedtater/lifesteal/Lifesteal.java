package com.github.certifiedtater.lifesteal;

import com.github.certifiedtater.lifesteal.block.ModBlocks;
import com.github.certifiedtater.lifesteal.commands.AdminReviveCommand;
import com.github.certifiedtater.lifesteal.commands.GiftCommand;
import com.github.certifiedtater.lifesteal.commands.WithdrawCommand;
import com.github.certifiedtater.lifesteal.data.DeathData;
import com.github.certifiedtater.lifesteal.effect.InvulnerableStatusEffect;
import com.github.certifiedtater.lifesteal.gamerules.LifeStealGamerules;
import com.github.certifiedtater.lifesteal.items.HeartItem;
import com.github.certifiedtater.lifesteal.items.ModItems;
import com.github.certifiedtater.lifesteal.utils.PlayerInvulnerabilityInterface;
import com.github.certifiedtater.lifesteal.utils.PlayerUtils;
import com.github.certifiedtater.lifesteal.world.Ores;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.nio.file.Path;
import java.util.*;

public class Lifesteal implements ModInitializer {

    public static final String MOD_ID = "lifesteal";
    public static final Map<UUID, DeathData> DEAD_PLAYERS = new HashMap<>();
    public static final Path DEAD_PLAYERS_FILE_PATH = Path.of(FabricLoader.getInstance().getConfigDir().resolve("lifesteal-deaths.json").toString());
    public static Team invulnerableTeam;

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(Lifesteal.MOD_ID);
        DeathData.loadDeathDataFromFile(); // Load DeathData
        ModItems.initialize();
        ModBlocks.registerBlocks();
        Ores.initOres();
        AdminReviveCommand.register();
        GiftCommand.register();
        WithdrawCommand.register();
        LifeStealGamerules.init();
        InvulnerableStatusEffect.register();

        // You can't remove the effect through suicide either... sorry
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, b) -> {
            if (((PlayerInvulnerabilityInterface)oldPlayer).isReviveInvulnerable()) {
                newPlayer.addStatusEffect(new StatusEffectInstance(InvulnerableStatusEffect.INVULNERABLE, ((PlayerInvulnerabilityInterface)oldPlayer).getRemaining(), 0, false, false, true));
                newPlayer.getScoreboard().addScoreHolderToTeam(newPlayer.getNameForScoreboard(), invulnerableTeam);
            }
        });

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> LifeStealGamerules.serverInstance = minecraftServer);

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            boolean containsTeam = minecraftServer.getScoreboard().getTeams().stream()
                    .anyMatch(team -> team.getName().equals("invulnerable"));

            if (!containsTeam) {
                invulnerableTeam = minecraftServer.getScoreboard().addTeam("invulnerable");
                invulnerableTeam.setColor(Formatting.DARK_RED);
                invulnerableTeam.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.ALWAYS);
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
            if (player instanceof ServerPlayerEntity serverPlayer) {
                MinecraftServer server = world.getServer();
                if (server.getGameRules().getBoolean(LifeStealGamerules.ALTARS)
                        && serverPlayer.isSneaking()
                        && hand == serverPlayer.getActiveHand()
                        && serverPlayer.getStackInHand(hand).isEmpty()
                        && HeartItem.isAltar(world, hitResult.getBlockPos())) {
                    PlayerUtils.convertHealthToHeartItems(serverPlayer, 1, server, true);
                }
            }
            return ActionResult.PASS;
        });
    }
}
