package com.github.tatercertified.lifesteal.item;

import com.github.tatercertified.lifesteal.Loader;
import com.github.tatercertified.lifesteal.util.ModelledPolymerItem;
import com.github.tatercertified.lifesteal.util.OfflineUtils;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.github.tatercertified.lifesteal.Loader.revival_block;

public class HeartItem extends ModelledPolymerItem {

    public HeartItem(Settings settings, PolymerModelData customModelData) {
        super(settings, customModelData);
    }

    Block block_from_config = Registries.BLOCK.get(Identifier.tryParse(revival_block));


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!user.isSneaking()) {
            user.getStackInHand(hand).decrement(1);
            updateValueOf(user, world.getGameRules().getInt(Loader.HEARTBONUS), true);
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (block_from_config == null) {
            return super.useOnBlock(context);
        }
        PlayerEntity player =  context.getPlayer();
        BlockPos pos = new BlockPos.Mutable(context.getBlockPos().getX(), context.getBlockPos().getY(), context.getBlockPos().getZ());
        String playername = context.getStack().getName().getString();
        if (player.isSneaking() && context.getWorld().getBlockState(pos).getBlock() == block_from_config) {
            if (!Objects.equals(playername, "Heart") && isAltar(context.getWorld(), pos)) {

                MinecraftServer server = player.getServer();

                if (playername.equalsIgnoreCase(player.getDisplayName().getString())) {

                    if(!player.getWorld().getGameRules().getBoolean(Loader.GIFTHEARTS)) {
                        player.sendMessage(Text.of("Heart Gifting is Disabled"), true);
                        return super.useOnBlock(context);
                    }

                    if (player.getHealth() > 2) {
                        updateValueOf(player, -2, true);
                        context.getStack().increment(1);
                        player.sendMessage(Text.of("Traded 1 Heart"));
                    } else {
                        player.sendMessage(Text.of("Your Health is Too Low"), true);
                    }

                } else {
                    Optional<GameProfile> profile = server.getUserCache().findByName(playername);
                    ServerPlayerEntity revived;

                    if (profile.isPresent()) {
                        UUID id = profile.get().getId();
                        revived = OfflineUtils.getPlayer(server, id).get();
                        NbtCompound player_data = OfflineUtils.getPlayerData(server, revived);
                        boolean is_online = OfflineUtils.isPlayerOnline(revived, server);

                        if (OfflineUtils.getGameMode(player_data) == GameMode.SPECTATOR) {
                            if (is_online) {
                                revive(revived, context);
                            } else {
                                reviveOffline(revived, context, player_data, server);
                            }
                        } else {
                            player.sendMessage(Text.of(playername + " is still alive"), true);
                        }

                    } else {
                        player.sendMessage(Text.of(playername + " does not exist"), true);
                    }
                }
            }
        }
        return super.useOnBlock(context);
    }

    private static void revive(ServerPlayerEntity player, ItemUsageContext context) {
        player.teleport(context.getBlockPos().getX() + 0.5, context.getBlockPos().getY() + 1, context.getBlockPos().getZ() + 0.5);
        player.changeGameMode(GameMode.SURVIVAL);
        player.sendMessage(Text.of(context.getPlayer().getDisplayName().getString() + " has revived you!"));
        player.getWorld().playSound(context.getBlockPos().getX() + 0.5, context.getBlockPos().getY() + 1, context.getBlockPos().getZ() + 0.5, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1, 1, true);
        updateValueOf(player, 1, true);
        if (context.getWorld().getGameRules().getBoolean(Loader.BANWHENMINHEALTH)) {
            player.getServer().getPlayerManager().getUserBanList().remove(player.getGameProfile());
        }
        context.getStack().decrement(1);
        context.getPlayer().sendMessage(Text.of("You revived " + player.getDisplayName().getString()), true);
    }

    private static void reviveOffline(ServerPlayerEntity player, ItemUsageContext context, NbtCompound data, MinecraftServer server) {
        OfflineUtils.teleportOffline(player, new Vec3d(context.getBlockPos().getX() + 0.5, context.getBlockPos().getY() + 1, context.getBlockPos().getZ() + 0.5));
        OfflineUtils.setDimension(data, (ServerWorld) context.getPlayer().getWorld());
        OfflineUtils.setGameMode(data, GameMode.SURVIVAL);
        updateValueOf(player, 1, false);
        data.putString("reviver", context.getPlayer().getName().getString());
        OfflineUtils.savePlayerData(player, data, server);
        if (context.getWorld().getGameRules().getBoolean(Loader.BANWHENMINHEALTH)) {
            server.getPlayerManager().getUserBanList().remove(player.getGameProfile());
        }
        context.getStack().decrement(1);
        context.getPlayer().sendMessage(Text.of("You revived " + player.getDisplayName().getString()), true);
    }

    public static void updateValueOf(PlayerEntity of, float by, boolean online) {
        EntityAttributeInstance health = of.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        assert health != null;
        float oldHealth = (float) health.getValue();
        float newHealth = oldHealth + by;
        int maxHealth = of.getWorld().getGameRules().getInt(Loader.MAXPLAYERHEALTH);

        if(maxHealth > 0 && newHealth > maxHealth) {
            newHealth = (float) maxHealth;
        }
        health.setBaseValue(newHealth);
        if (online) {
            of.sendMessage(Text.of("Your max health is now " + newHealth), true);
            if (oldHealth == (float) maxHealth) {
                of.giveItemStack(new ItemStack(ModItems.HEART, 1));
                of.getInventory().updateItems();
                of.sendMessage(Text.of("You are already at the maximum amount of health!"), true);
            } else {
                of.setHealth(of.getHealth() + by);
                if (of.getHealth() > maxHealth) of.setHealth(maxHealth);
            }
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
