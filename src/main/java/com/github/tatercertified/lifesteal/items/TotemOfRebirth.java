package com.github.tatercertified.lifesteal.items;

import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import com.github.tatercertified.lifesteal.gamerules.ReviveMethod;
import com.github.tatercertified.lifesteal.utils.RevivalGUI;
import de.olivermakesco.polyspring.api.BedrockItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class TotemOfRebirth extends Item implements PolymerItem, BedrockItem {

    public TotemOfRebirth(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity serverPlayer && ((ServerWorld) world).getGameRules().getValue(LifeStealGamerules.REVIVE_METHOD) == ReviveMethod.TOTEM) {
            RevivalGUI.openGUI(serverPlayer, hand);
        }
        return super.use(world, user, hand);
    }

    @Override
    public int getMaxCount() {
        return 1;
    }

    @Override
    public boolean bedrockEdible() {
        return true;
    }

    @Override
    public boolean bedrockFoil() {
        return true;
    }

    @Override
    public String bedrockName() {
        return Text.translatable(this.getTranslationKey()).getLiteralString();
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.TOTEM_OF_UNDYING;
    }
}
