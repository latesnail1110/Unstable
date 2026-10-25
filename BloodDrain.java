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

/** Bloodfang Sword: entzieht dem anvisierten Ziel Leben und heilt den Anwender um die Haelfte des Schadens. */
public class BloodDrain implements Ability {
    public String id() { return "blood_drain"; }

    public AbilityConfig defaults() { return new AbilityConfig(25f, 100, 7f, 6.0); }

    public boolean execute(ServerPlayer p, ServerLevel level, AbilityConfig cfg) {
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        LivingEntity target = null;
        double best = 0.93;
        for (LivingEntity e : Fx.targetsAround(level, p, p.position(), cfg.range)) {
            double dot = look.dot(e.getEyePosition().subtract(eye).normalize());
            if (dot > best) { best = dot; target = e; }
        }
        if (target == null) return false;
        float before = target.getHealth();
        Fx.damage(level, p, target, cfg.damage);
        float dealt = Math.max(0f, before - target.getHealth());
        p.heal(dealt * 0.5f);
        Vec3 to = target.position().add(0, target.getBbHeight() / 2, 0);
        Fx.line(level, ParticleTypes.CRIMSON_SPORE, to, eye.add(0, -0.3, 0), 30);
        Fx.particles(level, ParticleTypes.DAMAGE_INDICATOR, to, 6, 0.3, 0.1);
        Fx.sound(level, to, SoundEvents.WITHER_HURT, 0.5f, 1.8f);
        return true;
    }
}
