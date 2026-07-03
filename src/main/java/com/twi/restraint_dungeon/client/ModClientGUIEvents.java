package com.twi.restraint_dungeon.client;

import com.twi.restraint_dungeon.client.gui.TargetInventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID,  value = Dist.CLIENT)
public class ModClientGUIEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.TARGET_INVENTORY.get(), TargetInventoryScreen::new);
    }
}