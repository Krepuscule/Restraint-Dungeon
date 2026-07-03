package com.twi.restraint_dungeon.item.materials;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModMaterials {
    public static final DeferredRegister<Item> MATERIALS =
            DeferredRegister.create(Registries.ITEM, MODID);

    public static final DeferredHolder<Item, RubberItem> RUBBER = MATERIALS.register("rubber",
            () -> new RubberItem(new Item.Properties()));
}
