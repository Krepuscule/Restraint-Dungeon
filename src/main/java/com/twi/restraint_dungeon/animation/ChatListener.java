package com.twi.restraint_dungeon.animation;

import com.twi.restraint_dungeon.animation.utils.AnimationLayer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.animation.utils.AnimationPlayerUtils.sendAnimationSequence;

@EventBusSubscriber(modid = MODID)
public class ChatListener {

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {

        ServerPlayer player = event.getPlayer();

        String message = event.getRawText();

        if (message.equalsIgnoreCase("hello")) {
            List<String> animations = List.of("new1");

            sendAnimationSequence(
                    player,
                    animations,
                    AnimationLayer.FULL_BODY
            );

        }
    }
}