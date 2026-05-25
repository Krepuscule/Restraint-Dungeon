package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.impl.RestraintHoppingMove;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.impl.RestraintJerkingMove;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class ModRestraintMove {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            RestraintMoveManager.register(new RestraintHoppingMove());
            RestraintMoveManager.register(new RestraintJerkingMove());
        });
    }
}