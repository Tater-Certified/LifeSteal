package com.github.tatercertified.lifesteal.effect;
import com.github.tatercertified.lifesteal.Lifesteal;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.UUID;

public class InvulnerableStatusEffect extends StatusEffect implements PolymerStatusEffect {
    private UUID player;
    private MinecraftServer server;
    public static final RegistryEntry<StatusEffect> INVULNERABLE = Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(Lifesteal.MOD_ID, "tater"), new InvulnerableStatusEffect());

    public InvulnerableStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 16262179, ParticleTypes.MYCELIUM);
    }

    @Override
    public @Nullable ItemStack getPolymerIcon(StatusEffect effect, ServerPlayerEntity player) {
        return Items.SHIELD.getDefaultStack();
    }

    @Override
    public @Nullable StatusEffect getPolymerReplacement(StatusEffect effect, PacketContext context) {
        return StatusEffects.UNLUCK.value();
    }

    @Override
    public boolean isBeneficial() {
        return true;
    }
    @Override
    public StatusEffectCategory getCategory() {
        return StatusEffectCategory.BENEFICIAL;
    }

    @Override
    public void onRemoved(AttributeContainer attributeContainer) {
        super.onRemoved(attributeContainer);
        server.getScoreboard().removeScoreHolderFromTeam(server.getPlayerManager().getPlayer(player).getNameForScoreboard(), Lifesteal.invulnerableTeam);
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);
        this.player = entity.getUuid();
        this.server = entity.getEntityWorld().getServer();
    }

    public static void register() {
    }
}