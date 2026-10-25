package com.unstable.smp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Konfiguration: config/unstable_smp.json. Alle Werte werden beim Laden begrenzt (Schutz vor Unsinnswerten). */
public final class UnstableConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static UnstableConfig instance = new UnstableConfig();

    public float maxEnergy = 100f;
    public float energyRegenPerSecond = 2f;
    public boolean pvpAbilityDamage = true;
    public float globalDamageMultiplier = 1.0f;
    public int maxAffectedEntities = 12;
    public int maxParticlesPerCast = 120;
    public Map<String, AbilityConfig> abilities = new LinkedHashMap<>();

    public static final class AbilityConfig {
        public float energyCost;
        public int cooldownTicks;
        public float damage;
        public double range;

        public AbilityConfig() {}

        public AbilityConfig(float energyCost, int cooldownTicks, float damage, double range) {
            this.energyCost = energyCost;
            this.cooldownTicks = cooldownTicks;
            this.damage = damage;
            this.range = range;
        }
    }

    public static UnstableConfig get() {
        return instance;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("unstable_smp.json");
    }

    public static void load() {
        UnstableConfig loaded = new UnstableConfig();
        Path f = file();
        if (Files.exists(f)) {
            try (Reader r = Files.newBufferedReader(f)) {
                UnstableConfig parsed = GSON.fromJson(r, UnstableConfig.class);
                if (parsed != null) loaded = parsed;
            } catch (Exception e) {
                UnstableSmp.LOGGER.error("Config konnte nicht gelesen werden, verwende Standardwerte", e);
            }
        }
        loaded.sanitize();
        instance = loaded;
        try (Writer w = Files.newBufferedWriter(f)) {
            GSON.toJson(loaded, w);
        } catch (IOException e) {
            UnstableSmp.LOGGER.error("Config konnte nicht geschrieben werden", e);
        }
    }

    private void sanitize() {
        maxEnergy = clamp(maxEnergy, 1f, 10000f);
        energyRegenPerSecond = clamp(energyRegenPerSecond, 0f, 1000f);
        globalDamageMultiplier = clamp(globalDamageMultiplier, 0f, 20f);
        maxAffectedEntities = Math.max(1, Math.min(64, maxAffectedEntities));
        maxParticlesPerCast = Math.max(0, Math.min(500, maxParticlesPerCast));
        if (abilities == null) abilities = new LinkedHashMap<>();
        // Fehlende Eintraege mit Standardwerten der Faehigkeit auffuellen
        for (var a : com.unstable.smp.ability.Abilities.all()) {
            abilities.putIfAbsent(a.id(), a.defaults());
        }
        for (AbilityConfig c : abilities.values()) {
            c.energyCost = clamp(c.energyCost, 0f, maxEnergy);
            c.cooldownTicks = Math.max(0, Math.min(72000, c.cooldownTicks));
            c.damage = clamp(c.damage, 0f, 200f);
            c.range = Math.max(1.0, Math.min(64.0, c.range));
        }
    }

    private static float clamp(float v, float lo, float hi) {
        return Float.isNaN(v) ? lo : Math.max(lo, Math.min(hi, v));
    }

    public AbilityConfig forAbility(String id) {
        return abilities.get(id);
    }
}
