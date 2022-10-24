package com.github.tatercertified.lifesteal.item;

import com.github.tatercertified.lifesteal.util.ModelledPolymerItem;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import com.github.tatercertified.lifesteal.Loader;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class HeartItem extends ModelledPolymerItem {

    public HeartItem(Settings settings, PolymerModelData customModelData) {
        super(settings, customModelData);
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.getStackInHand(hand).decrement(1);
        updateValueOf(user, world.getGameRules().getInt(Loader.HEARTBONUS));
        return super.use(world, user, hand);
    }

    private static void updateValueOf(PlayerEntity of, float by) {
        EntityAttributeInstance health = of.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        assert health != null;
        float oldHealth = (float) health.getValue();
        float newHealth = oldHealth + by;
        int maxHealth = of.getWorld().getGameRules().getInt(Loader.MAXPLAYERHEALTH);

        if(maxHealth > 0 && newHealth > maxHealth) {
            newHealth = (float) maxHealth;
        }
        health.setBaseValue(newHealth);
        of.sendMessage(Text.of("Your max health is now " + newHealth), true);
        if(oldHealth == (float) maxHealth) {
            of.giveItemStack(new ItemStack(ModItems.HEART, 1));
            of.getInventory().updateItems();
            of.sendMessage(Text.of("You are already at the maximum amount of health!"), true);
        } else {
            of.setHealth(of.getHealth() + by);
            if(of.getHealth() > maxHealth) of.setHealth(maxHealth);
        }
    }
}
