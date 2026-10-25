package com.unstable.smp.ability;

import com.unstable.smp.UnstableConfig;
import com.unstable.smp.item.AbilityItem;
import com.unstable.smp.player.ModAttachments;
import com.unstable.smp.player.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Zentrale, ausschliesslich serverseitige Pruefung: Energie, Cooldown, Ausfuehrung.
 * Es gibt bewusst KEIN Client->Server-Paket fuer Faehigkeiten: der Server loest sie nur durch
 * das echte Item-Use-Event aus, daher gibt es keine manipulierbaren Faehigkeitspakete.
 */
public final class AbilityManager {
    private static int tickCounter = 0;

    private AbilityManager() {}

    public static void tryUse(ServerPlayer player, Ability ability) {
        ServerLevel level = player.level();
        UnstableConfig cfg = UnstableConfig.get();
        UnstableConfig.AbilityConfig ac = cfg.forAbility(ability.id());
        if (ac == null) return;

        long now = level.getGameTime();
        PlayerData data = player.getAttachedOrCreate(ModAttachments.PLAYER_DATA);

        long cd = data.remaining(ability.id(), now);
        if (cd > 0) {
            msg(player, "Abklingzeit: " + String.format("%.1f", cd / 20.0) + "s");
            return;
        }
        if (data.energy() < ac.energyCost) {
            msg(player, "Nicht genug Energie (" + (int) data.energy() + "/" + (int) ac.energyCost + ")");
            return;
        }
        if (!ability.execute(player, level, ac)) {
            msg(player, "Kein gueltiges Ziel");
            return;
        }
        // Daten nach der Ausfuehrung neu lesen (execute kann z. B. heilen, aber keine Daten aendern)
        data = player.getAttachedOrCreate(ModAttachments.PLAYER_DATA);
        player.setAttached(ModAttachments.PLAYER_DATA,
                data.withEnergy(data.energy() - ac.energyCost).withCooldown(ability.id(), now + ac.cooldownTicks));
    }

    /** Energie-Regeneration (1x pro Sekunde) und Aktionsleisten-Anzeige (alle 10 Ticks, nur mit Artefakt in der Hand). */
    public static void serverTick(MinecraftServer server) {
        tickCounter++;
        UnstableConfig cfg = UnstableConfig.get();
        boolean regen = tickCounter % 20 == 0;
        boolean hud = tickCounter % 10 == 0;
        if (!regen && !hud) return;

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!p.isAlive()) continue;
            PlayerData d = p.getAttachedOrCreate(ModAttachments.PLAYER_DATA);
            if (regen) {
                PlayerData n = d.withEnergy(d.energy() + cfg.energyRegenPerSecond).pruned(p.level().getGameTime());
                if (n != d) {
                    p.setAttached(ModAttachments.PLAYER_DATA, n);
                    d = n;
                }
            }
            if (hud && p.getMainHandItem().getItem() instanceof AbilityItem ai) {
                Ability a = Abilities.get(ai.abilityId());
                if (a != null) {
                    long cd = d.remaining(a.id(), p.level().getGameTime());
                    String cdText = cd > 0 ? String.format("  Abklingzeit %.1fs", cd / 20.0) : "  bereit";
                    p.sendSystemMessage(Component.literal("Energie " + (int) d.energy() + "/" + (int) cfg.maxEnergy + cdText), true);
                }
            }
        }
    }

    private static void msg(ServerPlayer p, String text) {
        p.sendSystemMessage(Component.literal(text), true);
    }
}
