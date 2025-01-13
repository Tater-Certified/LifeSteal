package com.github.certifiedtater.lifesteal.effect;

import com.github.certifiedtater.lifesteal.utils.LifeStealText;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class InvulnerableStatusEffect extends StatusEffect implements PolymerStatusEffect {
    public InvulnerableStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 16262179, ParticleTypes.HEART);
    }

    public InvulnerableStatusEffect(StatusEffectCategory category, int color, ParticleEffect particleEffect) {
        super(category, color, particleEffect);
    }

    @Override
    public @Nullable ItemStack getPolymerIcon(ServerPlayerEntity player) {
        return Items.SHIELD.getDefaultStack();
    }

    @Override
    public @Nullable StatusEffect getPolymerReplacement(PacketContext context) {
        return StatusEffects.RESISTANCE.value();
    }

    @Override
    public boolean isBeneficial() {
        return true;
    }

    @Override
    public Text getName() {
        return LifeStealText.INVULNERABILITY;
    }

    @Override
    public StatusEffectCategory getCategory() {
        return StatusEffectCategory.BENEFICIAL;
    }

    @Override
    public int getColor() {
        return 16262179;
    }
}
