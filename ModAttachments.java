package com.unstable.smp.player;

import com.unstable.smp.UnstableSmp;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

public final class ModAttachments {
    public static final AttachmentType<PlayerData> PLAYER_DATA = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(UnstableSmp.MOD_ID, "player_data"),
            b -> b.persistent(PlayerData.CODEC).copyOnDeath().initializer(PlayerData::fresh));

    private ModAttachments() {}

    public static void init() {}
}
