package com.github.tatercertified.lifesteal.item;

import com.github.tatercertified.lifesteal.util.Config;
import com.github.tatercertified.lifesteal.util.ModelledPolymerItem;
import com.github.tatercertified.lifesteal.util.OfflineUtils;
import com.github.tatercertified.lifesteal.util.PlayerUtils;
import com.github.tatercertified.lifesteal.world.gamerules.LSGameRules;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class HeartItem extends ModelledPolymerItem {

    public HeartItem(Settings settings, PolymerModelData customModelData) {
        super(settings, customModelData);
    }

    Block block_from_config = Registries.BLOCK.get(Identifier.tryParse(Config.revivalBlock));


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!user.isSneaking()) {
            user.getStackInHand(hand).decrement(1);
            updateValueOf((ServerPlayerEntity) user, world.getGameRules().getInt(LSGameRules.HEARTBONUS));
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (block_from_config == null || !context.getPlayer().getServer().getGameRules().getBoolean(LSGameRules.ALTARS)) {
            return super.useOnBlock(context);
        }
        PlayerEntity player =  context.getPlayer();
        BlockPos pos = new BlockPos.Mutable(context.getBlockPos().getX(), context.getBlockPos().getY(), context.getBlockPos().getZ());
        String playername = context.getStack().getName().getString();
        if (player.isSneaking() && context.getWorld().getBlockState(pos).getBlock() == block_from_config) {
            if (!Objects.equals(playername, "Heart") && isAltar(context.getWorld(), pos)) {

                MinecraftServer server = player.getServer();

                if (playername.equalsIgnoreCase(player.getDisplayName().getString())) {

                    if (!server.getGameRules().getBoolean(LSGameRules.GIFTHEARTS)) {
                        player.sendMessage(Text.of(Config.HEART_GIFTING_DISABLED), true);
                        return super.useOnBlock(context);
                    }

                    PlayerUtils.convertHealthToHeartItems((ServerPlayerEntity) player, 1, server);

                } else {
                    Optional<GameProfile> profile = server.getUserCache().findByName(playername);

                    if (profile.isPresent()) {
                        UUID id = profile.get().getId();
                        revive(id, context, playername);
                    } else {
                        context.getPlayer().sendMessage(Text.of(playername + Config.PLAYER_DOES_NOT_EXIST), true);
                    }
                }
            }
        }
        return super.useOnBlock(context);
    }

    private void revive(UUID uuid, ItemUsageContext context, String playerName) {
        MinecraftServer server = context.getWorld().getServer();
        if (PlayerUtils.isPlayerDead(uuid, server)) {
            if (OfflineUtils.isPlayerOnline(uuid, server)) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                player.teleport(context.getBlockPos().getX() + 0.5, context.getBlockPos().getY() + 1, context.getBlockPos().getZ() + 0.5);
                player.changeGameMode(GameMode.SURVIVAL);
                player.sendMessage(Text.of(context.getPlayer().getDisplayName().getString() + Config.REVIVER));
                updateValueOf(player, player.getServer().getGameRules().getInt(LSGameRules.HEARTBONUS));
            } else {
                NbtCompound compound = OfflineUtils.getPlayerData(uuid, server, playerName);
                OfflineUtils.setReviver(compound, context.getPlayer().getName().getString());
                OfflineUtils.setLocation(compound, context.getBlockPos());
                OfflineUtils.setDimension(compound, context.getWorld().getRegistryKey().getValue());
                OfflineUtils.savePlayerData(uuid, server, playerName, compound);
                if (server.getGameRules().getBoolean(LSGameRules.BANWHENMINHEALTH)) {
                    PlayerUtils.removePlayerFromDeadList(uuid, server);
                }
            }
            context.getWorld().playSound(context.getBlockPos().getX() + 0.5, context.getBlockPos().getY() + 1, context.getBlockPos().getZ() + 0.5, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1, 1, true);
            context.getStack().decrement(1);
            context.getPlayer().sendMessage(Text.of(Config.YOU_REVIVED + playerName), true);
        } else {
            context.getPlayer().sendMessage(Text.of(playerName + Config.PLAYER_IS_STILL_ALIVE), true);
        }
    }

    public static void updateValueOf(ServerPlayerEntity of, float by) {
        if (!PlayerUtils.setBaseHealth(of, by, of.getServer())) {
            of.giveItemStack(new ItemStack(ModItems.HEART, 1));
            of.getInventory().updateItems();
        }
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
