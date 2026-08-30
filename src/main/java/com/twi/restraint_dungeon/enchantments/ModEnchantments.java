package com.twi.restraint_dungeon.enchantments;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> CURSE_OF_VIVIFICATION = 
            ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MODID, "curse_of_vivification"));
}