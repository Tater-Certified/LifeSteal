package com.github.tatercertified.lifesteal.item;

import com.github.tatercertified.lifesteal.util.*;
import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class HeartItem extends ModelledPolymerItem {
    private static final double CENTER_OFFSET = .5d;
    private static final Set<PositionFlag>
            revivalTeleportFlags = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z);

    public HeartItem(Settings settings, PolymerModelData customModelData) {
        super(settings, customModelData);
    }

    Block block_from_config = Registries.BLOCK.get(Identifier.tryParse(Config.revivalBlock));


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && !user.isSneaking()) {
            final var stack = user.getStackInHand(hand);
            stack.decrement(1);
            updateValueOf((ServerPlayerEntity) user, world.getGameRules().getInt(LSGameRules.HEARTBONUS));

            return TypedActionResult.success(stack);
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getWorld() instanceof ServerWorld world)) {
            return super.useOnBlock(context);
        }
        final MinecraftServer server = world.getServer();
        if (block_from_config == null || !server.getGameRules().getBoolean(LSGameRules.ALTARS)) {
            return super.useOnBlock(context);
        }
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        BlockPos pos = context.getBlockPos();
        String playerName = getCustomName(context.getStack());

        if (player == null || playerName == null) {
            return super.useOnBlock(context);
        }

        if (player.isSneaking() && world.getBlockState(pos).isOf(block_from_config) && isAltar(context.getWorld(), pos)) {
            if (playerName.equalsIgnoreCase(player.getDisplayName().getString())) {

                if (!world.getGameRules().getBoolean(LSGameRules.GIFTHEARTS)) {
                    player.sendMessage(Text.of(Config.HEART_GIFTING_DISABLED), true);
                    failedSound(world, pos);
                    return ActionResult.FAIL;
                }

                PlayerUtils.convertHealthToHeartItems(player, 1, server);
                successSound(world, pos);
                return ActionResult.SUCCESS;
            }

            ServerPlayerEntity revivee = server.getPlayerManager().getPlayer(playerName);
            if (revivee != null) {
                if (reviveOnline(revivee, world, pos, player)) {
                    revived(player, context, revivee.getDisplayName());
                    return ActionResult.SUCCESS;
                }
                failed(player, pos, revivee.getDisplayName());
                return ActionResult.FAIL;
            }

            Optional<GameProfile> profile = server.getUserCache().findByName(playerName);
            if (profile.isPresent()) {
                if (reviveOffline(profile.get(), world, pos, player)) {
                    revived(player, context, Text.of(profile.get().getName()));
                    return ActionResult.SUCCESS;
                }
                failed(player, pos, Text.of(profile.get().getName()));
                return ActionResult.FAIL;
            }

            player.sendMessage(Text.literal(playerName).append(Config.PLAYER_DOES_NOT_EXIST), true);
            failedSound(world, pos);
            return ActionResult.FAIL;
        }
        return super.useOnBlock(context);
    }

    private static boolean reviveOnline(ServerPlayerEntity player, ServerWorld world, BlockPos alter, PlayerEntity reviver) {
        if (!PlayerUtils.isPlayerDead(player.getUuid(), world.getServer())) {
            return false;
        }
        teleport(player, world, alter);
        player.changeGameMode(GameMode.SURVIVAL);

        player.sendMessage(reviver.getDisplayName().copyContentOnly().append(Config.REVIVER));
        updateValueOf(player, world.getGameRules().getInt(LSGameRules.HEARTBONUS));
        return true;
    }

    private static boolean reviveOffline(GameProfile profile, ServerWorld world, BlockPos alter, PlayerEntity reviver) {
        MinecraftServer server = world.getServer();

        final OfflinePlayerData data = OfflineUtils.getOfflinePlayerData(server, profile);
        if (data == null) {
            return false;
        }

        UUID uuid = profile.getId();

        if (!PlayerUtils.isPlayerDead(uuid, world.getServer())) {
            if (!OfflineUtils.isDead(data.root, server)) {
                return false;
            }
            if (!PlayerUtils.unbanLegacyPlayer(server, profile, reviver)) {
                return false;
            }
        }

        data.setLifeStealData(new LifeStealPlayerData(
                reviver.getGameProfile().getName(),
                alter.up(),
                world.getRegistryKey().getValue()
        ));
        data.save();
        PlayerUtils.removePlayerFromDeadList(uuid, server);

        return true;
    }

    private static void revived(ServerPlayerEntity reviver, ItemUsageContext context, Text revived) {
        successSound(context.getWorld(), context.getBlockPos());
        context.getStack().decrement(1);
        reviver.sendMessage(Text.literal(Config.YOU_REVIVED).append(revived), true);
    }

    private static void successSound(World world, BlockPos alter) {
        world.playSound(null, alter, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 16.f, 1);
    }

    private static void failed(ServerPlayerEntity reviver, BlockPos alter, Text revived) {
        failedSound(reviver.getWorld(), alter);
        reviver.sendMessage(revived.copyContentOnly().append(Config.PLAYER_IS_STILL_ALIVE), true);
    }

    private static void failedSound(World world, BlockPos alter) {
        world.playSound(null, alter, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 16.f, 1);
    }

    public static void updateValueOf(ServerPlayerEntity of, float by) {
        if (!PlayerUtils.setBaseHealth(of, by, of.getServer())) {
            of.giveItemStack(new ItemStack(ModItems.HEART, 1));
            of.getInventory().updateItems();
        }
    }

    private static void teleport(PlayerEntity player, ServerWorld target, BlockPos alterPos) {
        double x = alterPos.getX() + CENTER_OFFSET;
        double y = alterPos.getY() + 1.d;
        double z = alterPos.getZ() + CENTER_OFFSET;

        player.teleport(target, x, y, z, revivalTeleportFlags, player.getYaw(), player.getPitch());
    }

    @Nullable
    private static String getCustomName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new AssertionError("stack is empty");
        }

        if (stack.hasCustomName()) {
            return stack.getName().getString();
        }

        return null;
    }

    public static boolean isAltar(World world, BlockPos pos) {
        BlockState north = world.getBlockState(pos.north());
        BlockState east = world.getBlockState(pos.east());
        BlockState south = world.getBlockState(pos.south());
        BlockState west = world.getBlockState(pos.west());

        return north.isIn(BlockTags.CANDLES) && north.get(CandleBlock.LIT)
                && east.isIn(BlockTags.CANDLES) && east.get(CandleBlock.LIT)
                && south.isIn(BlockTags.CANDLES) && south.get(CandleBlock.LIT)
                && west.isIn(BlockTags.CANDLES) && west.get(CandleBlock.LIT);
    }
}
