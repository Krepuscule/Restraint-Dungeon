package com.twi.restraint_dungeon.action.utils;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.event.custom_event.PlayerActionEvent;
import com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;

import java.util.UUID;

public class ActionTask {
    private final UUID carrierUUID;
    private final UUID targetUUID;
    private final BaseAction action;
    private int remaining;
    private boolean aborted = false;

    public ActionTask(ServerPlayer carrier, LivingEntity target, BaseAction action) {
        this.carrierUUID = carrier.getUUID();
        this.targetUUID = target.getUUID();
        this.action = action;
        this.remaining = action.getAnimTicks();
    }

    public void abort() { this.aborted = true; }
    public UUID getCarrierUUID() { return carrierUUID; }
    public UUID getTargetUUID() { return targetUUID; }

    /**
     * @return true 如果任务已结束需要被移除
     */
    public boolean tick(MinecraftServer server) {
        ServerPlayer actionPlayer = server.getPlayerList().getPlayer(carrierUUID);
        LivingEntity target = getTarget(server,targetUUID);

        // 异常中断检查
        if (aborted || actionPlayer == null || target == null || !actionPlayer.isAlive() || !target.isAlive()) {
            handleAbort(actionPlayer, target);
            return true;
        }

        // 距离或逻辑继续检查
        if (!action.canContinueUse(actionPlayer, target)) {
            handleAbort(actionPlayer, target);
            return true;
        }

        // 执行 Tick 回调
        action.onTick(actionPlayer, target, remaining);

        // 倒计时结束
        if (--remaining <= 0) {
            action.onFinish(actionPlayer, target);
            NeoForge.EVENT_BUS.post(new PlayerActionEvent.Finish(actionPlayer,action,target));
            // 动作完成，解绑状态
            PlayerActionUtils.unlinkAction(actionPlayer, target);
            return true;
        }

        return false;
    }

    private void handleAbort(ServerPlayer actionPlayer, LivingEntity target) {
        if (actionPlayer != null && target != null) {
            action.onAbort(actionPlayer, target);
            NeoForge.EVENT_BUS.post(new PlayerActionEvent.Abort(actionPlayer,action,target));
            PlayerActionUtils.unlinkAction(actionPlayer, target);
        }
    }

    private static LivingEntity getTarget(MinecraftServer server, UUID uuid) {
        if (uuid == null) return null;
        // 先检查在线玩家，这最快
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player != null) return player;

        // 如果不是玩家，遍历所有维度查找该实体
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }
}