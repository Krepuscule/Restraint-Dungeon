package com.twi.restraint_dungeon.client.keybind;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ModKeyBindRegister {

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBinds.CHANGE_PART_HUD);
        event.register(ModKeyBinds.CHANGE_PART_UP);
        event.register(ModKeyBinds.CHANGE_PART_DOWN);
        event.register(ModKeyBinds.CHANGE_POSITION);
        event.register(ModKeyBinds.OPEN_ACTION_MENU);
        event.register(ModKeyBinds.RESTRAINT_MENU);
        event.register(ModKeyBinds.STRUGGLE_MODE_SELECT_MENU);
    }
}