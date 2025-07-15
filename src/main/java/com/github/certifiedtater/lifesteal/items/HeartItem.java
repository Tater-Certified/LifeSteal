package com.github.certifiedtater.lifesteal.items;

import com.github.certifiedtater.lifesteal.data.DeathData;
import com.github.certifiedtater.lifesteal.gamerules.LifeStealGamerules;
import com.github.certifiedtater.lifesteal.utils.LifeStealText;
import com.github.certifiedtater.lifesteal.utils.OfflinePlayerData;
import com.github.certifiedtater.lifesteal.utils.PlayerReviveData;
import com.github.certifiedtater.lifesteal.utils.PlayerUtils;
import com.mojang.authlib.GameProfile;
import de.olivermakesco.polyspring.api.BedrockItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;
import java.util.UUID;

public class HeartItem extends Item implements PolymerItem, BedrockItem {

    public HeartItem(Item.Settings settings) {
        super(settings);
    }


    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if(world.isClient || user.isSneaking()) {
            return super.use(world, user, hand);
        }

        final var stack = user.getStackInHand(hand);
        final int amount = ((ServerWorld) world).getGameRules().getInt(LifeStealGamerules.HEARTBONUS);

        final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) user;
        if(!PlayerUtils.changeHealth(serverPlayer, amount)) {
            return ActionResult.FAIL;
        }

        stack.decrement(1);
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        if (!(context.getWorld() instanceof ServerWorld world)) {
            return super.useOnBlock(context);
        }
        final MinecraftServer server = world.getServer();
        if (!server.getGameRules().getBoolean(LifeStealGamerules.ALTARS)) {
            return super.useOnBlock(context);
        }

        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        if (player == null) {
            return super.useOnBlock(context);
        }

        String playerName = getCustomName(context.getStack());
        if (playerName == null) {
            return super.useOnBlock(context);
        }

        BlockPos pos = context.getBlockPos();
        if (player.isSneaking() && isAltar(context.getWorld(), pos)) {
            // Can't revive yourself
            if (playerName.equalsIgnoreCase(player.getDisplayName().getString())) {
                player.sendMessage(LifeStealText.noSelfReviving(player.getName()), true);
                failedSound(world, pos);
                return ActionResult.FAIL;
            }

            byte val = revive(playerName, server, world, pos, player, Optional.of(context));

            switch (val) {
                case 0 -> {
                    return ActionResult.SUCCESS;
                }
                case 1 -> {
                    return ActionResult.FAIL;
                }
                default -> {
                    player.sendMessage(LifeStealText.notFound(playerName), true);
                    failedSound(world, pos);
                    return ActionResult.FAIL;
                }
            }
        }
        return super.useOnBlock(context);
    }

    public static byte revive(String playerName, MinecraftServer server, ServerWorld world, BlockPos pos, ServerPlayerEntity reviver, Optional<ItemUsageContext> contextOptional) {
        return revive(server.getPlayerManager().getPlayer(playerName), null, playerName, server, world, pos, reviver, contextOptional);
    }

    public static byte revive(UUID reviveeId, MinecraftServer server, ServerWorld world, BlockPos pos, ServerPlayerEntity reviver, Optional<ItemUsageContext> contextOptional) {
        return revive(server.getPlayerManager().getPlayer(reviveeId), reviveeId, null, server, world, pos, reviver, contextOptional);
    }

    private static byte revive(ServerPlayerEntity revivee, @Nullable UUID reviveeId, @Nullable String reviveeName, MinecraftServer server, ServerWorld world, BlockPos pos, ServerPlayerEntity reviver, Optional<ItemUsageContext> contextOptional) {
        boolean fromHeart = contextOptional.isPresent();
        if (revivee != null) {
            if (reviveOnline(revivee, world, pos, reviver, fromHeart)) {
                contextOptional.ifPresent(itemUsageContext -> revived(reviver, itemUsageContext, revivee.getDisplayName()));
                return 0;
            }
            failed(reviver, pos, revivee.getDisplayName());
            return 1;
        }

        Optional<GameProfile> profile;
        if (reviveeId != null) {
            profile = server.getUserCache().getByUuid(reviveeId);
        } else if (reviveeName != null) {
            profile = server.getUserCache().findByName(reviveeName);
        } else {
            profile = Optional.empty();
        }

        if (profile.isPresent()) {
            if (reviveOffline(profile.get(), world, pos, reviver, fromHeart)) {
                contextOptional.ifPresent(itemUsageContext -> revived(reviver, itemUsageContext, Text.of(profile.get().getName())));
                return 0;
            }
            failed(reviver, pos, Text.of(profile.get().getName()));
            return 1;
        }
        return 2;
    }

    private static boolean reviveOnline(ServerPlayerEntity player, ServerWorld world, BlockPos alter, PlayerEntity reviver, boolean fromHeart) {
        if (!DeathData.isPlayerDead(player.getUuid(), world.getGameRules().getInt(LifeStealGamerules.AUTOREVIVAL))) {
            return false;
        }
        teleport(player, world, alter);
        player.changeGameMode(GameMode.SURVIVAL);

        player.sendMessage(LifeStealText.onRevivalText(reviver.getDisplayName()));
        PlayerUtils.setMaxHealth(world.getGameRules().getInt(LifeStealGamerules.MINPLAYERHEALTH), player);
        DeathData.removeFromDeathDataList(player.getUuid());
        // These players are not newly revived if a heart was consumed to revive them
        ((PlayerReviveData)player).setNewlyRevived(!fromHeart);
        return true;
    }

    private static boolean reviveOffline(GameProfile profile, ServerWorld world, BlockPos alter, PlayerEntity reviver, boolean fromHeart) {
        if (!DeathData.isPlayerDead(profile.getId(), world.getGameRules().getInt(LifeStealGamerules.AUTOREVIVAL))) {
            return false;
        }

        MinecraftServer server = world.getServer();
        OfflinePlayerData playerData = OfflinePlayerData.getOfflinePlayerData(server, profile);
        playerData.setPosition(world, alter.up().toCenterPos());
        playerData.setGamemode(GameMode.SURVIVAL);
        playerData.setMaxHealth(world.getGameRules().getInt(LifeStealGamerules.MINPLAYERHEALTH));
        // These players are not newly revived if a heart was consumed to revive them
        playerData.setNewlyRevived(!fromHeart);
        playerData.save();

        DeathData.setReviver(profile.getId(), reviver.getUuid());
        return true;
    }

    private static void revived(ServerPlayerEntity reviver, ItemUsageContext context, Text revived) {
        successSound(context.getWorld(), context.getBlockPos());
        context.getStack().decrement(1);
        reviver.sendMessage(LifeStealText.revived(revived), true);
    }

    private static void successSound(World world, BlockPos alter) {
        world.playSound(null, alter, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 16.f, 1);
    }

    private static void failed(ServerPlayerEntity reviver, BlockPos alter, Text revived) {
        failedSound(reviver.getWorld(), alter);
        reviver.sendMessage(LifeStealText.playerIsAlive(revived), true);
    }

    private static void failedSound(World world, BlockPos alter) {
        world.playSound(null, alter, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 16.f, 1);
    }

    private static void teleport(PlayerEntity player, ServerWorld target, BlockPos alterPos) {
        Vec3d pos = alterPos.up().toCenterPos();
        player.teleportTo(new TeleportTarget(target, pos, Vec3d.ZERO, player.getYaw(), player.getPitch(), TeleportTarget.NO_OP));
    }

    @Nullable
    private static String getCustomName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new AssertionError("stack is empty");
        }

        if (hasCustomName(stack)) {
            return stack.getName().getString();
        }

        return null;
    }

    private static boolean hasCustomName(ItemStack stack) {
        return stack.get(DataComponentTypes.CUSTOM_NAME) != null;
    }

    public static boolean isAltar(World world, BlockPos pos) {
        if (!world.getBlockState(pos).isOf(LifeStealGamerules.getBlockFromGameRule(world.getServer().getGameRules()))) {
            return false;
        }

        BlockState north = world.getBlockState(pos.north());
        BlockState east = world.getBlockState(pos.east());
        BlockState south = world.getBlockState(pos.south());
        BlockState west = world.getBlockState(pos.west());

        return north.isIn(BlockTags.CANDLES) && north.get(CandleBlock.LIT)
                && east.isIn(BlockTags.CANDLES) && east.get(CandleBlock.LIT)
                && south.isIn(BlockTags.CANDLES) && south.get(CandleBlock.LIT)
                && west.isIn(BlockTags.CANDLES) && west.get(CandleBlock.LIT);
    }

    @Override
    public int getMaxCount() {
        return LifeStealGamerules.serverInstance != null ? LifeStealGamerules.serverInstance.getGameRules().getInt(LifeStealGamerules.HEART_STACK_SIZE) : 1;
    }


    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.NETHER_STAR;
    }

    @Override
    public String bedrockName() {
        return Text.translatable(this.getTranslationKey()).getLiteralString();
    }

    @Override
    public boolean bedrockEdible() {
        return true;
    }

    @Override
    public boolean bedrockFoil() {
        return true;
    }
}
