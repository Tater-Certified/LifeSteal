package com.github.tatercertified.lifesteal.items;

import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.gamerules.ReviveMethod;
import com.github.tatercertified.lifesteal.utils.LifeStealText;
import com.github.tatercertified.lifesteal.utils.PlayerUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class HeartItem extends Item implements PolymerItem {

    public HeartItem(Item.Properties settings) {
        super(settings);
    }


    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if(world.isClientSide() || user.isShiftKeyDown()) {
            return super.use(world, user, hand);
        }

        final var stack = user.getItemInHand(hand);

        if (PlayerUtils.incrementHearts((ServerPlayer) user)) {
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        if (!(context.getLevel() instanceof ServerLevel world)) {
            return super.useOn(context);
        }
        final MinecraftServer server = world.getServer();

        if (world.getGameRules().get(LifeStealGamerules.REVIVE_METHOD) != ReviveMethod.ALTAR) {
            return super.useOn(context);
        }

        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player == null) {
            return super.useOn(context);
        }

        String playerName = getCustomName(context.getItemInHand());
        if (playerName == null) {
            return super.useOn(context);
        }

        BlockPos pos = context.getClickedPos();
        if (player.isShiftKeyDown() && isAltar(world, pos)) {
            // Can't revive yourself
            if (playerName.equalsIgnoreCase(player.getDisplayName().getString())) {
                player.displayClientMessage(LifeStealText.noSelfReviving(player.getName()), true);
                PlayerUtils.failedSound(world, pos);
                return InteractionResult.FAIL;
            }

            byte val = PlayerUtils.revive(playerName, server, world, pos, player, context);

            switch (val) {
                case 0 -> {
                    return InteractionResult.SUCCESS;
                }
                case 1 -> {
                    return InteractionResult.FAIL;
                }
                default -> {
                    player.displayClientMessage(LifeStealText.notFound(playerName), true);
                    PlayerUtils.failedSound(world, pos);
                    return InteractionResult.FAIL;
                }
            }
        }
        return super.useOn(context);
    }

    @Nullable
    private static String getCustomName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new AssertionError("stack is empty");
        }

        if (hasCustomName(stack)) {
            return stack.getHoverName().getString();
        }

        return null;
    }

    private static boolean hasCustomName(ItemStack stack) {
        return stack.get(DataComponents.CUSTOM_NAME) != null;
    }

    public static boolean isAltar(ServerLevel world, BlockPos pos) {
        if (!world.getBlockState(pos).is(LifeStealGamerules.getAltarBlock(world.getGameRules()))) {
            return false;
        }

        BlockState north = world.getBlockState(pos.north());
        BlockState east = world.getBlockState(pos.east());
        BlockState south = world.getBlockState(pos.south());
        BlockState west = world.getBlockState(pos.west());

        return north.is(BlockTags.CANDLES) && north.getValue(CandleBlock.LIT)
                && east.is(BlockTags.CANDLES) && east.getValue(CandleBlock.LIT)
                && south.is(BlockTags.CANDLES) && south.getValue(CandleBlock.LIT)
                && west.is(BlockTags.CANDLES) && west.getValue(CandleBlock.LIT);
    }

    @Override
    public int getDefaultMaxStackSize() {
        return LifeStealGamerules.serverInstance != null ? LifeStealGamerules.serverInstance.overworld().getGameRules().get(LifeStealGamerules.HEART_STACK_SIZE) : 1;
    }


    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.NETHER_STAR;
    }
}
