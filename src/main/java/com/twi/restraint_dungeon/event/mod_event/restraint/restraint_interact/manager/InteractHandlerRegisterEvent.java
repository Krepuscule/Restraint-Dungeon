package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.interact_event.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class InteractHandlerRegisterEvent {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            InteractTargetSelector.registerHandler(ContainerInteractEvent.getInstance());
            InteractTargetSelector.registerHandler(LecternInteractEvent.getInstance());
            InteractTargetSelector.registerHandler(RedstoneInteractEvent.getInstance());
            InteractTargetSelector.registerHandler(SwordInsertEvent.getInstance());
            InteractTargetSelector.registerHandler(SwordTakeEvent.getInstance());
        });
    }
}