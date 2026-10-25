package com.unstable.smp.item;

import com.unstable.smp.UnstableSmp;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    public static final Map<String, Item> ARTIFACTS = new LinkedHashMap<>();
    public static final Map<String, Item> WEAPONS = new LinkedHashMap<>();

    private ModItems() {}

    public static void init() {
        artifact("rift_core", "rift_blink");
        artifact("chaos_orb", "chaos_burst");
        artifact("storm_crystal", "chain_lightning");

        weapon("void_blade", "void_slash");
        weapon("stormbreaker", "thunder_slam");
        weapon("bloodfang_sword", "blood_drain");

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(e -> {
            ARTIFACTS.values().forEach(e::accept);
            WEAPONS.values().forEach(e::accept);
        });
    }

    private static void artifact(String name, String abilityId) {
        ResourceKey<Item> key = key(name);
        Item.Properties props = new Item.Properties().setId(key).stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC);
        ARTIFACTS.put(name, Registry.register(BuiltInRegistries.ITEM, key, new AbilityItem(props, abilityId)));
    }

    private static void weapon(String name, String abilityId) {
        ResourceKey<Item> key = key(name);
        Item.Properties props = new Item.Properties().setId(key).stacksTo(1)
                .rarity(net.minecraft.world.item.Rarity.EPIC)
                .sword(ToolMaterial.NETHERITE, 3f, -2.4f);
        WEAPONS.put(name, Registry.register(BuiltInRegistries.ITEM, key, new AbilityItem(props, abilityId)));
    }

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(UnstableSmp.MOD_ID, name));
    }
}
