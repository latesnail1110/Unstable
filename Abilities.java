package com.unstable.smp.ability;

import com.unstable.smp.ability.impl.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Abilities {
    private static final Map<String, Ability> REGISTRY = new LinkedHashMap<>();

    private Abilities() {}

    public static void init() {
        if (!REGISTRY.isEmpty()) return;
        register(new RiftBlink());
        register(new ChaosBurst());
        register(new ChainLightning());
        register(new VoidSlash());
        register(new ThunderSlam());
        register(new BloodDrain());
    }

    private static void register(Ability a) {
        REGISTRY.put(a.id(), a);
    }

    public static Ability get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<Ability> all() {
        return REGISTRY.values();
    }
}
