package com.unstable.smp.ability.impl;

import com.unstable.smp.UnstableConfig.AbilityConfig;
import com.unstable.smp.ability.Ability;
import com.unstable.smp.ability.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Chaos Orb: Energieexplosion am anvisierten Punkt. Zerstoert KEINE Bloecke. */
public class ChaosBurst implements Ability {
    private static final double RADIUS = 4.0;

    public String id() { return "chaos_burst"; }

    public AbilityConfig defaults() { return new AbilityConfig(40f, 160, 10f, 16.0); }

    public boolean execute(ServerPlayer p, ServerLevel level, AbilityConfig cfg) {
        Vec3 start = p.getEyePosition();
        Vec3 end = start.add(p.getLookAngle().scale(cfg.range));
        var hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        Vec3 center = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();

        Fx.line(level, ParticleTypes.DRAGON_BREATH, start.add(0, -0.3, 0), center, 40);
        Fx.particles(level, ParticleTypes.EXPLOSION_EMITTER, center, 1, 0, 0);
        Fx.particles(level, ParticleTypes.WITCH, center, 60, RADIUS / 2, 0.05);
        Fx.sound(level, center, SoundEvents.GENERIC_EXPLODE.value(), 1f, 1.3f);

        for (LivingEntity t : Fx.targetsAround(level, p, center, RADIUS)) {
            // Schaden faellt mit der Entfernung zum Zentrum ab
            double f = 1.0 - Math.sqrt(t.distanceToSqr(center)) / (RADIUS + 0.01);
            Fx.damage(level, p, t, (float) (cfg.damage * Math.max(0.3, f)));
            Fx.knockbackFrom(t, center, 0.9);
        }
        return true;
    }
}
