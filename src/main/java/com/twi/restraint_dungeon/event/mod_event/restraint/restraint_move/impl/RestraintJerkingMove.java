package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.impl;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.LegsPose;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.PlayerRestraintMove;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveClientManager;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import com.twi.restraint_dungeon.network.payload.player_restraint.RestraintMoveSyncBodyYawPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getLegsPose;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindLegs;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBusyState;

public class RestraintJerkingMove extends PlayerRestraintMove {

    private static final int JERK_PRE_DURATION = 10;
    private static final int JERK_MID_DURATION = 5;
    private static final int JERK_END_DURATION = 5;

    private static final float JERK_FORWARD_DIST = 0.5f;
    private static final float JERK_BACK_DIST = 0.4f;

    @Override public String getModId() { return MODID; }
    @Override public String getMoveTypeId() { return "JERKING"; }
    @Override public float getTurnAngle() { return 25.0f; }

    @Override
    public boolean canMove(Player player) {
        return getRestraintPosition(player) == RestraintPositionEvent.RestraintPosition.SITTING && getLegsPose(player) == LegsPose.LEGS_TOGETHER
                && isBeenBindLegs(player)
                && !isBusyState(player) && !isRidingRestraintDevice(player);
    }

    private boolean isFirstTickOfMid = true;

    @Override
    protected void onTick(LivingEntity entity) {
        if (!(entity instanceof LocalPlayer player)) return;

        if (currentStage == Stage.MID) {
            float yaw = RestraintMoveClientManager.getTargetBodyRot();

            if (currentDirection.equals("A") || currentDirection.equals("D")) {
                if (isFirstTickOfMid) {
                    float angle = getTurnAngle();
                    float updatedYaw = Mth.wrapDegrees(yaw + (currentDirection.equals("A") ? -angle : angle));
                    PacketDistributor.sendToServer(new RestraintMoveSyncBodyYawPayload(updatedYaw));
                    isFirstTickOfMid = false;
                }
                return;
            }

            float yawRad = (float) Math.toRadians(yaw);
            double sin = Mth.sin(yawRad);
            double cos = Mth.cos(yawRad);

            double totalDist = currentDirection.equals("W") ? JERK_FORWARD_DIST : -JERK_BACK_DIST;
            int duration = getDuration(currentDirection, Stage.MID);
            double speedPerTick = totalDist / (double) Math.max(1, duration);

            player.setDeltaMovement(-sin * speedPerTick, player.getDeltaMovement().y, cos * speedPerTick);
            player.hurtMarked = true;
        } else {
            isFirstTickOfMid = true;
        }
    }

    @Override public boolean canMoveForward(Player player) { return true; }
    @Override public boolean canMoveBackward(Player player) { return true; }
    @Override public boolean canTurnLeft(Player player) { return true; }
    @Override public boolean canTurnRight(Player player) { return true; }

    @Override
    public int getDuration(String direction, Stage stage) {
        return switch (stage) {
            case PRE -> JERK_PRE_DURATION;
            case MID -> JERK_MID_DURATION;
            case END -> JERK_END_DURATION;
            default -> 0;
        };
    }

    @Override public int getCooldown(Player player, String direction) { return 5; }
}