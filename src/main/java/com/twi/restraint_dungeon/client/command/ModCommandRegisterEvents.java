package com.twi.restraint_dungeon.client.command;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class ModCommandRegisterEvents {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}