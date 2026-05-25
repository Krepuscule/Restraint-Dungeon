package com.twi.restraint_dungeon.animation.utils;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID,value = Dist.CLIENT)
public class AnimateRegister {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        for (AnimationLayer layer : AnimationLayer.values()) {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    layer.getLocation(),
                    layer.getPriority(),
                    AnimateRegister::registerPlayerAnimation
            );
        }
    }

    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        return new ModifierLayer<>();
    }
}
