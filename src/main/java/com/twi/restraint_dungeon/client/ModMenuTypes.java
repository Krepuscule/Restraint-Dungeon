package com.twi.restraint_dungeon.client;

import com.twi.restraint_dungeon.client.gui.TargetInventoryMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS = 
            DeferredRegister.create(Registries.MENU, MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<TargetInventoryMenu>> TARGET_INVENTORY =
            MENUS.register("target_inventory", () -> new MenuType<>(TargetInventoryMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));


    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}