package com.twi.restraint_dungeon.action.utils;

import com.twi.restraint_dungeon.action.impl.StrokeAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.getPleasantValue;

@EventBusSubscriber(modid = MODID,value = Dist.CLIENT)
public class ActionClientEvent {
    private static final ResourceLocation EMPTY_BAR = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/empty_progress_bar.png");
    private static final ResourceLocation PINK_BAR = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/pleasant_progress_bar.png");

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        boolean shouldRender = false;
        LivingEntity targetEntity = null;
        double pleasantValue = 0.0f;

        boolean isCarrierDoingStroke = isDoingAction(player) && getCurrentAction(player) instanceof StrokeAction;

        if (isCarrierDoingStroke) {
            if (isTarget(player)) {
                pleasantValue = getPleasantValue(player);
                shouldRender = true;
            }else {
                UUID partnerUUID = getActionPartnerUUID(player);
                if (partnerUUID != null) {
                    Entity found = getClientEntityByUUID(mc.level, partnerUUID);
                    if (found instanceof LivingEntity living) {
                        targetEntity = living;
                        pleasantValue = getPleasantValue(targetEntity);
                        shouldRender = true;
                    }
                }
            }
        }


        if (shouldRender) {
            GuiGraphics graphics = event.getGuiGraphics();
            renderProgressBar(graphics, mc, pleasantValue);
        }
    }


    private static Entity getClientEntityByUUID(ClientLevel level, UUID uuid) {
        for (Entity entity : level.entitiesForRendering()) {
            if (entity.getUUID().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }


    private static void renderProgressBar(GuiGraphics graphics, Minecraft mc, double pleasantValue) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int barWidth = 128;
        int barHeight = 16;

        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight * 3 / 4;

        double progress = Math.clamp(pleasantValue, 0.0f, 100.0f);
        int filledWidth = (int) (barWidth * (progress / 100.0f));

        graphics.blit(EMPTY_BAR, x, y, 0, 0, barWidth, barHeight, barWidth, barHeight);

        if (filledWidth > 0) {
            graphics.blit(PINK_BAR,
                    x, y,
                    0, 0,
                    filledWidth, barHeight,
                    barWidth, barHeight
            );
        }
    }
}
