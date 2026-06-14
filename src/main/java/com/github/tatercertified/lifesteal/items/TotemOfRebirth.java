package com.github.tatercertified.lifesteal.items;

import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.gamerules.ReviveMethod;
import com.github.tatercertified.lifesteal.utils.RevivalGUI;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class TotemOfRebirth extends SmartTexturedPolymerItem {

    public TotemOfRebirth(Properties settings) {
        super(settings, Items.TOTEM_OF_UNDYING, true);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (user instanceof ServerPlayer serverPlayer && ((ServerLevel) world).getGameRules().get(LifeStealGamerules.REVIVE_METHOD) == ReviveMethod.TOTEM) {
            RevivalGUI.openGUI(serverPlayer, hand);
        }
        return super.use(world, user, hand);
    }

    @Override
    public int getDefaultMaxStackSize() {
        return 1;
    }
}
