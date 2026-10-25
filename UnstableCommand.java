package com.unstable.smp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.unstable.smp.UnstableConfig;
import com.unstable.smp.ability.Abilities;
import com.unstable.smp.ability.Ability;
import com.unstable.smp.item.ModItems;
import com.unstable.smp.player.ModAttachments;
import com.unstable.smp.player.PlayerData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class UnstableCommand {
    private UnstableCommand() {}

    /** Zentrale Stelle fuer die Admin-Berechtigung (Op-Stufe "Gamemaster"). Bei API-Aenderungen nur hier anpassen. */
    private static boolean isAdmin(CommandSourceStack s) {
        return Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(s);
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("unstable")
                .executes(c -> help(c))
                .then(Commands.literal("help").executes(UnstableCommand::help))
                .then(Commands.literal("stats").executes(UnstableCommand::stats))
                .then(Commands.literal("abilities").executes(UnstableCommand::abilities))
                .then(Commands.literal("reload").requires(UnstableCommand::isAdmin).executes(c -> {
                    UnstableConfig.load();
                    c.getSource().sendSuccess(() -> Component.literal("Config neu geladen."), true);
                    return 1;
                }))
                .then(Commands.literal("give").requires(UnstableCommand::isAdmin)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("artifact", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(ModItems.ARTIFACTS.keySet(), b))
                                        .executes(c -> give(c, ModItems.ARTIFACTS)))))
                .then(Commands.literal("weapon").requires(UnstableCommand::isAdmin)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("weapon", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(ModItems.WEAPONS.keySet(), b))
                                        .executes(c -> give(c, ModItems.WEAPONS)))))
                // Noch nicht implementiert (spaetere Phasen): awaken, quest, event, boss
        );
    }

    private static int help(CommandContext<CommandSourceStack> c) {
        var s = c.getSource();
        s.sendSuccess(() -> Component.literal("Unstable SMP - Befehle:"), false);
        s.sendSuccess(() -> Component.literal("/unstable stats | abilities"), false);
        if (isAdmin(s)) {
            s.sendSuccess(() -> Component.literal("/unstable give <spieler> <artefakt> | weapon <spieler> <waffe> | reload"), false);
        }
        return 1;
    }

    private static int stats(CommandContext<CommandSourceStack> c) {
        var s = c.getSource();
        if (!(s.getEntity() instanceof ServerPlayer p)) {
            s.sendFailure(Component.literal("Nur fuer Spieler."));
            return 0;
        }
        PlayerData d = p.getAttachedOrCreate(ModAttachments.PLAYER_DATA);
        int max = (int) UnstableConfig.get().maxEnergy;
        s.sendSuccess(() -> Component.literal("Energie: " + (int) d.energy() + "/" + max), false);
        return 1;
    }

    private static int abilities(CommandContext<CommandSourceStack> c) {
        var s = c.getSource();
        long now = s.getLevel().getGameTime();
        PlayerData d = s.getEntity() instanceof ServerPlayer p ? p.getAttachedOrCreate(ModAttachments.PLAYER_DATA) : null;
        for (Ability a : Abilities.all()) {
            var cfg = UnstableConfig.get().forAbility(a.id());
            long cd = d == null ? 0 : d.remaining(a.id(), now);
            String line = a.id() + ": " + (int) cfg.energyCost + " Energie, " + String.format("%.1f", cfg.cooldownTicks / 20.0)
                    + "s Abklingzeit" + (cd > 0 ? " (noch " + String.format("%.1f", cd / 20.0) + "s)" : "");
            s.sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    private static int give(CommandContext<CommandSourceStack> c, Map<String, Item> pool) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        String name = StringArgumentType.getString(c, pool == ModItems.ARTIFACTS ? "artifact" : "weapon");
        Item item = pool.get(name);
        if (item == null) {
            c.getSource().sendFailure(Component.literal("Unbekannt: " + name));
            return 0;
        }
        ItemStack stack = new ItemStack(item);
        if (!target.getInventory().add(stack)) target.drop(stack, false);
        c.getSource().sendSuccess(() -> Component.literal(name + " an " + target.getName().getString() + " gegeben."), true);
        return 1;
    }
}
