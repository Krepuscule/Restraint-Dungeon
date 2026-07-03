package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.impl;

import com.twi.restraint_dungeon.attachment.attributes.ModAttributes;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.PlayerRestraintMove;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveClientManager;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import com.twi.restraint_dungeon.network.payload.player_restraint.RestraintMoveAdvanceStagePayload;
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

public class RestraintHoppingMove extends PlayerRestraintMove {

    private static final int HOP_ANIM_DURATION = 10;
    private static final float HOP_FORWARD_SPEED = 0.4f;
    private static final float HOP_FORWARD_HEIGHT = 0.45f;
    private static final float HOP_BACK_SPEED = 0.25f;
    private static final float HOP_BACK_HEIGHT = 0.35f;
    private static final float HOP_TURN_HEIGHT = 0.3f;

    private boolean isFirstTickOfMid = true;
    private boolean wasInAirLastTick = false;

    @Override public String getModId() { return MODID; }
    @Override public String getMoveTypeId() { return "HOPPING"; }
    @Override public float getTurnAngle() { return 35.0f; }

    @Override public boolean hasCustomClientEndCondition() { return true; }

    @Override
    public boolean canMove(Player player) {
        return getRestraintPosition(player) == RestraintPositionEvent.RestraintPosition.STANDING && getLegsPose(player) == RestraintCapability.LegsPose.LEGS_TOGETHER
                && isBeenBindLegs(player)
                && !isBusyState(player) && !isRidingRestraintDevice(player);
    }

    @Override
    protected void onTick(LivingEntity entity) {
        if (currentStage == Stage.MID) {
            if (isFirstTickOfMid) {
                executeClientPhysics(entity);
                isFirstTickOfMid = false;
                wasInAirLastTick = !entity.onGround();
                this.timer = 999;
            } else {
                if (entity.onGround() && wasInAirLastTick) {
                    this.timer = 0;
                    this.isFirstTickOfMid = true;

                    PacketDistributor.sendToServer(new RestraintMoveAdvanceStagePayload(this.getMoveTypeId()));
                }
                wasInAirLastTick = !entity.onGround();
            }
        }
    }

    private void executeClientPhysics(LivingEntity entity) {
        if (!(entity instanceof LocalPlayer player)) return;


        float yaw = RestraintMoveClientManager.getTargetBodyRot();
        float yawRad = (float) Math.toRadians(yaw);
        double sin = Mth.sin(yawRad);
        double cos = Mth.cos(yawRad);

        switch (currentDirection) {
            case "W" -> player.setDeltaMovement(-sin * HOP_FORWARD_SPEED, HOP_FORWARD_HEIGHT, cos * HOP_FORWARD_SPEED);
            case "S" -> player.setDeltaMovement(sin * HOP_BACK_SPEED, HOP_BACK_HEIGHT, -cos * HOP_BACK_SPEED);
            case "A", "D" -> {
                player.setDeltaMovement(player.getDeltaMovement().x, HOP_TURN_HEIGHT, player.getDeltaMovement().z);

                float angle = getTurnAngle();
                float updatedYaw = Mth.wrapDegrees(yaw + (currentDirection.equals("A") ? -angle : angle));
                PacketDistributor.sendToServer(new RestraintMoveSyncBodyYawPayload(updatedYaw));
            }
        }
        player.hurtMarked = true;
    }

    @Override public boolean canMoveForward(Player player) { return true; }
    @Override public boolean canMoveBackward(Player player) { return true; }
    @Override public boolean canTurnLeft(Player player) { return true; }
    @Override public boolean canTurnRight(Player player) { return true; }

    @Override
    public int getDuration(String direction, Stage stage) {
        if(stage == Stage.PRE || stage == Stage.END){
            return HOP_ANIM_DURATION;
        }else{
            return 5;
        }
    }

    @Override
    public int getCooldown(Player player, String direction) {
        return switch (direction) { case "W" ->5; case "S" -> 10; default -> 5; };
    }
}