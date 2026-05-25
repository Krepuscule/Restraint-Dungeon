package com.twi.restraint_dungeon.client.hud.struggle_hud;

import com.twi.restraint_dungeon.network.payload.player_restraint.PlayerRestraintPartPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.client.keybind.ModKeyBinds.STRUGGLE_MODE_SELECT_MENU;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getTargetPart;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.setTargetPart;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class StruggleHUD {

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof LocalPlayer player && StruggleHUDManager.isActive()) {
            if (event.getOriginalDamage() > 0) {
                StruggleHUDManager.cancel();
                setTargetPart(player, getTargetPart(player));
                PacketDistributor.sendToServer(new PlayerRestraintPartPayload(getTargetPart(player).name()));
                player.displayClientMessage(Component.translatable("hud.restraint_dungeon.struggle_stopped_by_damage").withStyle(ChatFormatting.DARK_RED), true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !StruggleHUDManager.isActive()) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            int key = event.getKey();

            if (key == GLFW.GLFW_KEY_ESCAPE || key == STRUGGLE_MODE_SELECT_MENU.getKey().getValue()) {
                StruggleHUDManager.cancel();
                setTargetPart(player, getTargetPart(player));
                PacketDistributor.sendToServer(new PlayerRestraintPartPayload(getTargetPart(player).name()));

                if (key == STRUGGLE_MODE_SELECT_MENU.getKey().getValue()) return;
            }

            StruggleHUDManager.handleKeyInput(event.getKey(), event.getScanCode(), event.getModifiers());

        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        if (StruggleHUDManager.isActive()) {
            StruggleHUDManager.render(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(true));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        // 如果打开了任何 UI 界面，则自动取消挣扎状态
        if (minecraft.screen != null && StruggleHUDManager.isActive()) {
            StruggleHUDManager.cancel();
            setTargetPart(minecraft.player, getTargetPart(minecraft.player));
            PacketDistributor.sendToServer(new PlayerRestraintPartPayload(getTargetPart(minecraft.player).name()));
        }
    }
}