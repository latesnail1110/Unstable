package com.unstable.smp.ability.impl;

import com.unstable.smp.UnstableConfig;
import com.unstable.smp.UnstableConfig.AbilityConfig;
import com.unstable.smp.ability.Ability;
import com.unstable.smp.ability.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/** Storm Crystal: Blitz springt von Ziel zu Ziel. Nur Partikel/Sound, keine Blitz-Entitaeten (kein Feuer, kein Lag). */
public class ChainLightning implements Ability {
    private static final int MAX_JUMPS = 5;
    private static final double JUMP_RANGE = 6.0;

    public String id() { return "chain_lightning"; }

    public AbilityConfig defaults() { return new AbilityConfig(30f, 120, 6f, 16.0); }

    public boolean execute(ServerPlayer p, ServerLevel level, AbilityConfig cfg) {
        // Erstes Ziel: das Lebewesen, das am genauesten im Blickfeld liegt
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        LivingEntity first = null;
        double best = 0.9;
        for (LivingEntity e : Fx.targetsAround(level, p, p.position(), cfg.range)) {
            Vec3 to = e.getEyePosition().subtract(eye).normalize();
            double dot = look.dot(to);
            if (dot > best) { best = dot; first = e; }
        }
        if (first == null) return false;

        Set<LivingEntity> hit = new HashSet<>();
        LivingEntity cur = first;
        Vec3 from = eye.add(0, -0.3, 0);
        int jumps = Math.min(MAX_JUMPS, UnstableConfig.get().maxAffectedEntities);
        float dmg = cfg.damage;
        for (int i = 0; i < jumps && cur != null; i++) {
            hit.add(cur);
            Vec3 to = cur.position().add(0, cur.getBbHeight() / 2, 0);
            Fx.line(level, ParticleTypes.ELECTRIC_SPARK, from, to, 30);
            Fx.sound(level, to, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.6f, 1.6f);
            Fx.damage(level, p, cur, dmg);
            dmg *= 0.85f; // Schaden nimmt pro Sprung ab
            from = to;
            LivingEntity next = null;
            double nd = Double.MAX_VALUE;
            for (LivingEntity c : Fx.targetsAround(level, p, to, JUMP_RANGE)) {
                if (hit.contains(c)) continue;
                double d = c.distanceToSqr(to);
                if (d < nd) { nd = d; next = c; }
            }
            cur = next;
        }
        return true;
    }
}
