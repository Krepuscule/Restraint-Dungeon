package com.twi.restraint_dungeon.action.utils;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.action.type.CarryAction;
import com.twi.restraint_dungeon.action.type.CarryingAction;
import com.twi.restraint_dungeon.client.hud.action_hud.ActionSelectMenu;
import com.twi.restraint_dungeon.event.custom_event.PlayerActionEvent;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.client.keybind.ModKeyBinds.OPEN_ACTION_MENU;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.*;

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
            HitResult hitResult = mc.hitResult;

            if (!mc.player.getPassengers().isEmpty()) {
                Entity passenger = mc.player.getPassengers().get(0);
                if (passenger instanceof LivingEntity living) {
                    target = living;
                }
            } else if (mc.crosshairPickEntity instanceof LivingEntity living) {
                target = living;
            }


            mc.setScreen(new ActionSelectMenu(mc.player, target, hitResult));

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


    private static CameraType previousCameraType = null;
    private static boolean wasActionActive = false;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return;

        boolean isAction = isDoingAction(player);
        BaseAction action = getCurrentAction(player);

        if(action instanceof CarryingAction) return;

        if (isAction && !wasActionActive) {
            previousCameraType = mc.options.getCameraType();

            if (previousCameraType.isFirstPerson()) {
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }

            wasActionActive = true;
        }
        else if (!isAction && wasActionActive) {
            if (previousCameraType != null) {
                mc.options.setCameraType(previousCameraType);
                previousCameraType = null;
            }

            wasActionActive = false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onDoingActionLivingEntity(MovementInputUpdateEvent event) {
        LocalPlayer player = (LocalPlayer) event.getEntity();
        Input input = event.getInput();

        if (isDoingAction(player)) {
            input.leftImpulse = 0;
            input.forwardImpulse = 0;
            input.jumping = false;
            input.shiftKeyDown = false;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
        }
    }
}