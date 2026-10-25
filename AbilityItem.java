package com.unstable.smp.item;

import com.unstable.smp.ability.Abilities;
import com.unstable.smp.ability.AbilityManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Item, das bei Rechtsklick (serverseitig) eine Faehigkeit ausloest. Dient fuer Artefakte und Waffen. */
public class AbilityItem extends Item {
    private final String abilityId;

    public AbilityItem(Properties props, String abilityId) {
        super(props);
        this.abilityId = abilityId;
    }

    public String abilityId() {
        return abilityId;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            var ability = Abilities.get(abilityId);
            if (ability != null) AbilityManager.tryUse(sp, ability);
        }
        return InteractionResult.SUCCESS;
    }
}
