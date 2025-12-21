package com.github.tatercertified.lifesteal.items;

import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.gamerules.ReviveMethod;
import com.github.tatercertified.lifesteal.utils.LifeStealText;
import com.github.tatercertified.lifesteal.utils.PlayerUtils;
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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;

public class HeartItem extends Item implements PolymerItem, BedrockItem {

    public HeartItem(Item.Settings settings) {
        super(settings);
    }


    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if(world.isClient() || user.isSneaking()) {
            return super.use(world, user, hand);
        }

        final var stack = user.getStackInHand(hand);
        final int amount = ((ServerWorld) world).getGameRules().getValue(LifeStealGamerules.HEARTBONUS);

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

        if (world.getGameRules().getValue(LifeStealGamerules.REVIVE_METHOD) != ReviveMethod.ALTAR) {
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
        if (player.isSneaking() && isAltar(world, pos)) {
            // Can't revive yourself
            if (playerName.equalsIgnoreCase(player.getDisplayName().getString())) {
                player.sendMessage(LifeStealText.noSelfReviving(player.getName()), true);
                DeathData.failedSound(world, pos);
                return ActionResult.FAIL;
            }

            byte val = DeathData.revive(playerName, server, world, pos, player, Optional.of(context));

            switch (val) {
                case 0 -> {
                    return ActionResult.SUCCESS;
                }
                case 1 -> {
                    return ActionResult.FAIL;
                }
                default -> {
                    player.sendMessage(LifeStealText.notFound(playerName), true);
                    DeathData.failedSound(world, pos);
                    return ActionResult.FAIL;
                }
            }
        }
        return super.useOnBlock(context);
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

    public static boolean isAltar(ServerWorld world, BlockPos pos) {
        if (!world.getBlockState(pos).isOf(LifeStealGamerules.getAltarBlock(world.getGameRules()))) {
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
        return LifeStealGamerules.serverInstance != null ? LifeStealGamerules.serverInstance.getOverworld().getGameRules().getValue(LifeStealGamerules.HEART_STACK_SIZE) : 1;
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
