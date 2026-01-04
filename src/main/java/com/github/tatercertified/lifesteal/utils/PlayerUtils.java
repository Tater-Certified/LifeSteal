package com.github.tatercertified.lifesteal.utils;

import com.github.tatercertified.lifesteal.Lifesteal;
import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.gamerules.DeathAction;
import com.github.tatercertified.lifesteal.gamerules.DeathCriteria;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.gamerules.LimitedCraftingType;
import com.github.tatercertified.lifesteal.items.ModItems;
import com.github.tatercertified.lifesteal.mixin.ServerPlayerServerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class PlayerUtils {

    /**
     * Handles how the server should process a dead player.
     * Options include banning them, immediately reviving them, or putting them in spectator
     * @param player ServerPlayerEntity that died
     * @param data The player's DeathData
     */
    public static void handleDeadPlayerAction(ServerPlayer player, DeathData data) {
        GameRules gameRules = player.level().getGameRules();
        DeathAction action = gameRules.get(LifeStealGamerules.DEATH_ACTION);
        switch (action) {
            case BAN -> {
                if (gameRules.get(LifeStealGamerules.AUTO_REVIVAL) == 0) {
                    player.connection.disconnect(LifeStealText.DEATH);
                } else {
                    player.connection.disconnect(LifeStealText.deathTime((int) (data.deathTime + gameRules.get(LifeStealGamerules.AUTO_REVIVAL) - (System.currentTimeMillis() * 0.001))));
                }
            }
            case REVIVE -> {
                setMaxHearts(player, gameRules.get(LifeStealGamerules.MIN_PLAYER_HEARTS));
                DeathData.removeFromDeathDataList(player.getUUID()); // I know this is a waste of processing power... but I don't care
                ((PlayerReviveData)player).setNewlyRevived(true); // Prevent heart duplication
            }
            case SPECTATOR -> {
                player.setGameMode(GameType.SPECTATOR);
                ((PlayerGameModeInterface)(player.gameMode)).setPreviousGameMode(GameType.SPECTATOR);
            }
        }
    }

    /**
     * Handles the player joining the server.
     * It determines if the player should be revived or not
     * @param player ServerPlayerEntity that joined
     */
    public static void handlePlayerJoin(ServerPlayer player) {
        DeathData data = Lifesteal.DEAD_PLAYERS.get(player.getUUID());
        if (data != null) {
            // A reviver takes highest priority
            if (data.reviverPlayerID == null) {
                if (DeathData.shouldAutoRevive(data, player.level().getGameRules().get(LifeStealGamerules.AUTO_REVIVAL))) {
                    // Autorevival timer up
                    handlePostRevival(data, player, true);
                } else {
                    // Still dead
                    handleDeadPlayerAction(player, data);
                }
            } else {
                // Reviver found
                handlePostRevival(data, player, false);
            }
        }
    }

    /**
     * Handles the revival of a player when they log back in
     * @param data DeathData of the player
     * @param player ServerPlayerEntity that was revived
     * @param autoRevived If the player was revived due to the automatic revival system
     */
    private static void handlePostRevival(DeathData data, ServerPlayer player, boolean autoRevived) {
        GameRules gameRules = player.level().getGameRules();
        if (!autoRevived) {
            player.sendSystemMessage(LifeStealText.onRevivalText(data, ((ServerPlayerServerAccessor) player).getServer()));
        } else {
            // Autorevived players shouldn't be exempted from the antiHeartDupe
            ((PlayerReviveData)player).setNewlyRevived(true);
        }
        // Check if invulnerability should be applied
        int invulnerability = gameRules.get(LifeStealGamerules.RESPAWN_INVULNERABILITY);
        if (invulnerability != 0) {
            ((PlayerInvulnerabilityInterface)player).setReviveInvulnerability();
        }
        DeathData.removeFromDeathDataList(player.getUUID());
    }

    /**
     * Handles when a player dies in any circumstance (player or no player)
     * @param killed The player that died
     * @param attacker The player who killed the player who died (or null if there was none).
     */
    public static void handleDeath(ServerPlayer killed, @Nullable ServerPlayer attacker) {
        GameRules gameRules = killed.level().getGameRules();

        // Don't do anything if this gamerule is enabled
        if (gameRules.get(LifeStealGamerules.DEATH_CRITERIA) == DeathCriteria.PLAYER_ONLY && attacker == null) {
            return;
        }

        int killedOldHearts = getMaxHearts(killed);
        int killedNewHearts = killedOldHearts - gameRules.get(LifeStealGamerules.STEAL_AMOUNT);
        boolean isDead = killedNewHearts < gameRules.get(LifeStealGamerules.MIN_PLAYER_HEARTS);

        if (isDead) {
            // Considered "banned"
            if (gameRules.get(LifeStealGamerules.LIMITED_CRAFTING_TYPE) == LimitedCraftingType.UNTIL_BANNED) {
                ((CraftedHeartsInterface)killed).resetHeartsCrafted();
            }
            DeathData data = new DeathData(killed.getUUID());
            data.addToDeathDataList();
            handleDeadPlayerAction(killed, data);
        } else {
            setMaxHearts(killed, killedNewHearts);
        }

        // Check to see if heart should be rewarded
        if (((PlayerReviveData)killed).newlyRevived() && gameRules.get(LifeStealGamerules.ANTI_HEART_DUPE)) {
            return;
        }

        int heartsAwarded = Math.min(killedOldHearts, gameRules.get(LifeStealGamerules.STEAL_AMOUNT));
        if (attacker != null) {
            int attackerNewHearts = getMaxHearts(attacker) + heartsAwarded;
            if (attackerNewHearts > gameRules.get(LifeStealGamerules.MAX_PLAYER_HEARTS)) {
                // They can't get more health, but they can still get an item to prevent heart deletion
                attacker.displayClientMessage(LifeStealText.MAX_HEALTH, true);
                givePlayerHeart(attacker, heartsAwarded);
            } else {
                setMaxHearts(attacker, attackerNewHearts);
            }
        } else if (gameRules.get(LifeStealGamerules.DEATH_CRITERIA) == DeathCriteria.ANY_DEATH_DROP_HEART) {
            dropHearts(killed, heartsAwarded);
        }
    }

    /**
     * Gets the maximum number of hearts that can be accepted by a player
     * @param currentHearts Player's current max hearts
     * @param hearts Hearts requested
     * @param gameRules GameRules instance
     * @return The maximum number of hearts that the player can accept within their requested amount
     */
    public static int maxHeartsAccepted(int currentHearts, int hearts, GameRules gameRules) {
        if (hearts < 0) {
            return 0;
        }
        int newHearts = Math.min(gameRules.get(LifeStealGamerules.MAX_PLAYER_HEARTS), hearts + currentHearts);
        return newHearts - currentHearts;
    }

    /**
     * Gets the maximum number of hearts that can be removed from a player
     * @param player Player removing hearts
     * @param hearts Hearts requested
     * @return The maximum number of hearts that the player can remove within their requested amount
     */
    public static int maxHeartsRemoved(ServerPlayer player, int hearts) {
        if (hearts < 0) {
            return 0;
        }
        GameRules gameRules = player.level().getGameRules();
        int maxHearts = getMaxHearts(player);
        int newHearts = Math.max(gameRules.get(LifeStealGamerules.MIN_PLAYER_HEARTS), maxHearts - hearts);
        return maxHearts - newHearts;
    }

    /**
     * Sets the new max hearts for a Player
     * @param player Player to change the max base hearts
     * @param maxHearts Max number of hearts
     */
    public static void setMaxHearts(ServerPlayer player, int maxHearts) {
        ((PlayerMaxHealthInterface)player).setBaseMaxHealth(maxHearts * 2);
    }

    /**
     * Gets the max number of hearts of a player
     * @param player The player to query
     * @return Gets the max base hearts of the ServerPlayer
     */
    public static int getMaxHearts(ServerPlayer player) {
        return (int) ((PlayerMaxHealthInterface)player).getBaseMaxHealth() / 2;
    }

    /**
     * Withdraws the specified number of hearts from the player
     * @param player The player that is withdrawing hearts
     * @param hearts The number of requested hearts
     */
    public static void handleWithdraw(ServerPlayer player, int hearts) {
        if (hearts <= 0) {
            player.displayClientMessage(LifeStealText.LOW_HEALTH, true);
            return;
        }
        GameRules gameRules = player.level().getGameRules();
        int playerMaxHearts = getMaxHearts(player);
        if (playerMaxHearts == gameRules.get(LifeStealGamerules.MIN_PLAYER_HEARTS)) {
            // Not enough hearts
            player.displayClientMessage(LifeStealText.LOW_HEALTH, true);
        } else {
            int heartsToWithdraw = Math.min(hearts, playerMaxHearts - gameRules.get(LifeStealGamerules.MIN_PLAYER_HEARTS));
            int heartsAfterWithdraw = playerMaxHearts - heartsToWithdraw;
            setMaxHearts(player, heartsAfterWithdraw);
            givePlayerHeart(player, hearts);
            player.displayClientMessage(LifeStealText.withdrawnHealth(hearts), true);
        }
    }

    /**
     * Gives the player the specified number of hearts
     * @param player ServerPlayerEntity that gets the hearts
     * @param hearts Number of hearts
     */
    private static void givePlayerHeart(ServerPlayer player, int hearts) {
        final ItemStack heartStack = new ItemStack(ModItems.HEART, 1);
        for (int i = 0; i < hearts; i++) {
            if (!player.addItem(heartStack.copy())) {
                // Quick path for dropping the rest of the hearts to avoid unnecessary checks
                for (int j = i; j < hearts; j++) {
                    player.drop(heartStack.copy(), false, true);
                }
                break;
            }
        }
    }

    /**
     * Drops the heart items where the player died
     * @param deadPlayer The player that died
     * @param hearts The number of heart items to drop
     */
    private static void dropHearts(ServerPlayer deadPlayer, int hearts) {
        final ItemStack heartStack = new ItemStack(ModItems.HEART, 1);
        for (int i = 0; i < hearts; i++) {
            deadPlayer.drop(heartStack.copy(), false, true);
        }
    }

    /**
     * Increments the Player's hearts by 1 if possible
     * @param player Player to increment health by 1
     * @return If the player's health was incremented
     */
    public static boolean incrementHearts(ServerPlayer player) {
        GameRules gameRules = player.level().getGameRules();
        int playerNewMaxHearts = getMaxHearts(player) + 1;
        if (playerNewMaxHearts > gameRules.get(LifeStealGamerules.MAX_PLAYER_HEARTS)) {
            player.displayClientMessage(LifeStealText.MAX_HEALTH, true);
            return false;
        } else {
            setMaxHearts(player, playerNewMaxHearts);
            return true;
        }
    }

    /**
     * Revives a player at the location of the reviver
     * @param reviveeId The UUID of the player being revived
     * @param reviver The player doing the reviving
     * @param context If an item was used. Null if not
     * @return 0 if success, 1 if error, and 2 if the player was not found
     */
    public static byte revive(UUID reviveeId, ServerPlayer reviver, UseOnContext context) {
        return revive(reviveeId, reviver.level().getServer(), reviver.level(), reviver.blockPosition(), reviver, context);
    }

    /**
     * Revives a player at a specified location
     * @param playerName The player's name that is being revived
     * @param server Instance of MinecraftServer
     * @param world World that the revived player should spawn in
     * @param pos The position the revived player should spawn at
     * @param reviver The player doing the reviving
     * @param context If an item was used. Null if not
     * @return 0 if success, 1 if error, and 2 if the player was not found
     */
    public static byte revive(String playerName, MinecraftServer server, ServerLevel world, BlockPos pos, ServerPlayer reviver, @Nullable UseOnContext context) {
        return revive(server.getPlayerList().getPlayerByName(playerName), null, playerName, server, world, pos, reviver, context);
    }

    /**
     * Revives a player at a specified location
     * @param reviveeId The player's UUID that is being revived
     * @param server Instance of MinecraftServer
     * @param world World that the revived player should spawn in
     * @param pos The position the revived player should spawn at
     * @param reviver The player doing the reviving
     * @param context If an item was used. Null if not
     * @return 0 if success, 1 if error, and 2 if the player was not found
     */
    public static byte revive(UUID reviveeId, MinecraftServer server, ServerLevel world, BlockPos pos, ServerPlayer reviver, UseOnContext context) {
        return revive(server.getPlayerList().getPlayer(reviveeId), reviveeId, null, server, world, pos, reviver, context);
    }

    private static byte revive(ServerPlayer revivee, @Nullable UUID reviveeId, @Nullable String reviveeName, MinecraftServer server, ServerLevel world, BlockPos pos, ServerPlayer reviver, UseOnContext context) {
        boolean fromHeartItem = context != null;
        if (revivee != null) {
            if (reviveOnline(revivee, world, pos, reviver, fromHeartItem)) {
                revived(reviver, context, revivee.getDisplayName());
                return 0;
            }
            failed(reviver, pos, revivee.getDisplayName());
            return 1;
        }

        Optional<NameAndId> profile;
        if (reviveeId != null) {
            profile = server.services().nameToIdCache().get(reviveeId);
        } else if (reviveeName != null) {
            profile = server.services().nameToIdCache().get(reviveeName);
        } else {
            profile = Optional.empty();
        }

        if (profile.isPresent()) {
            if (reviveOffline(profile.get(), world, pos, reviver, fromHeartItem)) {
                revived(reviver, context, Component.nullToEmpty(profile.get().name()));
                return 0;
            }
            failed(reviver, pos, Component.nullToEmpty(profile.get().name()));
            return 1;
        }
        return 2;
    }

    private static boolean reviveOnline(ServerPlayer player, ServerLevel world, BlockPos alter, Player reviver, boolean fromHeartItem) {
        if (!DeathData.isPlayerDead(player.getUUID(), world.getGameRules().get(LifeStealGamerules.AUTO_REVIVAL))) {
            return false;
        }
        teleport(player, world, alter);
        player.setGameMode(GameType.SURVIVAL);

        player.sendSystemMessage(LifeStealText.onRevivalText(reviver.getDisplayName()));
        PlayerUtils.setMaxHearts(player, world.getGameRules().get(LifeStealGamerules.MIN_PLAYER_HEARTS));
        DeathData.removeFromDeathDataList(player.getUUID());
        // These players are not newly revived if a heart wasn't consumed to revive them
        ((PlayerReviveData)player).setNewlyRevived(!fromHeartItem);
        return true;
    }

    private static boolean reviveOffline(NameAndId profile, ServerLevel world, BlockPos alter, Player reviver, boolean fromHeartItem) {
        if (!DeathData.isPlayerDead(profile.id(), world.getGameRules().get(LifeStealGamerules.AUTO_REVIVAL))) {
            return false;
        }

        MinecraftServer server = world.getServer();
        OfflinePlayerData playerData = OfflinePlayerData.getOfflinePlayerData(server, profile);
        if (playerData == null) {
            return false;
        }
        playerData.setPosition(world, alter.above().getCenter());
        playerData.setGamemode(GameType.SURVIVAL);
        playerData.setMaxHearts(world.getGameRules().get(LifeStealGamerules.MIN_PLAYER_HEARTS));
        // These players are not newly revived if a heart was consumed to revive them
        playerData.setNewlyRevived(!fromHeartItem);
        playerData.save();

        DeathData.setReviver(profile.id(), reviver.getUUID());
        DeathData.removeFromDeathDataList(profile.id());
        return true;
    }

    private static void revived(ServerPlayer reviver, @Nullable UseOnContext context, Component revived) {
        if (context != null) {
            successSound(context.getLevel(), context.getClickedPos());
            context.getItemInHand().shrink(1);
            reviver.displayClientMessage(LifeStealText.revived(revived), true);
        }
    }

    private static void successSound(Level world, BlockPos alter) {
        world.playSound(null, alter, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 16.f, 1);
    }

    private static void failed(ServerPlayer reviver, BlockPos alter, Component revived) {
        failedSound(reviver.level(), alter);
        reviver.displayClientMessage(LifeStealText.playerIsAlive(revived), true);
    }

    /**
     * The sound that should play if a revive fails
     * @param world The world to play the sound in
     * @param pos The position to play it at
     */
    public static void failedSound(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.PLAYERS, 16.f, 1);
    }

    private static void teleport(Player player, ServerLevel target, BlockPos alterPos) {
        Vec3 pos = alterPos.above().getCenter();
        player.teleport(new TeleportTransition(target, pos, Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
    }
}