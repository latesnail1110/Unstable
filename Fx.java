package com.unstable.smp.ability;

import com.unstable.smp.UnstableConfig;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/** Gemeinsame Hilfsfunktionen: begrenzte Partikel, Zielsuche, sicherer Schaden. */
public final class Fx {
    private Fx() {}

    public static void particles(ServerLevel level, ParticleOptions type, Vec3 pos, int count, double spread, double speed) {
        int capped = Math.min(count, UnstableConfig.get().maxParticlesPerCast);
        if (capped > 0) level.sendParticles(type, pos.x, pos.y, pos.z, capped, spread, spread, spread, speed);
    }

    public static void line(ServerLevel level, ParticleOptions type, Vec3 from, Vec3 to, int maxPoints) {
        Vec3 d = to.subtract(from);
        double len = d.length();
        int n = (int) Math.min(maxPoints, Math.max(2, len * 2));
        for (int i = 0; i <= n; i++) {
            Vec3 p = from.add(d.scale(i / (double) n));
            level.sendParticles(type, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    public static void sound(ServerLevel level, Vec3 pos, SoundEvent s, float vol, float pitch) {
        level.playSound(null, pos.x, pos.y, pos.z, s, SoundSource.PLAYERS, vol, pitch);
    }

    /** Lebewesen im Radius, ohne den Anwender, PvP-Regel beachtet, nach Entfernung sortiert und begrenzt. */
    public static List<LivingEntity> targetsAround(ServerLevel level, ServerPlayer caster, Vec3 center, double radius) {
        UnstableConfig cfg = UnstableConfig.get();
        AABB box = new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius));
        return level.getEntitiesOfClass(LivingEntity.class, box, e ->
                        e != caster && e.isAlive() && !e.isSpectator()
                                && e.distanceToSqr(center) <= radius * radius
                                && (cfg.pvpAbilityDamage || !(e instanceof ServerPlayer)))
                .stream()
                .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                .limit(cfg.maxAffectedEntities)
                .toList();
    }

    /** Magischer Schaden, skaliert mit dem globalen Multiplikator aus der Config. */
    public static void damage(ServerLevel level, ServerPlayer caster, LivingEntity target, float amount) {
        float dmg = amount * UnstableConfig.get().globalDamageMultiplier;
        if (dmg <= 0) return;
        target.hurtServer(level, level.damageSources().indirectMagic(caster, caster), dmg);
    }

    public static void knockbackFrom(LivingEntity target, Vec3 origin, double strength) {
        Vec3 away = target.position().subtract(origin);
        // knockback() erwartet die Richtung zum Angreifer, daher negiert
        target.knockback(strength, -away.x, -away.z);
    }
}
