package com.unstable.smp.ability.impl;

import com.unstable.smp.UnstableConfig.AbilityConfig;
import com.unstable.smp.ability.Ability;
import com.unstable.smp.ability.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Rift Core: kurzer Teleport in Blickrichtung (stoppt vor Waenden). */
public class RiftBlink implements Ability {
    public String id() { return "rift_blink"; }

    public AbilityConfig defaults() { return new AbilityConfig(25f, 100, 0f, 10.0); }

    public boolean execute(ServerPlayer p, ServerLevel level, AbilityConfig cfg) {
        Vec3 start = p.getEyePosition();
        Vec3 end = start.add(p.getLookAngle().scale(cfg.range));
        var hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        Vec3 target = hit.getType() == HitResult.Type.MISS
                ? end : hit.getLocation().subtract(p.getLookAngle().scale(0.6));
        Vec3 from = p.position();
        Vec3 dest = new Vec3(target.x, target.y - p.getEyeHeight(), target.z);
        Fx.particles(level, ParticleTypes.REVERSE_PORTAL, from.add(0, 1, 0), 40, 0.4, 0.1);
        // randomTeleport sucht eine sichere Position und schlaegt fehl, wenn keine existiert
        if (!p.randomTeleport(dest.x, dest.y, dest.z, true)) return false;
        Fx.particles(level, ParticleTypes.PORTAL, p.position().add(0, 1, 0), 40, 0.4, 0.2);
        Fx.sound(level, p.position(), SoundEvents.ENDERMAN_TELEPORT, 1f, 0.8f);
        p.fallDistance = 0;
        return true;
    }
}
