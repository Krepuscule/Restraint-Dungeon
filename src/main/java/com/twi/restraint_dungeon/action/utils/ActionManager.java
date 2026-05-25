package com.twi.restraint_dungeon.action.utils;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.action.type.CarryingAction;
import com.twi.restraint_dungeon.event.custom_event.PlayerActionEvent;
import com.twi.restraint_dungeon.event.custom_event.RestraintPositionChangeEvent;
import com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class ActionManager {
    private static final Map<String, BaseAction> REGISTRY = new HashMap<>();
    private static final List<ActionTask> ACTIVE_TASKS = new CopyOnWriteArrayList<>();

    /**
     * 手动注册 Action
     */
    public static void register(BaseAction action) {
        REGISTRY.put(action.getActionId(), action);
    }

    public static BaseAction get(String id) {
        return REGISTRY.get(id);
    }

    /**
     * 执行动作入口
     */
    public static void execute(ServerPlayer actionPlayer, String actionId, HitResult hitResult) {
        BaseAction action = get(actionId);
        if (action == null) return;

        Component failureReason = action.canUse(actionPlayer, hitResult);
        if (failureReason != null) {
            actionPlayer.displayClientMessage(failureReason, true);
            return;
        }

        if (PlayerActionUtils.isDoingAction(actionPlayer)) return;

        LivingEntity target = null;
        if (action instanceof CarryingAction carryingAction && carryingAction.requiresCarrying()) {
            Entity passenger = actionPlayer.getPassengers().isEmpty() ? null : actionPlayer.getPassengers().get(0);
            if (passenger instanceof LivingEntity living) {
                target = living;
            }
        }
        else if (hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
            target = living;
        }

        PlayerActionUtils.linkAction(actionPlayer, target, actionId);

        ActionTask task = new ActionTask(actionPlayer, target, action);
        ACTIVE_TASKS.add(task);

        action.onStart(actionPlayer, target);
        NeoForge.EVENT_BUS.post(new PlayerActionEvent.Start(actionPlayer,action,hitResult));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        ACTIVE_TASKS.removeIf(task -> task.tick(server));
    }

    /**
     * 强制停止某个实体的所有动作任务
     */
    public static void forceStop(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        ACTIVE_TASKS.forEach(task -> {
            if (task.getCarrierUUID().equals(uuid) || task.getTargetUUID().equals(uuid)) {
                task.abort();
            }
        });
    }

    public static Set<String> getRegisteredIds() {
        return REGISTRY.keySet();
    }
}