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

/** Void Blade: weiter Schnitt in einem Kegel vor dem Spieler, zieht Ziele leicht heran. */
public class VoidSlash implements Ability {
    public String id() { return "void_slash"; }

    public AbilityConfig defaults() { return new AbilityConfig(20f, 80, 8f, 5.0); }

    public boolean execute(ServerPlayer p, ServerLevel level, AbilityConfig cfg) {
        Vec3 look = p.getLookAngle();
        Vec3 origin = p.getEyePosition();
        int count = 0;
        for (LivingEntity t : Fx.targetsAround(level, p, p.position(), cfg.range)) {
            Vec3 to = t.getEyePosition().subtract(origin).normalize();
            if (look.dot(to) < 0.5) continue; // nur im Kegel (ca. 60 Grad)
            Fx.damage(level, p, t, cfg.damage);
            Vec3 pull = p.position().subtract(t.position()).normalize().scale(0.35);
            t.push(pull.x, 0.1, pull.z);
            count++;
        }
        for (int i = -4; i <= 4; i++) {
            double a = i * 0.2;
            Vec3 dir = new Vec3(look.x * Math.cos(a) - look.z * Math.sin(a), look.y * 0.5, look.x * Math.sin(a) + look.z * Math.cos(a)).normalize();
            Fx.particles(level, ParticleTypes.SOUL_FIRE_FLAME, origin.add(dir.scale(2.5)), 4, 0.1, 0.01);
        }
        Fx.sound(level, p.position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1f, 0.6f);
        return count > 0;
    }
}
