package com.unstable.smp.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unstable.smp.UnstableConfig;

import java.util.HashMap;
import java.util.Map;

/** Persistente Spielerdaten (Fabric Attachments, werden im Spielstand gespeichert). Unveraenderlich. */
public record PlayerData(float energy, Map<String, Long> cooldownEnd) {
    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.optionalFieldOf("energy", 0f).forGetter(PlayerData::energy),
            Codec.unboundedMap(Codec.STRING, Codec.LONG).optionalFieldOf("cooldowns", Map.of()).forGetter(PlayerData::cooldownEnd)
    ).apply(i, PlayerData::new));

    public static PlayerData fresh() {
        return new PlayerData(UnstableConfig.get().maxEnergy, Map.of());
    }

    public PlayerData withEnergy(float e) {
        float max = UnstableConfig.get().maxEnergy;
        return new PlayerData(Math.max(0f, Math.min(max, e)), cooldownEnd);
    }

    public PlayerData withCooldown(String abilityId, long endGameTime) {
        Map<String, Long> m = new HashMap<>(cooldownEnd);
        m.put(abilityId, endGameTime);
        return new PlayerData(energy, m);
    }

    /** Verbleibende Ticks bis die Faehigkeit wieder bereit ist (0 = bereit). */
    public long remaining(String abilityId, long now) {
        Long end = cooldownEnd.get(abilityId);
        return end == null ? 0 : Math.max(0, end - now);
    }

    /** Entfernt abgelaufene Eintraege, damit die Map nicht waechst. */
    public PlayerData pruned(long now) {
        if (cooldownEnd.values().stream().noneMatch(v -> v <= now)) return this;
        Map<String, Long> m = new HashMap<>();
        cooldownEnd.forEach((k, v) -> { if (v > now) m.put(k, v); });
        return new PlayerData(energy, m);
    }
}
