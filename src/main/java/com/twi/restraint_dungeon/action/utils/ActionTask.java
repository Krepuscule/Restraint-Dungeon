package com.twi.restraint_dungeon.action.utils;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.action.type.AnimAction;
import com.twi.restraint_dungeon.event.custom_event.PlayerActionEvent;
import com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ActionTask {
    private final UUID carrierUUID;
    @Nullable private final UUID targetUUID;
    private final BaseAction action;
    private final HitResult hitResult;
    private int remaining;
    private boolean aborted = false;


    public ActionTask(ServerPlayer carrier, @Nullable LivingEntity target, BaseAction action, HitResult hitResult) {
        this.carrierUUID = carrier.getUUID();
        this.targetUUID = target != null ? target.getUUID() : null;
        this.action = action;
        this.hitResult = hitResult;
        this.remaining = action.getAnimTicks();
    }

    public void abort() { this.aborted = true; }
    public UUID getCarrierUUID() { return carrierUUID; }
    @Nullable public UUID getTargetUUID() { return targetUUID; }
    public HitResult getHitResult() { return hitResult; }



    public BaseAction getAction() { return action; }

    public void forceAbortNow(MinecraftServer server) {
        ServerPlayer actionPlayer = server.getPlayerList().getPlayer(carrierUUID);
        LivingEntity target = targetUUID != null ? getTarget(server, targetUUID) : null;
        handleAbort(actionPlayer, target);
    }

    public boolean tick(MinecraftServer server) {
        ServerPlayer actionPlayer = server.getPlayerList().getPlayer(carrierUUID);
        LivingEntity target = targetUUID != null ? getTarget(server, targetUUID) : null;

        boolean isTargetInvalid = targetUUID != null && (target == null || !target.isAlive());
        if (aborted || actionPlayer == null || !actionPlayer.isAlive() || isTargetInvalid) {
            handleAbort(actionPlayer, target);
            return true;
        }

        if (!action.canContinueUse(actionPlayer, target)) {
            handleAbort(actionPlayer, target);
            return true;
        }

        action.onTick(actionPlayer, target, hitResult, remaining);

        if (!action.isInfinite()) {
            if (--remaining <= 0) {
                action.onFinish(actionPlayer, target, hitResult);
                NeoForge.EVENT_BUS.post(new PlayerActionEvent.Finish(actionPlayer, action, target));

                if (target != null) {
                    PlayerActionUtils.unlinkAction(actionPlayer, target);
                } else {
                    PlayerActionUtils.resetSingleAction(actionPlayer, action.getActionId());
                }
                return true;
            }
        } else {
            remaining--;
        }

        return false;
    }


    private void handleAbort(ServerPlayer actionPlayer, @Nullable LivingEntity target) {

        action.onAbort(actionPlayer, target, hitResult);
        NeoForge.EVENT_BUS.post(new PlayerActionEvent.Abort(actionPlayer, action, target));

        if (target != null) {
            if (actionPlayer != null) {
                PlayerActionUtils.unlinkAction(actionPlayer, target);
            } else {
                PlayerActionUtils.resetSingleAction(target,action.getActionId());
            }
        }
        else if (actionPlayer != null) {
            PlayerActionUtils.resetSingleAction(actionPlayer, action.getActionId());
        }
    }

    @Nullable
    private static LivingEntity getTarget(MinecraftServer server, UUID uuid) {
        if (uuid == null) return null;
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player != null) return player;

        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }
}