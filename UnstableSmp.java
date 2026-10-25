package com.unstable.smp;

import com.unstable.smp.ability.AbilityManager;
import com.unstable.smp.command.UnstableCommand;
import com.unstable.smp.item.ModItems;
import com.unstable.smp.player.ModAttachments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnstableSmp implements ModInitializer {
    public static final String MOD_ID = "unstable_smp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        com.unstable.smp.ability.Abilities.init();
        UnstableConfig.load();
        ModAttachments.init();
        ModItems.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, ctx, env) -> UnstableCommand.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(AbilityManager::serverTick);
        LOGGER.info("Unstable SMP Ultimate (Phase 1) geladen");
    }
}
