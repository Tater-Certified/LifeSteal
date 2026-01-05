package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.Lifesteal;
import com.github.tatercertified.lifesteal.effect.AltarRitualAnimation;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.items.HeartItem;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public abstract class FlintAndSteelMixin {

    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/core/BlockPos;)V", ordinal = 0))
    private void lifesteal$checkForAnimation(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir, @Local Level level, @Local(ordinal = 0) BlockPos pos, @Local(ordinal = 0) BlockState blockState) {
        if (!level.isClientSide() && blockState.is(BlockTags.CANDLES)) {
            startAltarAnimation(pos, (ServerLevel) level);
        }
    }

    private static void startAltarAnimation(BlockPos pos, ServerLevel level) {
        GameRules gameRules = level.getGameRules();
        Block altarBlock = LifeStealGamerules.getAltarBlock(gameRules);
        BlockPos altarPos = isNextTo(pos, level, altarBlock);
        if (altarPos != null && HeartItem.isAltar(level, altarPos)) {
            Lifesteal.ANIMATIONS.add(AltarRitualAnimation.create(altarPos, level));
        }

    }

    private static @Nullable BlockPos isNextTo(BlockPos pos, ServerLevel level, Block block) {
        BlockPos north = pos.north();
        if (level.getBlockState(north) == block.defaultBlockState()) {
            return north;
        }

        BlockPos east = pos.east();
        if (level.getBlockState(east) == block.defaultBlockState()) {
            return east;
        }

        BlockPos south = pos.south();
        if (level.getBlockState(south) == block.defaultBlockState()) {
            return south;
        }

        BlockPos west = pos.west();
        if (level.getBlockState(west) == block.defaultBlockState()) {
            return west;
        }

        return null;
    }
}
