package com.unstable.smp.ability;

import com.unstable.smp.UnstableConfig.AbilityConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface Ability {
    /** Eindeutige ID, auch Schluessel in der Config und im Cooldown-Speicher. */
    String id();

    /** Standardwerte, falls die Config noch keinen Eintrag hat. */
    AbilityConfig defaults();

    /**
     * Fuehrt die Faehigkeit serverseitig aus.
     * @return false, wenn nichts passiert ist (z. B. kein Ziel) - dann werden weder Energie noch Cooldown verbraucht.
     */
    boolean execute(ServerPlayer player, ServerLevel level, AbilityConfig cfg);
}
