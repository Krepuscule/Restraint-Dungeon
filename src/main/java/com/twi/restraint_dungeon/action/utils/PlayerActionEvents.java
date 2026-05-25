package com.twi.restraint_dungeon.action.utils;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.action.type.CarryAction;
import com.twi.restraint_dungeon.client.hud.action_hud.ActionSelectMenu;
import com.twi.restraint_dungeon.event.custom_event.PlayerActionEvent;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.client.keybind.ModKeyBinds.OPEN_ACTION_MENU;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.updateRestraintPosition;

@EventBusSubscriber(modid = MODID)
public class PlayerActionEvents {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (OPEN_ACTION_MENU.consumeClick()) {

            if (PlayerActionUtils.isTarget(mc.player)) return;

            LivingEntity target = null;

            if (!mc.player.getPassengers().isEmpty()) {
                Entity passenger = mc.player.getPassengers().get(0);
                if (passenger instanceof LivingEntity living) {
                    target = living;
                }
            }

            else if (mc.crosshairPickEntity instanceof LivingEntity living) {
                target = living;
            }

            if(target != null) {
                mc.setScreen(new ActionSelectMenu(mc.player, target));
            }
        }
    }

    /**
     * 玩家死亡时，强行中断所有正在进行的 Action 任务
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ActionManager.forceStop(player);
        }
    }

    /**
     * 玩家离线时清理
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ActionManager.forceStop(player);
        }
    }
}