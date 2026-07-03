package com.twi.restraint_dungeon.item.restraint_tool;

import com.twi.restraint_dungeon.item.restraint_tool.tools.vibrator.VibratorItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModRestraintTools {
    public static final DeferredRegister<Item> RESTRAINT_TOOLS =
            DeferredRegister.create(Registries.ITEM, MODID);


    public static final DeferredHolder<Item, VibratorItem> VIBRATOR = RESTRAINT_TOOLS.register("vibrator",
            () -> new VibratorItem(new Item.Properties()));
}
