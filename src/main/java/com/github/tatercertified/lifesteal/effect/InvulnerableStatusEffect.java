package com.github.tatercertified.lifesteal.effect;
import com.github.tatercertified.lifesteal.Lifesteal;
import com.github.tatercertified.lifesteal.utils.EffectEndEvent;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

public class InvulnerableStatusEffect extends MobEffect implements PolymerStatusEffect, EffectEndEvent {
    public static final Holder<MobEffect> INVULNERABLE = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(Lifesteal.MOD_ID, "invulnerability"), new InvulnerableStatusEffect());

    public InvulnerableStatusEffect() {
        super(MobEffectCategory.BENEFICIAL, 16262179, ParticleTypes.MYCELIUM);
    }

    @Override
    public @Nullable ItemStack getPolymerIcon(MobEffect effect, ServerPlayer player) {
        return Items.SHIELD.getDefaultInstance();
    }

    @Override
    public @Nullable MobEffect getPolymerReplacement(MobEffect effect, PacketContext context) {
        return MobEffects.UNLUCK.value();
    }

    @Override
    public boolean isBeneficial() {
        return true;
    }
    @Override
    public MobEffectCategory getCategory() {
        return MobEffectCategory.BENEFICIAL;
    }

    @Override
    public void onEffectStarted(@NonNull LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);
    }

    public static void register() {
    }

    @Override
    public void onEffectFinished(ServerPlayer effectedPlayer) {
        Scoreboard scoreboard = effectedPlayer.level().getServer().getScoreboard();
        if (Lifesteal.invulnerableTeam.getPlayers().contains(effectedPlayer.getScoreboardName())) {
            scoreboard.removePlayerFromTeam(effectedPlayer.getScoreboardName(), Lifesteal.invulnerableTeam);
        }
    }
}