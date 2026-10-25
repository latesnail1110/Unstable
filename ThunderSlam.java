package com.unstable.smp.ability.impl;

import com.unstable.smp.UnstableConfig.AbilityConfig;
import com.unstable.smp.ability.Ability;
import com.unstable.smp.ability.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Stormbreaker: Donnerschlag um den Spieler herum, schleudert Gegner nach aussen. */
public class ThunderSlam implements Ability {
    public String id() { return "thunder_slam"; }

    public AbilityConfig defaults() { return new AbilityConfig(35f, 140, 9f, 5.0); }

    public boolean execute(ServerPlayer p, ServerLevel level, AbilityConfig cfg) {
        Vec3 c = p.position();
        for (LivingEntity t : Fx.targetsAround(level, p, c, cfg.range)) {
            Fx.damage(level, p, t, cfg.damage);
            Fx.knockbackFrom(t, c, 1.2);
        }
        for (int r = 1; r <= (int) cfg.range; r++) {
            for (int i = 0; i < 12; i++) {
                double a = i * Math.PI / 6;
                Fx.particles(level, ParticleTypes.ELECTRIC_SPARK, c.add(Math.cos(a) * r, 0.2, Math.sin(a) * r), 1, 0.05, 0.0);
            }
        }
        Fx.sound(level, c, SoundEvents.LIGHTNING_BOLT_THUNDER, 0.8f, 1.2f);
        return true;
    }
}
