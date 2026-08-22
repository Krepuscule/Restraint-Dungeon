package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.impl;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.PlayerRestraintMove;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveClientManager;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.item.restraint_item.restraints.ArmBinderItem;
import com.twi.restraint_dungeon.item.restraint_item.restraints.K9CorsetItem;
import com.twi.restraint_dungeon.network.payload.player_restraint.RestraintMoveAdvanceStagePayload;
import com.twi.restraint_dungeon.network.payload.player_restraint.RestraintMoveSyncBodyYawPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;

public class RestraintCrawlingMove extends PlayerRestraintMove {

    private static final int CRAWL_DURATION = 10;

    private static final float CRAWL_SPEED = 0.05f;
    private static final float CRAWL_TURN_ANGLE = 15.0f;

    private boolean hasTurnedInMid = false;

    @Override public String getModId() { return MODID; }
    @Override public String getMoveTypeId() { return "CRAWLING"; }
    @Override public float getTurnAngle() { return CRAWL_TURN_ANGLE; }

    @Override
    public boolean canMove(Player player) {
        return getRestraintPosition(player) == RestraintPosition.CONNECTING
                && isBeenBindLegs(player)
                && !isBusyState(player) && !isRidingRestraintDevice(player) && player.getPassengers().isEmpty()
                && getFirstConnectBind(player).getItem() instanceof K9CorsetItem;
    }

    @Override
    protected void onTick(LivingEntity entity) {
        if (!(entity instanceof LocalPlayer player)) return;

        if (currentStage != Stage.MID) {
            hasTurnedInMid = false;
            return;
        }

        float yaw = RestraintMoveClientManager.getTargetBodyRot();

        if (currentDirection.equals("A") || currentDirection.equals("D")) {
            if (!hasTurnedInMid) {
                float delta = currentDirection.equals("A") ? -getTurnAngle() : getTurnAngle();
                float updatedYaw = Mth.wrapDegrees(yaw + delta);

                PacketDistributor.sendToServer(new RestraintMoveSyncBodyYawPayload(updatedYaw));

                hasTurnedInMid = true;
            }
            return;
        }

        if (currentDirection.equals("W") || currentDirection.equals("S")) {
            float yawRad = (float) Math.toRadians(yaw);

            float dirMultiplier = currentDirection.equals("W") ? -1.0f : 1.0f;
            double speed = CRAWL_SPEED * dirMultiplier;

            player.setDeltaMovement(-Mth.sin(yawRad) * speed, player.getDeltaMovement().y, Mth.cos(yawRad) * speed);
            player.hurtMarked = true;
        }
    }

    @Override public boolean canMoveForward(Player player) { return true; }
    @Override public boolean canMoveBackward(Player player) { return true; }
    @Override public boolean canTurnLeft(Player player) { return true; }
    @Override public boolean canTurnRight(Player player) { return true; }

    @Override
    public int getDuration(String direction, Stage stage) {
        return CRAWL_DURATION;
    }

    @Override
    public int getCooldown(Player player, String direction) {
        return 0;
    }
}
