package com.twi.restraint_dungeon.entity.npc;

import com.twi.restraint_dungeon.client.keybind.ModKeyBinds;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.network.payload.npc.conversation.RequestStartConversationPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class NPCInteractionEvent {
    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Pre event) {

        if (!VanillaGuiLayers.CROSSHAIR.equals(event.getName())) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        HitResult hitResult = mc.hitResult;
        if (hitResult instanceof EntityHitResult entityHit && entityHit.getType() == HitResult.Type.ENTITY) {
            Entity target = entityHit.getEntity();

            if (target instanceof BaseNPCEntity npc && npc.canBeConversation(mc.player,npc)) {
                GuiGraphics graphics = event.getGuiGraphics();


                Component keyName = ModKeyBinds.NPC_CONVERSATION.getTranslatedKeyMessage();
                Component tipText = Component.translatable("event." + MODID + ".npc_conversation_tip.prefix")
                        .append(keyName)
                        .append(Component.translatable("event." + MODID + ".npc_conversation_tip.suffix"));

                int screenWidth = mc.getWindow().getGuiScaledWidth();
                int screenHeight = mc.getWindow().getGuiScaledHeight();

                int textWidth = mc.font.width(tipText);

                int x = (screenWidth - textWidth) / 2;
                int y = (screenHeight / 2) + 30;

                graphics.drawString(mc.font, tipText, x, y, 0xFFFFFF, true);
            }
        }
    }


    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (ModKeyBinds.NPC_CONVERSATION.consumeClick()) {
            HitResult hitResult = mc.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                Entity target = ((EntityHitResult) hitResult).getEntity();

                if (target instanceof BaseNPCEntity npc && npc.canBeConversation(mc.player,npc)) {
                    PacketDistributor.sendToServer(new RequestStartConversationPayload(npc.getId()));
                }
            }
        }
    }
}
